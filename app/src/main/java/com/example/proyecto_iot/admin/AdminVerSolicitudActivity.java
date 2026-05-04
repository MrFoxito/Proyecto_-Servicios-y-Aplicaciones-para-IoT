package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.databinding.ActivityAdminVerSolicitudBinding;

public class AdminVerSolicitudActivity extends BaseAdminActivity {

    private ActivityAdminVerSolicitudBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminVerSolicitudBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();

        binding.btnAceptarSolicitud.setOnClickListener(v -> {
            Toast.makeText(this, "Solicitud aceptada", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnRechazarSolicitud.setOnClickListener(v -> {
            Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
