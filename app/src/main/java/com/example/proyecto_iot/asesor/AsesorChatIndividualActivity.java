package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
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
        loadHardcodedMessages();
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

    private void loadHardcodedMessages() {
        mensajes.clear();
        
        mensajes.add(new MensajeChat("LUNES, 24 DE OCT", true));
        mensajes.add(new MensajeChat("msg1", "Hola Julian, vi la propiedad de la Costa Moderna que acabas de publicar. El plano se ve increíble, especialmente el balcón de la suite principal.", "10:14 AM", false));
        mensajes.add(new MensajeChat("msg2", "¡Buen día, Julian! Realmente es una joya. El arquitecto se enfocó en maximizar la vista al Pacífico desde cada rincón de esa suite.", "10:16 AM", true));
        mensajes.add(new MensajeChat("msg3", "Me gustaría agendar una visita presencial si es posible. ¿Tienen disponibilidad para el próximo sábado por la mañana?", "10:20 AM", false));
        mensajes.add(new MensajeChat("msg4", "Claro que sí, déjame revisar la agenda un momento...", "10:25 AM", true));
        mensajes.add(new MensajeChat("msg5", "Tengo un espacio a las 10:30 AM el sábado. ¿Te funciona ese horario?", "10:28 AM", true));
        
        adapter.notifyDataSetChanged();
        rvChatMessages.scrollToPosition(mensajes.size() - 1);
    }
}
