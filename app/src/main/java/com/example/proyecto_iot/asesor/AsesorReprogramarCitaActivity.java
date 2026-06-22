package com.example.proyecto_iot.asesor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();

    private EditText editNuevaFecha;
    private EditText editNuevaHora;
    private EditText editMotivo;
    private String citaId = "";
    private String asesorId = "";
    private String propertyId = "";
    private String selectedDateIso = "";
    private String selectedDateText = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);

        setupBackButton();
        bindViews();
        bindIntent();
        setupPickers();
        findViewById(R.id.btnConfirmarReprogramacion).setOnClickListener(v -> confirmarReprogramacion());
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void bindViews() {
        editNuevaFecha = findViewById(R.id.editNuevaFecha);
        editNuevaHora = findViewById(R.id.editNuevaHora);
        editMotivo = findViewById(R.id.editMotivoReprogramacion);
    }

    private void bindIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        citaId = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID));
        asesorId = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_ASESOR_ID));
        propertyId = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_PROPERTY_ID));
        selectedDateIso = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_FECHA));
        selectedDateText = displayDate(selectedDateIso);
        selectedTime = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_HORA));

        TextView title = findViewById(R.id.titleReprogramar);
        if (title != null) {
            title.setText("Reprogramar Cita");
        }
        if (editNuevaFecha != null) {
            editNuevaFecha.setText(selectedDateText);
        }
        if (editNuevaHora != null) {
            editNuevaHora.setText(selectedTime);
        }
    }

    private void setupPickers() {
        if (editNuevaFecha != null) {
            editNuevaFecha.setFocusable(false);
            editNuevaFecha.setOnClickListener(v -> showDatePicker());
        }
        if (editNuevaHora != null) {
            editNuevaHora.setFocusable(false);
            editNuevaHora.setOnClickListener(v -> showAvailableTimes());
        }
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar initial = Calendar.getInstance();
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            initial.setTime(format.parse(selectedDateIso));
        } catch (Exception ignored) {}

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    selectedDateText = String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, MESES[month], year);
                    selectedTime = "";
                    editNuevaFecha.setText(selectedDateText);
                    editNuevaHora.setText("");
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.show();
    }

    private void showAvailableTimes() {
        if (TextUtils.isEmpty(asesorId)) {
            Toast.makeText(this, "No se encontro el asesor de esta cita.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDateIso)) {
            Toast.makeText(this, "Selecciona una fecha primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        appointmentRepository.getAvailableSlots(asesorId, propertyId, selectedDateIso, new FirebaseAppointmentRepository.AvailableSlotsCallback() {
            @Override
            public void onSuccess(List<String> availableSlotKeys, FirebaseAppointmentRepository.Availability availability) {
                List<String> labels = new ArrayList<>();
                List<String> values = new ArrayList<>();
                for (String slot : availableSlotKeys) {
                    labels.add(FirebaseAppointmentRepository.displayTime(slot));
                    values.add(slot);
                }
                if (values.isEmpty()) {
                    Toast.makeText(AsesorReprogramarCitaActivity.this, "No hay horarios disponibles para esa fecha.", Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(AsesorReprogramarCitaActivity.this)
                        .setTitle("Nuevo horario")
                        .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                            selectedTime = labels.get(which);
                            editNuevaHora.setText(selectedTime);
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorReprogramarCitaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarReprogramacion() {
        if (TextUtils.isEmpty(citaId)) {
            Toast.makeText(this, "No se encontro la cita a reprogramar.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDateIso) || TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Selecciona nueva fecha y hora.", Toast.LENGTH_SHORT).show();
            return;
        }
        String motivo = editMotivo != null ? editMotivo.getText().toString().trim() : "";
        if (motivo.length() < 4) {
            Toast.makeText(this, "Ingresa un motivo de reprogramacion.", Toast.LENGTH_SHORT).show();
            return;
        }

        appointmentRepository.rescheduleAppointment(citaId, selectedDateIso, selectedDateText, selectedTime, motivo,
                new FirebaseAppointmentRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AsesorReprogramarCitaActivity.this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AsesorReprogramarCitaActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String displayDate(String iso) {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(input.parse(iso));
            return String.format(Locale.getDefault(), "%d %s %d",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    MESES[calendar.get(Calendar.MONTH)],
                    calendar.get(Calendar.YEAR));
        } catch (Exception ignored) {
            return iso;
        }
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
