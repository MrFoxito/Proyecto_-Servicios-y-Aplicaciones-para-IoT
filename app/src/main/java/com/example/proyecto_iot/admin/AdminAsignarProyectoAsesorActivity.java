package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.databinding.ActivityAdminAsignarProyectoAsesorBinding;

/**
 * Vista para asignar un proyecto a un asesor.
 */
public class AdminAsignarProyectoAsesorActivity extends BaseAdminActivity {

    private ActivityAdminAsignarProyectoAsesorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAsignarProyectoAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();

        binding.tvVerDetalles1.setOnClickListener(v -> openScreen(AdminDetalleProyectoActivity.class));
        binding.tvVerDetalles2.setOnClickListener(v -> openScreen(AdminDetalleProyectoActivity.class));
        binding.btnAsignarProyecto1.setOnClickListener(v -> Toast.makeText(this, "asignado", Toast.LENGTH_SHORT).show());
        binding.btnAsignarProyecto2.setOnClickListener(v -> Toast.makeText(this, "asignado", Toast.LENGTH_SHORT).show());

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
    }

    private void aplicarFiltro(String filtro, TextView seleccionado, TextView... otros) {
        seleccionarFiltro(seleccionado, otros);

        binding.cardProyectoAsignar1.setVisibility("todos".equals(filtro) || "polanco".equals(filtro) ? View.VISIBLE : View.GONE);
        binding.cardProyectoAsignar2.setVisibility("todos".equals(filtro) || "santa".equals(filtro) ? View.VISIBLE : View.GONE);

        boolean sinResultados = binding.cardProyectoAsignar1.getVisibility() == View.GONE
                && binding.cardProyectoAsignar2.getVisibility() == View.GONE;
        binding.tvAsignarProyectoVacio.setVisibility(sinResultados ? View.VISIBLE : View.GONE);
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
