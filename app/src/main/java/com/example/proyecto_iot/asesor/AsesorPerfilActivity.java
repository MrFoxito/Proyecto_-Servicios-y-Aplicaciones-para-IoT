package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

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
        findViewById(R.id.btnSolicitarUnionProyecto).setOnClickListener(v -> solicitarUnionProyecto());
        findViewById(R.id.btnCerrarSesionAsesor).setOnClickListener(v -> {
            new AuthSessionManager(this).logout();
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void solicitarUnionProyecto() {
        AuthSessionManager session = new AuthSessionManager(this);
        String advisorName = session.getUserName().isEmpty() ? "Julian Thorpe" : session.getUserName();
        String advisorEmail = session.getUserEmail().isEmpty()
                ? "asesor.local@editorialestate.com"
                : session.getUserEmail();
        boolean created = new LocalSchemaStorage(this).addAdvisorProjectJoinRequest(
                advisorName,
                advisorEmail,
                "Residencias Aura"
        );
        Toast.makeText(
                this,
                created
                        ? "Solicitud enviada al administrador"
                        : "Ya existe una solicitud pendiente para este proyecto",
                Toast.LENGTH_SHORT
        ).show();
    }
}
