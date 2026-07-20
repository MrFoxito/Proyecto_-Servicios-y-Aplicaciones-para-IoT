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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private boolean isSubmitting;

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
        if (isSubmitting) return;
        if (!ProjectBusinessRules.canCreateSeparation(propertyStatus)) {
            Toast.makeText(this, "Este proyecto esta en planos. Aun no permite separacion.", Toast.LENGTH_LONG).show();
            return;
        }
        AuthSessionManager session = AuthSessionManager.getInstance(this);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String clienteId = firebaseUser == null ? "" : firebaseUser.getUid();
        if (clienteId.isEmpty() || !clienteId.equals(session.getUid())) {
            Toast.makeText(this, "Tu sesiÃ³n no es vÃ¡lida. Inicia sesiÃ³n nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }
        if (propertyId == null || propertyId.trim().isEmpty()) {
            Toast.makeText(this, "No se recibiÃ³ un proyecto vÃ¡lido.", Toast.LENGTH_LONG).show();
            return;
        }
        setSubmitting(true);

        appointmentRepository.getAdvisorsForProject(propertyId, new FirebaseAppointmentRepository.AdvisorsCallback() {
            @Override
            public void onSuccess(java.util.List<FirebaseAppointmentRepository.Advisor> advisors) {
                if (isFinishing() || isDestroyed()) return;
                if (advisors == null || advisors.isEmpty()) {
                    setSubmitting(false);
                    Toast.makeText(UsuarioReservaPagoActivity.this,
                            "Este proyecto no tiene un asesor activo para registrar la separaciÃ³n.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (advisors.size() == 1) {
                    createSeparationForAdvisor(advisors.get(0), session, clienteId);
                    return;
                }
                String[] labels = new String[advisors.size()];
                for (int i = 0; i < advisors.size(); i++) labels[i] = advisors.get(i).name;
                new android.app.AlertDialog.Builder(UsuarioReservaPagoActivity.this)
                        .setTitle("Selecciona un asesor")
                        .setItems(labels, (dialog, which) ->
                                createSeparationForAdvisor(advisors.get(which), session, clienteId))
                        .setOnCancelListener(dialog -> setSubmitting(false))
                        .show();
            }

            @Override
            public void onError(String message) {
                setSubmitting(false);
                Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createSeparationForAdvisor(
            FirebaseAppointmentRepository.Advisor advisor,
            AuthSessionManager session,
            String clienteId
    ) {
        FirebaseSeparationRepository.SeparationDraft draft = new FirebaseSeparationRepository.SeparationDraft();
        draft.clienteId = clienteId;
        draft.clienteNombre = session.getUserName();
        draft.asesorId = advisor.uid;
        draft.asesorNombre = advisor.name;
        draft.propertyId = propertyId;
        draft.projectId = propertyId;
        draft.proyectoId = propertyId;
        draft.assignmentId = advisor.assignmentId;
        draft.inmuebleNombre = propertyTitle;
        draft.montoTexto = propertyPrice;
        draft.estado = "Pagada";
        draft.createdByRole = "cliente";
        createSeparationAndOpenActivity(draft);
    }

    private void createSeparationAndOpenActivity(FirebaseSeparationRepository.SeparationDraft draft) {
        separationRepository.createSeparation(draft, new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                Toast.makeText(UsuarioReservaPagoActivity.this, "Pago procesado correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UsuarioReservaPagoActivity.this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                setSubmitting(false);
                Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        View pay = findViewById(R.id.btnProcederPago);
        if (pay != null) pay.setEnabled(!submitting);
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
