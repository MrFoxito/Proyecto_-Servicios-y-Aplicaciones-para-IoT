package com.example.proyecto_iot.usuario;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UsuarioAgendarCitaActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "extra_property_id";
    public static final String EXTRA_PROPERTY_TITLE = "extra_property_title";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";
    public static final String EXTRA_APPOINTMENT_DATE = "extra_appointment_date";
    public static final String EXTRA_APPOINTMENT_DATE_ISO = "extra_appointment_date_iso";
    public static final String EXTRA_APPOINTMENT_TIME = "extra_appointment_time";
    public static final String EXTRA_APPOINTMENT_CONTACT = "extra_appointment_contact";
    public static final String EXTRA_APPOINTMENT_NOTE = "extra_appointment_note";
    public static final String EXTRA_ADVISOR_ID = "extra_advisor_id";
    public static final String EXTRA_ADVISOR_NAME = "extra_advisor_name";

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();

    private EditText inputDate;
    private EditText inputTime;
    private EditText inputContact;
    private EditText inputNote;
    private int selectedYear = -1;
    private int selectedMonth = -1;
    private int selectedDay = -1;
    private int selectedHour = -1;
    private int selectedMinute = -1;
    private String selectedDateIso = "";
    private String propertyId = "";
    private FirebaseAppointmentRepository.Advisor selectedAdvisor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_agendar_cita);
        applyInsets();
        bindViews();
        bindPropertyData();
        prefillContact();
        loadDefaultAdvisor();
        setupDatePicker();
        setupTimePicker();
        setupActions();
    }

    private void bindViews() {
        inputDate = findViewById(R.id.inputAppointmentDate);
        inputTime = findViewById(R.id.inputAppointmentTime);
        inputContact = findViewById(R.id.inputAppointmentContact);
        inputNote = findViewById(R.id.inputAppointmentNote);
    }

    private void bindPropertyData() {
        TextView title = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView location = findViewById(R.id.tvAppointmentPropertyLocation);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        propertyId = valueOr(intent.getStringExtra(EXTRA_PROPERTY_ID));
        String propertyTitle = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String propertyLocation = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);

        if (title != null && !TextUtils.isEmpty(propertyTitle)) {
            title.setText(propertyTitle);
        }
        if (location != null && !TextUtils.isEmpty(propertyLocation)) {
            location.setText(propertyLocation);
        }
    }

    private void prefillContact() {
        if (inputContact == null) {
            return;
        }
        String phone = AuthSessionManager.getInstance(this).getUserPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            inputContact.setText(phone.trim());
        }
    }

    private void setupDatePicker() {
        if (inputDate == null) {
            return;
        }
        inputDate.setOnClickListener(v -> showDatePicker());
        inputDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });
    }

    private void setupTimePicker() {
        if (inputTime == null) {
            return;
        }
        inputTime.setOnClickListener(v -> showTimePicker());
        inputTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showTimePicker();
            }
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        int initYear = selectedYear != -1 ? selectedYear : today.get(Calendar.YEAR);
        int initMonth = selectedMonth != -1 ? selectedMonth : today.get(Calendar.MONTH);
        int initDay = selectedDay != -1 ? selectedDay : today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;
                    selectedHour = -1;
                    selectedMinute = -1;
                    selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    inputDate.setText(String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, MESES[month], year));
                    inputTime.setText("");
                },
                initYear, initMonth, initDay
        );
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.show();
    }

    private void showTimePicker() {
        if (selectedAdvisor == null) {
            Toast.makeText(this, "Cargando asesor disponible. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            loadDefaultAdvisor();
            return;
        }
        if (selectedDateIso.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha antes de elegir la hora", Toast.LENGTH_SHORT).show();
            return;
        }
        appointmentRepository.getAvailableSlots(selectedAdvisor.uid, propertyId, selectedDateIso, new FirebaseAppointmentRepository.AvailableSlotsCallback() {
            @Override
            public void onSuccess(List<String> availableSlotKeys, FirebaseAppointmentRepository.Availability availability) {
                List<String> labels = new ArrayList<>();
                List<String> values = new ArrayList<>();
                for (String slot : availableSlotKeys) {
                    values.add(slot);
                    labels.add(FirebaseAppointmentRepository.displayTime(slot));
                }
                if (values.isEmpty()) {
                    Toast.makeText(UsuarioAgendarCitaActivity.this, "No hay horarios disponibles para esa fecha o el asesor no atiende ese dia.", Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(UsuarioAgendarCitaActivity.this)
                        .setTitle("Horarios disponibles")
                        .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                            String slot = values.get(which);
                            selectedHour = Integer.parseInt(slot.substring(0, 2));
                            selectedMinute = 0;
                            inputTime.setText(labels.get(which));
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioAgendarCitaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
        if (selectedYear == -1 || selectedDateIso.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha para la visita", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedHour == -1) {
            Toast.makeText(this, "Selecciona una hora disponible para la visita", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAdvisor == null) {
            Toast.makeText(this, "No se pudo asignar asesor a la cita", Toast.LENGTH_LONG).show();
            return;
        }

        String contact = inputContact != null ? inputContact.getText().toString().trim() : "";
        if (contact.length() < 7) {
            Toast.makeText(this, "Ingresa un numero de contacto valido", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView titleView = findViewById(R.id.tvAppointmentPropertyTitle);
        TextView locationView = findViewById(R.id.tvAppointmentPropertyLocation);

        Intent intent = new Intent(this, UsuarioCitaConfirmacionActivity.class);
        intent.putExtra(EXTRA_PROPERTY_ID, propertyId);
        intent.putExtra(EXTRA_PROPERTY_TITLE, titleView != null ? titleView.getText().toString() : "");
        intent.putExtra(EXTRA_PROPERTY_LOCATION, locationView != null ? locationView.getText().toString() : "");
        intent.putExtra(EXTRA_APPOINTMENT_DATE, inputDate.getText().toString().trim());
        intent.putExtra(EXTRA_APPOINTMENT_DATE_ISO, selectedDateIso);
        intent.putExtra(EXTRA_APPOINTMENT_TIME, inputTime.getText().toString().trim());
        intent.putExtra(EXTRA_APPOINTMENT_CONTACT, contact);
        intent.putExtra(EXTRA_APPOINTMENT_NOTE, inputNote != null ? inputNote.getText().toString().trim() : "");
        intent.putExtra(EXTRA_ADVISOR_ID, selectedAdvisor.uid);
        intent.putExtra(EXTRA_ADVISOR_NAME, selectedAdvisor.name);
        startActivity(intent);
    }

    private void loadDefaultAdvisor() {
        appointmentRepository.getAdvisorForProject(propertyId, new FirebaseAppointmentRepository.AdvisorCallback() {
            @Override
            public void onSuccess(FirebaseAppointmentRepository.Advisor advisor) {
                selectedAdvisor = advisor;
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioAgendarCitaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
