package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.example.proyecto_iot.entity.Chat;

import java.util.ArrayList;
import java.util.List;

public class AsesorChatsActivity extends BaseAsesorActivity {

    private RecyclerView rvChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private List<Chat> chatListFull = new ArrayList<>(); // Para filtro
    private EditText etSearch;

    private AuthSessionManager sessionManager;
    private FirebaseChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chats);

        sessionManager = AuthSessionManager.getInstance(this);
        chatRepository = new FirebaseChatRepository();

        setupBottomNavigation(R.id.navChats);

        etSearch = findViewById(R.id.etSearch);
        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptador
        chatAdapter = new ChatAdapter(chat -> {
            Intent intent = new Intent(this, AsesorChatIndividualActivity.class);
            intent.putExtra("conversationId", chat.getId());
            intent.putExtra("clienteId", chat.getClienteId());
            intent.putExtra("clienteNombre", chat.getClienteNombre());
            intent.putExtra("clienteAvatar", chat.getClienteAvatarUrl());
            intent.putExtra("projectName", chat.getProjectName());
            startActivity(intent);
        });
        rvChats.setAdapter(chatAdapter);

        setupSearch();

        // Cargar chats desde Firestore en tiempo real
        loadChatsFromFirestore();
    }

    private void loadChatsFromFirestore() {
        String asesorId = sessionManager.getUid();
        if (asesorId == null || asesorId.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.listenAdvisorConversations(asesorId, new FirebaseChatRepository.ChatCallback() {
            @Override
            public void onSuccess(List<Chat> conversations) {
                // Guardar lista completa para el filtro
                chatListFull.clear();
                chatListFull.addAll(conversations);

                // Mostrar todos
                chatList.clear();
                chatList.addAll(conversations);
                chatAdapter.updateList(chatList);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorChatsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filterChats(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterChats(String query) {
        if (query.isEmpty()) {
            chatList.clear();
            chatList.addAll(chatListFull);
            chatAdapter.updateList(chatList);
            return;
        }

        List<Chat> filtered = new ArrayList<>();
        for (Chat chat : chatListFull) {
            String name = chat.getClienteNombre() != null ? chat.getClienteNombre().toLowerCase() : "";
            String message = chat.getUltimoMensaje() != null ? chat.getUltimoMensaje().toLowerCase() : "";
            String project = chat.getProjectName() != null ? chat.getProjectName().toLowerCase() : "";
            if (name.contains(query) || message.contains(query) || project.contains(query)) {
                filtered.add(chat);
            }
        }
        chatList.clear();
        chatList.addAll(filtered);
        chatAdapter.updateList(chatList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // El repositorio maneja los listeners, pero si necesitas removerlos manualmente,
        // puedes guardar el ListenerRegistration devuelto por listenAdvisorConversations.
        // Por ahora, el repositorio maneja su propia limpieza (no tiene un método remove explícito,
        // así que la actividad no retiene listeners).
    }
}
