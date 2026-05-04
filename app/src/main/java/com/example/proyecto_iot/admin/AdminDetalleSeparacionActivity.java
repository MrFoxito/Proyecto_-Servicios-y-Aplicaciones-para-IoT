package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminDetalleSeparacionBinding;

/**
 * Vista de detalle de una notificacion de separacion.
 */
public class AdminDetalleSeparacionActivity extends BaseAdminActivity {

    private ActivityAdminDetalleSeparacionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
    }
}
