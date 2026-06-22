package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.ProjectBusinessRules;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;

public class UsuarioReservaPagoActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "extra_reserva_property_id";
    public static final String EXTRA_PROPERTY_TITLE = "extra_reserva_property_title";
    public static final String EXTRA_PROPERTY_PRICE = "extra_reserva_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_reserva_property_location";
    public static final String EXTRA_PROPERTY_STATUS = "extra_reserva_property_status";

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();
    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();

    private String propertyId;
    private String propertyTitle;
    private String propertyPrice;
    private String propertyStatus = ProjectBusinessRules.STATUS_PLANOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_reserva_pago);
        applyInsets();
        bindPropertyData();
        setupActions();
    }

    private void bindPropertyData() {
        Intent intent = getIntent();
        propertyId = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_ID) : null;
        propertyTitle = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_TITLE) : null;
        propertyPrice = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_PRICE) : null;
        propertyStatus = ProjectBusinessRules.normalizeStatus(intent != null ? intent.getStringExtra(EXTRA_PROPERTY_STATUS) : null);
        String propertyLocation = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_LOCATION) : null;

        if (propertyTitle == null || propertyTitle.isEmpty()) {
            propertyTitle = getString(R.string.reserva_title);
        }
        if (propertyPrice == null || propertyPrice.isEmpty()) {
            propertyPrice = getString(R.string.reserva_property_value);
        }

        TextView tvTitle = findViewById(R.id.tvReservaPropertyTitle);
        if (tvTitle != null) {
            tvTitle.setText(propertyTitle);
        }

        TextView tvPrice = findViewById(R.id.tvReservaPropertyValue);
        if (tvPrice != null) {
            tvPrice.setText(propertyPrice);
        }

        if (propertyLocation != null && !propertyLocation.isEmpty()) {
            TextView tvLocation = findViewById(R.id.tvReservaPropertyLocation);
            if (tvLocation != null) {
                tvLocation.setText(propertyLocation);
            }
        }
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackReservaPago);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View pay = findViewById(R.id.btnProcederPago);
        if (pay != null) {
            pay.setOnClickListener(v -> procesarPago());
        }
    }

    private void procesarPago() {
        if (!ProjectBusinessRules.canCreateSeparation(propertyStatus)) {
            Toast.makeText(this, "Este proyecto esta en planos. Aun no permite separacion.", Toast.LENGTH_LONG).show();
            return;
        }
        AuthSessionManager session = new AuthSessionManager(this);
        String clienteId = session.getUserId();

        appointmentRepository.getFirstActiveAdvisor(new FirebaseAppointmentRepository.AdvisorCallback() {
            @Override
            public void onSuccess(FirebaseAppointmentRepository.Advisor advisor) {
                FirebaseSeparationRepository.SeparationDraft draft = new FirebaseSeparationRepository.SeparationDraft();
                draft.clienteId = clienteId;
                draft.clienteNombre = session.getUserName();
                draft.asesorId = advisor.uid;
                draft.asesorNombre = advisor.name;
                draft.propertyId = propertyId;
                draft.inmuebleNombre = propertyTitle;
                draft.montoTexto = propertyPrice;
                draft.estado = "Pagada";
                draft.createdByRole = "cliente";
                createSeparationAndLocalActivity(draft, clienteId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createSeparationAndLocalActivity(FirebaseSeparationRepository.SeparationDraft draft, String clienteId) {
        separationRepository.createSeparation(draft, new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                LocalSchemaStorage storage = new LocalSchemaStorage(UsuarioReservaPagoActivity.this);
                String tramiteId = storage.addTramite(clienteId, propertyTitle, propertyPrice);
                storage.addHistorial(clienteId, propertyTitle, propertyPrice, tramiteId);
                Toast.makeText(UsuarioReservaPagoActivity.this, "Pago procesado correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UsuarioReservaPagoActivity.this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.reservaPagoRoot);
        if (root == null) {
            return;
        }
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
