package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.proyecto_iot.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    // Datos de la cita actual (accesibles desde setupActions)
    private String citaCliente   = "Alicia Velarde";
    private String citaPropiedad = "Penthouse Altos del Bosque";
    private String citaProyecto  = "Inmobiliaria Horizonte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        setupBackButton();
        populateHardcodedData();
        setupActions();
    }

    private void populateHardcodedData() {
        // --- Datos coherentes con AsesorMiAgendaActivity (Cita id="1") ---
        // Sincronizados con los campos de instancia para pasarlos a Registrar Separación
        String clientName   = citaCliente;
        String propertyName = citaPropiedad;
        String proyecto     = citaProyecto;
        String status       = "Confirmada";
        String time         = "08:30 AM";
        String notas        = "Cliente interesada en el piso 18 con vista panorámica. " +
                              "Presupuesto aprobado hasta $480,000. " +
                              "Llegará con su esposo. Prefiere pago en cuotas. " +
                              "Agendar seguimiento post-visita.";

        // Fecha = hoy
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM, yyyy", new Locale("es", "ES"));
        SimpleDateFormat dayFmt  = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String fechaStr = dateFmt.format(today.getTime());
        String diaStr   = dayFmt.format(today.getTime());
        fechaStr = fechaStr.substring(0, 1).toUpperCase() + fechaStr.substring(1);
        diaStr   = diaStr.substring(0, 1).toUpperCase() + diaStr.substring(1);

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
            default: // pasada, no conectada
                badge.setBackgroundResource(R.drawable.as_chip_light);
                badge.setTextColor(Color.parseColor("#746D4A"));
                break;
        }
    }

    private void setupActions() {
        // Registrar separación — pasa datos de la cita para auto-rellenar el formulario
        findViewById(R.id.btnRegistrarSeparacionDetalle).setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorRegistrarSeparacionActivity.class);
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE,   citaCliente);
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROPIEDAD, citaPropiedad);
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO,  citaProyecto);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Reprogramar
        findViewById(R.id.btnReprogramarCita)
                .setOnClickListener(v -> openScreen(AsesorReprogramarCitaActivity.class));


        // Llamar al cliente
        ImageButton btnLlamar = findViewById(R.id.btnLlamar);
        if (btnLlamar != null) {
            btnLlamar.setOnClickListener(v ->
                    Toast.makeText(this, "Llamando a Alicia Velarde…", Toast.LENGTH_SHORT).show());
        }

        // Mensaje al cliente
        ImageButton btnMensaje = findViewById(R.id.btnMensaje);
        if (btnMensaje != null) {
            btnMensaje.setOnClickListener(v -> openScreen(AsesorChatIndividualActivity.class));
        }
    }
}
