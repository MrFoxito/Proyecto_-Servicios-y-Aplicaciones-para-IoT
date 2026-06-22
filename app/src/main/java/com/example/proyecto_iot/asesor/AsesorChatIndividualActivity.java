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

import java.util.ArrayList;
import java.util.List;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    private RecyclerView rvChatMessages;
    private MensajeChatAdapter adapter;
    private final List<MensajeChat> mensajesList = new ArrayList<>();
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
                Toast.makeText(this, "Portafolio del cliente (por implementar)", Toast.LENGTH_SHORT).show()
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
        if (TextUtils.isEmpty(chatId)) {
            Toast.makeText(this, "Error: Conversación no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUid = sessionManager.getUid();
        if (currentUid == null || currentUid.isEmpty()) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.i("AsesorChat", "Cargando mensajes para chatId: " + chatId);
        
        messagesListener = chatRepository.listenMessages(chatId, currentUid,
                new FirebaseChatRepository.MensajeCallback() {
                    @Override
                    public void onSuccess(List<MensajeChat> messages) {
                        if (isFinishing() || isDestroyed()) return;

                        mensajesList.clear();
                        if (messages != null && !messages.isEmpty()) {
                            String lastDate = "";
                            for (MensajeChat msg : messages) {
                                String msgDate = msg.getDate();
                                if (!msgDate.isEmpty() && !msgDate.equals(lastDate)) {
                                    MensajeChat dateHeader = new MensajeChat();
                                    dateHeader.setDateHeader(true);
                                    dateHeader.setTexto(msgDate);
                                    dateHeader.setFechaHora(msgDate + "T00:00"); 
                                    mensajesList.add(dateHeader);
                                    lastDate = msgDate;
                                }
                                mensajesList.add(msg);
                            }
                        }

                        adapter.setMensajes(new ArrayList<>(mensajesList));
                        scrollToBottom();
                        markChatAsRead();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) {
                            Toast.makeText(AsesorChatIndividualActivity.this,
                                    "Error al cargar mensajes: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendMessage(String text) {
        String senderUid = sessionManager.getUid();
        if (senderUid == null || senderUid.isEmpty()) return;

        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(clienteId)) {
            Toast.makeText(this, "Error al identificar destinatario", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.sendMessageAs(chatId, senderUid, clienteId, text,
                new FirebaseChatRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // El listener se encargará de mostrarlo
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AsesorChatIndividualActivity.this, "Error al enviar: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markChatAsRead() {
        if (TextUtils.isEmpty(chatId)) return;
        String currentUid = sessionManager.getUid();
        chatRepository.markConversationAsRead(chatId, currentUid, new FirebaseChatRepository.SimpleCallback() {
            @Override public void onSuccess() {}
            @Override public void onError(String message) {}
        });
    }

    private void scrollToBottom() {
        if (!mensajesList.isEmpty()) {
            rvChatMessages.post(() -> rvChatMessages.smoothScrollToPosition(mensajesList.size() - 1));
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
