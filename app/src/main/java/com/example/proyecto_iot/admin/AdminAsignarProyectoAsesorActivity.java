package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminAssignableProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminAssignableProjectItem;
import com.example.proyecto_iot.databinding.ActivityAdminAsignarProyectoAsesorBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vista para asignar un proyecto a un asesor.
 */
public class AdminAsignarProyectoAsesorActivity extends BaseAdminActivity {

    private ActivityAdminAsignarProyectoAsesorBinding binding;
    private AdminAssignableProjectsAdapter adapter;
    private final List<AdminAssignableProjectItem> allProjects = Arrays.asList(
            new AdminAssignableProjectItem("Catalina Sky View", "Av. Javier Prado 450, Lima", "Polanco", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("The Obsidian Estate", "Calle Monte Real 210, Lima", "Santa Fe", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Residencial Nova", "Av. El Sol 980, Lima", "Roma Norte", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Paseo del Golf", "Av. El Golf 145, Lima", "Polanco", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Bosque Real", "Jr. Las Magnolias 318, Lima", "Santa Fe", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Marbella Point", "Malecon Norte 780, Lima", "Polanco", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Distrito Verde", "Av. Del Parque 510, Lima", "Roma Norte", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Solaris Hub", "Calle Central 155, Lima", "Santa Fe", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminAssignableProjectItem("Gran Reserva", "Alameda Real 42, Lima", "Polanco", "EN VENTA", R.drawable.sa_profile_admin)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAsignarProyectoAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupRecycler();

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

        renderProjects("todos");
    }

    private void setupRecycler() {
        adapter = new AdminAssignableProjectsAdapter(new AdminAssignableProjectsAdapter.Listener() {
            @Override
            public void onDetailsClick(AdminAssignableProjectItem item) {
                openScreen(AdminDetalleProyectoActivity.class);
            }

            @Override
            public void onAssignClick(AdminAssignableProjectItem item) {
                Toast.makeText(
                        AdminAsignarProyectoAsesorActivity.this,
                        "Proyecto asignado: " + item.getTitle(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        binding.rvAssignableProjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssignableProjects.setAdapter(adapter);
    }

    private void aplicarFiltro(String filtro, TextView seleccionado, TextView... otros) {
        seleccionarFiltro(seleccionado, otros);
        renderProjects(filtro);
    }

    private void renderProjects(String filtro) {
        List<AdminAssignableProjectItem> filtered = new ArrayList<>();
        for (AdminAssignableProjectItem item : allProjects) {
            boolean matches = "todos".equals(filtro)
                    || ("polanco".equals(filtro) && "Polanco".equals(item.getNeighborhood()))
                    || ("santa".equals(filtro) && "Santa Fe".equals(item.getNeighborhood()))
                    || ("roma".equals(filtro) && "Roma Norte".equals(item.getNeighborhood()));
            if (matches) {
                filtered.add(item);
            }
        }

        adapter.setItems(filtered);
        binding.tvAsignarProyectoVacio.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.rvAssignableProjects.setVisibility(filtered.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
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
