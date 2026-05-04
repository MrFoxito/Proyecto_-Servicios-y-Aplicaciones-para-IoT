package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SuperadminGestionUsuariosActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_gestion_usuarios);
        setupCommonNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerGestionUsuarios);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SuperadminGestionUsuarioAdapter(buildGestionUsuarios()));
        }
    }

    private List<SuperadminGestionUsuarioItem> buildGestionUsuarios() {
        return Arrays.asList(
                new SuperadminGestionUsuarioItem(
                        "Julian Restrepo",
                        "julian.r@estates.com",
                        "AGENCIA: ELITE RESIDENCES",
                        R.drawable.sa_avatar_01,
                        true
                ),
                new SuperadminGestionUsuarioItem(
                        "Sofia Valderrama",
                        "s.valderrama@global.co",
                        "AGENCIA: GLOBAL PROPERTIES",
                        R.drawable.sa_avatar_02,
                        true
                ),
                new SuperadminGestionUsuarioItem(
                        "Mateo Arango",
                        "m.arango@private.me",
                        "AGENCIA: PRIVATE REAL ESTATE",
                        R.drawable.sa_avatar_03,
                        false
                )
        );
    }
}


