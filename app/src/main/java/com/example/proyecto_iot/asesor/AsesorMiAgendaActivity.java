package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class AsesorMiAgendaActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_miagenda);

        setupBottomNavigation(R.id.navMiAgenda);
        findViewById(R.id.cardCitaPrincipal).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.cardCitaSecundaria).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.cardCitaTercera).setOnClickListener(v -> openScreen(AsesorDetalleCitaActivity.class));
        findViewById(R.id.btnVerHistorialCitas).setOnClickListener(v -> openScreen(AsesorHistorialCitasActivity.class));
        findViewById(R.id.btnAgendaAdd).setOnClickListener(v -> showPendingToast());
    }
}
