package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;

public class UsuarioAgendarCitaActivity extends AppCompatActivity {
    public static final String EXTRA_PROPERTY_TITLE = "extra_property_title";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";

    public static final String EXTRA_APPOINTMENT_DATE = "extra_appointment_date";
    public static final String EXTRA_APPOINTMENT_TIME = "extra_appointment_time";
    public static final String EXTRA_APPOINTMENT_CONTACT = "extra_appointment_contact";
    public static final String EXTRA_APPOINTMENT_NOTE = "extra_appointment_note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_agendar_cita);
        applyInsets();
        bindPropertyData();
        setupActions();
    }

    private void bindPropertyData() {
        TextView title = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView location = findViewById(R.id.tvAppointmentPropertyLocation);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String propertyTitle = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String propertyLocation = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);

        if (title != null && !TextUtils.isEmpty(propertyTitle)) {
            title.setText(propertyTitle);
        }
        if (location != null && !TextUtils.isEmpty(propertyLocation)) {
            location.setText(propertyLocation);
        }
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackAppointment);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View confirm = findViewById(R.id.btnConfirmAppointment);
        if (confirm != null) {
            confirm.setOnClickListener(v -> submitAppointment());
        }
    }

    private void submitAppointment() {
        EditText dateInput = findViewById(R.id.inputAppointmentDate);
        EditText timeInput = findViewById(R.id.inputAppointmentTime);
        EditText contactInput = findViewById(R.id.inputAppointmentContact);
        EditText noteInput = findViewById(R.id.inputAppointmentNote);
        TextView title = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView location = findViewById(R.id.tvAppointmentPropertyLocation);

        if (dateInput == null || timeInput == null || contactInput == null) {
            return;
        }

        String date = readInput(dateInput);
        String time = readInput(timeInput);
        String contact = readInput(contactInput);
        String note = noteInput != null ? readInput(noteInput) : "";

        if (date.isEmpty() || time.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, R.string.appointment_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, UsuarioCitaConfirmacionActivity.class);
        intent.putExtra(EXTRA_PROPERTY_TITLE, title != null ? title.getText().toString() : "");
        intent.putExtra(EXTRA_PROPERTY_LOCATION, location != null ? location.getText().toString() : "");
        intent.putExtra(EXTRA_APPOINTMENT_DATE, date);
        intent.putExtra(EXTRA_APPOINTMENT_TIME, time);
        intent.putExtra(EXTRA_APPOINTMENT_CONTACT, contact);
        intent.putExtra(EXTRA_APPOINTMENT_NOTE, note);
        startActivity(intent);
    }

    private String readInput(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.appointmentRoot);
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

