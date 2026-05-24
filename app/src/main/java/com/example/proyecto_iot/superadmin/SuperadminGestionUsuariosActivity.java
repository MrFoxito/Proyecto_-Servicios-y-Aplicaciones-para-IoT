package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SuperadminGestionUsuariosActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_gestion_usuarios);
        setupCommonNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerGestionUsuarios);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SuperadminGestionUsuarioAdapter(
                    new LocalSchemaStorage(this).getSuperadminUsers()
            ));
        }
    }
}


