package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AsesorPerfilActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_perfil);

        setupBottomNavigation(R.id.navPerfil);
        findViewById(R.id.btnEditarPerfilAsesor).setOnClickListener(v -> openScreen(AsesorEditarPerfilActivity.class));
        findViewById(R.id.btnCerrarSesionAsesor).setOnClickListener(v -> {
            new AuthSessionManager(this).logout();
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
