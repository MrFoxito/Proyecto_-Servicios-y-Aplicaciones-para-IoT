package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.admin.notifications.AdminNotificationHelper;
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
        AdminNotificationHelper.setup(this);

        binding.btnAceptarSolicitud.setOnClickListener(v -> {
            confirmRequestUpdate("aceptada", "Aceptar solicitud", "Deseas aceptar esta solicitud de asesor?");
        });

        binding.btnRechazarSolicitud.setOnClickListener(v -> {
            confirmRequestUpdate("rechazada", "Rechazar solicitud", "Deseas rechazar esta solicitud de asesor?");
        });
    }

    private void confirmRequestUpdate(String status, String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> updateRequest(status))
                .show();
    }

    private void updateRequest(String status) {
        boolean updated = !requestId.isEmpty()
                && new LocalSchemaStorage(this).updateAdvisorRequestStatus(requestId, status);
        Toast.makeText(
                this,
                updated
                        ? ("aceptada".equals(status) ? "Solicitud aceptada correctamente" : "Solicitud rechazada correctamente")
                        : "No se pudo actualizar la solicitud",
                Toast.LENGTH_SHORT
        ).show();
        if (updated) {
            AdminNotificationHelper.showAdvisorRequestDecisionNotification(this, status);
            finish();
        }
    }
}
