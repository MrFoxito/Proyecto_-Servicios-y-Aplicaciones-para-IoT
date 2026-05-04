package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorChatsActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chats);

        setupBottomNavigation(R.id.navChats);
        findViewById(R.id.rowChatJulian).setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        findViewById(R.id.rowChatElena).setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        findViewById(R.id.rowChatBeatrice).setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        findViewById(R.id.rowChatMarcus).setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        findViewById(R.id.btnChatNuevo).setOnClickListener(v -> showPendingToast());
    }
}
