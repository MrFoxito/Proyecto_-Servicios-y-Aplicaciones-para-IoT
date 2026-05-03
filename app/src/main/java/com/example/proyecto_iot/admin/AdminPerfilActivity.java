package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.databinding.ActivityAdminPerfilBinding;

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
        setupBackButton();

        binding.btnEditarPerfil.setOnClickListener(v -> openScreen(AdminEditarPerfilActivity.class));
        binding.btnCerrarSesion.setOnClickListener(v -> {
            new AuthSessionManager(this).logout();
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
