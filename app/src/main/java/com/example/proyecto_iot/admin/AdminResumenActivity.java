package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.databinding.ActivityAdminResumenBinding;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.data.AccountContext;
import com.example.proyecto_iot.data.AccountRepository;

/**
 * Dashboard principal del Administrador.
 * Muestra informacion de la empresa y accesos rapidos.
 */
public class AdminResumenActivity extends BaseAdminActivity {

    private ActivityAdminResumenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResumenBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        loadCompany();

        binding.btnNotificaciones.setOnClickListener(v -> openScreen(AdminNotificacionesActivity.class));
        binding.btnEditProfile.setOnClickListener(v -> openScreen(AdminEditarEmpresaActivity.class));
    }

    private void loadCompany() {
        new AccountRepository().load(AuthSessionManager.getInstance(this).getUid(), new AccountRepository.Callback() {
            @Override
            public void onSuccess(AccountContext account) {
                binding.tvAdminCompanyEmail.setText(account.email.isEmpty() ? "Correo pendiente" : account.email);
                binding.tvAdminCompanyPhone.setText(account.telefono.isEmpty() ? "Teléfono pendiente" : account.telefono);
                binding.tvAdminCompanyAddress.setText(account.empresaNombre.isEmpty()
                        ? "Completa el perfil de tu empresa"
                        : account.empresaNombre);
            }

            @Override
            public void onError(String message) {
                binding.tvAdminCompanyAddress.setText("No se pudo cargar la empresa");
            }
        });
    }
}
