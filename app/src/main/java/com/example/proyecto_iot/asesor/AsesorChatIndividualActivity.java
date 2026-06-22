package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.MensajeChat;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    private RecyclerView rvChatMessages;
    private MensajeChatAdapter adapter;
    private List<MensajeChat> mensajes = new ArrayList<>();
    private EditText etMessage;

    private String chatId;
    private String clienteId;
    private String clienteNombre;
    private String clienteAvatarUrl;

    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        sessionManager = AuthSessionManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        // Obtener datos del intent
        chatId = getIntent().getStringExtra("chatId");
        clienteId = getIntent().getStringExtra("clienteId");
        clienteNombre = getIntent().getStringExtra("clienteNombre");
        clienteAvatarUrl = getIntent().getStringExtra("clienteAvatar");

        setupBackButton();
        setupHeader();

        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });

        findViewById(R.id.btnMoreChat).setOnClickListener(v ->
                Toast.makeText(this, "Más opciones (por implementar)", Toast.LENGTH_SHORT).show()
        );

        setupRecyclerView();
        loadMessages();
    }

    private void setupHeader() {
        TextView txtUserName = findViewById(R.id.txtChatUserName);
        ImageView imgAvatar = findViewById(R.id.chatAvatar);

        txtUserName.setText(clienteNombre != null ? clienteNombre : "Cliente");

        if (clienteAvatarUrl != null && !clienteAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(clienteAvatarUrl)
                    .placeholder(R.drawable.sa_profile_user_1)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.sa_profile_user_1);
        }
    }

    private void setupRecyclerView() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        adapter = new MensajeChatAdapter();
        rvChatMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Error: chat no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Escuchar mensajes en tiempo real
        messagesListener = db.collection("Mensajes")
                .whereEqualTo("conversationId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar mensajes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mensajes.clear();
                    if (value != null && !value.isEmpty()) {
                        String currentDate = "";
                        String asesorId = sessionManager.getUid();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MensajeChat msg = doc.toObject(MensajeChat.class);
                            if (msg != null) {
                                msg.setId(doc.getId());
                                msg.setSentByMe(asesorId != null && asesorId.equals(msg.getSenderId()));

                                // Agregar header de fecha si cambia el día
                                String msgDate = msg.getDate();
                                if (!msgDate.equals(currentDate)) {
                                    MensajeChat dateHeader = new MensajeChat();
                                    dateHeader.setDateHeader(true);
                                    dateHeader.setTexto(msgDate);
                                    mensajes.add(dateHeader);
                                    currentDate = msgDate;
                                }

                                mensajes.add(msg);
                            }
                        }

                        adapter.setMensajes(mensajes);
                        scrollToBottom();

                        // Marcar como leído
                        markChatAsRead();
                    } else {
                        adapter.setMensajes(mensajes);
                    }
                });
    }

    private void sendMessage(String text) {
        String asesorId = sessionManager.getUid();
        if (asesorId == null || asesorId.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis();

        // Crear mensaje
        Map<String, Object> msgData = new HashMap<>();
        msgData.put("conversationId", chatId);
        msgData.put("senderId", asesorId);
        msgData.put("texto", text);
        msgData.put("fechaHora", currentTime);
        msgData.put("timestamp", timestamp);

        // Guardar en Firestore
        db.collection("Mensajes").add(msgData)
                .addOnSuccessListener(docRef -> {
                    // Actualizar último mensaje en la conversación
                    Map<String, Object> update = new HashMap<>();
                    update.put("ultimoMensaje", text);
                    update.put("ultimoMensajeFecha", currentTime);
                    // El mensaje enviado por el asesor no debe marcar como no leído para el asesor
                    // pero sí para el cliente (lo gestionas en el cliente)
                    db.collection("Conversaciones").document(chatId)
                            .update(update)
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al actualizar conversación", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void markChatAsRead() {
        // Marcar conversación como leída para el asesor
        db.collection("Conversaciones").document(chatId)
                .update("unread", false)
                .addOnFailureListener(e ->
                        // No mostramos toast para no molestar
                        e.printStackTrace()
                );
    }

    private void scrollToBottom() {
        if (!mensajes.isEmpty()) {
            rvChatMessages.smoothScrollToPosition(mensajes.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}