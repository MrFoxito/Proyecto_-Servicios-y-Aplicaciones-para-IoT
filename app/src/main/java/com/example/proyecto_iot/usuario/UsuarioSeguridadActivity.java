package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyecto_iot.R;

public class UsuarioSeguridadActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_seguridad);
        setupUserBottomNav(R.id.navUserProfile);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackSecurity);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View action = findViewById(R.id.btnSecurityPrimaryAction);
        if (action != null) {
            action.setOnClickListener(v ->
                    Toast.makeText(this, R.string.profile_security_action_toast, Toast.LENGTH_SHORT).show());
        }
    }
}
