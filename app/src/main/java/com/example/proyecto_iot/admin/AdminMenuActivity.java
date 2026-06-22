package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminMenuBinding;

/**
 * Actividad que representa el menu lateral del Administrador.
 */
public class AdminMenuActivity extends BaseAdminActivity {

    private ActivityAdminMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMenuBinding.inflate(getLayoutInflater());
        setContentView(binding);

        binding.menuOverlay.setOnClickListener(v -> finish());
    }
}
