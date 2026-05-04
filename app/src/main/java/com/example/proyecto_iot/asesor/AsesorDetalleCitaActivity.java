package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    private Cita citaActual;
    private EventoCitaAdapter eventoCitaAdapter;

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
        // Obtenemos la cita centralizada (hardcodeada por ahora)
        // citaActual = CitaRepository.getInstance().getCitaActual();
        citaActual = new Cita("1", "Alicia Velarde", "Penthouse Altos del Bosque", "08:30 AM", "2026-05-04", "Confirmada", "Inmobiliaria Horizonte", false);

        java.util.List<com.example.proyecto_iot.entity.EventoCita> eventos = new java.util.ArrayList<>();
        eventos.add(new com.example.proyecto_iot.entity.EventoCita("e1", "1", "Cita agendada", "Agendada desde la app por el asesor", "2026-05-03T14:32", "AGENDADA"));
        eventos.add(new com.example.proyecto_iot.entity.EventoCita("e2", "1", "Cita confirmada por el cliente", "Confirmado vía WhatsApp", "2026-05-04T08:00", "CONFIRMADA"));
        
        // Asignamos el historial
        for (com.example.proyecto_iot.entity.EventoCita e : eventos) {
            citaActual.addEvento(e);
        }

        if (citaActual == null) return;

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
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPIEDAD, citaActual.getPropertyName());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO,  citaActual.getProyecto());
                intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CITA_ID,   citaActual.getId());
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Reprogramar
        findViewById(R.id.btnReprogramarCita)
                .setOnClickListener(v -> openScreen(AsesorReprogramarCitaActivity.class));

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
}
