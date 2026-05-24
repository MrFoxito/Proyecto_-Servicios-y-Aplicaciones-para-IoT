package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.MensajeChat;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    private RecyclerView rvChatMessages;
    private MensajeChatAdapter adapter;
    private List<MensajeChat> mensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        setupBackButton();
        
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> showPendingToast());
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
        mensajes.addAll(new LocalSchemaStorage(this).getAdvisorMessages());
        adapter.notifyDataSetChanged();
        if (!mensajes.isEmpty()) {
            rvChatMessages.scrollToPosition(mensajes.size() - 1);
        }
    }
}
