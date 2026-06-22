package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.example.proyecto_iot.entity.MensajeChat;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private FirebaseChatRepository chatRepository;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        sessionManager = AuthSessionManager.getInstance(this);
        chatRepository = new FirebaseChatRepository();

        // Obtener datos del intent
        chatId = getIntent().getStringExtra("conversationId");
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

        String currentUid = sessionManager.getUid();
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Escuchar mensajes en tiempo real usando el repositorio}
        Log.i("AsesorChatIndividualActivity", "Cargando mensaje: chatId=" + chatId + ", currentUid=" + currentUid);
        messagesListener = chatRepository.listenMessages(chatId, currentUid,
                new FirebaseChatRepository.MensajeCallback() {
                    @Override
                    public void onSuccess(List<MensajeChat> messages) {
                        mensajes.clear();

                        if (messages != null && !messages.isEmpty()) {
                            String currentDate = "";
                            for (MensajeChat msg : messages) {
                                // Agregar header de fecha si cambia el día
                                String msgDate = msg.getDate();
                                if (msgDate != null && !msgDate.equals(currentDate)) {
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

                        // Marcar conversación como leída para el usuario actual
                        markChatAsRead();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AsesorChatIndividualActivity.this,
                                "Error al cargar mensajes: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendMessage(String text) {
        String senderUid = sessionManager.getUid();
        if (senderUid == null || senderUid.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(clienteId)) {
            Toast.makeText(this, "Error: destinatario no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.sendMessageAs(chatId, senderUid, clienteId, text,
                new FirebaseChatRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // Mensaje enviado correctamente
                        // (el listener de mensajes lo mostrará automáticamente)
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AsesorChatIndividualActivity.this,
                                "Error al enviar mensaje: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void markChatAsRead() {
        String currentUid = sessionManager.getUid();
        if (currentUid == null || currentUid.isEmpty() || TextUtils.isEmpty(chatId)) {
            return;
        }

        chatRepository.markConversationAsRead(chatId, currentUid,
                new FirebaseChatRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // Marcado como leído
                    }

                    @Override
                    public void onError(String message) {
                        // No mostramos toast para no molestar
                    }
                });
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