package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperadminGestionUsuariosActivity extends BaseSuperadminActivity {

    private List<SuperadminGestionUsuarioItem> allUsers = new ArrayList<>();
    private String selectedRole = "Todos";
    private String selectedAgency = "Todas";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_gestion_usuarios);
        setupCommonNavigation();

        recyclerView = findViewById(R.id.recyclerGestionUsuarios);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        loadUsers();
        setupRoleFilter();
        setupAgencyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        allUsers = new LocalSchemaStorage(this).getSuperadminUsers();
        applyFilters();
    }

    private void applyFilters() {
        if (recyclerView == null) return;

        List<SuperadminGestionUsuarioItem> filtered = new ArrayList<>();
        for (SuperadminGestionUsuarioItem user : allUsers) {
            if (!matchesRole(user)) continue;
            if (!matchesAgency(user)) continue;
            filtered.add(user);
        }

        SuperadminGestionUsuarioAdapter adapter = new SuperadminGestionUsuarioAdapter(filtered);
        adapter.setOnUserToggledListener(this::loadUsers);
        recyclerView.setAdapter(adapter);
    }

    private boolean matchesRole(SuperadminGestionUsuarioItem user) {
        if ("Todos".equals(selectedRole)) return true;
        return selectedRole.equalsIgnoreCase(user.getRole());
    }

    private boolean matchesAgency(SuperadminGestionUsuarioItem user) {
        if ("Todas".equals(selectedAgency)) return true;
        String userAgency = user.getAgency().replace("AGENCIA: ", "").trim();
        return selectedAgency.toUpperCase(Locale.ROOT).equals(userAgency);
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
                    selectedRole = item.getTitle().toString();
                    roleValue.setText(selectedRole);
                    applyFilters();
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
                menu.getMenu().add("Todas");
                List<String> agencies = new LocalSchemaStorage(this).getDistinctAgencies();
                for (String agency : agencies) {
                    menu.getMenu().add(agency);
                }
                menu.setOnMenuItemClickListener(item -> {
                    selectedAgency = item.getTitle().toString();
                    agencyValue.setText(selectedAgency);
                    applyFilters();
                    return true;
                });
                menu.show();
            });
        }
    }
}
