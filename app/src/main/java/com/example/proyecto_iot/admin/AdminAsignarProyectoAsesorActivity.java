package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminAssignableProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminAssignmentRecord;
import com.example.proyecto_iot.admin.model.AdminAssignableProjectItem;
import com.example.proyecto_iot.admin.notifications.AdminNotificationHelper;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.databinding.ActivityAdminAsignarProyectoAsesorBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista para asignar un proyecto a un asesor.
 */
public class AdminAsignarProyectoAsesorActivity extends BaseAdminActivity {

    private static final String ADVISOR_NAME = "Elena Valdes";
    private static final String FILTER_SCREEN_KEY = "admin_assign_project";

    private ActivityAdminAsignarProyectoAsesorBinding binding;
    private AdminAssignableProjectsAdapter adapter;
    private AdminLocalStorage adminLocalStorage;
    private List<AdminAssignableProjectItem> allProjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAsignarProyectoAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        allProjects = new ArrayList<>();
        AdminNotificationHelper.setup(this);

        setupBackButton();
        setupRecycler();
        loadProjects();

        binding.filtroTodosAsignar.setOnClickListener(
                v -> aplicarFiltro("todos", binding.filtroTodosAsignar, binding.filtroPolancoAsignar, binding.filtroSantaFeAsignar, binding.filtroRomaAsignar)
        );
        binding.filtroPolancoAsignar.setOnClickListener(
                v -> aplicarFiltro("polanco", binding.filtroPolancoAsignar, binding.filtroTodosAsignar, binding.filtroSantaFeAsignar, binding.filtroRomaAsignar)
        );
        binding.filtroSantaFeAsignar.setOnClickListener(
                v -> aplicarFiltro("santa", binding.filtroSantaFeAsignar, binding.filtroTodosAsignar, binding.filtroPolancoAsignar, binding.filtroRomaAsignar)
        );
        binding.filtroRomaAsignar.setOnClickListener(
                v -> aplicarFiltro("roma", binding.filtroRomaAsignar, binding.filtroTodosAsignar, binding.filtroPolancoAsignar, binding.filtroSantaFeAsignar)
        );

        restoreLastFilter();
        binding.cardHistorialAsignaciones.setVisibility(View.GONE);
    }

    private void setupRecycler() {
        adapter = new AdminAssignableProjectsAdapter(new AdminAssignableProjectsAdapter.Listener() {
            @Override
            public void onDetailsClick(AdminAssignableProjectItem item) {
                android.content.Intent intent = new android.content.Intent(
                        AdminAsignarProyectoAsesorActivity.this,
                        AdminDetalleProyectoActivity.class
                );
                intent.putExtra("project_id", item.getProjectId());
                intent.putExtra("project_title", item.getTitle());
                startActivity(intent);
            }

            @Override
            public void onAssignClick(AdminAssignableProjectItem item) {
                confirmProjectAssignment(item);
            }
        });
        binding.rvAssignableProjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssignableProjects.setAdapter(adapter);
    }

    private void loadProjects() {
        new FirebaseDataRepository().readAdminProjects(new FirebaseDataRepository.AdminProjectsCallback() {
            @Override
            public void onSuccess(List<com.example.proyecto_iot.admin.model.AdminProjectItem> projects) {
                allProjects.clear();
                for (com.example.proyecto_iot.admin.model.AdminProjectItem project : projects) {
                    allProjects.add(new AdminAssignableProjectItem(
                            project.getProjectId(),
                            project.getTitle(),
                            project.getLocation(),
                            project.getLocation(),
                            project.getStatus(),
                            project.getImageRes(),
                            project.getImageUrl()
                    ));
                }
                renderProjects("todos");
            }

            @Override
            public void onError(String message) {
                allProjects.clear();
                renderProjects("todos");
                Toast.makeText(AdminAsignarProyectoAsesorActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmProjectAssignment(AdminAssignableProjectItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Asignar proyecto")
                .setMessage("Deseas asignar " + item.getTitle() + " a " + ADVISOR_NAME + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Asignar", (dialog, which) -> {
                    AdminAssignmentRecord record = adminLocalStorage.saveProjectAssignment(item, ADVISOR_NAME);
                    AdminNotificationHelper.showAssignmentNotification(AdminAsignarProyectoAsesorActivity.this, record);
                    binding.cardHistorialAsignaciones.setVisibility(View.GONE);
                    Toast.makeText(
                            AdminAsignarProyectoAsesorActivity.this,
                            "Proyecto asignado correctamente",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    private void aplicarFiltro(String filtro, TextView seleccionado, TextView... otros) {
        adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, filtro);
        seleccionarFiltro(seleccionado, otros);
        renderProjects(filtro);
    }

    private void restoreLastFilter() {
        aplicarFiltro("todos", binding.filtroTodosAsignar, binding.filtroPolancoAsignar,
                binding.filtroSantaFeAsignar, binding.filtroRomaAsignar);
    }

    private void renderProjects(String filtro) {
        List<AdminAssignableProjectItem> filtered = new ArrayList<>();
        for (AdminAssignableProjectItem item : allProjects) {
            boolean matches = "todos".equals(filtro)
                    || item.getNeighborhood().toLowerCase(java.util.Locale.ROOT).contains(filtro);
            if (matches) {
                filtered.add(item);
            }
        }

        adapter.setItems(filtered);
        binding.tvAsignarProyectoVacio.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.rvAssignableProjects.setVisibility(filtered.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    private void renderAssignmentHistory() {
        binding.cardHistorialAsignaciones.setVisibility(View.GONE);
    }

    private void seleccionarFiltro(TextView seleccionado, TextView... otros) {
        seleccionado.setBackgroundResource(R.drawable.bg_pill_active);
        seleccionado.setTextColor(Color.WHITE);

        for (TextView item : otros) {
            item.setBackgroundResource(R.drawable.bg_pill_inactive);
            item.setTextColor(Color.parseColor("#8C7A65"));
        }
    }
}
