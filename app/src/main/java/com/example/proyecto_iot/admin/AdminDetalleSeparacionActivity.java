package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

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
        binding.btnAprobarSeparacion.setOnClickListener(v -> confirmSeparationAction(
                "Aprobar separacion",
                "Deseas aprobar esta separacion?",
                "Separacion aprobada correctamente"
        ));
        binding.btnRechazarSeparacion.setOnClickListener(v -> confirmSeparationAction(
                "Rechazar / observar separacion",
                "Deseas registrar una observacion sobre esta separacion?",
                "Separacion observada correctamente"
        ));
    }

    private void confirmSeparationAction(String title, String message, String successMessage) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                )
                .show();
    }
}
