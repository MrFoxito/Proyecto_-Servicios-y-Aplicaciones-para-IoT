package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminDetalleProyectoBinding;

/**
 * Vista de detalle de un proyecto inmobiliario.
 */
public class AdminDetalleProyectoActivity extends BaseAdminActivity {

    private ActivityAdminDetalleProyectoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        binding.btnEditarProyecto.setOnClickListener(v -> openScreen(AdminEditarProyectoActivity.class));
    }
}
