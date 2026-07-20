package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class AsesorSeparacionesActivity extends BaseAsesorActivity {

    private RecyclerView rvSeparaciones;
    private SeparacionAdapter separacionAdapter;
    private List<Separacion> allSeparaciones = new ArrayList<>();
    private List<Separacion> displayList = new ArrayList<>();

    private TextView txtConversionCount, txtMontoMensual, txtListState, btnRetry;
    private TextView filterAll, filterPending;
    private ProgressBar progressSeparaciones;

    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private ListenerRegistration separacionesListener;
    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
    private final SeparationProjectImageResolver projectImageResolver = new SeparationProjectImageResolver();
    private String activeFilter = "Todo";
    private final Set<String> pendingStatusUpdates = new HashSet<>();

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
        progressSeparaciones = findViewById(R.id.progressSeparaciones);
        txtListState = findViewById(R.id.txtSeparacionesState);
        btnRetry = findViewById(R.id.btnRetrySeparaciones);

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
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadSeparacionesFromFirestore());

        // Cargar datos desde Firestore
        loadSeparacionesFromFirestore();
    }

    private void loadSeparacionesFromFirestore() {
        if (separacionesListener != null) {
            separacionesListener.remove();
            separacionesListener = null;
        }
        renderLoading();
        String asesorId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? "" : FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (asesorId.isEmpty() || !asesorId.equals(sessionManager.getUid())) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        separacionesListener = separationRepository.listenSeparationsForAdvisor(asesorId, new FirebaseSeparationRepository.SeparationsListener() {
            @Override
            public void onDataChanged(List<Separacion> list) {
                if (isFinishing() || isDestroyed()) return;
                allSeparaciones.clear();
                allSeparaciones.addAll(list);

                // Actualizar vista según filtro activo
                applyFilter(activeFilter);
                updateStats(allSeparaciones);
                projectImageResolver.resolve(allSeparaciones, () -> {
                    if (isFinishing() || isDestroyed()) return;
                    applyFilter(activeFilter);
                    renderListState();
                });
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                renderError();
                Toast.makeText(AsesorSeparacionesActivity.this, "Error al cargar separaciones: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validarPago(Separacion separacion) {
        if (!beginStatusUpdate(separacion)) return;
        separationRepository.updateSeparationStatus(separacion.getId(), "Pagada", new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                finishStatusUpdate(separationId);
                Toast.makeText(AsesorSeparacionesActivity.this, "Pago validado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                finishStatusUpdate(separacion.getId());
                Toast.makeText(AsesorSeparacionesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aprobarSeparacion(Separacion separacion) {
        if (!beginStatusUpdate(separacion)) return;
        separationRepository.updateSeparationStatus(separacion.getId(), "Aprobada", new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                finishStatusUpdate(separationId);
                Toast.makeText(AsesorSeparacionesActivity.this, "Separación aprobada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                finishStatusUpdate(separacion.getId());
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
            if (SeparationPresentationPolicy.isManagedStatus(s)
                    && SeparationPresentationPolicy.isManagedPen(s)) {
                // Verificar si es del mes actual (por createdAt)
                if (s.getCreatedAt() > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(s.getCreatedAt());
                    if (cal.get(java.util.Calendar.MONTH) == currentMonth &&
                            cal.get(java.util.Calendar.YEAR) == currentYear) {
                        montoMensual += SeparationPresentationPolicy.penAmount(s);
                    }
                }
            }
        }

        // Actualizar vistas
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        formatter.setCurrency(Currency.getInstance("PEN"));
        txtMontoMensual.setText(formatter.format(montoMensual));
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
        activeFilter = "Pendiente".equalsIgnoreCase(status) ? "Pendiente" : "Todo";
        displayList.clear();
        if ("Todo".equalsIgnoreCase(activeFilter)) {
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
        renderListState();
    }

    private void updateFilterStyles(TextView active, TextView inactive) {
        // Estilo Activo (Dark Chip)
        active.setBackground(ContextCompat.getDrawable(this, R.drawable.as_chip_dark));
        active.setTextColor(Color.WHITE);

        // Estilo Inactivo (Light Chip)
        inactive.setBackground(ContextCompat.getDrawable(this, R.drawable.as_filter_inactive));
        inactive.setTextColor(ContextCompat.getColor(this, R.color.admin_text_secondary));
    }

    private void renderLoading() {
        if (progressSeparaciones != null) progressSeparaciones.setVisibility(View.VISIBLE);
        if (txtListState != null) {
            txtListState.setText("Cargando separaciones…");
            txtListState.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private void renderError() {
        if (progressSeparaciones != null) progressSeparaciones.setVisibility(View.GONE);
        if (txtListState != null) {
            txtListState.setText("No se pudieron cargar las separaciones.");
            txtListState.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
    }

    private void renderListState() {
        if (progressSeparaciones != null) progressSeparaciones.setVisibility(View.GONE);
        if (txtListState == null) return;
        if (displayList.isEmpty()) {
            txtListState.setText(allSeparaciones.isEmpty()
                    ? "Aún no tienes separaciones registradas."
                    : "No hay separaciones pendientes.");
            txtListState.setVisibility(View.VISIBLE);
        } else {
            txtListState.setVisibility(View.GONE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private boolean beginStatusUpdate(Separacion separation) {
        String id = separation == null ? "" : separation.getId();
        if (id == null || id.trim().isEmpty() || pendingStatusUpdates.contains(id)) return false;
        pendingStatusUpdates.add(id);
        return true;
    }

    private void finishStatusUpdate(String separationId) {
        if (separationId != null) pendingStatusUpdates.remove(separationId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (separacionesListener != null) {
            separacionesListener.remove();
        }
    }
}
