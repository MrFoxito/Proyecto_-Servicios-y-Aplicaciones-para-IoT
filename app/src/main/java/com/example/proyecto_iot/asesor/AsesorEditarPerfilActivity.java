package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorEditarPerfilActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_editar_perfil);

        setupBackButton();
        findViewById(R.id.btnGuardarPerfilAsesor).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        findViewById(R.id.btnCancelarEditarPerfilAsesor).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
