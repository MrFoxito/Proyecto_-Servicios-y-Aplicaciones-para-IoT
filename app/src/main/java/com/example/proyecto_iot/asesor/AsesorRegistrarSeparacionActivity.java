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
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AsesorRegistrarSeparacionActivity extends BaseAsesorActivity {

    public static final String EXTRA_CLIENTE = "extra_cliente";
    public static final String EXTRA_CLIENTE_ID = "extra_cliente_id";
    public static final String EXTRA_PROPIEDAD = "extra_propiedad";
    public static final String EXTRA_PROJECT_ID = "extra_proyecto_id";
    public static final String EXTRA_TIPOLOGY_ID = "extra_tipologia_id";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_CITA_ID = "extra_cita_id";

    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
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

    private void prefillFromIntent() {
        Intent intent = getIntent();
        String cliente = intent.getStringExtra(EXTRA_CLIENTE);
        String proyecto = intent.getStringExtra(EXTRA_PROYECTO);
        String citaId = intent.getStringExtra(EXTRA_CITA_ID);

        boolean fromCita = cliente != null && proyecto != null;

        LinearLayout banner = findViewById(R.id.bannerCitaVinculada);
        TextView txtBannerDetalle = findViewById(R.id.txtBannerCitaDetalle);
        if (fromCita) {
            banner.setVisibility(View.VISIBLE);
            txtBannerDetalle.setText("Cita" + (citaId != null ? " #" + citaId : "") + " vinculada.");
        } else {
            banner.setVisibility(View.GONE);
        }

        TextView txtCitaId = findViewById(R.id.txtSepCitaId);
        if (citaId != null) {
            txtCitaId.setText(citaId);
        }

        AutoCompleteTextView txtProyecto = findViewById(R.id.txtSepProyecto);
        TextView badgeProyecto = findViewById(R.id.badgeSepProyectoVinculado);
        if (fromCita) {
            txtProyecto.setText(proyecto, false);
            txtProyecto.setEnabled(false);
            badgeProyecto.setVisibility(View.VISIBLE);
        } else {
            txtProyecto.setEnabled(true);
            badgeProyecto.setVisibility(View.GONE);
            String[] proyectosMocks = {"Inmobiliaria Horizonte", "Costa Moderna", "Torres del Bosque", "Residencial Alba"};
            txtProyecto.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proyectosMocks));
        }

        AutoCompleteTextView txtCliente = findViewById(R.id.txtSepCliente);
        TextView badgeCliente = findViewById(R.id.badgeSepClienteVinculado);
        if (fromCita) {
            txtCliente.setText(cliente, false);
            txtCliente.setEnabled(false);
            badgeCliente.setVisibility(View.VISIBLE);
        } else {
            txtCliente.setEnabled(true);
            badgeCliente.setVisibility(View.GONE);
            String[] clientesMocks = {"Alicia Velarde", "Julian Montgomery", "Carlos Ruiz", "Maria Fernanda"};
            txtCliente.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clientesMocks));
        }

        TextView txtSugerido = findViewById(R.id.txtSepMontoSugerido);
        String propiedad = intent.getStringExtra(EXTRA_PROPIEDAD);
        if (propiedad != null && propiedad.toLowerCase().contains("penthouse")) {
            txtSugerido.setText("Monto sugerido para este proyecto: $20,000.00");
        }
    }

    private void setupPagoChips() {
        TextView chipEfectivo = findViewById(R.id.chipPagoEfectivo);
        TextView chipTransferencia = findViewById(R.id.chipPagoTransferencia);
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
        findViewById(R.id.btnConfirmarRegistroSeparacion).setOnClickListener(v -> registrarSeparacion());

        findViewById(R.id.btnCancelarRegistroSeparacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void registrarSeparacion() {
        String asesorId = currentUid();
        if (asesorId.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa para registrar separacion.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = getIntent();
        AutoCompleteTextView clienteView = findViewById(R.id.txtSepCliente);
        AutoCompleteTextView proyectoView = findViewById(R.id.txtSepProyecto);
        EditText montoView = findViewById(R.id.txtSepMonto);

        FirebaseSeparationRepository.SeparationDraft draft = new FirebaseSeparationRepository.SeparationDraft();
        draft.clienteId = valueOr(intent.getStringExtra(EXTRA_CLIENTE_ID));
        draft.clienteNombre = clienteView != null ? clienteView.getText().toString() : valueOr(intent.getStringExtra(EXTRA_CLIENTE));
        draft.asesorId = asesorId;
        draft.asesorNombre = "";
        draft.citaId = valueOr(intent.getStringExtra(EXTRA_CITA_ID));
        draft.propertyId = valueOr(intent.getStringExtra(EXTRA_PROJECT_ID));
        draft.inmuebleNombre = proyectoView != null ? proyectoView.getText().toString() : valueOr(intent.getStringExtra(EXTRA_PROYECTO));
        draft.montoTexto = montoView != null ? montoView.getText().toString() : "";
        draft.estado = "Pendiente";
        draft.createdByRole = "asesor";

        separationRepository.createSeparation(draft, new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                openScreen(AsesorSolicitudSeparacionActivity.class);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorRegistrarSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
