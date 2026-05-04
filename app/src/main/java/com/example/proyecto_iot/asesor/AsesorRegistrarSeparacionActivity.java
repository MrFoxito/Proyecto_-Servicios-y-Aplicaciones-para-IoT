package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
        TextView txtProyecto      = findViewById(R.id.txtSepProyecto);
        TextView badgeProyecto    = findViewById(R.id.badgeSepProyectoVinculado);
        if (fromCita) {
            txtProyecto.setText(proyecto);
            txtProyecto.setTextColor(Color.parseColor("#2F383F"));
            badgeProyecto.setVisibility(View.VISIBLE);
        } else {
            txtProyecto.setText("Buscar proyecto por nombre o ID...");
            txtProyecto.setTextColor(Color.parseColor("#A0A6AC"));
            badgeProyecto.setVisibility(View.GONE);
        }

        // ── Campo cliente ──
        TextView txtCliente   = findViewById(R.id.txtSepCliente);
        TextView badgeCliente = findViewById(R.id.badgeSepClienteVinculado);
        if (fromCita) {
            txtCliente.setText(cliente);
            txtCliente.setTextColor(Color.parseColor("#2F383F"));
            badgeCliente.setVisibility(View.VISIBLE);
        } else {
            txtCliente.setText("Buscar cliente por nombre o ID...");
            txtCliente.setTextColor(Color.parseColor("#A0A6AC"));
            badgeCliente.setVisibility(View.GONE);
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
