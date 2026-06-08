package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.Chat;

import java.util.ArrayList;
import java.util.List;

public class AsesorChatsActivity extends BaseAsesorActivity {

    private RecyclerView rvChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private EditText etSearch;
    private LocalSchemaStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chats);

        storage = new LocalSchemaStorage(this);
        setupBottomNavigation(R.id.navChats);

        etSearch = findViewById(R.id.etSearch);
        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        initAdapter();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats(); // Recargar por si hubo mensajes nuevos que cambien el preview
    }

    private void initAdapter() {
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(this, AsesorChatIndividualActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("userName", chat.getUserName());
            intent.putExtra("userAvatar", chat.getProfileImageRes());
            startActivity(intent);
        });
        rvChats.setAdapter(chatAdapter);
    }

    private void loadChats() {
        List<Chat> data = storage.getAdvisorChats();
        chatAdapter.updateList(data);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chatAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}