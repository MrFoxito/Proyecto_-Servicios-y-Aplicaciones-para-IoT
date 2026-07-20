package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AsesorSolicitudSeparacionActivity extends BaseAsesorActivity {

    private TextView txtReservaId, txtStatus, txtEstadoDescripcion, txtFecha;
    private TextView txtCliente, txtClienteSub;
    private TextView txtProyecto, txtPropiedad;
    private TextView txtPrecioTotal, txtMontoSeparacion;
    private ImageView imgProperty;

    private FirebaseFirestore db;
    private AuthSessionManager sessionManager;
    private String separacionId;
    private final SeparationProjectImageResolver projectImageResolver = new SeparationProjectImageResolver();
    private final SeparationPricingResolver pricingResolver = new SeparationPricingResolver();
    private boolean actionInFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_solicitud_separacion);

        db = FirebaseFirestore.getInstance();
        sessionManager = AuthSessionManager.getInstance(this);

        setupBackButton();
        bindViews();

        // Obtener ID de la separación desde el intent
        separacionId = getIntent().getStringExtra("separacionId");
        if (separacionId == null || separacionId.isEmpty()) {
            Toast.makeText(this, "Error: ID de separación no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar datos desde Firestore
        loadSeparacionFromFirestore(separacionId);

        // Configurar botones
        findViewById(R.id.btnAprobarPagoSolicitud).setOnClickListener(v -> {
            if (separacionId != null) {
                aprobarSeparacion(separacionId);
            }
        });

        findViewById(R.id.btnRechazarSolicitud).setOnClickListener(v -> {
            if (separacionId != null) {
                rechazarSeparacion(separacionId);
            }
        });
    }

    private void bindViews() {
        txtReservaId = findViewById(R.id.txtSolReservaId);
        txtStatus = findViewById(R.id.txtSolStatus);
        txtEstadoDescripcion = findViewById(R.id.txtSolEstadoDescripcion);
        txtFecha = findViewById(R.id.txtSolFecha);
        txtCliente = findViewById(R.id.txtSolCliente);
        txtClienteSub = findViewById(R.id.txtSolClienteSub);
        txtProyecto = findViewById(R.id.txtSolProyecto);
        txtPropiedad = findViewById(R.id.txtSolPropiedad);
        txtPrecioTotal = findViewById(R.id.txtSolPrecioTotal);
        txtMontoSeparacion = findViewById(R.id.txtSolMontoSeparacion);
        imgProperty = findViewById(R.id.imgProperty);
    }

    private void loadSeparacionFromFirestore(String id) {
        db.collection("separaciones").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (doc.exists()) {
                        Separacion sep = doc.toObject(Separacion.class);
                        if (sep != null) {
                            enrichPresentationData(doc, sep);
                            populateViews(sep);
                            pricingResolver.resolve(sep, () -> {
                                if (isFinishing() || isDestroyed()) return;
                                bindPricing(sep);
                                bindTypology(sep);
                            });
                            imgProperty.setImageResource(R.drawable.as_project_placeholder);
                            projectImageResolver.resolve(sep, () -> {
                                if (isFinishing() || isDestroyed()) return;
                                ProjectImageLoader.load(imgProperty, sep.getProjectImageUrl(), R.drawable.as_project_placeholder);
                            });
                        } else {
                            Toast.makeText(this, "Error al mapear datos", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Separación no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    /** Adds only existing document fields for a consistent presentation; it never writes data. */
    private void enrichPresentationData(DocumentSnapshot doc, Separacion sep) {
        sep.setId(doc.getId());
        sep.setProjectId(SeparationPresentationPolicy.canonicalProjectId(
                doc.getString("propertyId"), doc.getString("projectId"), doc.getString("proyectoId")));
        Object amount = doc.get("amount");
        if (amount instanceof Number) sep.setAmount(((Number) amount).doubleValue());
        sep.setCurrency(doc.getString("currency"));
        sep.setPrecioTotal(SeparationPricingPolicy.number(doc.get("precioTotal")));
        sep.setPrecioTotalTexto(doc.getString("precioTotalTexto"));
        sep.setMontoSeparacion(SeparationPricingPolicy.number(doc.get("montoSeparacion")));
        sep.setMontoSeparacionTexto(doc.getString("montoSeparacionTexto"));
    }

    private void populateViews(Separacion sep) {
        // ID de reserva
        txtReservaId.setText(sep.getId() != null ? "#" + sep.getId() : "---");

        // Estado
        String estado = sep.getEstado() != null ? sep.getEstado() : "Pendiente";
        txtStatus.setText(estado.toUpperCase());
        applyStatusStyle(txtStatus, estado);
        txtEstadoDescripcion.setText("Estado: " + estado);

        // Fecha de solicitud
        if (sep.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "ES"));
            String fecha = sdf.format(new Date(sep.getCreatedAt()));
            txtFecha.setText("Solicitado: " + fecha);
        } else if (sep.getFechaTexto() != null) {
            txtFecha.setText("Solicitado: " + sep.getFechaTexto());
        } else {
            txtFecha.setText("Solicitado: ---");
        }

        // Cliente
        txtCliente.setText(sep.getClienteNombre() != null ? sep.getClienteNombre() : "---");
        txtClienteSub.setText("Cliente ID: " + (sep.getClienteId() != null ? sep.getClienteId() : "---"));

        // Proyecto y propiedad
        txtProyecto.setText(sep.getInmuebleNombre() != null ? sep.getInmuebleNombre() : "---");
        bindTypology(sep);

        // Montos
        bindPricing(sep);

        // Imagen (placeholder)
        // Ocultar botones si ya está aprobada/rechazada
        String estadoLower = estado.toLowerCase();
        if (estadoLower.equals("aprobada") || estadoLower.equals("rechazada")) {
            findViewById(R.id.btnAprobarPagoSolicitud).setVisibility(android.view.View.GONE);
            findViewById(R.id.btnRechazarSolicitud).setVisibility(android.view.View.GONE);
        }
    }

    private void bindTypology(Separacion sep) {
        String typology = SeparationPricingPolicy.value(sep.getTipologiaId());
        txtPropiedad.setText("Tipología: " + (typology.isEmpty() ? "No especificada" : typology));
    }

    private void bindPricing(Separacion sep) {
        txtPrecioTotal.setText(SeparationPricingPolicy.display(
                sep.getPrecioTotalTexto(), sep.getPrecioTotal(), ""));
        txtMontoSeparacion.setText(SeparationPricingPolicy.display(
                sep.getMontoSeparacionTexto(), sep.getMontoSeparacion(), sep.getMontoTexto()));
    }

    private void applyStatusStyle(TextView tvStatus, String estado) {
        String estadoLower = estado.toLowerCase();
        switch (estadoLower) {
            case "pagada":
            case "aprobada":
                tvStatus.setBackgroundResource(R.drawable.as_status_green);
                tvStatus.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "pendiente":
                tvStatus.setBackgroundResource(R.drawable.as_status_pending);
                tvStatus.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "rechazada":
                tvStatus.setBackgroundResource(R.drawable.as_status_red);
                tvStatus.setTextColor(Color.WHITE);
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.as_chip_light);
                tvStatus.setTextColor(Color.parseColor("#746D4A"));
                break;
        }
    }

    private void aprobarSeparacion(String id) {
        if (actionInFlight) return;
        setActionInFlight(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Aprobada");

        db.collection("separaciones").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    setActionInFlight(false);
                    Toast.makeText(this, "Separación aprobada", Toast.LENGTH_SHORT).show();
                    // Actualizar vista
                    loadSeparacionFromFirestore(id);
                })
                .addOnFailureListener(e -> {
                    setActionInFlight(false);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void rechazarSeparacion(String id) {
        if (actionInFlight) return;
        setActionInFlight(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Rechazada");

        db.collection("separaciones").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    setActionInFlight(false);
                    Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                    loadSeparacionFromFirestore(id);
                })
                .addOnFailureListener(e -> {
                    setActionInFlight(false);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void setActionInFlight(boolean inFlight) {
        actionInFlight = inFlight;
        findViewById(R.id.btnAprobarPagoSolicitud).setEnabled(!inFlight);
        findViewById(R.id.btnRechazarSolicitud).setEnabled(!inFlight);
    }
}
