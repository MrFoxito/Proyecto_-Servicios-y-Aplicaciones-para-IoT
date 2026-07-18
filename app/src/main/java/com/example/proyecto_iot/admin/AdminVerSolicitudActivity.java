package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.admin.notifications.AdminNotificationHelper;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminVerSolicitudBinding;

public class AdminVerSolicitudActivity extends BaseAdminActivity {

    private ActivityAdminVerSolicitudBinding binding;
    private String requestId = "";
    private String requestStatus = "PENDIENTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminVerSolicitudBinding.inflate(getLayoutInflater());
        setContentView(binding);
        requestId = getIntent().getStringExtra("request_id");
        if (requestId == null) {
            requestId = "";
        }
        requestStatus = getIntent().getStringExtra("request_status");
        if (requestStatus == null || requestStatus.trim().isEmpty()) {
            requestStatus = "PENDIENTE";
        }

        setupBackButton();
        AdminNotificationHelper.setup(this);
        updateDecisionButtons();

        binding.btnAceptarSolicitud.setOnClickListener(v -> {
            confirmRequestUpdate("aceptada", "Aceptar solicitud", "Deseas aceptar esta solicitud de asesor?");
        });

        binding.btnRechazarSolicitud.setOnClickListener(v -> {
            confirmRequestUpdate("rechazada", "Rechazar solicitud", "Deseas rechazar esta solicitud de asesor?");
        });
    }

    private void confirmRequestUpdate(String status, String title, String message) {
        if (!isPending()) {
            Toast.makeText(this, "Esta solicitud ya fue decidida y no se puede revertir.", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message + " Esta decision no se podra revertir.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> updateRequest(status))
                .show();
    }

    private void updateRequest(String status) {
        new FirebaseDataRepository().decideAdvisorRequest(requestId, status, new FirebaseDataRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                new LocalSchemaStorage(AdminVerSolicitudActivity.this).updateAdvisorRequestStatus(requestId, status);
                finishDecision(status);
            }

            @Override
            public void onError(String message) {
                boolean updated = requestId.startsWith("sol_local_")
                        && new LocalSchemaStorage(AdminVerSolicitudActivity.this)
                        .updateAdvisorRequestStatus(requestId, status);
                if (updated) {
                    finishDecision(status);
                } else {
                    Toast.makeText(AdminVerSolicitudActivity.this, message, Toast.LENGTH_LONG).show();
                    requestStatus = status.toUpperCase(java.util.Locale.ROOT);
                    updateDecisionButtons();
                }
            }
        });
    }

    private void finishDecision(String status) {
        Toast.makeText(
                this,
                "aceptada".equals(status) ? "Solicitud aceptada correctamente" : "Solicitud rechazada correctamente",
                Toast.LENGTH_SHORT
        ).show();
        AdminNotificationHelper.showAdvisorRequestDecisionNotification(this, status);
        finish();
    }

    private void updateDecisionButtons() {
        boolean pending = isPending();
        binding.btnAceptarSolicitud.setEnabled(pending);
        binding.btnRechazarSolicitud.setEnabled(pending);
        binding.btnAceptarSolicitud.setAlpha(pending ? 1f : 0.45f);
        binding.btnRechazarSolicitud.setAlpha(pending ? 1f : 0.45f);
    }

    private boolean isPending() {
        return "PENDIENTE".equalsIgnoreCase(requestStatus);
    }
}
