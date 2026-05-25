package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
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
                Intent intent = new Intent(AsesorSeparacionesActivity.this, AsesorSolicitudSeparacionActivity.class);
                intent.putExtra("extra_separacion_id", separacion.getId());
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            @Override
            public void onAprobarPago(Separacion separacion) {
                new LocalSchemaStorage(AsesorSeparacionesActivity.this).updateSeparacionStatus(separacion.getId(), "Aprobado");
                android.widget.Toast.makeText(AsesorSeparacionesActivity.this, "Pago aprobado correctamente", android.widget.Toast.LENGTH_SHORT).show();
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
        allSeparaciones = new ArrayList<>(new LocalSchemaStorage(this).getAdvisorSeparaciones());
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
