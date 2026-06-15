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

public class UsuarioCitaConfirmacionActivity extends AppCompatActivity {

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();
    private FirebaseAppointmentRepository.AppointmentDraft pendingDraft;
    private boolean appointmentSaved;
    private View goActivity;
    private View continueExplore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_confirmacion);
        applyInsets();
        bindData();
        setupActions();
        saveAppointment();
    }

    private void bindData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String propertyTitle = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_PROPERTY_TITLE);
        String propertyLocation = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_PROPERTY_LOCATION);
        String date = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_DATE);
        String time = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_TIME);
        String note = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_NOTE);

        bindText(R.id.tvAppointmentConfirmPropertyTitle, propertyTitle);
        bindText(R.id.tvAppointmentConfirmPropertyLocation, propertyLocation);
        bindText(R.id.tvAppointmentConfirmDate, date);
        bindText(R.id.tvAppointmentConfirmTime, time);
        bindText(R.id.tvAppointmentConfirmContact, intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_CONTACT));
        bindText(R.id.tvAppointmentConfirmNote, note);

        AuthSessionManager session = new AuthSessionManager(this);
        pendingDraft = new FirebaseAppointmentRepository.AppointmentDraft();
        pendingDraft.clienteId = session.getUserId();
        pendingDraft.clienteNombre = session.getUserName();
        pendingDraft.asesorId = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_ADVISOR_ID);
        pendingDraft.asesorNombre = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_ADVISOR_NAME);
        pendingDraft.propertyId = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_PROPERTY_ID);
        pendingDraft.inmuebleNombre = propertyTitle;
        pendingDraft.proyectoNombre = propertyTitle;
        pendingDraft.fechaISO = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_DATE_ISO);
        pendingDraft.fechaTexto = date;
        pendingDraft.hora = time;
        pendingDraft.meetingPoint = "Lobby principal - " + (propertyLocation != null ? propertyLocation : "");
        pendingDraft.nota = note;
        pendingDraft.imageKey = "user_featured_house";
    }

    private void saveAppointment() {
        if (pendingDraft == null || isEmpty(pendingDraft.clienteId) || isEmpty(pendingDraft.asesorId)
                || isEmpty(pendingDraft.fechaISO) || isEmpty(pendingDraft.hora)) {
            Toast.makeText(this, "No se pudo preparar la cita. Vuelve a elegir horario.", Toast.LENGTH_LONG).show();
            setActionsEnabled(false);
            return;
        }
        setActionsEnabled(false);
        appointmentRepository.reserveAppointment(pendingDraft, new FirebaseAppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(String citaId) {
                appointmentSaved = true;
                setActionsEnabled(true);
                Toast.makeText(UsuarioCitaConfirmacionActivity.this, "Cita confirmada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                appointmentSaved = false;
                setActionsEnabled(false);
                Toast.makeText(UsuarioCitaConfirmacionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null && value != null && !value.trim().isEmpty()) {
            view.setText(value);
        }
    }

    private void setupActions() {
        goActivity = findViewById(R.id.btnGoToActivityFromAppointment);
        if (goActivity != null) {
            goActivity.setOnClickListener(v -> {
                if (!appointmentSaved) {
                    Toast.makeText(this, "Espera a que la cita se confirme.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        continueExplore = findViewById(R.id.btnContinueExploreFromAppointment);
        if (continueExplore != null) {
            continueExplore.setOnClickListener(v -> {
                if (!appointmentSaved) {
                    finish();
                    return;
                }
                Intent intent = new Intent(this, UsuarioHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setActionsEnabled(boolean enabled) {
        if (goActivity != null) {
            goActivity.setEnabled(enabled);
        }
        if (continueExplore != null) {
            continueExplore.setEnabled(true);
        }
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.appointmentConfirmRoot);
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

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
