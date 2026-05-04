package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminAdvisorsAdapter;
import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.databinding.ActivityAdminAsesoresBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminAsesoresActivity extends BaseAdminActivity {

    private ActivityAdminAsesoresBinding binding;
    private AdminAdvisorsAdapter adapter;
    private final List<AdminAdvisorItem> allAdvisors = Arrays.asList(
            new AdminAdvisorItem("Elena Valdes", "5.0", "evaldes@editorialestate.com", true, R.drawable.sa_profile_asesor_1, Arrays.asList("Skyline Res.", "Marina Bay")),
            new AdminAdvisorItem("Julian Costa", "4.8", "jcosta@editorialestate.com", false, R.drawable.sa_profile_asesor_3, Arrays.asList("Vista Central")),
            new AdminAdvisorItem("Sofia Mendez", "4.9", "smendez@editorialestate.com", true, R.drawable.sa_profile_asesor_2, Arrays.asList("The Lofts", "Green Valley", "Azure Hills")),
            new AdminAdvisorItem("Martin Salazar", "4.7", "msalazar@editorialestate.com", true, R.drawable.sa_profile_asesor_1, Arrays.asList("Bosque Real", "Portal del Sol")),
            new AdminAdvisorItem("Camila Paredes", "4.9", "cparedes@editorialestate.com", true, R.drawable.sa_profile_asesor_2, Arrays.asList("Catalina Sky View")),
            new AdminAdvisorItem("Renzo Huaman", "4.6", "rhuaman@editorialestate.com", false, R.drawable.sa_profile_asesor_3, Arrays.asList("Distrito Verde")),
            new AdminAdvisorItem("Valeria Nunez", "5.0", "vnunez@editorialestate.com", true, R.drawable.sa_profile_asesor_2, Arrays.asList("Costa Azul", "Marbella Point")),
            new AdminAdvisorItem("Diego Rivas", "4.5", "drivas@editorialestate.com", false, R.drawable.sa_profile_asesor_1, Arrays.asList("Urban Plaza")),
            new AdminAdvisorItem("Lucia Ferrer", "4.8", "lferrer@editorialestate.com", true, R.drawable.sa_profile_asesor_3, Arrays.asList("Villa Horizonte", "Paseo del Golf")),
            new AdminAdvisorItem("Andres Poma", "4.4", "apoma@editorialestate.com", true, R.drawable.sa_profile_asesor_1, Arrays.asList("Solaris Hub")),
            new AdminAdvisorItem("Daniela Cardenas", "4.9", "dcardenas@editorialestate.com", true, R.drawable.sa_profile_asesor_2, Arrays.asList("Gran Reserva", "Torre Mistral")),
            new AdminAdvisorItem("Fabio Quispe", "4.3", "fquispe@editorialestate.com", false, R.drawable.sa_profile_asesor_3, Arrays.asList("Lagos del Este")),
            new AdminAdvisorItem("Mariana Tello", "4.7", "mtello@editorialestate.com", true, R.drawable.sa_profile_asesor_2, Arrays.asList("Riverside 360", "Mirador Central"))
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAsesoresBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        setupRecycler();
        setupFilters();
        binding.btnVerSolicitudes.setOnClickListener(v -> openScreen(AdminSolicitudAsesoresActivity.class));
        renderAdvisors("todos");
    }

    private void setupRecycler() {
        adapter = new AdminAdvisorsAdapter(new AdminAdvisorsAdapter.Listener() {
            @Override
            public void onAdvisorClick(AdminAdvisorItem item) {
                openScreen(AdminDetalleAsesorActivity.class);
            }

            @Override
            public void onAssignProjectClick(AdminAdvisorItem item) {
                openScreen(AdminAsignarProyectoAsesorActivity.class);
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
        selectFilter(selected, others);
        renderAdvisors(filter);
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
