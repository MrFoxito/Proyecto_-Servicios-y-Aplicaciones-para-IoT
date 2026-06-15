package com.example.proyecto_iot.data;

import androidx.annotation.Nullable;

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

        public Advisor(String uid, String name, String email, String avatarKey) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.avatarKey = avatarKey;
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

        public Conversation(
                String id,
                String clienteUid,
                String asesorUid,
                String asesorNombre,
                String clienteNombre,
                String lastMessage,
                long lastMessageAt,
                boolean unreadForCliente,
                boolean active
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
                            if (conversation.active) {
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
        String conversationId = conversationId(clienteUid, advisor.uid);
        DocumentReference reference = firestore.collection(COLLECTION_CONVERSACIONES).document(conversationId);
        reference.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onSuccess(conversationFromSnapshot(snapshot));
                        return;
                    }

                    long now = System.currentTimeMillis();
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", conversationId);
                    data.put("clienteUid", clienteUid);
                    data.put("asesorUid", advisor.uid);
                    data.put("participantUids", Arrays.asList(clienteUid, advisor.uid));
                    data.put("asesorNombre", advisor.name);
                    data.put("clienteNombre", clienteNombre);
                    data.put("lastMessage", "Conversacion iniciada");
                    data.put("lastMessageAt", now);
                    data.put("updatedAt", now);
                    data.put("unreadForCliente", false);
                    data.put("unreadForAsesor", false);
                    data.put("active", true);

                    reference.set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess(new Conversation(
                                    conversationId,
                                    clienteUid,
                                    advisor.uid,
                                    advisor.name,
                                    clienteNombre,
                                    "Conversacion iniciada",
                                    now,
                                    false,
                                    true
                            )))
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo crear la conversacion: " + safeMessage(error)));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo abrir la conversacion: " + safeMessage(error)));
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

    private String conversationId(String clienteUid, String asesorUid) {
        return clienteUid + "_" + asesorUid;
    }

    private long longValue(Object value) {
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
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }
}
