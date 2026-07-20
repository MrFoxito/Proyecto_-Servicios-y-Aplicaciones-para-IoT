package com.example.proyecto_iot.data;

import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Loads the data used by the administrator reports without changing business records. */
public class FirebaseReportRepository {
    static final double USD_TO_PEN_RATE = 3.70d;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final ProjectAssignmentRepository assignmentRepository = new ProjectAssignmentRepository();

    public interface Callback {
        void onSuccess(ReportData data);
        void onError(String message);
    }

    public static class SeparationRecord {
        public final String projectId;
        public final String projectName;
        public final String advisorId;
        public final String advisorName;
        public final double amount;
        public final String currency;
        public final long createdAt;
        public final long paymentProcessedAt;
        public final boolean processed;

        SeparationRecord(DocumentSnapshot document) {
            projectId = firstNonEmpty(document.getString("projectId"), document.getString("propertyId"));
            projectName = firstNonEmpty(document.getString("projectName"), document.getString("inmuebleNombre"), projectId);
            advisorId = firstNonEmpty(document.getString("asesorId"));
            advisorName = firstNonEmpty(document.getString("asesorNombre"), advisorId, "Asesor sin nombre");
            amount = numberValue(document.get("amount"), parseAmount(document.getString("montoTexto")));
            // Historic records did not persist a currency and were stored as dollars.
            currency = firstNonEmpty(document.getString("currency"), "USD");
            createdAt = longValue(document.get("createdAt"));
            paymentProcessedAt = longValue(document.get("paymentProcessedAt"));
            processed = countsAsApprovedAmount(
                    document.getString("estado"),
                    document.getString("paymentStatus")
            );
        }

        public double amountInPen() {
            return normalizeAmountToPen(amount, currency);
        }
    }

    public static class ReportData {
        public final List<SeparationRecord> separations;
        public final Map<String, String> projects;
        public final Map<String, String> advisors;
        public final Map<String, Set<String>> activeProjectIdsByAdvisor;

        ReportData(
                List<SeparationRecord> separations,
                Map<String, String> projects,
                Map<String, String> advisors,
                Map<String, Set<String>> activeProjectIdsByAdvisor
        ) {
            this.separations = separations;
            this.projects = projects;
            this.advisors = advisors;
            this.activeProjectIdsByAdvisor = activeProjectIdsByAdvisor;
        }
    }

