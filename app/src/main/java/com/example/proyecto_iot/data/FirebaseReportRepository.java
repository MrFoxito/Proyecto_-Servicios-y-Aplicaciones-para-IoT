package com.example.proyecto_iot.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirebaseReportRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
            currency = firstNonEmpty(document.getString("currency"), "USD");
            createdAt = longValue(document.get("createdAt"));
            paymentProcessedAt = longValue(document.get("paymentProcessedAt"));
            processed = "processed".equalsIgnoreCase(document.getString("paymentStatus"))
                    || "cobro procesado".equalsIgnoreCase(document.getString("estado"));
        }
    }

    public static class ReportData {
        public final List<SeparationRecord> separations;
        public final Map<String, String> projects;
        public final Map<String, String> advisors;

        ReportData(List<SeparationRecord> separations, Map<String, String> projects, Map<String, String> advisors) {
            this.separations = separations;
            this.projects = projects;
            this.advisors = advisors;
        }
    }

    public void loadCurrentAdminReport(Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("No hay una sesión de administrador activa.");
            return;
        }
        String adminId = user.getUid();
        firestore.collection("proyectos")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnSuccessListener(projectSnapshot -> {
                    Map<String, String> projects = new LinkedHashMap<>();
                    for (DocumentSnapshot document : projectSnapshot.getDocuments()) {
                        projects.put(document.getId(), firstNonEmpty(document.getString("nombre"), document.getId()));
                    }
                    loadSeparations(adminId, projects, callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron cargar los proyectos del reporte: " + safeMessage(error)));
    }

    private void loadSeparations(String adminId, Map<String, String> projects, Callback callback) {
        firestore.collection("separaciones")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SeparationRecord> records = new ArrayList<>();
                    Map<String, String> advisors = new LinkedHashMap<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        SeparationRecord record = new SeparationRecord(document);
                        if (!projects.containsKey(record.projectId)) {
                            continue;
                        }
                        records.add(record);
                        if (!record.advisorId.isEmpty()) {
                            advisors.put(record.advisorId, record.advisorName);
                        }
                    }
                    loadAdvisorNames(records, projects, advisors, callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron cargar las separaciones del reporte: " + safeMessage(error)));
    }

    private void loadAdvisorNames(
            List<SeparationRecord> records,
            Map<String, String> projects,
            Map<String, String> advisors,
            Callback callback
    ) {
        if (advisors.isEmpty()) {
            callback.onSuccess(new ReportData(records, projects, advisors));
            return;
        }
        List<String> ids = new ArrayList<>(advisors.keySet());
        loadAdvisorAt(0, ids, records, projects, advisors, callback);
    }

    private void loadAdvisorAt(
            int index,
            List<String> ids,
            List<SeparationRecord> records,
            Map<String, String> projects,
            Map<String, String> advisors,
            Callback callback
    ) {
        if (index >= ids.size()) {
            callback.onSuccess(new ReportData(records, projects, advisors));
            return;
        }
        String id = ids.get(index);
        firestore.collection("usuarios").document(id).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        advisors.put(id, firstNonEmpty(
                                document.getString("nombre"),
                                (firstNonEmpty(document.getString("nombres")) + " "
                                        + firstNonEmpty(document.getString("apellidos"))).trim(),
                                advisors.get(id)
                        ));
                    }
                    loadAdvisorAt(index + 1, ids, records, projects, advisors, callback);
                })
                .addOnFailureListener(error ->
                        loadAdvisorAt(index + 1, ids, records, projects, advisors, callback));
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

    private String safeMessage(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
