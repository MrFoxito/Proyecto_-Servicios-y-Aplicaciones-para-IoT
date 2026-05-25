package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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

        setupRoleFilter();
        setupAgencyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerUsers = findViewById(R.id.recyclerGestionUsuarios);
        if (recyclerUsers != null) {
            recyclerUsers.setAdapter(new SuperadminGestionUsuarioAdapter(
                    new LocalSchemaStorage(this).getSuperadminUsers()
            ));
        }
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
}
