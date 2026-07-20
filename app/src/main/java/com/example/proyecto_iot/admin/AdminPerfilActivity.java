package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.databinding.ActivityAdminPerfilBinding;
import com.example.proyecto_iot.data.AccountRepository;
import com.example.proyecto_iot.data.AccountContext;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Vista de perfil personal del Administrador.
 */
public class AdminPerfilActivity extends BaseAdminActivity {

    private ActivityAdminPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        loadProfile();

        binding.btnEditarPerfil.setOnClickListener(v -> openScreen(AdminEditarPerfilActivity.class));
        binding.btnCerrarSesion.setOnClickListener(v -> {
            AuthSessionManager.getInstance(this).logout();
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String uid = currentUid();
        new AccountRepository().load(uid, new AccountRepository.Callback() {
            @Override
            public void onSuccess(AccountContext account) {
                binding.tvAdminProfileName.setText(account.nombreCompleto.isEmpty() ? "Administrador" : account.nombreCompleto);
                binding.tvAdminProfileFullName.setText(account.nombreCompleto.isEmpty() ? "Administrador" : account.nombreCompleto);
                binding.tvAdminProfileEmail.setText(account.email);
                binding.tvAdminProfilePhone.setText(account.telefono.isEmpty() ? "Sin teléfono registrado" : account.telefono);
                binding.tvAdminProfileCompany.setText(account.empresaNombre.isEmpty()
                        ? "Nombre de inmobiliaria pendiente" : account.empresaNombre);
                ProjectImageLoader.load(binding.ivAdminProfileAvatar, account.avatarUrl, com.example.proyecto_iot.R.drawable.sa_profile_admin);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminPerfilActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null && !user.getUid().trim().isEmpty()) {
            return user.getUid();
        }
        return AuthSessionManager.getInstance(this).getUid();
    }
}