    public void loadCurrentAdminReport(Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("No hay una sesión de administrador activa.");
            return;
        }
        String adminId = user.getUid();
        firestore.collection("usuarios").document(adminId).get()
                .addOnSuccessListener(admin -> {
                    String empresaId = firstNonEmpty(admin.getString("empresaId"), admin.getString("inmobiliariaId"));
                    if (empresaId.isEmpty()) {
                        callback.onError("El administrador no está vinculado a una inmobiliaria.");
                        return;
                    }
                    loadProjectsAndSeparations(adminId, empresaId, callback);
                })
                .addOnFailureListener(error -> callback.onError(
                        "No se pudo validar la inmobiliaria del administrador: " + safeMessage(error)));
    }

    private void loadProjectsAndSeparations(String adminId, String empresaId, Callback callback) {
        firestore.collection("proyectos")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnSuccessListener(projectSnapshot -> {
                    Map<String, String> projects = new LinkedHashMap<>();
                    for (DocumentSnapshot document : projectSnapshot.getDocuments()) {
                        projects.put(document.getId(), firstNonEmpty(document.getString("nombre"), document.getId()));
                    }
                    loadSeparations(adminId, empresaId, projects, callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron cargar los proyectos del reporte: " + safeMessage(error)));
    }

    private void loadSeparations(
            String adminId,
            String empresaId,
            Map<String, String> projects,
            Callback callback
    ) {
        firestore.collection("separaciones")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SeparationRecord> records = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        SeparationRecord record = new SeparationRecord(document);
                        if (projects.containsKey(record.projectId)) {
                            records.add(record);
                        }
                    }
                    loadCompanyAdvisorsAndAssignments(empresaId, records, projects, callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron cargar las separaciones del reporte: " + safeMessage(error)));
    }

    private void loadCompanyAdvisorsAndAssignments(
            String empresaId,
            List<SeparationRecord> records,
            Map<String, String> projects,
            Callback callback
    ) {
        assignmentRepository.readAdvisors(empresaId, new ProjectAssignmentRepository.AdvisorsCallback() {
            @Override
            public void onSuccess(List<AdminAdvisorItem> companyAdvisors) {
                Map<String, String> advisors = new LinkedHashMap<>();
                List<String> activeAdvisorIds = new ArrayList<>();
                for (AdminAdvisorItem advisor : companyAdvisors) {
                    if (!advisor.isActive() || advisor.getUid().isEmpty()) {
                        continue;
                    }
                    advisors.put(advisor.getUid(), firstNonEmpty(advisor.getName(), "Asesor sin nombre"));
                    activeAdvisorIds.add(advisor.getUid());
                }
                loadAdvisorAssignmentsAt(0, activeAdvisorIds, advisors, new LinkedHashMap<>(),
                        records, projects, callback);
            }

            @Override
            public void onError(String message) {
                // The sales figures remain usable when the optional advisor filter cannot be loaded.
                callback.onSuccess(new ReportData(records, projects, new LinkedHashMap<>(), new LinkedHashMap<>()));
            }
        });
    }

    private void loadAdvisorAssignmentsAt(
            int index,
            List<String> advisorIds,
            Map<String, String> advisors,
            Map<String, Set<String>> activeProjectIdsByAdvisor,
            List<SeparationRecord> records,
            Map<String, String> projects,
            Callback callback
    ) {
        if (index >= advisorIds.size()) {
            callback.onSuccess(new ReportData(records, projects, advisors, activeProjectIdsByAdvisor));
            return;
        }
        String advisorId = advisorIds.get(index);
        assignmentRepository.readActiveProjectIdsForAdvisor(advisorId,
                new ProjectAssignmentRepository.ProjectIdsCallback() {
                    @Override
                    public void onSuccess(Set<String> projectIds) {
                        activeProjectIdsByAdvisor.put(advisorId, new LinkedHashSet<>(projectIds));
                        loadAdvisorAssignmentsAt(index + 1, advisorIds, advisors, activeProjectIdsByAdvisor,
                                records, projects, callback);
                    }

                    @Override
                    public void onError(String message) {
                        activeProjectIdsByAdvisor.put(advisorId, new LinkedHashSet<>());
                        loadAdvisorAssignmentsAt(index + 1, advisorIds, advisors, activeProjectIdsByAdvisor,
                                records, projects, callback);
                    }
                });
    }

    static double normalizeAmountToPen(double amount, String currency) {
        return "PEN".equalsIgnoreCase(currency) ? amount : amount * USD_TO_PEN_RATE;
    }

    /**
     * A separation is not a completed property sale. Reports count only the reservation
     * amount once it has been approved or paid, without changing the source document.
     */
    static boolean countsAsApprovedAmount(String status, String paymentStatus) {
        return "aprobada".equalsIgnoreCase(status)
                || "pagada".equalsIgnoreCase(status)
                || "cobro procesado".equalsIgnoreCase(status)
                || "processed".equalsIgnoreCase(paymentStatus);
    }

    private static double numberValue(Object value, double fallback) {
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private static long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private static double parseAmount(String value) {
        if (value == null) {
            return 0d;
        }
        String upper = value.toUpperCase(java.util.Locale.ROOT);
        double multiplier = upper.matches(".*\\d\\s*M\\b.*") || upper.matches(".*\\dM\\b.*")
                ? 1_000_000d
                : (upper.matches(".*\\d\\s*K\\b.*") || upper.matches(".*\\dK\\b.*") ? 1_000d : 1d);
        String normalized = value.replaceAll("[^0-9,.-]", "");
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(",", "");
        } else if (normalized.contains(",")) {
            String[] parts = normalized.split(",");
            normalized = parts.length == 2 && parts[1].length() <= 2
                    ? normalized.replace(",", ".")
                    : normalized.replace(",", "");
        }
        try {
            return Double.parseDouble(normalized) * multiplier;
        } catch (NumberFormatException ignored) {
            return 0d;
        }
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String safeMessage(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
