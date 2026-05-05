package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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

        setupRoleFilter();
        setupAgencyFilter();
    }

    private void setupRoleFilter() {
        TextView roleValue = findViewById(R.id.textRoleFilterValue);
        View trigger = findViewById(R.id.layoutRoleFilter);
        if (trigger != null && roleValue != null) {
            trigger.setOnClickListener(view -> {
                PopupMenu menu = new PopupMenu(this, view);
                menu.getMenu().add("Todos");
                menu.getMenu().add("Admin");
                menu.getMenu().add("Asesor");
                menu.getMenu().add("Cliente");
                menu.setOnMenuItemClickListener(item -> {
                    roleValue.setText(item.getTitle());
                    Toast.makeText(this, "Rol: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                menu.show();
            });
        }
    }

    private void setupAgencyFilter() {
        TextView agencyValue = findViewById(R.id.textAgencyFilterValue);
        View trigger = findViewById(R.id.layoutAgencyFilter);
        if (trigger != null && agencyValue != null) {
            trigger.setOnClickListener(view -> {
                PopupMenu menu = new PopupMenu(this, view);
                menu.getMenu().add("Elite");
                menu.getMenu().add("Global");
                menu.getMenu().add("Prime");
                menu.setOnMenuItemClickListener(item -> {
                    agencyValue.setText(item.getTitle());
                    Toast.makeText(this, "Agencia: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                menu.show();
            });
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
