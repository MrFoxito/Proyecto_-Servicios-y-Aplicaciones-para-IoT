package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
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
        chatList = new ArrayList<>();
        // Hardcoded data matching the previous static layout
        chatList.add(new Chat("1", "Julian Mendoza", "El piso del entrepiso se ve...", "14:02 PM", R.drawable.sa_profile_user_1, null, true));
        chatList.add(new Chat("2", "Elena Rossi", "Le envio los planos para el...", "AYER", R.drawable.sa_profile_user_2, null, false));
        chatList.add(new Chat("3", "Beatrice H.", "Image_06ASD486GRE", "MARTES", 0, "BH", false));
        chatList.add(new Chat("4", "Marco Torres", "El departamento me pareció un poco más...", "LUNES", R.drawable.sa_profile_asesor_3, null, false));
    }
}
