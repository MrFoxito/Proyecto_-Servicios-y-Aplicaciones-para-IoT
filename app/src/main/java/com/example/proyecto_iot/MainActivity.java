package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.asesor.AsesorHomeActivity;
import com.example.proyecto_iot.superadmin.SuperadminResumenActivity;
import com.example.proyecto_iot.usuario.UsuarioHomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthSessionManager sessionManager = new AuthSessionManager(this);
        Class<?> destination;

        if (sessionManager.isLoggedIn()) {
            destination = resolveDestinationByRole(sessionManager.getRole());
        } else {
            destination = LoginActivity.class;
        }

        startActivity(new Intent(this, destination));
        finish();
    }

    private Class<?> resolveDestinationByRole(String role) {
        switch (role) {
            case AuthSessionManager.ROLE_USER:
                return UsuarioHomeActivity.class;
            case AuthSessionManager.ROLE_ASESOR:
                return AsesorHomeActivity.class;
            case AuthSessionManager.ROLE_ADMIN:
                return AdminHomeActivity.class;
            case AuthSessionManager.ROLE_SUPERADMIN:
            default:
                return SuperadminResumenActivity.class;
        }
    }
}
