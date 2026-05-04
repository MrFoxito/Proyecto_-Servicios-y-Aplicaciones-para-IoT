package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorSolicitudSeparacionActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_solicitud_separacion);

        setupBackButton();
        findViewById(R.id.btnAprobarPagoSolicitud).setOnClickListener(v -> openScreen(AsesorPagoAprobadoActivity.class));
        findViewById(R.id.btnRechazarSolicitud).setOnClickListener(v -> showPendingToast());
    }
}
