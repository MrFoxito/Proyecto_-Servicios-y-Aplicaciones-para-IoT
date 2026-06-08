package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminProyectosBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProyectosActivity extends BaseAdminActivity {

    private static final String FILTER_SCREEN_KEY = "admin_projects";

    private ActivityAdminProyectosBinding binding;
    private AdminProjectsAdapter adapter;
    private AdminLocalStorage adminLocalStorage;
    private List<AdminProjectItem> allProjects = new ArrayList<>();
    private String activeFilter = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProyectosBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);

        setupBottomNavigation();
        setupRecycler();
        setupFilters();

        binding.btnCrearProyecto.setOnClickListener(v -> openScreen(AdminCrearProyectoActivity.class));
        restoreLastFilter();
    }

    private void setupRecycler() {
        adapter = new AdminProjectsAdapter(item -> {
            Intent intent = new Intent(this, AdminDetalleProyectoActivity.class);
            intent.putExtra("project_title", item.getTitle());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        binding.rvProyectos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProyectos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadProjects();
        }
    }

    private void loadProjects() {
        allProjects = new LocalSchemaStorage(this).getAdminProjects();
        renderProjects(activeFilter);
        new FirebaseDataRepository().readAdminProjects(new FirebaseDataRepository.AdminProjectsCallback() {
            @Override
            public void onSuccess(List<AdminProjectItem> projects) {
                if (projects.isEmpty()) {
                    return;
                }
                allProjects = mergeProjects(projects, new LocalSchemaStorage(AdminProyectosActivity.this).getAdminProjects());
                renderProjects(activeFilter);
            }

            @Override
            public void onError(String message) {
                renderProjects(activeFilter);
            }
        });
    }

    private List<AdminProjectItem> mergeProjects(List<AdminProjectItem> primary, List<AdminProjectItem> fallback) {
        List<AdminProjectItem> merged = new ArrayList<>(primary);
        for (AdminProjectItem localItem : fallback) {
            boolean exists = false;
            for (AdminProjectItem item : merged) {
                if (item.getTitle().equalsIgnoreCase(localItem.getTitle())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                merged.add(localItem);
            }
        }
        return merged;
    }

    private void setupFilters() {
        binding.filtroTodosProyectos.setOnClickListener(
                v -> applyFilter("todos", binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroPreventaProyectos, binding.filtroVentaProyectos)
        );
        binding.filtroPlanosProyectos.setOnClickListener(
                v -> applyFilter("planos", binding.filtroPlanosProyectos, binding.filtroTodosProyectos, binding.filtroPreventaProyectos, binding.filtroVentaProyectos)
        );
        binding.filtroPreventaProyectos.setOnClickListener(
                v -> applyFilter("preventa", binding.filtroPreventaProyectos, binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroVentaProyectos)
        );
        binding.filtroVentaProyectos.setOnClickListener(
                v -> applyFilter("venta", binding.filtroVentaProyectos, binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroPreventaProyectos)
        );
    }

    private void applyFilter(String filter, TextView selected, TextView... others) {
        activeFilter = filter;
        adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, filter);
        selectFilter(selected, others);
        renderProjects(filter);
    }

    private void restoreLastFilter() {
        String filter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, "todos");
        if ("planos".equals(filter)) {
            applyFilter("planos", binding.filtroPlanosProyectos, binding.filtroTodosProyectos, binding.filtroPreventaProyectos, binding.filtroVentaProyectos);
        } else if ("preventa".equals(filter)) {
            applyFilter("preventa", binding.filtroPreventaProyectos, binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroVentaProyectos);
        } else if ("venta".equals(filter)) {
            applyFilter("venta", binding.filtroVentaProyectos, binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroPreventaProyectos);
        } else {
            applyFilter("todos", binding.filtroTodosProyectos, binding.filtroPlanosProyectos, binding.filtroPreventaProyectos, binding.filtroVentaProyectos);
        }
    }

    private void renderProjects(String filter) {
        List<AdminProjectItem> filtered = new ArrayList<>();
        for (AdminProjectItem item : allProjects) {
            boolean matches = "todos".equals(filter)
                    || ("planos".equals(filter) && "EN PLANOS".equals(item.getStatus()))
                    || ("preventa".equals(filter) && "EN PREVENTA".equals(item.getStatus()))
                    || ("venta".equals(filter) && "EN VENTA".equals(item.getStatus()));
            if (matches) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
        binding.tvProyectosVacio.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.rvProyectos.setVisibility(filtered.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    private void selectFilter(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_pill_active);
        selected.setTextColor(Color.WHITE);
        selected.setTypeface(null, android.graphics.Typeface.BOLD);

        for (TextView item : others) {
            item.setBackgroundResource(R.drawable.bg_pill_inactive);
            item.setTextColor(Color.parseColor("#8C7A65"));
            item.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}
