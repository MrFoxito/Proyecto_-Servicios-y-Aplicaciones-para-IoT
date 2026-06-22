package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminAdvisorsAdapter;
import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.data.AccountContext;
import com.example.proyecto_iot.data.AccountRepository;
import com.example.proyecto_iot.data.ProjectAssignmentRepository;
import com.example.proyecto_iot.databinding.ActivityAdminAsesoresBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminAsesoresActivity extends BaseAdminActivity {

    private static final String FILTER_SCREEN_KEY = "admin_advisors";

    private ActivityAdminAsesoresBinding binding;
    private AdminAdvisorsAdapter adapter;
    private AdminLocalStorage adminLocalStorage;
    private List<AdminAdvisorItem> allAdvisors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAsesoresBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        allAdvisors = new ArrayList<>();

        setupBottomNavigation();
        setupRecycler();
        setupFilters();
        binding.btnVerSolicitudes.setOnClickListener(v -> openScreen(AdminSolicitudAsesoresActivity.class));
        binding.btnHistorialAsignaciones.setOnClickListener(v -> openScreen(AdminHistorialAsignacionesActivity.class));
        restoreLastFilter();
        loadAdvisors();
    }

    private void setupRecycler() {
        adapter = new AdminAdvisorsAdapter(new AdminAdvisorsAdapter.Listener() {
            @Override
            public void onAdvisorClick(AdminAdvisorItem item) {
                android.content.Intent intent = new android.content.Intent(AdminAsesoresActivity.this, AdminDetalleAsesorActivity.class);
                putAdvisor(intent, item);
                startActivity(intent);
            }

            @Override
            public void onAssignProjectClick(AdminAdvisorItem item) {
                android.content.Intent intent = new android.content.Intent(AdminAsesoresActivity.this, AdminAsignarProyectoAsesorActivity.class);
                putAdvisor(intent, item);
                startActivity(intent);
            }
        });
        binding.rvAsesores.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAsesores.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.filtroTodosAsesores.setOnClickListener(
                v -> applyFilter("todos", binding.filtroTodosAsesores, binding.filtroActivosAsesores, binding.filtroInactivosAsesores)
        );
        binding.filtroActivosAsesores.setOnClickListener(
                v -> applyFilter("activos", binding.filtroActivosAsesores, binding.filtroTodosAsesores, binding.filtroInactivosAsesores)
        );
        binding.filtroInactivosAsesores.setOnClickListener(
                v -> applyFilter("inactivos", binding.filtroInactivosAsesores, binding.filtroTodosAsesores, binding.filtroActivosAsesores)
        );
    }

    private void applyFilter(String filter, TextView selected, TextView... others) {
        adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, filter);
        selectFilter(selected, others);
        renderAdvisors(filter);
    }

    private void loadAdvisors() {
        new AccountRepository().load(AuthSessionManager.getInstance(this).getUid(), new AccountRepository.Callback() {
            @Override
            public void onSuccess(AccountContext account) {
                new ProjectAssignmentRepository().readAdvisors(account.empresaId, new ProjectAssignmentRepository.AdvisorsCallback() {
                    @Override
                    public void onSuccess(List<AdminAdvisorItem> advisors) {
                        allAdvisors.clear();
                        allAdvisors.addAll(advisors);
                        restoreLastFilter();
                    }

                    @Override
                    public void onError(String message) {
                        android.widget.Toast.makeText(AdminAsesoresActivity.this, message, android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                android.widget.Toast.makeText(AdminAsesoresActivity.this, message, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void putAdvisor(android.content.Intent intent, AdminAdvisorItem item) {
        intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_ID, item.getUid());
        intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_NAME, item.getName());
        intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_EMPRESA_ID, item.getEmpresaId());
    }

    private void restoreLastFilter() {
        String filter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, "todos");
        if ("activos".equals(filter)) {
            applyFilter("activos", binding.filtroActivosAsesores, binding.filtroTodosAsesores, binding.filtroInactivosAsesores);
        } else if ("inactivos".equals(filter)) {
            applyFilter("inactivos", binding.filtroInactivosAsesores, binding.filtroTodosAsesores, binding.filtroActivosAsesores);
        } else {
            applyFilter("todos", binding.filtroTodosAsesores, binding.filtroActivosAsesores, binding.filtroInactivosAsesores);
        }
    }

    private void renderAdvisors(String filter) {
        List<AdminAdvisorItem> filtered = new ArrayList<>();
        for (AdminAdvisorItem item : allAdvisors) {
            boolean matches = "todos".equals(filter)
                    || ("activos".equals(filter) && item.isActive())
                    || ("inactivos".equals(filter) && !item.isActive());
            if (matches) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    private void selectFilter(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_pill_active);
        selected.setTextColor(Color.WHITE);

        for (TextView item : others) {
            item.setBackgroundResource(R.drawable.bg_pill_inactive);
            item.setTextColor(Color.parseColor("#8C7A65"));
        }
    }
}
