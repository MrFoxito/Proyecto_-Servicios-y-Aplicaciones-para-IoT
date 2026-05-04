package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyecto_iot.R;

public class UsuarioDatosPersonalesActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_datos_personales);
        setupUserBottomNav(R.id.navUserProfile);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPersonalData);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View save = findViewById(R.id.btnSavePersonalData);
        if (save != null) {
            save.setOnClickListener(v ->
                    Toast.makeText(this, R.string.profile_personal_save_toast, Toast.LENGTH_SHORT).show());
        }
    }
}
