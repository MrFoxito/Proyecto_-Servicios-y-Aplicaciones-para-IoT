package com.example.proyecto_iot.asesor;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chats);

        setupBottomNavigation(R.id.navChats);
        
        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        loadChats();

        chatAdapter = new ChatAdapter(chatList, chat -> {
            openScreen(AsesorChatIndividualActivity.class);
        });
        rvChats.setAdapter(chatAdapter);

        findViewById(R.id.btnChatNuevo).setOnClickListener(v -> showPendingToast());
    }

    private void loadChats() {
        chatList = new ArrayList<>(new LocalSchemaStorage(this).getAdvisorChats());
    }
}
