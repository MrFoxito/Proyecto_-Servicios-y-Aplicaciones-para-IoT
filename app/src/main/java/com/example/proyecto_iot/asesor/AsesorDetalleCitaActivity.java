package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        setupBackButton();
        findViewById(R.id.btnRegistrarSeparacionDetalle).setOnClickListener(v -> openScreen(AsesorRegistrarSeparacionActivity.class));
        findViewById(R.id.btnReprogramarCita).setOnClickListener(v -> openScreen(AsesorReprogramarCitaActivity.class));
        findViewById(R.id.btnDetalleAccion).setOnClickListener(v -> showPendingToast());
    }
}
