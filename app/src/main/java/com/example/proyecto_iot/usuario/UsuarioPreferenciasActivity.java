package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyecto_iot.R;

public class UsuarioPreferenciasActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_preferencias);
        setupUserBottomNav(R.id.navUserProfile);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPreferences);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View save = findViewById(R.id.btnSavePreferences);
        if (save != null) {
            save.setOnClickListener(v ->
                    Toast.makeText(this, R.string.profile_preferences_save_toast, Toast.LENGTH_SHORT).show());
        }
    }
}
