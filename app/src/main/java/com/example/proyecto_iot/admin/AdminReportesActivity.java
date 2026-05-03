package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminReportesBinding;

/**
 * Dashboard de reportes y metricas del Administrador.
 * Vista de solo lectura sin destinos internos.
 */
public class AdminReportesActivity extends BaseAdminActivity {

    private ActivityAdminReportesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReportesBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        setupBackButton();
    }
}
