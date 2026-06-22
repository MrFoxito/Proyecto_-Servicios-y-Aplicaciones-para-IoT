package com.example.proyecto_iot.data;

import com.example.proyecto_iot.admin.model.AdminNotificationItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseAdminNotificationRepository {
    public static final String TYPE_ADVISOR_SEPARATION = "advisor_separation";
    public static final String TYPE_CHECKOUT_PAYMENT = "checkout_payment";
    public static final String TYPE_DELIVERY_DUE = "project_delivery_due";

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface NotificationsCallback {
        void onSuccess(List<AdminNotificationItem> notifications);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public ListenerRegistration listenAllowedAdminNotifications(NotificationsCallback callback) {
        return firestore.collection("notificaciones")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("No se pudieron leer notificaciones: " + safeMessage(error));
                        return;
                    }
                    List<AdminNotificationItem> items = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            AdminNotificationItem item = notificationFromSnapshot(document);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                    Collections.sort(items, (left, right) -> Long.compare(right.getCreatedAt(), left.getCreatedAt()));
                    callback.onSuccess(items);
                });
    }

    public void updateSeparationDecision(String separationId, boolean approved, SimpleCallback callback) {
        if (separationId == null || separationId.trim().isEmpty()) {
            callback.onError("No se encontro la separacion a actualizar.");
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", approved ? "Aprobada" : "Rechazada");
        updates.put("adminDecision", approved ? "aprobada" : "rechazada");
        updates.put("adminDecisionAt", now);
        updates.put("updatedAt", now);
        firestore.collection("separaciones").document(separationId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo actualizar la separacion: " + safeMessage(error)));
    }

    public void confirmCheckoutPayment(String separationId, SimpleCallback callback) {
        if (separationId == null || separationId.trim().isEmpty()) {
            callback.onError("No se encontro el pago de separacion a procesar.");
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Cobro procesado");
        updates.put("paymentStatus", "processed");
        updates.put("paymentProcessedAt", now);
        updates.put("updatedAt", now);
        firestore.collection("separaciones").document(separationId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo procesar el pago: " + safeMessage(error)));
    }

    private AdminNotificationItem notificationFromSnapshot(DocumentSnapshot document) {
        String kind = firstNonEmpty(
                document.getString("tipo"),
                document.getString("notificationKind"),
                document.getString("type")
        );
        if (!isAllowedKind(kind)) {
            return null;
        }

        AdminNotificationItem.Type type;
        String action;
        if (TYPE_ADVISOR_SEPARATION.equals(kind)) {
            type = AdminNotificationItem.Type.SEPARATION;
            action = "REVISAR";
        } else if (TYPE_CHECKOUT_PAYMENT.equals(kind)) {
            type = AdminNotificationItem.Type.PAYMENT;
            action = "PROCESAR";
        } else {
            type = AdminNotificationItem.Type.DELIVERY;
            action = "VER PROYECTO";
        }

        long createdAt = longValue(document.get("createdAt"));
        AdminNotificationItem.Section section = isYesterday(createdAt)
                ? AdminNotificationItem.Section.YESTERDAY
                : AdminNotificationItem.Section.TODAY;

        return new AdminNotificationItem(
                document.getId(),
                section,
                type,
                firstNonEmpty(document.getString("titulo"), defaultTitle(kind)),
                badge(createdAt),
                firstNonEmpty(document.getString("body"), document.getString("line1"), defaultBody(kind)),
                firstNonEmpty(document.getString("line2"), document.getString("inmuebleNombre"), document.getString("projectName")),
                action,
                firstNonEmpty(document.getString("separationId"), document.getString("separacionId")),
                firstNonEmpty(document.getString("projectId"), document.getString("propertyId")),
                kind,
                createdAt
        );
    }

    private boolean isAllowedKind(String kind) {
        return TYPE_ADVISOR_SEPARATION.equals(kind)
                || TYPE_CHECKOUT_PAYMENT.equals(kind)
                || TYPE_DELIVERY_DUE.equals(kind);
    }

    private String defaultTitle(String kind) {
        if (TYPE_ADVISOR_SEPARATION.equals(kind)) {
            return "Separacion registrada por asesor";
        }
        if (TYPE_CHECKOUT_PAYMENT.equals(kind)) {
            return "Pago de checkout recibido";
        }
        return "Fecha de entrega cumplida";
    }

    private String defaultBody(String kind) {
        if (TYPE_ADVISOR_SEPARATION.equals(kind)) {
            return "Un asesor registro una separacion pendiente de decision.";
        }
        if (TYPE_CHECKOUT_PAYMENT.equals(kind)) {
            return "Un cliente realizo el pago de separacion.";
        }
        return "Revisa si el proyecto debe cambiar manualmente a En venta.";
    }

    private boolean isYesterday(long timestamp) {
        if (timestamp <= 0L) {
            return false;
        }
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        return today.get(java.util.Calendar.DAY_OF_YEAR) - date.get(java.util.Calendar.DAY_OF_YEAR) == 1
                && today.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR);
    }

    private String badge(long timestamp) {
        if (timestamp <= 0L) {
            return "Ahora";
        }
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date(timestamp));
    }

    private long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (Exception ignored) {}
        }
        return 0L;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }
}
