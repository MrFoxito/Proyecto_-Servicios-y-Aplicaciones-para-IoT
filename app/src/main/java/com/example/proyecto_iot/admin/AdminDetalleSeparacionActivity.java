package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.data.FirebaseAdminNotificationRepository;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleSeparacionBinding;

/**
 * Vista de detalle de una notificacion de separacion.
 */
public class AdminDetalleSeparacionActivity extends BaseAdminActivity {

    private ActivityAdminDetalleSeparacionBinding binding;
    private final FirebaseAdminNotificationRepository notificationRepository = new FirebaseAdminNotificationRepository();
    private String separationId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding);
        separationId = getIntent() != null ? valueOr(getIntent().getStringExtra("separation_id")) : "";

        setupBackButton();
        binding.btnAprobarSeparacion.setOnClickListener(v -> confirmSeparationAction(
                "Aprobar separacion",
                "Deseas aprobar esta separacion?",
                "Separacion aprobada correctamente",
                true
        ));
        binding.btnRechazarSeparacion.setOnClickListener(v -> confirmSeparationAction(
                "Rechazar / observar separacion",
                "Deseas registrar una observacion sobre esta separacion?",
                "Separacion rechazada correctamente",
                false
        ));
    }

    private void confirmSeparationAction(String title, String message, String successMessage, boolean approved) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        notificationRepository.updateSeparationDecision(separationId, approved, new FirebaseAdminNotificationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AdminDetalleSeparacionActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminDetalleSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        })
                )
                .show();
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
