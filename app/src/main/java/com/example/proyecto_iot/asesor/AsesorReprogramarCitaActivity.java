package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);

        setupBackButton();
        findViewById(R.id.btnConfirmarReprogramacion).setOnClickListener(v -> showPendingToast());
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
