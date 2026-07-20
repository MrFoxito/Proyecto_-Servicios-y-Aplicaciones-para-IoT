package com.example.proyecto_iot.data;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
                .addOnSuccessListener(snapshot -> {
                    java.util.List<com.example.proyecto_iot.usuario.UsuarioTramiteItem> items = new java.util.ArrayList<>();
                    if (snapshot.isEmpty()) {
                        callback.onSuccess(items);
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String estado = valueOr(doc.getString("estado"), "Pendiente");
                        String inmuebleNombre = valueOr(doc.getString("inmuebleNombre"), "Inmueble");
                        String monto = valueOr(doc.getString("montoTexto"), "S/ 0");
                        String nota = "Monto: " + monto;
                        boolean canPay = "Pendiente".equalsIgnoreCase(estado);
                        String projectId = firstNonEmpty(doc.getString("propertyId"), doc.getString("projectId"), doc.getString("proyectoId"));
                        
                        items.add(new com.example.proyecto_iot.usuario.UsuarioTramiteItem(
                                inmuebleNombre,
                                doc.getId(),
                                estado,
                                nota,
                                valueOr(doc.getString("fechaCreacionISO"), ""),
                                canPay,
                                projectId
                        ));
                    }
                    java.util.Collections.sort(items, (a, b) -> b.getDue().compareTo(a.getDue()));
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error -> callback.onError("Error al obtener separaciones: " + safeMessage(error)));
    }

    public static class SeparationDraft {
        public String clienteId;
        public String clienteNombre;
        public String asesorId;
        public String asesorNombre;
        public String citaId;
        public String propertyId;
        public String tipologiaId;
        public String formaPago;
        public String inmuebleNombre;
        public String montoTexto;
        public String estado;
        public String createdByRole;
    }

    public void createSeparation(SeparationDraft draft, SimpleCallback callback) {
        if (draft.propertyId == null || draft.propertyId.trim().isEmpty()) {
            createSeparationUnchecked(draft, null, callback);
            return;
        }
        firestore.collection("proyectos").document(draft.propertyId.trim()).get()
                .addOnSuccessListener(project -> {
                    if (project.exists() && !canSeparate(project)) {
                        callback.onError("Este proyecto esta en planos. Aun no permite separaciones.");
                        return;
                    }
                    createSeparationUnchecked(draft, project, callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo validar el estado del proyecto: " + safeMessage(error)));
    }

    private void createSeparationUnchecked(SeparationDraft draft, @Nullable DocumentSnapshot project, SimpleCallback callback) {
        long now = System.currentTimeMillis();
        String separationId = "sep_" + now;
        DocumentReference separationRef = firestore.collection("separaciones").document(separationId);
        String adminId = project != null ? valueOr(project.getString("adminId")) : "";
        String projectName = project != null ? valueOr(project.getString("nombre"), valueOr(draft.inmuebleNombre)) : valueOr(draft.inmuebleNombre);
        boolean fromAdvisor = "asesor".equalsIgnoreCase(valueOr(draft.createdByRole));

        Map<String, Object> data = new HashMap<>();
        data.put("id", separationId);
        data.put("clienteId", valueOr(draft.clienteId));
        data.put("clienteNombre", valueOr(draft.clienteNombre));
        data.put("asesorId", valueOr(draft.asesorId));
        data.put("asesorNombre", valueOr(draft.asesorNombre));
        data.put("citaId", valueOr(draft.citaId));
        data.put("propertyId", valueOr(draft.propertyId));
        data.put("projectId", valueOr(draft.propertyId));
        data.put("tipologiaId", valueOr(draft.tipologiaId));
        data.put("formaPago", valueOr(draft.formaPago));
        data.put("inmuebleNombre", valueOr(draft.inmuebleNombre));
        data.put("montoTexto", valueOr(draft.montoTexto));
        data.put("amount", parseAmount(draft.montoTexto));
        data.put("currency", detectCurrency(draft.montoTexto));
        data.put("estado", valueOr(draft.estado, "Pendiente"));
        data.put("createdByRole", valueOr(draft.createdByRole, "cliente"));
        data.put("adminId", adminId);
        data.put("createdAt", now);
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
            notification.put("projectId", valueOr(draft.propertyId));
            notification.put("propertyId", valueOr(draft.propertyId));
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

    private String detectCurrency(String value) {
        String normalized = valueOr(value).toUpperCase(Locale.ROOT);
        return normalized.contains("S/") || normalized.contains("PEN") ? "PEN" : "USD";
    }

    public interface SeparationsListener {
        void onDataChanged(List<Separacion> list);
        void onError(String message);
    }

    public ListenerRegistration listenSeparationsForAdvisor(String asesorId, SeparationsListener callback) {
        return firestore.collection("separaciones")
                .whereEqualTo("asesorId", asesorId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    List<Separacion> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Separacion sep = doc.toObject(Separacion.class);
                            if (sep != null) {
                                sep.setId(doc.getId());
                                list.add(sep);
                            }
                        }
                    }
                    callback.onDataChanged(list);
                });
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
