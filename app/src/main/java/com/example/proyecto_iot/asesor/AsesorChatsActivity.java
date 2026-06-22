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
import com.example.proyecto_iot.entity.Chat;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AsesorChatsActivity extends BaseAsesorActivity {

    private RecyclerView rvChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private EditText etSearch;
    private FirebaseFirestore db;
    private AuthSessionManager sessionManager;
    private ListenerRegistration chatsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chats);

        db = FirebaseFirestore.getInstance();
        sessionManager = AuthSessionManager.getInstance(this);
        setupBottomNavigation(R.id.navChats);

        etSearch = findViewById(R.id.etSearch);
        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptador
        chatAdapter = new ChatAdapter(chat -> {
            // Abrir chat individual
            Intent intent = new Intent(this, AsesorChatIndividualActivity.class);
            intent.putExtra("conversationId", chat.getId());
            intent.putExtra("clienteId", chat.getClienteId());
            intent.putExtra("clienteNombre", chat.getClienteNombre());
            intent.putExtra("clienteAvatar", chat.getClienteAvatarUrl());
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

        // Escuchar conversaciones donde el asesorId coincide
        Query query = db.collection("Conversaciones")
                .whereEqualTo("asesorId", asesorId)
                .orderBy("ultimoMensajeFecha", Query.Direction.DESCENDING); // Asumiendo que tienes un campo fecha

        chatsListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error al cargar chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                chatList.clear();
                chatAdapter.updateList(chatList);
                return;
            }

            // Primero, obtener las conversaciones
            List<Chat> tempChats = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Chat chat = doc.toObject(Chat.class);
                if (chat != null) {
                    chat.setId(doc.getId());
                    tempChats.add(chat);
                }
            }

            // Ahora, para cada chat, obtener datos del cliente y proyecto
            fetchChatDetails(tempChats);
        });
    }

    private void fetchChatDetails(List<Chat> chats) {
        if (chats.isEmpty()) {
            chatList.clear();
            chatAdapter.updateList(chatList);
            return;
        }

        // Para cada chat, obtener el nombre y avatar del cliente
        // También podrías obtener el nombre del proyecto si quisieras
        for (int i = 0; i < chats.size(); i++) {
            Chat chat = chats.get(i);
            String clienteId = chat.getClienteId();

            if (clienteId != null && !clienteId.isEmpty()) {
                db.collection("usuarios").document(clienteId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String nombres = doc.getString("nombres");
                                String apellidos = doc.getString("apellidos");
                                String fullName = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
                                chat.setClienteNombre(fullName.trim());
                                chat.setClienteAvatarUrl(doc.getString("avatarUrl"));
                            } else {
                                chat.setClienteNombre("Cliente");
                            }

                            // Verificar si todos los chats ya tienen datos de cliente
                            checkAllChatsLoaded(chats);
                        })
                        .addOnFailureListener(e -> {
                            chat.setClienteNombre("Cliente");
                            checkAllChatsLoaded(chats);
                        });
            } else {
                chat.setClienteNombre("Cliente");
                checkAllChatsLoaded(chats);
            }
        }
    }

    private void checkAllChatsLoaded(List<Chat> chats) {
        // Si todos los chats tienen nombre de cliente (o fallaron), actualizar la lista
        boolean allLoaded = true;
        for (Chat c : chats) {
            if (c.getClienteNombre() == null) {
                allLoaded = false;
                break;
            }
        }
        if (allLoaded) {
            chatList.clear();
            chatList.addAll(chats);
            chatAdapter.updateList(chatList);
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar por nombre del cliente
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    chatAdapter.updateList(chatList);
                } else {
                    List<Chat> filtered = new ArrayList<>();
                    for (Chat chat : chatList) {
                        if (chat.getClienteNombre() != null &&
                                chat.getClienteNombre().toLowerCase().contains(query)) {
                            filtered.add(chat);
                        }
                    }
                    chatAdapter.updateList(filtered);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
}