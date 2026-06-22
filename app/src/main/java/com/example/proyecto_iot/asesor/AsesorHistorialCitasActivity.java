package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsesorHistorialCitasActivity extends BaseAsesorActivity {

    private RecyclerView rvHistorial;
    private AsesorHistorialAdapter adapter;
    private List<Cita> allCitas = new ArrayList<>();
    private List<Cita> filteredCitas = new ArrayList<>();

    // Filtros activos
    private String activePeriodo = "todo";   // "todo", "mes", "semana"
    private String activeEstado = "todas";   // "todas", "cierre", "confirmada", "pendiente"

    // Vistas de stats
    private TextView txtStatTotal, txtStatCerradas, txtStatActivas, txtStatPendientes, txtTasaCierre;
    private ProgressBar progressCierre;
    private TextView txtHistorialLabel, txtConteo;

    // Filtro chips
    private TextView filterPeriodoTodo, filterPeriodoMes, filterPeriodoSemana;
    private TextView filterEstadoTodas, filterEstadoCerradas, filterEstadoConfirmadas, filterEstadoPendientes;

    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private ListenerRegistration citasListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_historial_citas);

        setupBottomNavigation(R.id.navMiAgenda);
        setupBackButton();

        sessionManager = AuthSessionManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupFilters();
        loadCitasFromFirestore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (citasListener != null) {
            citasListener.remove();
        }
    }

    private void bindViews() {
        rvHistorial = findViewById(R.id.rvHistorial);
        txtStatTotal = findViewById(R.id.txtStatTotal);
        txtStatCerradas = findViewById(R.id.txtStatCerradas);
        txtStatActivas = findViewById(R.id.txtStatActivas);
        txtStatPendientes = findViewById(R.id.txtStatPendientes);
        txtTasaCierre = findViewById(R.id.txtTasaCierre);
        progressCierre = findViewById(R.id.progressCierre);
        txtHistorialLabel = findViewById(R.id.txtHistorialLabel);
        txtConteo = findViewById(R.id.txtConteo);

        filterPeriodoTodo = findViewById(R.id.filterPeriodoTodo);
        filterPeriodoMes = findViewById(R.id.filterPeriodoMes);
        filterPeriodoSemana = findViewById(R.id.filterPeriodoSemana);
        filterEstadoTodas = findViewById(R.id.filterEstadoTodas);
        filterEstadoCerradas = findViewById(R.id.filterEstadoCerradas);
        filterEstadoConfirmadas = findViewById(R.id.filterEstadoConfirmadas);
        filterEstadoPendientes = findViewById(R.id.filterEstadoPendientes);

        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AsesorHistorialAdapter(filteredCitas, cita -> {
            // Abrir detalle de la cita
            openScreen(AsesorDetalleCitaActivity.class, cita.getId());
        });
        rvHistorial.setAdapter(adapter);
    }

    private void loadCitasFromFirestore() {
        String asesorId = sessionManager.getUid();
        if (asesorId == null || asesorId.isEmpty()) {
            return;
        }

        citasListener = db.collection("citas")
                .whereEqualTo("asesorId", asesorId)
                .orderBy("fechaISO", Query.Direction.DESCENDING)
                .orderBy("hora", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Manejar error (puedes mostrar un Toast)
                        return;
                    }
                    if (value != null) {
                        allCitas.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Cita cita = doc.toObject(Cita.class);
                            if (cita != null) {
                                cita.setId(doc.getId());
                                allCitas.add(cita);
                            }
                        }
                        applyFilters();
                    }
                });
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
        filteredCitas.clear();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Cita c : allCitas) {
            // --- Filtro periodo ---
            if (!"todo".equals(activePeriodo)) {
                try {
                    LocalDate citaDate = LocalDate.parse(c.getFechaISO(), formatter);

                    if ("mes".equals(activePeriodo)) {
                        if (citaDate.getMonth() != now.getMonth() ||
                                citaDate.getYear() != now.getYear()) {
                            continue;
                        }
                    } else if ("semana".equals(activePeriodo)) {
                        LocalDate weekAgo = now.minusDays(7);
                        if (citaDate.isBefore(weekAgo) || citaDate.isAfter(now)) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    // Si falla el parseo, omitimos la cita
                    continue;
                }
            }

            // --- Filtro estado ---
            String estado = c.getEstado() != null ? c.getEstado() : "";
            switch (activeEstado) {
                case "cierre":
                    if (!c.isHasCierre()) continue;
                    break;
                case "confirmada":
                    if (!estado.equalsIgnoreCase("Confirmada") &&
                            !estado.equalsIgnoreCase("Cerrada") &&
                            !estado.equalsIgnoreCase("En Camino")) {
                        continue;
                    }
                    break;
                case "pendiente":
                    if (!estado.equalsIgnoreCase("Pendiente") &&
                            !estado.equalsIgnoreCase("Reprogramada")) {
                        continue;
                    }
                    break;
                // "todas" no necesita filtro
            }

            filteredCitas.add(c);
        }

        // Ordenar por fecha descendente (más reciente primero) y luego por hora
        Collections.sort(filteredCitas, (a, b) -> {
            int dateCompare = b.getFechaISO().compareTo(a.getFechaISO());
            if (dateCompare != 0) return dateCompare;
            return b.getHora().compareTo(a.getHora());
        });

        adapter.notifyDataSetChanged();
        updateStats();
        updateLabel();
    }

    private void updateStats() {
        // Stats siempre sobre el total global (resumen de desempeño)
        int total = allCitas.size();
        int cerradas = 0, activas = 0, pendientes = 0;
        for (Cita c : allCitas) {
            if (c.isHasCierre()) cerradas++;
            String estado = c.getEstado() != null ? c.getEstado() : "";
            if (estado.equalsIgnoreCase("Confirmada") || estado.equalsIgnoreCase("En Camino")) {
                activas++;
            }
            if (estado.equalsIgnoreCase("Pendiente")) {
                pendientes++;
            }
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
        setChipActive(filterPeriodoTodo, "todo".equals(activePeriodo));
        setChipActive(filterPeriodoMes, "mes".equals(activePeriodo));
        setChipActive(filterPeriodoSemana, "semana".equals(activePeriodo));
    }

    private void updateEstadoChips() {
        setChipActive(filterEstadoTodas, "todas".equals(activeEstado));
        setChipActive(filterEstadoCerradas, "cierre".equals(activeEstado));
        setChipActive(filterEstadoConfirmadas, "confirmada".equals(activeEstado));
        setChipActive(filterEstadoPendientes, "pendiente".equals(activeEstado));
    }

    private void setChipActive(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.as_chip_dark : R.drawable.as_chip_light);
        chip.setTextColor(active ? Color.WHITE : Color.parseColor("#746D4A"));
    }

    private void openScreen(Class<?> cls, String citaId) {
        android.content.Intent intent = new Intent(this, cls);
        intent.putExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID, citaId);
        startActivity(intent);
    }
}