package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminResumenBinding;

/**
 * Dashboard principal del Administrador.
 * Muestra informacion de la empresa y accesos rapidos.
 */
public class AdminResumenActivity extends BaseAdminActivity {

    private ActivityAdminResumenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResumenBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();

        binding.btnNotificaciones.setOnClickListener(v -> openScreen(AdminNotificacionesActivity.class));
        binding.btnEditProfile.setOnClickListener(v -> openScreen(AdminEditarEmpresaActivity.class));
    }
}
