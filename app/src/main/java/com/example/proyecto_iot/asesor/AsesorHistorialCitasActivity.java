package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorHistorialCitasActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_historial_citas);

        setupBackButton();
        findViewById(R.id.cardHistorial1).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.cardHistorial2).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.cardHistorial3).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.cardHistorial4).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.btnExportarHistorial).setOnClickListener(v -> showPendingToast());
    }
}
