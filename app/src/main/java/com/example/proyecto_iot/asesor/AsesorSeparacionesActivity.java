package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorSeparacionesActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_separaciones);

        setupBottomNavigation(R.id.navSeparaciones);
        findViewById(R.id.btnRegistrarSeparacion).setOnClickListener(v -> openScreen(AsesorRegistrarSeparacionActivity.class));
        findViewById(R.id.btnVerSolicitud1).setOnClickListener(v -> openScreen(AsesorSolicitudSeparacionActivity.class));
        findViewById(R.id.btnVerSolicitud2).setOnClickListener(v -> openScreen(AsesorSolicitudSeparacionActivity.class));
        findViewById(R.id.btnVerSolicitud3).setOnClickListener(v -> openScreen(AsesorSolicitudSeparacionActivity.class));
        findViewById(R.id.btnAprobarPago1).setOnClickListener(v -> openScreen(AsesorPagoAprobadoActivity.class));
        findViewById(R.id.btnAprobarPago2).setOnClickListener(v -> openScreen(AsesorPagoAprobadoActivity.class));
        findViewById(R.id.btnAprobarPago3).setOnClickListener(v -> openScreen(AsesorPagoAprobadoActivity.class));
    }
}
