package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.MensajeChat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    private RecyclerView rvChatMessages;
    private MensajeChatAdapter adapter;
    private List<MensajeChat> mensajes;
    private LocalSchemaStorage storage;
    private String chatId;
    private String clientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        setupBackButton();
        storage = new LocalSchemaStorage(this);

        // Recibir datos del Intent
        chatId = getIntent().getStringExtra("extra_chat_id");
        clientName = getIntent().getStringExtra("extra_client_name");

        // Mostrar nombre del cliente dinámicamente
        TextView txtClientName = findViewById(R.id.txtClientName);
        if (clientName != null && !clientName.isEmpty()) {
            txtClientName.setText(clientName);
        }

        // Configurar input y botón de envío
        EditText editMessageInput = findViewById(R.id.editMessageInput);
        if (clientName != null && !clientName.isEmpty()) {
            editMessageInput.setHint("Mensaje a " + clientName + "...");
        }

        findViewById(R.id.btnSendMessage).setOnClickListener(v -> {
            String text = editMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                // Persistir en la DB local
                storage.addChatMessage(chatId, text, true);
                editMessageInput.setText("");
                // Recargar mensajes
                loadMessages();
            }
        });

        findViewById(R.id.btnMoreChat).setOnClickListener(v -> showPendingToast());

        setupRecyclerView();
        loadMessages();
    }

    private void setupRecyclerView() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Empujar contenido hacia arriba si es necesario y comenzar desde el fondo
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        
        mensajes = new ArrayList<>();
        adapter = new MensajeChatAdapter(mensajes);
        rvChatMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        mensajes.clear();
        // Cargar mensajes filtrados por chatId desde la DB local
        mensajes.addAll(storage.getChatMessages(chatId));
        adapter.notifyDataSetChanged();
        if (!mensajes.isEmpty()) {
            rvChatMessages.scrollToPosition(mensajes.size() - 1);
        }
    }
}

