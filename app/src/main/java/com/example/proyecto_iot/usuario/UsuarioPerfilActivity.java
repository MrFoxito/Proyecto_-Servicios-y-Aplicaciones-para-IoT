package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;

public class UsuarioPerfilActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_perfil);
        setupUserBottomNav(R.id.navUserProfile);
        setupLogout();
    }

    private void setupLogout() {
        View logoutButton = findViewById(R.id.btnLogout);
        if (logoutButton == null) {
            return;
        }
        logoutButton.setOnClickListener(v -> {
            new AuthSessionManager(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
