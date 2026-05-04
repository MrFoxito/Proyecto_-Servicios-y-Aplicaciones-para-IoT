package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorChatIndividualActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_chat_individual);

        setupBackButton();
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> showPendingToast());
        findViewById(R.id.btnMoreChat).setOnClickListener(v -> showPendingToast());
    }
}
