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
import androidx.core.widget.NestedScrollView;

import com.example.proyecto_iot.R;

public class UsuarioCitaConfirmacionActivity extends AppCompatActivity {

    private View goActivity;
    private View continueExplore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_confirmacion);
        applyInsets();
        Intent intent = getIntent();
        if (intent == null || intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_ID) == null
                || intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_ID).trim().isEmpty()) {
            Toast.makeText(this, "No se encontró una cita confirmada.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        bindData();
        setupActions();
        setActionsEnabled(true);
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
                Intent intent = new Intent(this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        continueExplore = findViewById(R.id.btnContinueExploreFromAppointment);
        if (continueExplore != null) {
            continueExplore.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            continueExplore.setEnabled(enabled);
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
        NestedScrollView scroll = findViewById(R.id.appointmentConfirmScroll);
        View actionBar = findViewById(R.id.appointmentConfirmActionBar);
        final int scrollBottom = scroll != null ? scroll.getPaddingBottom() : 0;
        final int actionBarBottom = actionBar != null ? actionBar.getPaddingBottom() : 0;
        final NestedScrollView finalScroll = scroll;
        final View finalActionBar = actionBar;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int protectedBottom = Math.max(bars.bottom, ime.bottom);
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom);
            if (finalActionBar != null) {
                finalActionBar.setPadding(
                        finalActionBar.getPaddingLeft(),
                        finalActionBar.getPaddingTop(),
                        finalActionBar.getPaddingRight(),
                        actionBarBottom + protectedBottom
                );
            }
            if (finalScroll != null) {
                finalScroll.setPadding(
                        finalScroll.getPaddingLeft(),
                        finalScroll.getPaddingTop(),
                        finalScroll.getPaddingRight(),
                        scrollBottom + protectedBottom
                );
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

}
