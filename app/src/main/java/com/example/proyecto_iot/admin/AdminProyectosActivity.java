package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectsAdapter;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.databinding.ActivityAdminProyectosBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminProyectosActivity extends BaseAdminActivity {

    private ActivityAdminProyectosBinding binding;
    private AdminProjectsAdapter adapter;
    private final List<AdminProjectItem> allProjects = Arrays.asList(
            new AdminProjectItem("Catalina Sky View", "Av. Javier Prado, San Isidro", "USD 1.2M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("San Isidro Lofts", "Calle Los Laureles, San Isidro", "USD 1.8M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Mirador Central", "Av. Arequipa, Lince", "USD 980K", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Bosque Real", "Jr. Los Robles, Surco", "USD 1.4M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Parque Alameda", "Calle Las Flores, Miraflores", "USD 1.6M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Torre Aurora", "Av. Salaverry, Jesus Maria", "USD 1.1M", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Residencial Nova", "Jr. Los Olivos, Pueblo Libre", "USD 1.3M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Costa Azul", "Malecon Cisneros, Miraflores", "USD 2.1M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Altos del Parque", "Av. Benavides, Surco", "USD 1.25M", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Solaris Hub", "Av. Brasil, Magdalena", "USD 1.05M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Gran Reserva", "Calle Monte Real, La Molina", "USD 2.4M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Paseo del Golf", "Av. El Golf, San Isidro", "USD 2.8M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Urban Plaza", "Av. Colonial, Cercado de Lima", "USD 860K", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Marbella Point", "Circuito de Playas, Barranco", "USD 1.95M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Villa Horizonte", "Av. La Encalada, Surco", "USD 1.7M", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Distrito Verde", "Calle Las Camelias, San Borja", "USD 1.15M", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Torre Mistral", "Av. Universitaria, San Miguel", "USD 1.02M", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Portal del Sol", "Jr. Pedro Ruiz, Brena", "USD 920K", "EN PREVENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Capital View", "Av. Petit Thouars, Lince", "USD 1.35M", "EN PLANOS", R.drawable.sa_profile_admin),
            new AdminProjectItem("Lagos del Este", "Av. Los Frutales, Ate", "USD 890K", "EN VENTA", R.drawable.sa_profile_admin),
            new AdminProjectItem("Riverside 360", "Av. Universitaria, Los Olivos", "USD 970K", "EN PREVENTA", R.drawable.sa_profile_admin)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProyectosBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        setupRecycler();
        setupFilters();

        binding.btnCrearProyecto.setOnClickListener(v -> openScreen(AdminCrearProyectoActivity.class));
        renderProjects("todos");
    }

    private void setupRecycler() {
        adapter = new AdminProjectsAdapter(item -> openScreen(AdminDetalleProyectoActivity.class));
        binding.rvProyectos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProyectos.setAdapter(adapter);
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
        selectFilter(selected, others);
        renderProjects(filter);
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
