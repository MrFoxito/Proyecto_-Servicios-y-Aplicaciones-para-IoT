package com.example.proyecto_iot.data;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.entity.Chat;
import com.example.proyecto_iot.entity.MensajeChat;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FirebaseChatRepository {

    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String COLLECTION_CONVERSACIONES = "conversaciones";
    private static final String COLLECTION_MENSAJES = "mensajes";

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface ConversationsCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(String message);
    }

    public interface AdvisorsCallback {
        void onSuccess(List<Advisor> advisors);
        void onError(String message);
    }

    public interface MessagesCallback {
        void onSuccess(List<Message> messages);
        void onError(String message);
    }

    public interface ConversationCallback {
        void onSuccess(Conversation conversation);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public static class Advisor {
        public final String uid;
        public final String name;
        public final String email;
        public final String avatarKey;
        public final String assignmentId;

        public Advisor(String uid, String name, String email, String avatarKey) {
            this(uid, name, email, avatarKey, "");
        }

        public Advisor(String uid, String name, String email, String avatarKey, String assignmentId) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.avatarKey = avatarKey;
            this.assignmentId = firstNonEmptyStatic(assignmentId);
        }
    }

    public static class Conversation {
        public final String id;
        public final String clienteUid;
        public final String asesorUid;
        public final String asesorNombre;
        public final String clienteNombre;
        public final String lastMessage;
        public final long lastMessageAt;
        public final boolean unreadForCliente;
        public final boolean active;
        public final String projectId;
        public final String projectName;
        public final String projectLocation;
        public final String projectPrice;
        public final String projectImageUrl;

        public Conversation(
                String id,
                String clienteUid,
                String asesorUid,
                String asesorNombre,
                String clienteNombre,
                String lastMessage,
                long lastMessageAt,
                boolean unreadForCliente,
                boolean active,
                String projectId,
                String projectName,
                String projectLocation,
                String projectPrice,
                String projectImageUrl
        ) {
            this.id = id;
            this.clienteUid = clienteUid;
            this.asesorUid = asesorUid;
            this.asesorNombre = asesorNombre;
            this.clienteNombre = clienteNombre;
            this.lastMessage = lastMessage;
            this.lastMessageAt = lastMessageAt;
            this.unreadForCliente = unreadForCliente;
            this.active = active;
            this.projectId = projectId;
            this.projectName = projectName;
            this.projectLocation = projectLocation;
            this.projectPrice = projectPrice;
            this.projectImageUrl = projectImageUrl;
        }
    }

    /** Snapshot used to keep a chat tied to the property that started it. */
    public static class ProjectChatContext {
        public final String projectId;
        public final String projectName;
        public final String projectLocation;
        public final String projectPrice;
        public final String projectImageUrl;

        public ProjectChatContext(String projectId, String projectName, String projectLocation,
                                  String projectPrice, String projectImageUrl) {
            this.projectId = projectId == null ? "" : projectId.trim();
            this.projectName = projectName == null ? "" : projectName.trim();
            this.projectLocation = projectLocation == null ? "" : projectLocation.trim();
            this.projectPrice = projectPrice == null ? "" : projectPrice.trim();
            this.projectImageUrl = projectImageUrl == null ? "" : projectImageUrl.trim();
        }
    }

    public static class Message {
        public final String id;
        public final String conversationId;
        public final String senderUid;
        public final String receiverUid;
        public final String text;
        public final long createdAt;

        public Message(String id, String conversationId, String senderUid, String receiverUid, String text, long createdAt) {
            this.id = id;
            this.conversationId = conversationId;
            this.senderUid = senderUid;
            this.receiverUid = receiverUid;
            this.text = text;
            this.createdAt = createdAt;
        }
    }

    public ListenerRegistration listenClientConversations(String clienteUid, ConversationsCallback callback) {
        return firestore.collection(COLLECTION_CONVERSACIONES)
                .whereArrayContains("participantUids", clienteUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("No se pudieron leer conversaciones: " + safeMessage(error));
                        return;
                    }
                    List<Conversation> conversations = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Conversation conversation = conversationFromSnapshot(document);
                            if (conversation.active && !"migrated".equals(document.getString("legacyState"))) {
                                conversations.add(conversation);
                            }
                        }
                    }
                    Collections.sort(conversations, (left, right) ->
                            Long.compare(right.lastMessageAt, left.lastMessageAt));
                    callback.onSuccess(conversations);
                });
    }

    public void searchActiveAdvisors(@Nullable String queryText, AdvisorsCallback callback) {
        String normalizedQuery = normalize(queryText);
        firestore.collection(COLLECTION_USUARIOS)
                .whereEqualTo("rol", "asesor")
                .whereEqualTo("estado", "activo")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Advisor> advisors = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Advisor advisor = advisorFromSnapshot(document);
                        if (normalizedQuery.isEmpty()
                                || normalize(advisor.name).contains(normalizedQuery)
                                || normalize(advisor.email).contains(normalizedQuery)) {
                            advisors.add(advisor);
                        }
                    }
                    advisors.sort(Comparator.comparing(advisor -> advisor.name.toLowerCase(Locale.ROOT)));
                    callback.onSuccess(advisors);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron buscar asesores: " + safeMessage(error)));
    }

    public void findOrCreateConversation(
            String clienteUid,
            String clienteNombre,
            Advisor advisor,
            ConversationCallback callback
    ) {
        callback.onError("Selecciona un proyecto para iniciar una conversación.");
    }

    public ListenerRegistration listenMessages(String conversationId, String currentUid, MessagesCallback callback) {
        return firestore.collection(COLLECTION_MENSAJES)
                .whereArrayContains("participantUids", currentUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("No se pudieron leer mensajes: " + safeMessage(error));
                        return;
                    }
                    List<Message> messages = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Message message = messageFromSnapshot(document);
                            if (conversationId.equals(message.conversationId)) {
                                messages.add(message);
                            }
                        }
                    }
                    messages.sort(Comparator.comparingLong(message -> message.createdAt));
                    callback.onSuccess(messages);
                });
    }

    public void findOrCreateProjectConversation(
            String clienteUid,
            String clienteNombre,
            Advisor advisor,
            ProjectChatContext project,
            ConversationCallback callback
    ) {
        String normalizedClient = firstNonEmpty(clienteUid);
        if (advisor == null || project == null || normalizedClient.isEmpty()
                || firstNonEmpty(advisor.uid).isEmpty() || project.projectId.isEmpty()) {
            callback.onError("No se pudo identificar el proyecto o asesor para iniciar el chat.");
            return;
        }
        String conversationId = projectConversationId(normalizedClient, project.projectId);
        DocumentReference reference = firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId);
        firestore.runTransaction(transaction -> {
                    DocumentSnapshot existing = transaction.get(reference);
                    if (existing.exists()) {
                        String existingClient = firstNonEmpty(existing.getString("clienteUid"));
                        String existingProject = firstNonEmpty(existing.getString("projectId"));
                        if (!normalizedClient.equals(existingClient) || !project.projectId.equals(existingProject)) {
                            throw new FirebaseFirestoreException("La conversación existente no coincide con el proyecto.",
                                    FirebaseFirestoreException.Code.ABORTED);
                        }
                        return null;
                    }
                    transaction.set(reference, projectConversationData(conversationId, normalizedClient,
                            clienteNombre, advisor, project, System.currentTimeMillis()));
                    return null;
                })
                .addOnSuccessListener(unused -> reference.get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) callback.onSuccess(conversationFromSnapshot(snapshot));
                            else callback.onError("No se pudo recuperar la conversación creada.");
                        })
                        .addOnFailureListener(error -> callback.onError(
                                "No se pudo abrir la conversación: " + safeMessage(error))))
                .addOnFailureListener(error -> callback.onError(
                        "No se pudo crear la conversación: " + safeMessage(error)));
    }

    /** Opens the unique project chat from an appointment without duplicating conversation history. */
    public void findOrCreateAppointmentConversation(
            String clienteUid,
            String clienteNombre,
            Advisor advisor,
            String citaId,
            ProjectChatContext project,
            ConversationCallback callback
    ) {
        String normalizedClient = firstNonEmpty(clienteUid);
        String normalizedAppointment = firstNonEmpty(citaId);
        if (advisor == null || project == null || normalizedClient.isEmpty()
                || advisor.uid == null || advisor.uid.trim().isEmpty()
                || normalizedAppointment.isEmpty() || project.projectId.isEmpty()) {
            callback.onError("No se pudo validar la cita, el proyecto o el asesor para iniciar el chat.");
            return;
        }
        findOrCreateProjectConversation(normalizedClient, clienteNombre, advisor, project, callback);
    }

    /** Advisors can open only an appointment chat already initiated by its client. */
    public void getAppointmentConversation(String clienteUid, String asesorUid, String citaId,
                                           ConversationCallback callback) {
        String conversationId = appointmentConversationId(clienteUid, asesorUid, citaId);
        firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("El cliente aún no inició la conversación de esta cita.");
                    } else if (!matchesAppointmentConversation(snapshot, clienteUid, asesorUid, citaId)) {
                        callback.onError("La conversación no coincide con la cita seleccionada.");
                    } else {
                        callback.onSuccess(conversationFromSnapshot(snapshot));
                    }
                })
                .addOnFailureListener(error -> callback.onError(
                        "No se pudo abrir la conversación de la cita: " + safeMessage(error)));
    }

    public void sendMessage(
            String conversationId,
            String clienteUid,
            String asesorUid,
            String text,
            SimpleCallback callback
    ) {
        long now = System.currentTimeMillis();
        String messageId = "msg_" + now;
        DocumentReference messageReference = firestore.collection(COLLECTION_MENSAJES).document(messageId);
        DocumentReference conversationReference = firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId);

        Map<String, Object> message = new HashMap<>();
        message.put("id", messageId);
        message.put("conversationId", conversationId);
        message.put("senderUid", clienteUid);
        message.put("receiverUid", asesorUid);
        message.put("clienteUid", clienteUid);
        message.put("asesorUid", asesorUid);
        message.put("participantUids", Arrays.asList(clienteUid, asesorUid));
        message.put("text", text);
        message.put("createdAt", now);
        message.put("sentByRole", "cliente");
        message.put("read", false);

        Map<String, Object> conversationUpdate = new HashMap<>();
        conversationUpdate.put("lastMessage", text);
        conversationUpdate.put("lastMessageAt", now);
        conversationUpdate.put("updatedAt", now);
        conversationUpdate.put("unreadForCliente", false);
        conversationUpdate.put("unreadForAsesor", true);
        conversationUpdate.put("active", true);

        WriteBatch batch = firestore.batch();
        batch.set(messageReference, message, SetOptions.merge());
        batch.set(conversationReference, conversationUpdate, SetOptions.merge());
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo enviar el mensaje: " + safeMessage(error)));
    }

    private Conversation conversationFromSnapshot(DocumentSnapshot snapshot) {
        return new Conversation(
                snapshot.getId(),
                firstNonEmpty(snapshot.getString("clienteUid")),
                firstNonEmpty(snapshot.getString("asesorUid")),
                firstNonEmpty(snapshot.getString("asesorNombre"), "Asesor"),
                firstNonEmpty(snapshot.getString("clienteNombre"), "Cliente"),
                firstNonEmpty(snapshot.getString("lastMessage"), "Sin mensajes aun"),
                longValue(snapshot.get("lastMessageAt")),
                Boolean.TRUE.equals(snapshot.getBoolean("unreadForCliente")),
                !Boolean.FALSE.equals(snapshot.getBoolean("active"))
                , firstNonEmpty(snapshot.getString("projectId"))
                , firstNonEmpty(snapshot.getString("projectName"))
                , firstNonEmpty(snapshot.getString("projectLocation"))
                , firstNonEmpty(snapshot.getString("projectPrice"))
                , firstNonEmpty(snapshot.getString("projectImageUrl"))
        );
    }

    private Advisor advisorFromSnapshot(DocumentSnapshot snapshot) {
        String name = firstNonEmpty(
                snapshot.getString("nombre"),
                (firstNonEmpty(snapshot.getString("nombres")) + " " + firstNonEmpty(snapshot.getString("apellidos"))).trim(),
                "Asesor"
        );
        return new Advisor(
                firstNonEmpty(snapshot.getString("uid"), snapshot.getId()),
                name,
                firstNonEmpty(snapshot.getString("correo"), snapshot.getString("email")),
                firstNonEmpty(snapshot.getString("avatarKey"), "sa_profile_asesor_1")
        );
    }

    private Message messageFromSnapshot(DocumentSnapshot snapshot) {
        return new Message(
                snapshot.getId(),
                firstNonEmpty(snapshot.getString("conversationId")),
                firstNonEmpty(snapshot.getString("senderUid")),
                firstNonEmpty(snapshot.getString("receiverUid")),
                firstNonEmpty(snapshot.getString("text")),
                longValue(snapshot.get("createdAt"))
        );
    }

    static String appointmentConversationId(String clienteUid, String asesorUid, String citaId) {
        return stableConversationId("appointment_chat_", clienteUid, asesorUid, citaId);
    }

    static String projectConversationId(String clienteUid, String projectId) {
        return "project_chat_" + firstNonEmptyStatic(clienteUid) + "_" + firstNonEmptyStatic(projectId);
    }

    private Map<String, Object> projectConversationData(String conversationId, String clienteUid,
                                                        String clienteNombre, Advisor advisor,
                                                        ProjectChatContext project, long now) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", conversationId);
        data.put("conversationType", "project");
        data.put("schemaVersion", 2);
        data.put("clienteUid", clienteUid);
        data.put("asesorUid", firstNonEmpty(advisor.uid));
        data.put("participantUids", Arrays.asList(clienteUid, firstNonEmpty(advisor.uid)));
        data.put("asesorNombre", firstNonEmpty(advisor.name, "Asesor"));
        data.put("clienteNombre", firstNonEmpty(clienteNombre, "Cliente"));
        data.put("projectId", project.projectId);
        data.put("assignmentId", firstNonEmpty(advisor.assignmentId));
        data.put("projectName", firstNonEmpty(project.projectName, "Proyecto"));
        data.put("projectLocation", project.projectLocation);
        data.put("projectPrice", project.projectPrice);
        data.put("projectImageUrl", project.projectImageUrl);
        data.put("lastMessage", "Sin mensajes aun");
        data.put("lastMessageAt", now);
        data.put("updatedAt", now);
        data.put("createdAt", now);
        data.put("unreadForCliente", false);
        data.put("unreadForAsesor", false);
        data.put("active", true);
        return data;
    }

    private static String stableConversationId(String prefix, String clienteUid, String asesorUid, String sourceId) {
        String source = firstNonEmptyStatic(clienteUid) + "\u0000" + firstNonEmptyStatic(asesorUid)
                + "\u0000" + firstNonEmptyStatic(sourceId);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder(prefix);
            for (byte part : digest) value.append(String.format(Locale.ROOT, "%02x", part));
            return value.toString();
        } catch (NoSuchAlgorithmException ignored) {
            return prefix + Integer.toHexString(source.hashCode());
        }
    }

    private static String firstNonEmptyStatic(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean matchesAppointmentConversation(DocumentSnapshot snapshot, String clienteUid,
                                                   String asesorUid, String citaId) {
        List<String> participants = (List<String>) snapshot.get("participantUids");
        return citaId.equals(firstNonEmpty(snapshot.getString("citaId")))
                && clienteUid.equals(firstNonEmpty(snapshot.getString("clienteUid")))
                && asesorUid.equals(firstNonEmpty(snapshot.getString("asesorUid")))
                && participants != null && participants.size() == 2
                && participants.contains(clienteUid) && participants.contains(asesorUid);
    }

    private long longValue(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String normalize(@Nullable String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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
        if (error instanceof FirebaseFirestoreException
                && ((FirebaseFirestoreException) error).getCode()
                == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            return "No tienes autorización para esta conversación. Verifica que el asesor esté activo y asignado al proyecto.";
        }
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }


    /*


     */

    public interface ChatCallback {
        void onSuccess(List<Chat> chats);
        void onError(String message);
    }

    public interface MensajeCallback {
        void onSuccess(List<MensajeChat> mensajes);
        void onError(String message);
    }

    /**
     * Escucha las conversaciones de un asesor (basado en participantUids).
     * Devuelve una lista de objetos Chat (entidad propia).
     */
    public ListenerRegistration listenAdvisorConversations(String asesorUid, ChatCallback callback) {
        return firestore.collection(COLLECTION_CONVERSACIONES)
                .whereArrayContains("participantUids", asesorUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("No se pudieron leer conversaciones: " + safeMessage(error));
                        return;
                    }
                    List<Chat> chats = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Chat chat = convertirSnapshotAChat(doc, asesorUid);
                            if (chat != null && chat.isActive() && !"migrated".equals(doc.getString("legacyState"))) {
                                chats.add(chat);
                            }
                        }
                    }
                    // Ordenar por último mensaje (más reciente primero)
                    chats.sort((a, b) -> Long.compare(b.getLastMessageAt(), a.getLastMessageAt()));
                    callback.onSuccess(chats);
                });
    }

    /**
     * Escucha los mensajes de una conversación.
     * Devuelve una lista de MensajeChat (entidad propia).
     */
    public ListenerRegistration listenMessages(String conversationId, String currentUid, MensajeCallback callback) {
        return firestore.collection(COLLECTION_MENSAJES)
                .whereEqualTo("conversationId", conversationId)
                .whereArrayContains("participantUids", currentUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("No se pudieron leer mensajes: " + safeMessage(error));
                        return;
                    }
                    List<MensajeChat> mensajes = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            MensajeChat msg = convertirSnapshotAMensaje(doc, currentUid);
                            if (msg != null) {
                                mensajes.add(msg);
                            }
                        }
                    }
                    mensajes.sort(Comparator.comparingLong(MensajeChat::getTimestamp));
                    callback.onSuccess(mensajes);
                });
    }

    /**
     * Envía un mensaje en una conversación (usando entidades propias).
     */
    public void sendMessageAs(String conversationId, String senderUid, String receiverUid,
                            String text, SimpleCallback callback) {
        long now = System.currentTimeMillis();
        String messageId = "msg_" + now;
        DocumentReference msgRef = firestore.collection(COLLECTION_MENSAJES).document(messageId);
        DocumentReference convRef = firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId);

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("id", messageId);
        msgData.put("conversationId", conversationId);
        msgData.put("senderUid", senderUid);
        msgData.put("receiverUid", receiverUid);
        // The conversation stores participants in cliente/asesor order, regardless of sender.
        msgData.put("participantUids", Arrays.asList(receiverUid, senderUid));
        msgData.put("text", text);
        msgData.put("createdAt", now);
        msgData.put("sentByRole", "asesor");
        msgData.put("fechaHora", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(new java.util.Date(now)));
        msgData.put("timestamp", now);

        // Actualizar conversación: último mensaje y marcar como no leído para el receptor
        Map<String, Object> convUpdate = new HashMap<>();
        convUpdate.put("lastMessage", text);
        convUpdate.put("lastMessageAt", now);
        convUpdate.put("updatedAt", now);
        convUpdate.put("unreadForCliente", true);
        convUpdate.put("unreadForAsesor", false);

        WriteBatch batch = firestore.batch();
        batch.set(msgRef, msgData, SetOptions.merge());
        batch.set(convRef, convUpdate, SetOptions.merge());
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo enviar el mensaje: " + safeMessage(error)));
    }

    /**
     * Marca una conversación como leída para el usuario actual.
     */
    public void markConversationAsRead(String conversationId, String currentUid, SimpleCallback callback) {
        // Actualizar ambos campos a false, pero solo el que corresponda al usuario actual
        // Podríamos leer primero la conversación para saber si currentUid es cliente o asesor,
        // pero por simplicidad actualizamos ambos.
        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadForCliente", false);
        updates.put("unreadForAsesor", false);

        firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError("Error al marcar como leído: " + safeMessage(error)));
    }

    // ===================== CONVERSIÓN A ENTIDADES PROPIAS =====================

    private Chat convertirSnapshotAChat(DocumentSnapshot doc, String currentUid) {
        if (!doc.exists()) return null;

        String id = doc.getId();
        String clienteUid = doc.getString("clienteUid");
        String asesorUid = doc.getString("asesorUid");
        String clienteNombre = doc.getString("clienteNombre");
        String asesorNombre = doc.getString("asesorNombre");
        String lastMessage = doc.getString("lastMessage");
        Long lastMessageAt = doc.getLong("lastMessageAt");
        Boolean active = doc.getBoolean("active");
        Boolean unreadForCliente = doc.getBoolean("unreadForCliente");
        Boolean unreadForAsesor = doc.getBoolean("unreadForAsesor");

        // Determinar si el mensaje no leído es para el usuario actual
        boolean unread = false;
        if (currentUid != null && currentUid.equals(clienteUid)) {
            unread = Boolean.TRUE.equals(unreadForCliente);
        } else if (currentUid != null && currentUid.equals(asesorUid)) {
            unread = Boolean.TRUE.equals(unreadForAsesor);
        }

        Chat chat = new Chat();
        chat.setId(id);
        chat.setClienteId(clienteUid);
        chat.setAsesorId(asesorUid);
        chat.setClienteNombre(clienteNombre != null ? clienteNombre : "Cliente");
        chat.setAsesorNombre(asesorNombre != null ? asesorNombre : "Asesor");
        chat.setProjectId(firstNonEmpty(doc.getString("projectId")));
        chat.setProjectName(firstNonEmpty(doc.getString("projectName")));
        chat.setProjectLocation(firstNonEmpty(doc.getString("projectLocation")));
        chat.setProjectPrice(firstNonEmpty(doc.getString("projectPrice")));
        chat.setProjectImageUrl(firstNonEmpty(doc.getString("projectImageUrl")));
        chat.setUltimoMensaje(lastMessage != null ? lastMessage : "");
        chat.setUltimoMensajeFecha(lastMessageAt != null ? String.valueOf(lastMessageAt) : "");
        chat.setLastMessageAt(lastMessageAt != null ? lastMessageAt : 0);
        chat.setUnread(unread);
        chat.setActive(active != null ? active : true);

        return chat;
    }

    private MensajeChat convertirSnapshotAMensaje(DocumentSnapshot doc, String currentUid) {
        if (!doc.exists()) return null;

        String id = doc.getId();
        String conversationId = doc.getString("conversationId");
        String senderUid = doc.getString("senderUid");
        // Robustez extrema en el campo de texto
        String texto = firstNonEmpty(doc.getString("text"), doc.getString("texto"), doc.getString("mensaje"), doc.getString("message"));
        
        long createdAt = longValue(doc.get("createdAt"));
        if (createdAt == 0) {
            createdAt = longValue(doc.get("timestamp"));
        }

        String fechaHora = doc.getString("fechaHora");

        if (fechaHora == null && createdAt > 0) {
            fechaHora = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(new java.util.Date(createdAt));
        }

        MensajeChat msg = new MensajeChat();
        msg.setId(id);
        msg.setConversationId(conversationId);
        msg.setSenderId(senderUid);
        msg.setTexto(texto != null ? texto : "");
        msg.setTimestamp(createdAt);
        msg.setFechaHora(fechaHora != null ? fechaHora : "");
        if (currentUid != null) {
            msg.setSentByMe(currentUid.equals(senderUid));
        }
        return msg;
    }
}
