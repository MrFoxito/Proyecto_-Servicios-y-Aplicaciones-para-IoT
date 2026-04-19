package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.os.Bundle;

public class SuperadminReportesUsuariosActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_reportes_usuarios);
        setupCommonNavigation();
    }
}


