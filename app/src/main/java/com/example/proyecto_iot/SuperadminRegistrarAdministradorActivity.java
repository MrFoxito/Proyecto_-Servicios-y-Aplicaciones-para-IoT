package com.example.proyecto_iot;

import android.os.Bundle;
import android.widget.Toast;

public class SuperadminRegistrarAdministradorActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_registrar_administrador);
        setupCommonNavigation();

        if (findViewById(R.id.btnSaveAdmin) != null) {
            findViewById(R.id.btnSaveAdmin).setOnClickListener(v ->
                    Toast.makeText(this, "Administrador guardado (mock)", Toast.LENGTH_SHORT).show());
        }
    }
}

