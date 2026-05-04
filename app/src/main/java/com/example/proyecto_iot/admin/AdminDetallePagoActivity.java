package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminDetallePagoBinding;

/**
 * Vista de detalle de una notificacion de pago.
 */
public class AdminDetallePagoActivity extends BaseAdminActivity {

    private ActivityAdminDetallePagoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetallePagoBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
    }
}
