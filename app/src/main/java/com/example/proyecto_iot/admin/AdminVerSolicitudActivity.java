package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminVerSolicitudBinding;

public class AdminVerSolicitudActivity extends BaseAdminActivity {

    private ActivityAdminVerSolicitudBinding binding;
    private String requestId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminVerSolicitudBinding.inflate(getLayoutInflater());
        setContentView(binding);
        requestId = getIntent().getStringExtra("request_id");
        if (requestId == null) {
            requestId = "";
        }

        setupBackButton();

        binding.btnAceptarSolicitud.setOnClickListener(v -> {
            updateRequest("aceptada", "Solicitud aceptada y guardada localmente");
        });

        binding.btnRechazarSolicitud.setOnClickListener(v -> {
            updateRequest("rechazada", "Solicitud rechazada y guardada localmente");
        });
    }

    private void updateRequest(String status, String message) {
        boolean updated = !requestId.isEmpty()
                && new LocalSchemaStorage(this).updateAdvisorRequestStatus(requestId, status);
        Toast.makeText(this, updated ? message : "No se pudo actualizar la solicitud", Toast.LENGTH_SHORT).show();
        if (updated) {
            finish();
        }
    }
}
