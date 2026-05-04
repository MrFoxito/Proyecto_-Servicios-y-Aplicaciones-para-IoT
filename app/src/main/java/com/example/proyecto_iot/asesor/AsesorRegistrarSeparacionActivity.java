package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorRegistrarSeparacionActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_registrar_separaciones);

        setupBackButton();
        findViewById(R.id.btnConfirmarRegistroSeparacion).setOnClickListener(v -> openScreen(AsesorSolicitudSeparacionActivity.class));
        findViewById(R.id.btnCancelarRegistroSeparacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
