package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    public static final String EXTRA_CITA_ID = "extra_cita_id";

    private Cita citaActual;
    private EventoCitaAdapter eventoCitaAdapter;
    private FirebaseFirestore db;
    private AuthSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        db = FirebaseFirestore.getInstance();
        sessionManager = AuthSessionManager.getInstance(this);

        setupBackButton();
        setupRecyclerView();

        String citaId = getIntent().getStringExtra(EXTRA_CITA_ID);
        if (citaId == null || citaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de cita no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCitaFromFirestore(citaId);
        setupActions();
    }

    private void loadCitaFromFirestore(String citaId) {
        db.collection("citas").document(citaId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        citaActual = doc.toObject(Cita.class);
                        if (citaActual != null) {
                            citaActual.setId(doc.getId());
                            populateData();
                        } else {
                            Toast.makeText(this, "Error al cargar la cita", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Cita no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupRecyclerView() {
        RecyclerView rvHistorial = findViewById(R.id.rvHistorialCita);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        eventoCitaAdapter = new EventoCitaAdapter(null);
        rvHistorial.setAdapter(eventoCitaAdapter);
    }

    private void populateData() {
        if (citaActual == null) return;

        String clientName = citaActual.getClienteNombre() != null ? citaActual.getClienteNombre() : "Cliente";
        String propertyName = citaActual.getProyectoNombre() != null ? citaActual.getProyectoNombre() : "Inmueble";
        String proyecto = citaActual.getProyectoNombre() != null ? citaActual.getProyectoNombre() : "";
        String status = citaActual.getEstado() != null ? citaActual.getEstado() : "Pendiente";

        // Fecha y hora formateadas
        String fechaStr = citaActual.getFechaFormateada();
        String horaStr = citaActual.getHoraFormateada();

        // Día de la semana (extraer de fechaISO)
        String diaStr = "";
        try {
            SimpleDateFormat dbFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dbFmt.parse(citaActual.getFechaISO()));
            SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
            diaStr = dayFmt.format(cal.getTime());
            diaStr = diaStr.substring(0, 1).toUpperCase() + diaStr.substring(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciales del avatar
        String[] parts = clientName.split(" ");
        String initials = (parts.length >= 2)
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : String.valueOf(parts[0].charAt(0));

        // --- Imagen de propiedad (placeholder) ---
        ImageView imgPropiedad = findViewById(R.id.imgDetallePropiedad);
        imgPropiedad.setImageResource(R.drawable.as_property_01);

        // --- Proyecto badge + nombre propiedad ---
        TextView txtProyecto = findViewById(R.id.txtDetalleProyecto);
        TextView txtPropiedad = findViewById(R.id.txtDetallePropiedad);
        txtProyecto.setText(proyecto.toUpperCase());
        txtPropiedad.setText(propertyName);

        // --- Status badge ---
        applyStatusBadge(findViewById(R.id.txtDetalleStatusImg), status);
        applyStatusBadge(findViewById(R.id.txtDetalleStatus), status);

        // --- Cliente ---
        ((TextView) findViewById(R.id.txtDetalleAvatar)).setText(initials);
        ((TextView) findViewById(R.id.txtDetalleCliente)).setText(clientName);
        ((TextView) findViewById(R.id.txtDetalleClienteTipo)).setText("Cliente · Portafolio Activo");

        // --- Fecha y hora ---
        ((TextView) findViewById(R.id.txtDetalleFecha)).setText(fechaStr);
        ((TextView) findViewById(R.id.txtDetalleDia)).setText(diaStr);
        ((TextView) findViewById(R.id.txtDetalleHora)).setText(horaStr);

        // --- Ubicación (usamos el nombre del proyecto como dirección) ---
        ((TextView) findViewById(R.id.txtDetalleDireccion)).setText(propertyName);
        ((TextView) findViewById(R.id.txtDetalleSubdireccion)).setText("Av. del Bosque 120, Piso 18"); // Podrías guardar la dirección real en la cita

        // --- Notas (si existen) ---
        String notas = citaActual.getNota() != null ? citaActual.getNota() : "Sin notas adicionales.";
        ((TextView) findViewById(R.id.txtDetalleNotas)).setText(notas);

        // --- Mostrar/ocultar acciones según estado ---
        boolean isClosed = status.equalsIgnoreCase("Cancelada") ||
                status.equalsIgnoreCase("Atendida") ||
                status.equalsIgnoreCase("No asistio") ||
                status.equalsIgnoreCase("Cerrada") ||
                status.equalsIgnoreCase("Pasada");

        findViewById(R.id.btnRegistrarSeparacionDetalle).setVisibility(
                isClosed ? View.GONE : View.VISIBLE
        );
        applyActionVisibility(status);

        // --- Historial (si existe) ---
        if (citaActual.getHistorial() != null) {
            eventoCitaAdapter.setEventos(citaActual.getHistorial());
        }
    }

    private void applyStatusBadge(TextView badge, String status) {
        badge.setText(status.toUpperCase());
        switch (status.toLowerCase()) {
            case "confirmada":
            case "cerrada":
                badge.setBackgroundResource(R.drawable.as_status_green);
                badge.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "en camino":
                badge.setBackgroundResource(R.drawable.as_status_blue);
                badge.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "pendiente":
            case "reprogramada":
                badge.setBackgroundResource(R.drawable.as_status_pending);
                badge.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            default: // pasada, no conectada, etc.
                badge.setBackgroundResource(R.drawable.as_chip_light);
                badge.setTextColor(Color.parseColor("#746D4A"));
                break;
        }
    }

    private void setupActions() {
        // Registrar separación
        findViewById(R.id.btnRegistrarSeparacionDetalle).setOnClickListener(v -> {
            if (citaActual != null) {
                Intent intent = new Intent(this, AsesorRegistrarSeparacionActivity.class);
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE, citaActual.getClienteNombre());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPIEDAD, citaActual.getProyectoNombre());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROJECT_ID, citaActual.getProyectoId());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO, citaActual.getProyectoNombre());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CITA_ID, citaActual.getId());
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Reprogramar
        findViewById(R.id.btnReprogramarCita).setOnClickListener(v -> openReschedule());

        // Atendida
        findViewById(R.id.btnMarcarAtendida).setOnClickListener(v -> updateAttendance(true));

        // No asistió
        findViewById(R.id.btnMarcarNoAsistio).setOnClickListener(v -> updateAttendance(false));

        // Cancelar
        findViewById(R.id.btnCancelarCita).setOnClickListener(v -> showCancelDialog());

        // Llamar
        findViewById(R.id.btnLlamar).setOnClickListener(v ->
                Toast.makeText(this, "Llamando a " + citaActual.getClienteNombre() + "…", Toast.LENGTH_SHORT).show()
        );

        // Mensaje
        findViewById(R.id.btnMensaje).setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorChatIndividualActivity.class);
            intent.putExtra("chatId", citaActual.getId()); // O usar un ID de conversación
            intent.putExtra("clienteId", citaActual.getClienteId());
            intent.putExtra("clienteNombre", citaActual.getClienteNombre());
            // Si tienes la URL del avatar, pásala
            startActivity(intent);
        });
    }

    private void openReschedule() {
        if (citaActual == null) return;
        Intent intent = new Intent(this, AsesorReprogramarCitaActivity.class);
        intent.putExtra(EXTRA_CITA_ID, citaActual.getId());
        intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE, citaActual.getClienteNombre());
        intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
        intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPIEDAD, citaActual.getProyectoNombre());
        intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROJECT_ID, citaActual.getProyectoId());

        startActivity(intent);
    }

    private void updateAttendance(boolean attended) {
        if (citaActual == null) return;

        String newStatus = attended ? "Atendida" : "No asistio";
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", newStatus);

        db.collection("citas").document(citaActual.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, attended ? "Asistencia confirmada" : "Inasistencia registrada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showCancelDialog() {
        if (citaActual == null) return;

        EditText input = new EditText(this);
        input.setHint("Motivo de cancelación");
        input.setMinLines(2);
        input.setPadding(32, 20, 32, 20);

        new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("El horario quedará disponible nuevamente.")
                .setView(input)
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar cita", (dialog, which) -> {
                    String motivo = input.getText().toString().trim();
                    if (motivo.isEmpty()) motivo = "Cancelada por el asesor";

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("estado", "Cancelada");
                    updates.put("nota", motivo); // Guardar motivo en nota

                    db.collection("citas").document(citaActual.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al cancelar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .show();
    }

    private void applyActionVisibility(String status) {
        boolean closed = status.equalsIgnoreCase("Cancelada") ||
                status.equalsIgnoreCase("Atendida") ||
                status.equalsIgnoreCase("No asistio") ||
                status.equalsIgnoreCase("Cerrada") ||
                status.equalsIgnoreCase("Pasada");

        int visibility = closed ? View.GONE : View.VISIBLE;
        findViewById(R.id.btnReprogramarCita).setVisibility(visibility);
        findViewById(R.id.btnMarcarAtendida).setVisibility(visibility);
        findViewById(R.id.btnMarcarNoAsistio).setVisibility(visibility);
        findViewById(R.id.btnCancelarCita).setVisibility(visibility);
    }
}