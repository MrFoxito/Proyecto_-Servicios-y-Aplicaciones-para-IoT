package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.MensajeChat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    private RecyclerView rvChatMessages;
    private MensajeChatAdapter adapter;
    private List<MensajeChat> mensajes;
    private String chatId;
    private String userName;
    private int userAvatarRes;
    private LocalSchemaStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        storage = new LocalSchemaStorage(this);

        // Obtener datos del intent
        chatId = getIntent().getStringExtra("chatId");
        userName = getIntent().getStringExtra("userName");
        userAvatarRes = getIntent().getIntExtra("userAvatar", R.drawable.sa_profile_user_1);

        setupBackButton();
        setupHeader();

        EditText etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });

        findViewById(R.id.btnMoreChat).setOnClickListener(v -> showPendingToast());

        setupRecyclerView();
        loadMessages();
    }

    private void setupHeader() {
        TextView txtUserName = findViewById(R.id.txtChatUserName);
        ImageView imgAvatar = findViewById(R.id.chatAvatar);

        if (userName != null) {
            txtUserName.setText(userName);
        }
        if (userAvatarRes != 0) {
            imgAvatar.setImageResource(userAvatarRes);
        }
    }

    private void setupRecyclerView() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Los mensajes nuevos aparecen abajo
        rvChatMessages.setLayoutManager(layoutManager);

        mensajes = new ArrayList<>();
        adapter = new MensajeChatAdapter(mensajes);
        rvChatMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        mensajes.clear();
        // Cargamos mensajes desde el storage local (ficticios/persistidos localmente)
        mensajes.addAll(storage.getAdvisorMessages());
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void sendMessage(String text) {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        // 1. Guardar en storage local (para que persista en la sesión de prueba)
        storage.addChatMessage(text, true);

        // 2. Actualizar UI inmediatamente
        MensajeChat nuevoMensaje = new MensajeChat(text, currentTime, true);
        mensajes.add(nuevoMensaje);
        adapter.notifyItemInserted(mensajes.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!mensajes.isEmpty()) {
            rvChatMessages.smoothScrollToPosition(mensajes.size() - 1);
        }
    }
}