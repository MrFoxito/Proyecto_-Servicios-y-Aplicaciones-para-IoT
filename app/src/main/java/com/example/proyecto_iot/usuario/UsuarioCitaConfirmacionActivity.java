package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

public class UsuarioCitaConfirmacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_confirmacion);
        applyInsets();
        bindData();
        setupActions();
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

        new LocalSchemaStorage(this).addCita(
                new AuthSessionManager(this).getUserId(),
                new AuthSessionManager(this).getUserName(),
                propertyTitle,
                date,
                time,
                "Lobby principal - " + (propertyLocation != null ? propertyLocation : ""),
                note,
                "Elena Valdes",
                "user_featured_house"
        );
    }

    private void bindText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null && value != null && !value.trim().isEmpty()) {
            view.setText(value);
        }
    }

    private void setupActions() {
        View goActivity = findViewById(R.id.btnGoToActivityFromAppointment);
        if (goActivity != null) {
            goActivity.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View continueExplore = findViewById(R.id.btnContinueExploreFromAppointment);
        if (continueExplore != null) {
            continueExplore.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
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
}

