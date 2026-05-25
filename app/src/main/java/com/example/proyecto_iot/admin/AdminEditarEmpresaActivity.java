package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminEditarEmpresaBinding;

/**
 * Vista para editar los datos corporativos de la empresa inmobiliaria.
 */
public class AdminEditarEmpresaActivity extends BaseAdminActivity {

    private ActivityAdminEditarEmpresaBinding binding;
    private AdminLocalStorage adminLocalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarEmpresaBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);

        setupBackButton();
        restoreCompanyProfile();

        binding.btnCompletarConfig.setOnClickListener(v -> saveCompanyProfile());
        binding.btnCancelar.setOnClickListener(v -> closeWithAnimation());
    }

    private void restoreCompanyProfile() {
        String[] profile = adminLocalStorage.getCompanyProfile();
        binding.etDireccion.setText(profile[0]);
        binding.etCorreo.setText(profile[1]);
        binding.etTelefono.setText(profile[2]);
    }

    private void saveCompanyProfile() {
        String address = binding.etDireccion.getText().toString().trim();
        String email = binding.etCorreo.getText().toString().trim();
        String phone = binding.etTelefono.getText().toString().trim();
        adminLocalStorage.saveCompanyProfile(address, email, phone);
        new LocalSchemaStorage(this).addAdminNotification(
                "company_profile_" + System.currentTimeMillis(),
                "action",
                "Empresa actualizada",
                "Hace un momento",
                "Perfil corporativo",
                "Datos guardados en storage local",
                "REVISAR"
        );
        Toast.makeText(this, "Datos de empresa guardados localmente", Toast.LENGTH_SHORT).show();
        closeWithAnimation();
    }

    private void closeWithAnimation() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
