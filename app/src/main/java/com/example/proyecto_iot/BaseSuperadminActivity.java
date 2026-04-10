package com.example.proyecto_iot;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseSuperadminActivity extends AppCompatActivity {

    protected void setupCommonNavigation() {
        setupClick(R.id.btnOpenMenu, SuperadminMenuActivity.class);

        setupClick(R.id.drawerDashboard, SuperadminResumenActivity.class);
        setupClick(R.id.drawerUsuarios, SuperadminGestionUsuariosActivity.class);
        setupClick(R.id.drawerAgencias, SuperadminReportesGlobalesActivity.class);
        setupClick(R.id.drawerReportes, SuperadminReportesUsuariosActivity.class);
        setupClick(R.id.drawerLogs, SuperadminLogsActivity.class);

        setupClick(R.id.btnRegisterAdmin, SuperadminRegistrarAdministradorActivity.class);
        setupClick(R.id.btnViewAdvisorRequests, SuperadminAprobacionAsesoresActivity.class);
        setupClick(R.id.btnGoLogs, SuperadminLogsActivity.class);

        setupClick(R.id.navAgencias, SuperadminReportesGlobalesActivity.class);
        setupClick(R.id.navClientes, SuperadminReportesUsuariosActivity.class);

        View logout = findViewById(R.id.drawerLogout);
        if (logout != null) {
            logout.setOnClickListener(v -> Toast.makeText(this, "Sesion cerrada (mock)", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> openScreen(destination));
        }
    }

    protected void openScreen(Class<?> destination) {
        if (!getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
        }
    }
}

