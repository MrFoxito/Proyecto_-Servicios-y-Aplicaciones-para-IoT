package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminEditarEmpresaBinding;

/**
 * Vista para editar los datos corporativos de la empresa inmobiliaria.
 */
public class AdminEditarEmpresaActivity extends BaseAdminActivity {

    private ActivityAdminEditarEmpresaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarEmpresaBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();

        binding.btnCompletarConfig.setOnClickListener(v -> closeWithAnimation());
        binding.btnCancelar.setOnClickListener(v -> closeWithAnimation());
    }

    private void closeWithAnimation() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
