package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AsesorHistorialCitasActivity extends BaseAsesorActivity {

    private RecyclerView rvHistorial;
    private AsesorHistorialAdapter adapter;
    private List<Cita> allCitas;
    private List<Cita> filteredCitas;

    // Filtros activos
    private String activePeriodo = "todo";   // "todo", "mes", "semana"
    private String activeEstado  = "todas";  // "todas", "cierre", "confirmada", "pendiente"

    // Vistas de stats
    private TextView txtStatTotal, txtStatCerradas, txtStatActivas, txtStatPendientes, txtTasaCierre;
    private ProgressBar progressCierre;

    private TextView txtHistorialLabel, txtConteo;

    // Filtro chips
    private TextView filterPeriodoTodo, filterPeriodoMes, filterPeriodoSemana;
    private TextView filterEstadoTodas, filterEstadoCerradas, filterEstadoConfirmadas, filterEstadoPendientes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_historial_citas);

        setupBottomNavigation(R.id.navMiAgenda);

        setupBackButton();
        bindViews();
        loadHardcodedCitas();
        setupFilters();
        applyFilters();

    }

    private void bindViews() {
        rvHistorial            = findViewById(R.id.rvHistorial);
        txtStatTotal           = findViewById(R.id.txtStatTotal);
        txtStatCerradas        = findViewById(R.id.txtStatCerradas);
        txtStatActivas         = findViewById(R.id.txtStatActivas);
        txtStatPendientes      = findViewById(R.id.txtStatPendientes);
        txtTasaCierre          = findViewById(R.id.txtTasaCierre);
        progressCierre         = findViewById(R.id.progressCierre);
        txtHistorialLabel      = findViewById(R.id.txtHistorialLabel);
        txtConteo              = findViewById(R.id.txtConteo);

        filterPeriodoTodo      = findViewById(R.id.filterPeriodoTodo);
        filterPeriodoMes       = findViewById(R.id.filterPeriodoMes);
        filterPeriodoSemana    = findViewById(R.id.filterPeriodoSemana);
        filterEstadoTodas      = findViewById(R.id.filterEstadoTodas);
        filterEstadoCerradas   = findViewById(R.id.filterEstadoCerradas);
        filterEstadoConfirmadas = findViewById(R.id.filterEstadoConfirmadas);
        filterEstadoPendientes = findViewById(R.id.filterEstadoPendientes);

        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        filteredCitas = new ArrayList<>();
        adapter = new AsesorHistorialAdapter(filteredCitas, cita -> openScreen(AsesorDetalleCitaActivity.class));
        rvHistorial.setAdapter(adapter);
    }

    private void loadHardcodedCitas() {
        allCitas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Fechas relativas para que los filtros funcionen en demo
        String today      = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -2); String hace2dias = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -3); String hace5dias = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -9); String hace14dias = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -16); String hace30dias = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -20); String hace50dias = sdf.format(cal.getTime());

        allCitas.add(new Cita("1", "Alicia Velarde",      "Penthouse Altos del Bosque", "08:30 AM", today,      "Confirmada",   "Inmobiliaria Horizonte", false));
        allCitas.add(new Cita("2", "Inversiones Valero",  "Penthouse El Cielo",          "11:15 AM", today,      "En Camino",    "Proyecto Cumbre",        false));
        allCitas.add(new Cita("3", "Julian Ortega",       "Residencia Brisa",            "02:45 PM", hace2dias,  "Pendiente",    "Inmobiliaria Horizonte", false));
        allCitas.add(new Cita("4", "Maria Garcia",        "Condominio Pacifico",         "10:00 AM", hace2dias,  "Cerrada",      "Proyecto Cumbre",        true));
        allCitas.add(new Cita("5", "Roberto Carlos",      "Villa del Mar",               "04:30 PM", hace5dias,  "Cerrada",      "Villa Amanece",          true));
        allCitas.add(new Cita("6", "Carss M. Soria",      "Villa Amanece",               "09:30 AM", hace5dias,  "Pasada",       "Villa Amanece",          false));
        allCitas.add(new Cita("7", "Familia Vega",        "Mirador del Sol",             "08:00 AM", hace14dias, "Reprogramada", "Proyecto Cumbre",        false));
        allCitas.add(new Cita("8", "D. Julian Ortega",    "Residencia Brisa",            "04:45 PM", hace14dias, "No Conectada", "Inmobiliaria Horizonte", false));
        allCitas.add(new Cita("9", "Sofia Restrepo",      "Penthouse Vista Mar",         "03:00 PM", hace30dias, "Cerrada",      "Villa Amanece",          true));
        allCitas.add(new Cita("10","Carlos Mendez",       "Condominio Sol Naciente",     "11:00 AM", hace50dias, "Pasada",       "Proyecto Cumbre",        false));
    }

    private void setupFilters() {
        filterPeriodoTodo.setOnClickListener(v -> {
            activePeriodo = "todo";
            updatePeriodoChips();
            applyFilters();
        });
        filterPeriodoMes.setOnClickListener(v -> {
            activePeriodo = "mes";
            updatePeriodoChips();
            applyFilters();
        });
        filterPeriodoSemana.setOnClickListener(v -> {
            activePeriodo = "semana";
            updatePeriodoChips();
            applyFilters();
        });

        filterEstadoTodas.setOnClickListener(v -> {
            activeEstado = "todas";
            updateEstadoChips();
            applyFilters();
        });
        filterEstadoCerradas.setOnClickListener(v -> {
            activeEstado = "cierre";
            updateEstadoChips();
            applyFilters();
        });
        filterEstadoConfirmadas.setOnClickListener(v -> {
            activeEstado = "confirmada";
            updateEstadoChips();
            applyFilters();
        });
        filterEstadoPendientes.setOnClickListener(v -> {
            activeEstado = "pendiente";
            updateEstadoChips();
            applyFilters();
        });
    }

    private void applyFilters() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar now = Calendar.getInstance();

        filteredCitas.clear();
        for (Cita c : allCitas) {
            // --- Filtro periodo ---
            if (!"todo".equals(activePeriodo)) {
                try {
                    java.util.Date citaDate = sdf.parse(c.getDate());
                    Calendar citaCal = Calendar.getInstance();
                    citaCal.setTime(citaDate);

                    if ("mes".equals(activePeriodo)) {
                        if (citaCal.get(Calendar.MONTH) != now.get(Calendar.MONTH) ||
                            citaCal.get(Calendar.YEAR)  != now.get(Calendar.YEAR)) continue;
                    } else if ("semana".equals(activePeriodo)) {
                        Calendar weekAgo = (Calendar) now.clone();
                        weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                        if (citaDate.before(weekAgo.getTime()) || citaDate.after(now.getTime())) continue;
                    }
                } catch (Exception e) { /* ignorar */ }
            }

            // --- Filtro estado ---
            switch (activeEstado) {
                case "cierre":
                    if (!c.hasCierre()) continue;
                    break;
                case "confirmada":
                    if (!"Confirmada".equalsIgnoreCase(c.getStatus()) &&
                        !"Cerrada".equalsIgnoreCase(c.getStatus()) &&
                        !"En Camino".equalsIgnoreCase(c.getStatus())) continue;
                    break;
                case "pendiente":
                    if (!"Pendiente".equalsIgnoreCase(c.getStatus()) &&
                        !"Reprogramada".equalsIgnoreCase(c.getStatus())) continue;
                    break;
            }

            filteredCitas.add(c);
        }

        adapter.notifyDataSetChanged();
        updateStats();
        updateLabel();
    }

    private void updateStats() {
        // Stats siempre sobre el total global (resumen de desempeño)
        int total = allCitas.size();
        int cerradas = 0, activas = 0, pendientes = 0;
        for (Cita c : allCitas) {
            if (c.hasCierre()) cerradas++;
            if ("Confirmada".equalsIgnoreCase(c.getStatus()) || "En Camino".equalsIgnoreCase(c.getStatus())) activas++;
            if ("Pendiente".equalsIgnoreCase(c.getStatus())) pendientes++;
        }
        int tasa = (total > 0) ? (cerradas * 100 / total) : 0;

        txtStatTotal.setText(String.valueOf(total));
        txtStatTotal.setTextColor(Color.parseColor("#D6C65E"));
        txtStatCerradas.setText(String.valueOf(cerradas));
        txtStatCerradas.setTextColor(Color.parseColor("#D6C65E"));
        txtStatActivas.setText(String.valueOf(activas));
        txtStatActivas.setTextColor(Color.parseColor("#D6C65E"));
        txtStatPendientes.setText(String.valueOf(pendientes));
        txtStatPendientes.setTextColor(Color.parseColor("#D6C65E"));
        txtTasaCierre.setText(tasa + "%");
        progressCierre.setProgress(tasa);

    }

    private void updateLabel() {
        String label;
        switch (activeEstado) {
            case "cierre":       label = "CON CIERRE"; break;
            case "confirmada":   label = "CONFIRMADAS / ACTIVAS"; break;
            case "pendiente":    label = "PENDIENTES"; break;
            default:             label = "TODAS LAS CITAS"; break;
        }
        if (!"todo".equals(activePeriodo)) {
            label += " · " + ("mes".equals(activePeriodo) ? "ESTE MES" : "ESTA SEMANA");
        }
        txtHistorialLabel.setText(label);
        txtConteo.setText(filteredCitas.size() + " resultado" + (filteredCitas.size() != 1 ? "s" : ""));
    }

    private void updatePeriodoChips() {
        setChipActive(filterPeriodoTodo,   "todo".equals(activePeriodo));
        setChipActive(filterPeriodoMes,    "mes".equals(activePeriodo));
        setChipActive(filterPeriodoSemana, "semana".equals(activePeriodo));
    }

    private void updateEstadoChips() {
        setChipActive(filterEstadoTodas,       "todas".equals(activeEstado));
        setChipActive(filterEstadoCerradas,     "cierre".equals(activeEstado));
        setChipActive(filterEstadoConfirmadas,  "confirmada".equals(activeEstado));
        setChipActive(filterEstadoPendientes,   "pendiente".equals(activeEstado));
    }

    private void setChipActive(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.as_chip_dark : R.drawable.as_chip_light);
        chip.setTextColor(active ? Color.WHITE : Color.parseColor("#746D4A"));
    }
}
