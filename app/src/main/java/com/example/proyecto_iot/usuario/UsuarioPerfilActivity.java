package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;

public class UsuarioPerfilActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_perfil);
        setupUserBottomNav(R.id.navUserProfile);
        loadUserName();
        setupSectionNavigation();
        setupLogout();
    }

    private void loadUserName() {
        TextView tvName = findViewById(R.id.tvProfileName);
        if (tvName == null) return;
        AuthSessionManager session = AuthSessionManager.getInstance(this);
        String name = session.getUserName();
        if (name != null && !name.trim().isEmpty()) {
            tvName.setText(name);
        }
    }

    private void setupSectionNavigation() {
        setupSection(R.id.tileProfilePersonal, UsuarioDatosPersonalesActivity.class);
        setupSection(R.id.tileProfilePayment, UsuarioMetodosPagoActivity.class);
        setupSection(R.id.tileProfilePreferences, UsuarioPreferenciasActivity.class);
        setupSection(R.id.tileProfileSecurity, UsuarioSeguridadActivity.class);
    }

    private void setupSection(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> startActivity(new Intent(this, destination)));
    }

    private void setupLogout() {
        View logoutButton = findViewById(R.id.btnLogout);
        if (logoutButton == null) {
            return;
        }
        logoutButton.setOnClickListener(v -> {
            AuthSessionManager.getInstance(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
