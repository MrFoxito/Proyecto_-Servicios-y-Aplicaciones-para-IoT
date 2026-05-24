package com.example.proyecto_iot.usuario;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;

import java.util.Calendar;
import java.util.Locale;

public class UsuarioAgendarCitaActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_TITLE    = "extra_property_title";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";
    public static final String EXTRA_APPOINTMENT_DATE    = "extra_appointment_date";
    public static final String EXTRA_APPOINTMENT_TIME    = "extra_appointment_time";
    public static final String EXTRA_APPOINTMENT_CONTACT = "extra_appointment_contact";
    public static final String EXTRA_APPOINTMENT_NOTE    = "extra_appointment_note";

    // Meses en español para mostrar en el campo
    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private EditText inputDate;
    private EditText inputTime;
    private EditText inputContact;
    private EditText inputNote;

    // Valores seleccionados — se guardan separados para consistencia
    private int selectedYear  = -1;
    private int selectedMonth = -1; // 0-based
    private int selectedDay   = -1;
    private int selectedHour  = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_agendar_cita);
        applyInsets();
        bindViews();
        bindPropertyData();
        prefillContact();
        setupDatePicker();
        setupTimePicker();
        setupActions();
    }

    private void bindViews() {
        inputDate    = findViewById(R.id.inputAppointmentDate);
        inputTime    = findViewById(R.id.inputAppointmentTime);
        inputContact = findViewById(R.id.inputAppointmentContact);
        inputNote    = findViewById(R.id.inputAppointmentNote);
    }

    private void bindPropertyData() {
        TextView title    = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView location = findViewById(R.id.tvAppointmentPropertyLocation);
        Intent intent = getIntent();
        if (intent == null) return;

        String propertyTitle    = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String propertyLocation = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);

        if (title != null && !TextUtils.isEmpty(propertyTitle)) {
            title.setText(propertyTitle);
        }
        if (location != null && !TextUtils.isEmpty(propertyLocation)) {
            location.setText(propertyLocation);
        }
    }

    /** Pre-llena el campo contacto con el teléfono del usuario logueado */
    private void prefillContact() {
        if (inputContact == null) return;
        String phone = new AuthSessionManager(this).getUserPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            inputContact.setText(phone.trim());
        }
    }

    private void setupDatePicker() {
        if (inputDate == null) return;
        inputDate.setOnClickListener(v -> showDatePicker());
        inputDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePicker();
        });
    }

    private void setupTimePicker() {
        if (inputTime == null) return;
        inputTime.setOnClickListener(v -> showTimePicker());
        inputTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePicker();
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();

        // Si ya había una fecha seleccionada, abre en esa fecha; si no, en hoy
        int initYear  = selectedYear  != -1 ? selectedYear  : today.get(Calendar.YEAR);
        int initMonth = selectedMonth != -1 ? selectedMonth : today.get(Calendar.MONTH);
        int initDay   = selectedDay   != -1 ? selectedDay   : today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedYear  = year;
                    selectedMonth = month;
                    selectedDay   = dayOfMonth;
                    // Formato legible: "26 Oct 2026"
                    inputDate.setText(String.format(Locale.getDefault(),
                            "%d %s %d", dayOfMonth, MESES[month], year));
                },
                initYear, initMonth, initDay
        );

        // No permitir fechas pasadas — mínimo hoy
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());

        // No permitir fechas más de 1 año en el futuro
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        dialog.show();
    }

    private void showTimePicker() {
        // Si ya había hora seleccionada, abre en esa; si no, en la hora actual
        Calendar now = Calendar.getInstance();
        int initHour   = selectedHour   != -1 ? selectedHour   : now.get(Calendar.HOUR_OF_DAY);
        int initMinute = selectedMinute != -1 ? selectedMinute : 0;

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour   = hourOfDay;
                    selectedMinute = minute;
                    // Formato 12h con AM/PM: "11:30 AM"
                    String amPm = hourOfDay < 12 ? "AM" : "PM";
                    int hour12  = hourOfDay % 12;
                    if (hour12 == 0) hour12 = 12;
                    inputTime.setText(String.format(Locale.getDefault(),
                            "%d:%02d %s", hour12, minute, amPm));
                },
                initHour, initMinute,
                false // false = formato 12h con AM/PM
        );
        dialog.show();
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
        // Validar fecha
        if (selectedYear == -1) {
            Toast.makeText(this, "Selecciona una fecha para la visita", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar hora
        if (selectedHour == -1) {
            Toast.makeText(this, "Selecciona una hora para la visita", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contacto — mínimo 7 caracteres
        String contact = inputContact != null
                ? inputContact.getText().toString().trim() : "";
        if (contact.length() < 7) {
            Toast.makeText(this, "Ingresa un número de contacto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String date  = inputDate.getText().toString().trim();
        String time  = inputTime.getText().toString().trim();
        String note  = inputNote != null ? inputNote.getText().toString().trim() : "";

        TextView titleView    = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView locationView = findViewById(R.id.tvAppointmentPropertyLocation);

        Intent intent = new Intent(this, UsuarioCitaConfirmacionActivity.class);
        intent.putExtra(EXTRA_PROPERTY_TITLE,
                titleView != null ? titleView.getText().toString() : "");
        intent.putExtra(EXTRA_PROPERTY_LOCATION,
                locationView != null ? locationView.getText().toString() : "");
        intent.putExtra(EXTRA_APPOINTMENT_DATE,    date);
        intent.putExtra(EXTRA_APPOINTMENT_TIME,    time);
        intent.putExtra(EXTRA_APPOINTMENT_CONTACT, contact);
        intent.putExtra(EXTRA_APPOINTMENT_NOTE,    note);
        startActivity(intent);
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.appointmentRoot);
        if (root == null) return;

        final int left   = root.getPaddingLeft();
        final int top    = root.getPaddingTop();
        final int right  = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top,
                    right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}

