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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.entity.Cita;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {
    public static final String EXTRA_CITA_ID = "extra_cita_id";
    public static final String EXTRA_CLIENTE_ID = "extra_cliente_id";
    public static final String EXTRA_CLIENTE = "extra_cliente";
    public static final String EXTRA_PROPIEDAD = "extra_propiedad";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_FECHA = "extra_fecha";
    public static final String EXTRA_HORA = "extra_hora";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_PROPERTY_ID = "extra_property_id";
    public static final String EXTRA_ASESOR_ID = "extra_asesor_id";
    public static final String EXTRA_SLOT_ID = "extra_slot_id";
    public static final String EXTRA_DURATION_MINUTOS = "extra_duration_minutos";
    public static final String EXTRA_CAPACIDAD_HORARIO = "extra_capacidad_horario";

    private Cita citaActual;
    private EventoCitaAdapter eventoCitaAdapter;
    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        setupBackButton();
        setupRecyclerView();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateData();
    }

    private void setupRecyclerView() {
        RecyclerView rvHistorial = findViewById(R.id.rvHistorialCita);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        eventoCitaAdapter = new EventoCitaAdapter(null);
        rvHistorial.setAdapter(eventoCitaAdapter);
    }

    private void populateData() {
        citaActual = citaFromIntent();
        if (citaActual == null) {
            return;
        }

        String clientName   = citaActual.getClientName();
        String propertyName = citaActual.getPropertyName();
        String proyecto     = citaActual.getProyecto();
        String status       = citaActual.getStatus();
        String time         = citaActual.getTime();
        String notas        = "Cliente interesada en el piso 18 con vista panorámica. " +
                              "Presupuesto aprobado hasta $480,000. " +
                              "Llegará con su esposo. Prefiere pago en cuotas. " +
                              "Agendar seguimiento post-visita.";

        // Formato de fecha
        String dateStr = citaActual.getDate(); // format YYYY-MM-DD
        String fechaStr = dateStr;
        String diaStr = "";
        try {
            SimpleDateFormat dbFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dbFmt.parse(dateStr));
            
            SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM, yyyy", new Locale("es", "ES"));
            SimpleDateFormat dayFmt  = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
            fechaStr = dateFmt.format(cal.getTime());
            diaStr   = dayFmt.format(cal.getTime());
            fechaStr = fechaStr.substring(0, 1).toUpperCase() + fechaStr.substring(1);
            diaStr   = diaStr.substring(0, 1).toUpperCase() + diaStr.substring(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initiales del avatar
        String[] parts   = clientName.split(" ");
        String initials  = (parts.length >= 2)
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : String.valueOf(parts[0].charAt(0));

        // --- Imagen de propiedad ---
        ImageView imgPropiedad = findViewById(R.id.imgDetallePropiedad);
        imgPropiedad.setImageResource(R.drawable.as_property_01);

        // --- Proyecto badge + nombre propiedad ---
        TextView txtProyecto  = findViewById(R.id.txtDetalleProyecto);
        TextView txtPropiedad = findViewById(R.id.txtDetallePropiedad);
        txtProyecto.setText(proyecto.toUpperCase());
        txtPropiedad.setText(propertyName);

        // --- Status badge (imagen y panel) ---
        applyStatusBadge(findViewById(R.id.txtDetalleStatusImg), status);
        applyStatusBadge(findViewById(R.id.txtDetalleStatus), status);

        // --- Cliente ---
        ((TextView) findViewById(R.id.txtDetalleAvatar)).setText(initials);
        ((TextView) findViewById(R.id.txtDetalleCliente)).setText(clientName);
        ((TextView) findViewById(R.id.txtDetalleClienteTipo)).setText("Cliente · Portafolio Activo");

        // --- Fecha y hora ---
        ((TextView) findViewById(R.id.txtDetalleFecha)).setText(fechaStr);
        ((TextView) findViewById(R.id.txtDetalleDia)).setText(diaStr);
        ((TextView) findViewById(R.id.txtDetalleHora)).setText(time);

        // --- Ubicación ---
        ((TextView) findViewById(R.id.txtDetalleDireccion)).setText(propertyName);
        ((TextView) findViewById(R.id.txtDetalleSubdireccion)).setText("Av. del Bosque 120, Piso 18");

        // --- Notas ---
        ((TextView) findViewById(R.id.txtDetalleNotas)).setText(notas);

        // --- Ocultar "Registrar Separación" si ya está cerrada/pasada ---
        if ("Pasada".equalsIgnoreCase(status) || "Cerrada".equalsIgnoreCase(status)) {
            findViewById(R.id.btnRegistrarSeparacionDetalle).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btnRegistrarSeparacionDetalle).setVisibility(View.VISIBLE);
        }
        applyActionVisibility(status);

        // Actualizar RecyclerView del historial
        eventoCitaAdapter.setEventos(citaActual.getHistorial());
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
            default: // pasada, no conectada
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
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE,   citaActual.getClientName());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPIEDAD, citaActual.getPropertyName());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPERTY_ID, citaActual.getPropertyId());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO,  citaActual.getProyecto());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CITA_ID,   citaActual.getId());
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Reprogramar
        findViewById(R.id.btnReprogramarCita)
                .setOnClickListener(v -> openReschedule());

        View btnAtendida = findViewById(R.id.btnMarcarAtendida);
        if (btnAtendida != null) {
            btnAtendida.setOnClickListener(v -> updateAttendance(true));
        }

        View btnNoAsistio = findViewById(R.id.btnMarcarNoAsistio);
        if (btnNoAsistio != null) {
            btnNoAsistio.setOnClickListener(v -> updateAttendance(false));
        }

        View btnCancelar = findViewById(R.id.btnCancelarCita);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> showCancelDialog());
        }

        // Llamar al cliente
        ImageButton btnLlamar = findViewById(R.id.btnLlamar);
        if (btnLlamar != null) {
            btnLlamar.setOnClickListener(v ->
                    Toast.makeText(this, "Llamando a " + (citaActual != null ? citaActual.getClientName() : "") + "…", Toast.LENGTH_SHORT).show());
        }

        // Mensaje al cliente
        ImageButton btnMensaje = findViewById(R.id.btnMensaje);
        if (btnMensaje != null) {
            btnMensaje.setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        }
    }

    private Cita citaFromIntent() {
        Intent intent = getIntent();
        if (intent == null || intent.getStringExtra(EXTRA_CITA_ID) == null) {
            return null;
        }
        Cita cita = new Cita(
                intent.getStringExtra(EXTRA_CITA_ID),
                valueOr(intent.getStringExtra(EXTRA_CLIENTE), "Cliente"),
                valueOr(intent.getStringExtra(EXTRA_PROPIEDAD), "Inmueble"),
                valueOr(intent.getStringExtra(EXTRA_HORA), ""),
                valueOr(intent.getStringExtra(EXTRA_FECHA), ""),
                valueOr(intent.getStringExtra(EXTRA_STATUS), "Confirmada"),
                valueOr(intent.getStringExtra(EXTRA_PROYECTO), ""),
                false
        );
        cita.setClienteId(valueOr(intent.getStringExtra(EXTRA_CLIENTE_ID), ""));
        cita.setAsesorId(valueOr(intent.getStringExtra(EXTRA_ASESOR_ID), ""));
        cita.setPropertyId(valueOr(intent.getStringExtra(EXTRA_PROPERTY_ID), ""));
        cita.setFechaISO(valueOr(intent.getStringExtra(EXTRA_FECHA), ""));
        cita.setSlotId(valueOr(intent.getStringExtra(EXTRA_SLOT_ID), ""));
        cita.setDurationMinutos(intent.getIntExtra(EXTRA_DURATION_MINUTOS, 60));
        cita.setCapacidadHorario(intent.getIntExtra(EXTRA_CAPACIDAD_HORARIO, 1));
        return cita;
    }

    private void openReschedule() {
        if (citaActual == null) {
            return;
        }
        Intent intent = new Intent(this, AsesorReprogramarCitaActivity.class);
        intent.putExtra(EXTRA_CITA_ID, citaActual.getId());
        intent.putExtra(EXTRA_CLIENTE_ID, citaActual.getClienteId());
        intent.putExtra(EXTRA_CLIENTE, citaActual.getClientName());
        intent.putExtra(EXTRA_PROPIEDAD, citaActual.getPropertyName());
        intent.putExtra(EXTRA_PROYECTO, citaActual.getProyecto());
        intent.putExtra(EXTRA_FECHA, citaActual.getDate());
        intent.putExtra(EXTRA_HORA, citaActual.getTime());
        intent.putExtra(EXTRA_STATUS, citaActual.getStatus());
        intent.putExtra(EXTRA_PROPERTY_ID, citaActual.getPropertyId());
        intent.putExtra(EXTRA_ASESOR_ID, citaActual.getAsesorId());
        intent.putExtra(EXTRA_SLOT_ID, citaActual.getSlotId());
        startActivity(intent);
    }

    private void updateAttendance(boolean attended) {
        if (citaActual == null) {
            return;
        }
        appointmentRepository.updateAttendance(citaActual.getId(), attended, new FirebaseAppointmentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AsesorDetalleCitaActivity.this,
                        attended ? "Asistencia confirmada" : "Inasistencia registrada",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorDetalleCitaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCancelDialog() {
        if (citaActual == null) {
            return;
        }
        EditText input = new EditText(this);
        input.setHint("Motivo de cancelacion");
        input.setMinLines(2);
        input.setPadding(32, 20, 32, 20);
        new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("El horario quedara disponible nuevamente.")
                .setView(input)
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar cita", (dialog, which) ->
                        appointmentRepository.cancelAppointment(citaActual.getId(), input.getText().toString(), new FirebaseAppointmentRepository.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AsesorDetalleCitaActivity.this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AsesorDetalleCitaActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }

    private void applyActionVisibility(String status) {
        boolean closed = "Cancelada".equalsIgnoreCase(status)
                || "Atendida".equalsIgnoreCase(status)
                || "No asistio".equalsIgnoreCase(status)
                || "Cerrada".equalsIgnoreCase(status)
                || "Pasada".equalsIgnoreCase(status);
        int visibility = closed ? View.GONE : View.VISIBLE;
        View reschedule = findViewById(R.id.btnReprogramarCita);
        View attended = findViewById(R.id.btnMarcarAtendida);
        View noShow = findViewById(R.id.btnMarcarNoAsistio);
        View cancel = findViewById(R.id.btnCancelarCita);
        if (reschedule != null) reschedule.setVisibility(visibility);
        if (attended != null) attended.setVisibility(visibility);
        if (noShow != null) noShow.setVisibility(visibility);
        if (cancel != null) cancel.setVisibility(visibility);
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
