package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AsesorSeparacionesActivity extends BaseAsesorActivity {

    private RecyclerView rvSeparaciones;
    private SeparacionAdapter separacionAdapter;
    private List<Separacion> allSeparaciones = new ArrayList<>();
    private List<Separacion> displayList = new ArrayList<>();

    private TextView txtConversionCount, txtMontoMensual;
    private TextView filterAll, filterPending;

    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private ListenerRegistration separacionesListener;
    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_separaciones);

        setupBottomNavigation(R.id.navSeparaciones);

        sessionManager = AuthSessionManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        // Inicializar Vistas
        txtConversionCount = findViewById(R.id.txtConversionCount);
        txtMontoMensual = findViewById(R.id.txtMontoMensual);
        filterAll = findViewById(R.id.filterAll);
        filterPending = findViewById(R.id.filterPending);
        rvSeparaciones = findViewById(R.id.rvSeparaciones);

        rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptador con listener
        separacionAdapter = new SeparacionAdapter(new SeparacionAdapter.OnSeparacionActionListener() {
            @Override
            public void onVerDetalles(Separacion separacion) {
                // Abrir detalle de la separación
                Intent intent = new Intent(AsesorSeparacionesActivity.this, AsesorSolicitudSeparacionActivity.class);
                intent.putExtra("separacionId", separacion.getId());
                startActivity(intent);
            }

            @Override
            public void onAccionPrincipal(Separacion separacion) {
                // Determinar acción según estado
                String estado = separacion.getEstado() != null ? separacion.getEstado().toLowerCase() : "";
                if ("pendiente".equals(estado)) {
                    validarPago(separacion);
                } else if ("pagada".equals(estado)) {
                    aprobarSeparacion(separacion);
                } else {
                    // Si es aprobada o rechazada, abrir detalle
                    onVerDetalles(separacion);
                }
            }
        });
        rvSeparaciones.setAdapter(separacionAdapter);

        // Configurar Filtros
        filterAll.setOnClickListener(v -> applyFilter("Todo"));
        filterPending.setOnClickListener(v -> applyFilter("Pendiente"));

        findViewById(R.id.btnRegistrarSeparacion).setOnClickListener(v ->
                openScreen(AsesorRegistrarSeparacionActivity.class)
        );

        // Cargar datos desde Firestore
        loadSeparacionesFromFirestore();
    }

    private void loadSeparacionesFromFirestore() {
        String asesorId = sessionManager.getUid();
        if (asesorId == null || asesorId.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        separacionesListener = separationRepository.listenSeparationsForAdvisor(asesorId, new FirebaseSeparationRepository.SeparationsListener() {
            @Override
            public void onDataChanged(List<Separacion> list) {
                allSeparaciones.clear();
                allSeparaciones.addAll(list);

                // Actualizar vista según filtro activo
                applyFilter("Todo");
                updateStats(allSeparaciones);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorSeparacionesActivity.this, "Error al cargar separaciones: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validarPago(Separacion separacion) {
        separationRepository.updateSeparationStatus(separacion.getId(), "Pagada", new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                Toast.makeText(AsesorSeparacionesActivity.this, "Pago validado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorSeparacionesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aprobarSeparacion(Separacion separacion) {
        separationRepository.updateSeparationStatus(separacion.getId(), "Aprobada", new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                Toast.makeText(AsesorSeparacionesActivity.this, "Separación aprobada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorSeparacionesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStats(List<Separacion> lista) {
        // Calcular monto mensual (solo pagadas del mes actual)
        double montoMensual = 0;
        int enConversion = 0;

        java.util.Calendar now = java.util.Calendar.getInstance();
        int currentMonth = now.get(java.util.Calendar.MONTH);
        int currentYear = now.get(java.util.Calendar.YEAR);

        for (Separacion s : lista) {
            // Contar pendientes para "En Conversión"
            if ("Pendiente".equalsIgnoreCase(s.getEstado())) {
                enConversion++;
            }

            // Sumar montos pagados del mes actual
            if ("Pagada".equalsIgnoreCase(s.getEstado()) || "Aprobada".equalsIgnoreCase(s.getEstado())) {
                // Verificar si es del mes actual (por createdAt)
                if (s.getCreatedAt() > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(s.getCreatedAt());
                    if (cal.get(java.util.Calendar.MONTH) == currentMonth &&
                            cal.get(java.util.Calendar.YEAR) == currentYear) {
                        double monto = parseMonto(s.getMontoTexto());
                        montoMensual += monto;
                    }
                }
            }
        }

        // Actualizar vistas
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        txtMontoMensual.setText("$" + formatter.format(montoMensual));
        txtConversionCount.setText(String.valueOf(enConversion));
    }

    private double parseMonto(String montoTexto) {
        if (montoTexto == null || montoTexto.isEmpty()) return 0;
        try {
            // Limpiar: eliminar todo excepto dígitos, punto, coma
            String clean = montoTexto.replaceAll("[^\\d,.]", "");
            // Reemplazar coma por punto para parsear
            clean = clean.replace(",", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private void applyFilter(String status) {
        displayList.clear();
        if ("Todo".equalsIgnoreCase(status)) {
            displayList.addAll(allSeparaciones);
            updateFilterStyles(filterAll, filterPending);
        } else {
            for (Separacion s : allSeparaciones) {
                if (s.getEstado() != null && s.getEstado().equalsIgnoreCase(status)) {
                    displayList.add(s);
                }
            }
            updateFilterStyles(filterPending, filterAll);
        }
        separacionAdapter.updateList(displayList);
    }

    private void updateFilterStyles(TextView active, TextView inactive) {
        // Estilo Activo (Dark Chip)
        active.setBackground(ContextCompat.getDrawable(this, R.drawable.as_chip_dark));
        active.setTextColor(Color.WHITE);

        // Estilo Inactivo (Light Chip)
        inactive.setBackground(ContextCompat.getDrawable(this, R.drawable.as_chip_light));
        inactive.setTextColor(Color.parseColor("#746D4A"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (separacionesListener != null) {
            separacionesListener.remove();
        }
    }
}