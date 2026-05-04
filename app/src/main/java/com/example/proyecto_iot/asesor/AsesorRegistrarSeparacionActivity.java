package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.proyecto_iot.R;

public class AsesorRegistrarSeparacionActivity extends BaseAsesorActivity {

    // Claves de extras que envía AsesorDetalleCitaActivity
    public static final String EXTRA_CLIENTE   = "extra_cliente";
    public static final String EXTRA_PROPIEDAD = "extra_propiedad";
    public static final String EXTRA_PROYECTO  = "extra_proyecto";
    public static final String EXTRA_CITA_ID   = "extra_cita_id";

    private String activePago = "efectivo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_registrar_separaciones);

        setupBackButton();
        prefillFromIntent();
        setupPagoChips();
        setupActions();
    }

    /**
     * Pre-rellena los campos de cliente y proyecto si venimos desde DetalleCita.
     * Muestra el banner de "originado desde cita" y oculta los badges si no hay cita.
     */
    private void prefillFromIntent() {
        Intent intent = getIntent();
        String cliente   = intent.getStringExtra(EXTRA_CLIENTE);
        String proyecto  = intent.getStringExtra(EXTRA_PROYECTO);
        String citaId    = intent.getStringExtra(EXTRA_CITA_ID);

        boolean fromCita = cliente != null && proyecto != null;

        // ── Banner de origen ──
        LinearLayout banner = findViewById(R.id.bannerCitaVinculada);
        TextView txtBannerDetalle = findViewById(R.id.txtBannerCitaDetalle);
        if (fromCita) {
            banner.setVisibility(View.VISIBLE);
            txtBannerDetalle.setText("Cita"+(citaId != null ? " #"+citaId : "")+" vinculada.");
        } else {
            banner.setVisibility(View.GONE);
        }

        // ── Campo oculto cita ID ──
        TextView txtCitaId = findViewById(R.id.txtSepCitaId);
        if (citaId != null) {
            txtCitaId.setText(citaId);
        }

        // ── Campo proyecto ──
        AutoCompleteTextView txtProyecto      = findViewById(R.id.txtSepProyecto);
        TextView badgeProyecto    = findViewById(R.id.badgeSepProyectoVinculado);
        if (fromCita) {
            txtProyecto.setText(proyecto, false);
            txtProyecto.setEnabled(false);
            badgeProyecto.setVisibility(View.VISIBLE);
        } else {
            txtProyecto.setEnabled(true);
            badgeProyecto.setVisibility(View.GONE);
            String[] proyectosMocks = {"Inmobiliaria Horizonte", "Costa Moderna", "Torres del Bosque", "Residencial Alba"};
            ArrayAdapter<String> proyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proyectosMocks);
            txtProyecto.setAdapter(proyAdapter);
        }

        // ── Campo cliente ──
        AutoCompleteTextView txtCliente   = findViewById(R.id.txtSepCliente);
        TextView badgeCliente = findViewById(R.id.badgeSepClienteVinculado);
        if (fromCita) {
            txtCliente.setText(cliente, false);
            txtCliente.setEnabled(false);
            badgeCliente.setVisibility(View.VISIBLE);
        } else {
            txtCliente.setEnabled(true);
            badgeCliente.setVisibility(View.GONE);
            String[] clientesMocks = {"Alicia Velarde", "Julian Montgomery", "Carlos Ruiz", "Maria Fernanda"};
            ArrayAdapter<String> cliAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clientesMocks);
            txtCliente.setAdapter(cliAdapter);
        }

        // ── Monto sugerido según proyecto ──
        TextView txtSugerido = findViewById(R.id.txtSepMontoSugerido);
        String propiedad = intent.getStringExtra(EXTRA_PROPIEDAD);
        if (propiedad != null && propiedad.toLowerCase().contains("penthouse")) {
            txtSugerido.setText("Monto sugerido para este proyecto: $20,000.00");
        }
    }

    private void setupPagoChips() {
        TextView chipEfectivo       = findViewById(R.id.chipPagoEfectivo);
        TextView chipTransferencia  = findViewById(R.id.chipPagoTransferencia);
        TextView chipFinanciamiento = findViewById(R.id.chipPagoFinanciamiento);

        chipEfectivo.setOnClickListener(v -> {
            activePago = "efectivo";
            updatePagoChips(chipEfectivo, chipTransferencia, chipFinanciamiento);
        });
        chipTransferencia.setOnClickListener(v -> {
            activePago = "transferencia";
            updatePagoChips(chipTransferencia, chipEfectivo, chipFinanciamiento);
        });
        chipFinanciamiento.setOnClickListener(v -> {
            activePago = "financiamiento";
            updatePagoChips(chipFinanciamiento, chipEfectivo, chipTransferencia);
        });
    }

    private void updatePagoChips(TextView active, TextView... inactive) {
        active.setBackgroundResource(R.drawable.as_chip_dark);
        active.setTextColor(Color.WHITE);
        for (TextView chip : inactive) {
            chip.setBackgroundResource(R.drawable.as_chip_light);
            chip.setTextColor(Color.parseColor("#746D4A"));
        }
    }

    private void setupActions() {
        findViewById(R.id.btnConfirmarRegistroSeparacion)
                .setOnClickListener(v -> openScreen(AsesorSolicitudSeparacionActivity.class));

        findViewById(R.id.btnCancelarRegistroSeparacion)
                .setOnClickListener(v -> {
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                });
    }
}
