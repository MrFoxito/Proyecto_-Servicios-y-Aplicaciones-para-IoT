package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminResenasAsesorBinding;

/**
 * Vista de resenas y comentarios de clientes sobre un asesor.
 */
public class AdminResenasAsesorActivity extends BaseAdminActivity {

    private ActivityAdminResenasAsesorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResenasAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
    }
}
