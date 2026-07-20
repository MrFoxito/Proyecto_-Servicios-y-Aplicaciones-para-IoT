package com.example.proyecto_iot.data;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class FirebaseSeparationRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface SimpleCallback {
        void onSuccess(String separationId);
        void onError(String message);
    }

    public interface UserTramitesCallback {
        void onSuccess(java.util.List<com.example.proyecto_iot.usuario.UsuarioTramiteItem> items);
        void onError(String message);
    }

    public void readUserSeparations(String clienteId, UserTramitesCallback callback) {
        if (clienteId == null || clienteId.isEmpty()) {
            callback.onError("ID de cliente invalido");
            return;
        }

        firestore.collection("separaciones")
                .whereEqualTo("clienteId", clienteId)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(userTramiteItems(snapshot)))
                .addOnFailureListener(error -> callback.onError("Error al obtener separaciones: " + safeMessage(error)));
    }

    /** Keeps the client's separation procedures synchronized with their current Firestore state. */
    public ListenerRegistration listenUserSeparations(String clienteId, UserTramitesCallback callback) {
        if (valueOr(clienteId).isEmpty()) {
            callback.onError("ID de cliente invalido");
            return null;
        }
        return firestore.collection("separaciones")
                .whereEqualTo("clienteId", clienteId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("Error al escuchar separaciones: " + safeMessage(error));
                        return;
                    }
                    callback.onSuccess(userTramiteItems(snapshot));
                });
    }

    private List<com.example.proyecto_iot.usuario.UsuarioTramiteItem> userTramiteItems(
            @Nullable com.google.firebase.firestore.QuerySnapshot snapshot) {
        List<com.example.proyecto_iot.usuario.UsuarioTramiteItem> items = new ArrayList<>();
        if (snapshot == null) return items;
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String estado = valueOr(doc.getString("estado"), "Pendiente");
            String inmuebleNombre = valueOr(doc.getString("inmuebleNombre"), "Inmueble");
            String monto = valueOr(doc.getString("montoTexto"), "S/ 0");
            String projectId = firstNonEmpty(doc.getString("propertyId"), doc.getString("projectId"),
                    doc.getString("proyectoId"));
            items.add(new com.example.proyecto_iot.usuario.UsuarioTramiteItem(
                    inmuebleNombre,
                    doc.getId(),
                    estado,
                    "Monto: " + monto,
                    valueOr(doc.getString("fechaCreacionISO"), ""),
                    "Pendiente".equalsIgnoreCase(estado),
                    projectId
            ));
        }
        java.util.Collections.sort(items, (a, b) -> b.getDue().compareTo(a.getDue()));
        return items;
    }

    public static class SeparationDraft {
        public String clienteId;
        public String clienteNombre;
        public String asesorId;
        public String asesorNombre;
        public String citaId;
        public String propertyId;
        /** Legacy aliases accepted as input; every new document writes the three fields in sync. */
        public String projectId;
        public String proyectoId;
        /** Canonical active assignment that authorizes the advisor/project relation. */
        public String assignmentId;
        public String tipologiaId;
        public String formaPago;
        public String inmuebleNombre;
        public String montoTexto;
        /** Immutable pricing snapshot supplied by the selected project typology. */
        public double precioTotal;
        public String precioTotalTexto;
        public double montoSeparacion;
        public String montoSeparacionTexto;
        public String currency;
        public String estado;
        public String createdByRole;
        public String fechaCreacionISO;
    }

    public void createSeparation(SeparationDraft draft, SimpleCallback callback) {
        if (draft == null) {
            callback.onError("No se recibieron datos para la separacion.");
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user == null ? "" : valueOr(user.getUid());
        if (uid.isEmpty()) {
            callback.onError("No hay una sesion Firebase activa.");
            return;
        }
        boolean fromAdvisor = "asesor".equalsIgnoreCase(valueOr(draft.createdByRole));
        String expectedUid = fromAdvisor ? valueOr(draft.asesorId) : valueOr(draft.clienteId);
        if (expectedUid.isEmpty() || !uid.equals(expectedUid)) {
            callback.onError("La sesion actual no corresponde a quien registra la separacion.");
            return;
        }
        String requestedProjectId = firstNonEmpty(draft.propertyId, draft.projectId, draft.proyectoId);
        if (requestedProjectId.isEmpty() || valueOr(draft.asesorId).isEmpty() || valueOr(draft.clienteId).isEmpty()) {
            callback.onError("La separacion requiere cliente, asesor y proyecto validos.");
            return;
        }
        resolveProject(requestedProjectId, new ProjectCallback() {
            @Override public void onSuccess(DocumentSnapshot project) {
                if (!canSeparate(project)) {
                    callback.onError("Este proyecto esta en planos. Aun no permite separaciones.");
                    return;
                }
                String canonicalProjectId = project.getId();
                draft.propertyId = canonicalProjectId;
                draft.projectId = canonicalProjectId;
                draft.proyectoId = canonicalProjectId;
                validateActiveAssignment(draft, project, new ProjectCallback() {
                    @Override public void onSuccess(DocumentSnapshot ignored) {
                        resolvePricingSnapshot(draft, project, new ValidationCallback() {
                            @Override public void onSuccess() {
                                validateAppointmentContext(draft, new ValidationCallback() {
                                    @Override public void onSuccess() {
                                        createSeparationUnchecked(draft, project, callback);
                                    }
                                    @Override public void onError(String message) { callback.onError(message); }
                                });
                            }
                            @Override public void onError(String message) { callback.onError(message); }
                        });
                    }
                    @Override public void onError(String message) { callback.onError(message); }
                });
            }
            @Override public void onError(String message) { callback.onError(message); }
        });
    }

    private interface ProjectCallback {
        void onSuccess(DocumentSnapshot project);
        void onError(String message);
    }

    private interface ValidationCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Adds a pricing snapshot only for new separations. Pricing lookup must never make a
     * previously valid reservation fail; absent legacy pricing remains visibly unconfigured.
     */
    private void resolvePricingSnapshot(SeparationDraft draft, DocumentSnapshot project, ValidationCallback callback) {
        applyProjectPriceFallback(draft, project);
        String typologyId = valueOr(draft.tipologiaId);
        if (!typologyId.isEmpty()) {
            firestore.collection("proyectos_tipologias").document(typologyId).get()
                    .addOnSuccessListener(typology -> {
                        if (typology.exists() && project.getId().equals(valueOr(typology.getString("projectId")))) {
                            applyTypologyPricing(draft, typology);
                        }
                        callback.onSuccess();
                    })
                    .addOnFailureListener(error -> callback.onSuccess());
            return;
        }
        firestore.collection("proyectos_tipologias").whereEqualTo("projectId", project.getId()).limit(2).get()
                .addOnSuccessListener(types -> {
                    if (types.size() == 1) {
                        DocumentSnapshot typology = types.getDocuments().get(0);
                        draft.tipologiaId = typology.getId();
                        applyTypologyPricing(draft, typology);
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(error -> callback.onSuccess());
    }

    private void applyProjectPriceFallback(SeparationDraft draft, DocumentSnapshot project) {
        if (hasAmount(draft.precioTotal)) return;
        String priceText = valueOr(project.getString("precioDesde"));
        double price = parseAmount(priceText);
        if (hasAmount(price)) {
            draft.precioTotal = price;
            draft.precioTotalTexto = priceText;
            if (valueOr(draft.currency).isEmpty()) draft.currency = valueOr(project.getString("currency"), "PEN");
        }
    }

    private void applyTypologyPricing(SeparationDraft draft, DocumentSnapshot typology) {
        double total = numberOf(firstValue(typology, "totalAmount", "montoTotal"));
        double separation = numberOf(firstValue(typology, "separationAmount", "montoSeparacion"));
        if (!hasAmount(draft.precioTotal) && hasAmount(total)) {
            draft.precioTotal = total;
            draft.precioTotalTexto = valueOr(firstString(typology, "totalAmountLabel", "montoTotalLabel"), formatPen(total));
        }
        if (!hasAmount(draft.montoSeparacion) && hasAmount(separation)) {
            draft.montoSeparacion = separation;
            draft.montoSeparacionTexto = valueOr(firstString(typology, "separationAmountLabel", "montoSeparacionLabel"), formatPen(separation));
        }
        if (valueOr(draft.currency).isEmpty()) draft.currency = valueOr(typology.getString("currency"), "PEN");
    }

    private Object firstValue(DocumentSnapshot document, String first, String second) {
        Object value = document.get(first);
        return value != null ? value : document.get(second);
    }

    private String firstString(DocumentSnapshot document, String first, String second) {
        return valueOr(document.getString(first), document.getString(second));
    }

    private void resolveProject(String reference, ProjectCallback callback) {
        String safeReference = valueOr(reference);
        firestore.collection("proyectos").document(safeReference).get()
                .addOnSuccessListener(project -> {
                    if (project.exists()) {
                        callback.onSuccess(project);
                    } else {
                        resolveLegacyProject(safeReference, callback);
                    }
                })
                .addOnFailureListener(error -> callback.onError("No se pudo validar el proyecto: " + safeMessage(error)));
    }

    /** Supports one legacy reference at a time and rejects ambiguous matches. */
    private void resolveLegacyProject(String reference, ProjectCallback callback) {
        firestore.collection("proyectos").whereEqualTo("projectId", reference).get()
                .addOnSuccessListener(byProjectId -> firestore.collection("proyectos")
                        .whereEqualTo("propertyId", reference).get()
                        .addOnSuccessListener(byPropertyId -> firestore.collection("proyectos")
                                .whereEqualTo("proyectoId", reference).get()
                                .addOnSuccessListener(byProyectoId -> {
                                    Map<String, DocumentSnapshot> matches = new HashMap<>();
                                    for (DocumentSnapshot doc : byProjectId.getDocuments()) matches.put(doc.getId(), doc);
                                    for (DocumentSnapshot doc : byPropertyId.getDocuments()) matches.put(doc.getId(), doc);
                                    for (DocumentSnapshot doc : byProyectoId.getDocuments()) matches.put(doc.getId(), doc);
                                    if (matches.size() == 1) callback.onSuccess(matches.values().iterator().next());
                                    else if (matches.isEmpty()) callback.onError("El proyecto seleccionado ya no existe.");
                                    else callback.onError("La referencia del proyecto es ambigua.");
                                })
                                .addOnFailureListener(error -> callback.onError("No se pudo resolver el proyecto: " + safeMessage(error))))
                        .addOnFailureListener(error -> callback.onError("No se pudo resolver el proyecto: " + safeMessage(error))))
                .addOnFailureListener(error -> callback.onError("No se pudo resolver el proyecto: " + safeMessage(error)));
    }

    private void validateActiveAssignment(SeparationDraft draft, DocumentSnapshot project, ProjectCallback callback) {
        String advisorId = valueOr(draft.asesorId);
        String canonicalProjectId = project.getId();
        firestore.collection("asignaciones").whereEqualTo("asesorId", advisorId).get()
                .addOnSuccessListener(assignments -> {
                    Map<String, DocumentSnapshot> active = new HashMap<>();
                    for (DocumentSnapshot assignment : assignments.getDocuments()) {
                        if (!"ACTIVO".equalsIgnoreCase(valueOr(assignment.getString("estado")))) continue;
                        String assignmentProjectId = firstNonEmpty(assignment.getString("projectId"),
                                assignment.getString("propertyId"), assignment.getString("proyectoId"));
                        if (canonicalProjectId.equals(assignmentProjectId)) active.put(assignment.getId(), assignment);
                    }
                    String requestedAssignmentId = valueOr(draft.assignmentId);
                    if (!requestedAssignmentId.isEmpty()) {
                        DocumentSnapshot selected = active.get(requestedAssignmentId);
                        if (selected == null) {
                            callback.onError("La asignacion del asesor ya no esta activa para este proyecto.");
                            return;
                        }
                        draft.assignmentId = selected.getId();
                        callback.onSuccess(selected);
                    } else if (active.size() == 1) {
                        DocumentSnapshot selected = active.values().iterator().next();
                        draft.assignmentId = selected.getId();
                        callback.onSuccess(selected);
                    } else if (active.isEmpty()) {
                        callback.onError("El asesor no tiene una asignacion activa para este proyecto.");
                    } else {
                        callback.onError("Se encontro mas de una asignacion activa. Selecciona nuevamente el asesor.");
                    }
                })
                .addOnFailureListener(error -> callback.onError("No se pudo validar la asignacion: " + safeMessage(error)));
    }

    private void validateAppointmentContext(SeparationDraft draft, ValidationCallback callback) {
        String appointmentId = valueOr(draft.citaId);
        if (appointmentId.isEmpty()) {
            callback.onSuccess();
            return;
        }
        firestore.collection("citas").document(appointmentId).get()
                .addOnSuccessListener(cita -> {
                    if (!cita.exists()) {
                        callback.onError("La cita vinculada ya no existe.");
                        return;
                    }
                    String citaProjectId = firstNonEmpty(cita.getString("propertyId"), cita.getString("projectId"), cita.getString("proyectoId"));
                    String citaAdvisorId = firstNonEmpty(cita.getString("asesorId"), cita.getString("advisorId"));
                    if (!valueOr(draft.clienteId).equals(valueOr(cita.getString("clienteId")))
                            || !valueOr(draft.asesorId).equals(citaAdvisorId)
                            || !valueOr(draft.propertyId).equals(citaProjectId)) {
                        callback.onError("La cita no coincide con el cliente, asesor o proyecto seleccionado.");
                        return;
                    }
                    String citaAssignmentId = valueOr(cita.getString("assignmentId"));
                    if (!citaAssignmentId.isEmpty() && !citaAssignmentId.equals(valueOr(draft.assignmentId))) {
                        callback.onError("La cita pertenece a una asignacion diferente.");
                        return;
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(error -> callback.onError("No se pudo validar la cita: " + safeMessage(error)));
    }

    private void createSeparationUnchecked(SeparationDraft draft, @Nullable DocumentSnapshot project, SimpleCallback callback) {
        long now = System.currentTimeMillis();
        String separationId = "sep_" + now;
        DocumentReference separationRef = firestore.collection("separaciones").document(separationId);
        String adminId = project != null ? valueOr(project.getString("adminId")) : "";
        String projectName = project != null ? valueOr(project.getString("nombre"), valueOr(draft.inmuebleNombre)) : valueOr(draft.inmuebleNombre);
        boolean fromAdvisor = "asesor".equalsIgnoreCase(valueOr(draft.createdByRole));
        String canonicalProjectId = valueOr(draft.propertyId);
        String fechaCreacionISO = firstNonEmpty(draft.fechaCreacionISO,
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(now)));

        Map<String, Object> data = new HashMap<>();
        data.put("id", separationId);
        data.put("clienteId", valueOr(draft.clienteId));
        data.put("clienteNombre", valueOr(draft.clienteNombre));
        data.put("asesorId", valueOr(draft.asesorId));
        data.put("asesorNombre", valueOr(draft.asesorNombre));
        data.put("citaId", valueOr(draft.citaId));
        data.put("propertyId", canonicalProjectId);
        data.put("projectId", canonicalProjectId);
        data.put("proyectoId", canonicalProjectId);
        data.put("assignmentId", valueOr(draft.assignmentId));
        data.put("tipologiaId", valueOr(draft.tipologiaId));
        data.put("formaPago", valueOr(draft.formaPago));
        data.put("inmuebleNombre", valueOr(draft.inmuebleNombre));
        data.put("montoTexto", valueOr(draft.montoTexto));
        data.put("amount", parseAmount(draft.montoTexto));
        data.put("currency", valueOr(draft.currency, detectCurrency(draft.montoTexto)));
        data.put("precioTotal", draft.precioTotal);
        data.put("precioTotalTexto", valueOr(draft.precioTotalTexto));
        data.put("montoSeparacion", draft.montoSeparacion);
        data.put("montoSeparacionTexto", valueOr(draft.montoSeparacionTexto));
        data.put("estado", valueOr(draft.estado, "Pendiente"));
        data.put("createdByRole", valueOr(draft.createdByRole, "cliente"));
        data.put("adminId", adminId);
        data.put("createdAt", now);
        data.put("fechaCreacionISO", fechaCreacionISO);
        data.put("fechaTexto", new SimpleDateFormat("dd MMM yyyy", new Locale("es", "PE")).format(new java.util.Date(now)));

        WriteBatch batch = firestore.batch();
        batch.set(separationRef, data, SetOptions.merge());
        if (!adminId.isEmpty()) {
            String notificationType = fromAdvisor
                    ? FirebaseAdminNotificationRepository.TYPE_ADVISOR_SEPARATION
                    : FirebaseAdminNotificationRepository.TYPE_CHECKOUT_PAYMENT;
            String notificationId = notificationType + "_" + separationId;
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", notificationId);
            notification.put("recipientId", adminId);
            notification.put("recipientRole", "admin");
            notification.put("tipo", notificationType);
            notification.put("separationId", separationId);
            notification.put("projectId", canonicalProjectId);
            notification.put("propertyId", canonicalProjectId);
            notification.put("proyectoId", canonicalProjectId);
            notification.put("assignmentId", valueOr(draft.assignmentId));
            notification.put("inmuebleNombre", valueOr(draft.inmuebleNombre));
            notification.put("projectName", projectName);
            notification.put("clienteId", valueOr(draft.clienteId));
            notification.put("asesorId", valueOr(draft.asesorId));
            notification.put("createdAt", now);
            notification.put("read", false);
            if (fromAdvisor) {
                notification.put("titulo", "Separacion registrada por asesor");
                notification.put("body", valueOr(draft.asesorNombre, "Un asesor") + " registro una separacion para " + valueOr(draft.inmuebleNombre, projectName) + ".");
                notification.put("line2", "Revisar para aprobar o rechazar");
            } else {
                notification.put("titulo", "Pago de checkout recibido");
                notification.put("body", valueOr(draft.clienteNombre, "Un cliente") + " realizo el pago de separacion para " + valueOr(draft.inmuebleNombre, projectName) + ".");
                notification.put("line2", "Procesar cobro con tarjeta registrada");
            }
            batch.set(firestore.collection("notificaciones").document(notificationId), notification, SetOptions.merge());
        }
        if (draft.citaId != null && !draft.citaId.trim().isEmpty()) {
            Map<String, Object> citaUpdate = new HashMap<>();
            citaUpdate.put("hasCierre", true);
            citaUpdate.put("separacionId", separationId);
            batch.set(firestore.collection("citas").document(draft.citaId), citaUpdate, SetOptions.merge());
        }
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(separationId))
                .addOnFailureListener(error ->
                        callback.onError("No se pudo crear la separacion: " + safeMessage(error)));
    }

    private boolean canSeparate(DocumentSnapshot project) {
        return ProjectBusinessRules.canCreateSeparation(valueOr(
                project.getString("estadoProyecto"),
                valueOr(project.getString("estadoComercial"), project.getString("estado"))
        ));
    }

    private String valueOr(@Nullable String value) {
        return valueOr(value, "");
    }

    private String valueOr(@Nullable String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }

    private double parseAmount(String value) {
        if (value == null) {
            return 0d;
        }
        String upper = value.toUpperCase(Locale.ROOT);
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

    private double numberOf(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : parseAmount(value == null ? "" : String.valueOf(value));
    }

    private boolean hasAmount(double amount) {
        return amount > 0d && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    private String formatPen(double amount) {
        return String.format(Locale.US, "S/ %,.2f", amount);
    }

    private String detectCurrency(String value) {
        String normalized = valueOr(value).toUpperCase(Locale.ROOT);
        return normalized.contains("S/") || normalized.contains("PEN") ? "PEN" : "USD";
    }

    public interface SeparationsListener {
        void onDataChanged(List<Separacion> list);
        void onError(String message);
    }

    public ListenerRegistration listenSeparationsForAdvisor(String asesorId, SeparationsListener callback) {
        String safeAdvisorId = valueOr(asesorId);
        if (safeAdvisorId.isEmpty()) {
            callback.onError("ID de asesor invÃ¡lido.");
            return () -> { };
        }
        AdvisorSeparationsRegistration registration = new AdvisorSeparationsRegistration(safeAdvisorId, callback);
        registration.start();
        return registration;
    }

    /**
     * A Firestore query is rejected when it can return even one separation the
     * advisor is not allowed to read. Listen to current assignments first, then
     * narrow every separation listener to the corresponding active project.
     */
    private final class AdvisorSeparationsRegistration implements ListenerRegistration {
        private final String advisorId;
        private final SeparationsListener callback;
        private final Map<String, ListenerRegistration> separationListeners = new HashMap<>();
        private final Map<String, List<Separacion>> separationsByAssignment = new HashMap<>();
        private ListenerRegistration assignmentsListener;
        private boolean removed;

        AdvisorSeparationsRegistration(String advisorId, SeparationsListener callback) {
            this.advisorId = advisorId;
            this.callback = callback;
        }

        void start() {
            assignmentsListener = firestore.collection("asignaciones")
                    .whereEqualTo("asesorId", advisorId)
                    .addSnapshotListener((assignments, error) -> {
                        if (removed) return;
                        if (error != null) {
                            callback.onError("No se pudieron cargar las asignaciones: " + safeMessage(error));
                            return;
                        }
                        synchronizeActiveAssignments(assignments);
                    });
        }

        private void synchronizeActiveAssignments(@Nullable com.google.firebase.firestore.QuerySnapshot assignments) {
            Map<String, String> activeAssignments = new HashMap<>();
            if (assignments != null) {
                for (DocumentSnapshot assignment : assignments.getDocuments()) {
                    if (!"ACTIVO".equalsIgnoreCase(valueOr(assignment.getString("estado")))) continue;
                    String assignmentId = valueOr(assignment.getId());
                    String projectId = firstNonEmpty(assignment.getString("projectId"),
                            assignment.getString("propertyId"), assignment.getString("proyectoId"));
                    if (!assignmentId.isEmpty() && !projectId.isEmpty()) {
                        activeAssignments.put(assignmentId, projectId);
                    }
                }
            }

            Set<String> removedAssignments = new HashSet<>(separationListeners.keySet());
            removedAssignments.removeAll(activeAssignments.keySet());
            for (String assignmentId : removedAssignments) {
                ListenerRegistration listener = separationListeners.remove(assignmentId);
                if (listener != null) listener.remove();
                separationsByAssignment.remove(assignmentId);
            }

            for (Map.Entry<String, String> assignment : activeAssignments.entrySet()) {
                if (!separationListeners.containsKey(assignment.getKey())) {
                    listenAssignmentSeparations(assignment.getKey(), assignment.getValue());
                }
            }
            emitMergedSeparations();
        }

        private void listenAssignmentSeparations(String assignmentId, String projectId) {
            Query query = firestore.collection("separaciones")
                    .whereEqualTo("asesorId", advisorId)
                    .whereEqualTo("propertyId", projectId);
            ListenerRegistration listener = query.addSnapshotListener((separations, error) -> {
                if (removed) return;
                if (error != null) {
                    // A stale assignment can briefly race with a deactivation. Keep the rest visible.
                    separationsByAssignment.remove(assignmentId);
                    emitMergedSeparations();
                    return;
                }
                separationsByAssignment.put(assignmentId, separationItems(separations));
                emitMergedSeparations();
            });
            separationListeners.put(assignmentId, listener);
        }

        private void emitMergedSeparations() {
            if (removed) return;
            Map<String, Separacion> unique = new HashMap<>();
            for (List<Separacion> items : separationsByAssignment.values()) {
                for (Separacion item : items) unique.put(item.getId(), item);
            }
            List<Separacion> merged = new ArrayList<>(unique.values());
            Collections.sort(merged, (left, right) -> Long.compare(right.getCreatedAt(), left.getCreatedAt()));
            callback.onDataChanged(merged);
        }

        @Override
        public void remove() {
            removed = true;
            if (assignmentsListener != null) assignmentsListener.remove();
            for (ListenerRegistration listener : separationListeners.values()) listener.remove();
            separationListeners.clear();
            separationsByAssignment.clear();
        }
    }

    private List<Separacion> separationItems(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots) {
        List<Separacion> list = new ArrayList<>();
        if (snapshots == null) return list;
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            Separacion sep = doc.toObject(Separacion.class);
            if (sep == null) continue;
            sep.setId(doc.getId());
            sep.setProjectId(firstNonEmpty(doc.getString("propertyId"), doc.getString("projectId"),
                    doc.getString("proyectoId")));
            Object amount = doc.get("amount");
            if (amount instanceof Number) sep.setAmount(((Number) amount).doubleValue());
            sep.setCurrency(valueOr(doc.getString("currency")));
            list.add(sep);
        }
        return list;
    }

    public void updateSeparationStatus(String separationId, String newStatus, SimpleCallback callback) {
        firestore.collection("separaciones").document(separationId)
                .update("estado", newStatus)
                .addOnSuccessListener(unused -> callback.onSuccess(separationId))
                .addOnFailureListener(error -> callback.onError("No se pudo actualizar la separación: " + safeMessage(error)));
    }

    private static String firstNonEmpty(String... values) {
        if (values != null) {
            for (String v : values) {
                if (v != null && !v.trim().isEmpty()) return v.trim();
            }
        }
        return "";
    }
}
