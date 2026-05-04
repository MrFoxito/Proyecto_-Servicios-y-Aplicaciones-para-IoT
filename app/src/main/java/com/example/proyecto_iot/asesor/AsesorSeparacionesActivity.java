package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Separacion;
import java.util.ArrayList;
import java.util.List;

public class AsesorSeparacionesActivity extends BaseAsesorActivity {

    private RecyclerView rvSeparaciones;
    private SeparacionAdapter separacionAdapter;
    private List<Separacion> allSeparaciones;
    private List<Separacion> displayList;
    
    private TextView txtConversionCount;
    private TextView filterAll;
    private TextView filterPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_separaciones);

        setupBottomNavigation(R.id.navSeparaciones);

        // Inicializar Vistas
        txtConversionCount = findViewById(R.id.txtConversionCount);
        filterAll = findViewById(R.id.filterAll);
        filterPending = findViewById(R.id.filterPending);
        rvSeparaciones = findViewById(R.id.rvSeparaciones);

        rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));

        loadSeparaciones();
        updateCounter();

        // Inicializar listas (displayList es la que usa el adapter)
        displayList = new ArrayList<>(allSeparaciones);
        
        separacionAdapter = new SeparacionAdapter(displayList, new SeparacionAdapter.OnSeparacionActionListener() {
            @Override
            public void onVerDetalles(Separacion separacion) {
                openScreen(AsesorSolicitudSeparacionActivity.class);
            }

            @Override
            public void onAprobarPago(Separacion separacion) {
                openScreen(AsesorPagoAprobadoActivity.class);
            }
        });
        rvSeparaciones.setAdapter(separacionAdapter);

        // Configurar Filtros
        filterAll.setOnClickListener(v -> applyFilter("Todo"));
        filterPending.setOnClickListener(v -> applyFilter("Pendiente"));

        findViewById(R.id.btnRegistrarSeparacion).setOnClickListener(v -> openScreen(AsesorRegistrarSeparacionActivity.class));
    }

    private void loadSeparaciones() {
        allSeparaciones = new ArrayList<>();
        // Datos de ejemplo basados en el diseño previo
        allSeparaciones.add(new Separacion("ASP-294", "Hugo Pena", "Penthouse Altos del Bosque", "$950,000", "12 Oct 2023", R.drawable.as_property_04, "Pendiente"));
        allSeparaciones.add(new Separacion("ASP-288", "Pilar Ortiz", "Villa Serena Estates", "$1,200,000", "10 Oct 2023", R.drawable.as_property_05, "Pendiente"));
        allSeparaciones.add(new Separacion("ASP-183", "Antonio Ruiz", "Loft Industrial Distrito Norte", "$340,000", "08 Oct 2023", R.drawable.as_property_06, "Pendiente"));
        // Uno extra con estado diferente para demostrar el filtrado
        allSeparaciones.add(new Separacion("ASP-150", "Maria Garcia", "Condominio Pacifico", "$520,000", "05 Oct 2023", R.drawable.as_property_07, "Aprobado"));
    }

    private void updateCounter() {
        int pendingCount = 0;
        for (Separacion s : allSeparaciones) {
            if ("Pendiente".equalsIgnoreCase(s.getStatus())) {
                pendingCount++;
            }
        }
        if (txtConversionCount != null) {
            txtConversionCount.setText(String.valueOf(pendingCount));
        }
    }

    private void applyFilter(String status) {
        displayList.clear();
        if ("Todo".equalsIgnoreCase(status)) {
            displayList.addAll(allSeparaciones);
            updateFilterStyles(filterAll, filterPending);
        } else {
            for (Separacion s : allSeparaciones) {
                if (s.getStatus().equalsIgnoreCase(status)) {
                    displayList.add(s);
                }
            }
            updateFilterStyles(filterPending, filterAll);
        }
        separacionAdapter.notifyDataSetChanged();
    }

    private void updateFilterStyles(TextView active, TextView inactive) {
        // Estilo Activo (Dark Chip)
        active.setBackground(ContextCompat.getDrawable(this, R.drawable.as_chip_dark));
        active.setTextColor(Color.WHITE);
        
        // Estilo Inactivo (Light Chip)
        inactive.setBackground(ContextCompat.getDrawable(this, R.drawable.as_chip_light));
        inactive.setTextColor(Color.parseColor("#746D4A"));
    }
}
