package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.data.FirebaseAdminNotificationRepository;
import com.example.proyecto_iot.databinding.ActivityAdminDetallePagoBinding;

/**
 * Vista de detalle de una notificacion de pago.
 */
public class AdminDetallePagoActivity extends BaseAdminActivity {

    private ActivityAdminDetallePagoBinding binding;
    private final FirebaseAdminNotificationRepository notificationRepository = new FirebaseAdminNotificationRepository();
    private String separationId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetallePagoBinding.inflate(getLayoutInflater());
        setContentView(binding);
        separationId = getIntent() != null ? valueOr(getIntent().getStringExtra("separation_id")) : "";

        setupBackButton();
        binding.btnConfirmar.setOnClickListener(v ->
                notificationRepository.confirmCheckoutPayment(separationId, new FirebaseAdminNotificationRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminDetallePagoActivity.this, "Cobro procesado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminDetallePagoActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }));
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
