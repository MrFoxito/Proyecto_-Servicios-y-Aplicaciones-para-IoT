package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminAssignedProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminAssignedProjectItem;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleAsesorBinding;

import java.util.Arrays;

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
        binding.btnAsignarProyecto.setOnClickListener(v -> openScreen(AdminAsignarProyectoAsesorActivity.class));
        binding.btnVerComentarios.setOnClickListener(v -> openScreen(AdminResenasAsesorActivity.class));
    }

    private void setupAssignedProjects() {
        assignedProjectsAdapter = new AdminAssignedProjectsAdapter(item -> openScreen(AdminDetalleProyectoActivity.class));
        binding.rvAssignedProjects.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvAssignedProjects.setAdapter(assignedProjectsAdapter);
        assignedProjectsAdapter.setItems(Arrays.asList(
                new AdminAssignedProjectItem("Catalina Sky View", "Polanco, CDMX", "ACTIVO", R.drawable.sa_profile_admin),
                new AdminAssignedProjectItem("San Isidro Lofts", "Roma Sur, CDMX", "EN CURSO", R.drawable.sa_profile_admin),
                new AdminAssignedProjectItem("Bosque Real", "Santa Fe, CDMX", "ACTIVO", R.drawable.sa_profile_admin),
                new AdminAssignedProjectItem("Distrito Verde", "Roma Norte, CDMX", "EN CURSO", R.drawable.sa_profile_admin),
                new AdminAssignedProjectItem("Marbella Point", "Polanco, CDMX", "ACTIVO", R.drawable.sa_profile_admin),
                new AdminAssignedProjectItem("Solaris Hub", "Santa Fe, CDMX", "EN CURSO", R.drawable.sa_profile_admin)
        ));
    }
}
