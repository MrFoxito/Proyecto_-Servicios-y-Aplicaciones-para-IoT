package com.example.proyecto_iot.usuario;

import android.os.Bundle;

import com.example.proyecto_iot.R;

public class UsuarioActividadActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_actividad);
        setupUserBottomNav(R.id.navUserActivity);
    }
}
