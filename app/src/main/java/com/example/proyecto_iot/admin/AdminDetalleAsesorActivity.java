package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminDetalleAsesorBinding;

/**
 * Vista de detalle de un asesor de ventas.
 */
public class AdminDetalleAsesorActivity extends BaseAdminActivity {

    private ActivityAdminDetalleAsesorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        binding.btnAsignarProyecto.setOnClickListener(v -> openScreen(AdminAsignarProyectoAsesorActivity.class));
        binding.btnVerComentarios.setOnClickListener(v -> openScreen(AdminResenasAsesorActivity.class));
    }
}
