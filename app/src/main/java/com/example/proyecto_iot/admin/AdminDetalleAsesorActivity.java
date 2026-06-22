package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.admin.adapter.AdminAssignedProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminAssignedProjectItem;
import com.example.proyecto_iot.data.AccountContext;
import com.example.proyecto_iot.data.AccountRepository;
import com.example.proyecto_iot.data.ProjectAssignmentRepository;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleAsesorBinding;
import com.example.proyecto_iot.entity.Proyecto;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista de detalle de un asesor de ventas.
 */
public class AdminDetalleAsesorActivity extends BaseAdminActivity {

    private ActivityAdminDetalleAsesorBinding binding;
    private AdminAssignedProjectsAdapter assignedProjectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupAssignedProjects();
        loadAdvisor();
        binding.btnAsignarProyecto.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminAsignarProyectoAsesorActivity.class);
            intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_ID,
                    getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_ID));
            intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_NAME,
                    getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_NAME));
            intent.putExtra(AdminAsignarProyectoAsesorActivity.EXTRA_EMPRESA_ID,
                    getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_EMPRESA_ID));
            startActivity(intent);
        });
        binding.btnVerComentarios.setOnClickListener(v -> openScreen(AdminResenasAsesorActivity.class));
    }

    private void setupAssignedProjects() {
        assignedProjectsAdapter = new AdminAssignedProjectsAdapter(item -> openScreen(AdminDetalleProyectoActivity.class));
        binding.rvAssignedProjects.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvAssignedProjects.setAdapter(assignedProjectsAdapter);
        assignedProjectsAdapter.setItems(new ArrayList<>());
    }

    private void loadAdvisor() {
        String advisorId = getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_ID);
        String fallbackName = getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_NAME);
        binding.tvAdvisorName.setText(fallbackName == null ? "Asesor" : fallbackName);
        new AccountRepository().load(advisorId, new AccountRepository.Callback() {
            @Override
            public void onSuccess(AccountContext account) {
                binding.tvAdvisorName.setText(account.nombreCompleto);
                binding.tvAdvisorStatus.setText(account.estado.isEmpty() ? "ACTIVO" : account.estado.toUpperCase());
                binding.tvAdvisorEmail.setText("Correo: " + account.email);
                binding.tvAdvisorPhone.setText("Teléfono: "
                        + (account.telefono.isEmpty() ? "Sin registrar" : account.telefono));
            }

            @Override
            public void onError(String message) {
                android.widget.Toast.makeText(AdminDetalleAsesorActivity.this,
                        message, android.widget.Toast.LENGTH_LONG).show();
            }
        });
        new ProjectAssignmentRepository().readProjectsForAdvisor(
                advisorId,
                new ProjectAssignmentRepository.ProjectsCallback() {
                    @Override
                    public void onSuccess(List<Proyecto> projects) {
                        List<AdminAssignedProjectItem> items = new ArrayList<>();
                        for (Proyecto project : projects) {
                            items.add(new AdminAssignedProjectItem(
                                    project.getNombre(),
                                    project.getDireccion(),
                                    "ACTIVO",
                                    com.example.proyecto_iot.R.drawable.ic_home
                            ));
                        }
                        assignedProjectsAdapter.setItems(items);
                    }

                    @Override
                    public void onError(String message) {
                        android.widget.Toast.makeText(AdminDetalleAsesorActivity.this,
                                message, android.widget.Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
