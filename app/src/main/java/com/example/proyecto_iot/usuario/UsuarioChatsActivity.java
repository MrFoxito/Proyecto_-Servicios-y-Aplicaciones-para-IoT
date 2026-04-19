package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

public class UsuarioChatsActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chats);
        setupUserBottomNav(R.id.navUserChats);
        setupChatClicks();
    }

    private void setupChatClicks() {
        int[] chatRows = {R.id.chatRow2, R.id.chatRow3, R.id.chatRow4, R.id.chatRow5};
        int[] names = {
                R.string.chat_row_2_name,
                R.string.chat_row_3_name,
                R.string.chat_row_4_name,
                R.string.chat_row_5_name
        };

        for (int i = 0; i < chatRows.length; i++) {
            final int index = i;
            View row = findViewById(chatRows[i]);
            if (row != null) {
                row.setOnClickListener(v -> {
                    Intent intent = new Intent(this, UsuarioChatDetalleActivity.class);
                    intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONTACT_NAME, getString(names[index]));
                    startActivity(intent);
                });
            }
        }
    }
}
