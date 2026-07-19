package com.example.proyecto_iot.asesor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    public static final String EXTRA_ASESOR_ID = "extra_asesor_id";
    public static final String EXTRA_FECHA = "extra_fecha";
    public static final String EXTRA_HORA = "extra_hora";

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private EditText editNuevaFecha;
    private EditText editNuevaHora;
    private EditText editMotivo;
    private String citaId = "";
    private String asesorId = "";
    private String propertyId = "";
    private String selectedDateIso = "";
    private String selectedDateText = "";
    // Always persisted in HH:mm; the EditText only shows a localized label.
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
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        editNuevaFecha = findViewById(R.id.editNuevaFecha);
        editNuevaHora = findViewById(R.id.editNuevaHora);
        editMotivo = findViewById(R.id.editMotivoReprogramacion);
    }

    private void bindIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        citaId = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID));
        asesorId = valueOr(intent.getStringExtra(EXTRA_ASESOR_ID));
        selectedDateIso = valueOr(intent.getStringExtra(EXTRA_FECHA));
        selectedDateText = displayDate(selectedDateIso);
        selectedTime = valueOr(intent.getStringExtra(EXTRA_HORA));
        if (asesorId.isEmpty()) asesorId = AuthSessionManager.getInstance(this).getUid();

        if (editNuevaFecha != null) editNuevaFecha.setText(selectedDateText);
        if (editNuevaHora != null && !selectedTime.isEmpty()) {
            editNuevaHora.setText(FirebaseAppointmentRepository.displayTime(selectedTime));
        }
        TextView title = findViewById(R.id.titleReprogramar);
        if (title != null) title.setText("Reprogramar Cita");
        loadAppointmentContext();
    }

    private void loadAppointmentContext() {
        if (citaId.isEmpty()) return;
        firestore.collection("citas").document(citaId).get()
                .addOnSuccessListener(cita -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (!cita.exists()) {
                        Toast.makeText(this, "La cita ya no existe.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    asesorId = valueOr(cita.getString("asesorId"));
                    propertyId = valueOr(cita.getString("propertyId"));
                    if (selectedDateIso.isEmpty()) {
                        selectedDateIso = valueOr(cita.getString("fechaISO"));
                        selectedDateText = valueOr(cita.getString("fechaTexto"));
                        if (selectedDateText.isEmpty()) selectedDateText = displayDate(selectedDateIso);
                        if (editNuevaFecha != null) editNuevaFecha.setText(selectedDateText);
                    }
                    if (selectedTime.isEmpty()) {
                        selectedTime = valueOr(cita.getString("hora"));
                        if (editNuevaHora != null) editNuevaHora.setText(FirebaseAppointmentRepository.displayTime(selectedTime));
                    }
                })
                .addOnFailureListener(error -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "No se pudo cargar la cita: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupPickers() {
        editNuevaFecha.setFocusable(false);
        editNuevaFecha.setOnClickListener(v -> showDatePicker());
        editNuevaHora.setFocusable(false);
        editNuevaHora.setOnClickListener(v -> showAvailableTimes());
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar initial = Calendar.getInstance();
        try {
            initial.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDateIso));
        } catch (Exception ignored) { }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            selectedDateText = String.format(Locale.getDefault(), "%d %s %d", day, MESES[month], year);
            selectedTime = "";
            editNuevaFecha.setText(selectedDateText);
            editNuevaHora.setText("");
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.show();
    }

    private void showAvailableTimes() {
        if (asesorId.isEmpty() || propertyId.isEmpty()) {
            Toast.makeText(this, "La cita todavia se esta cargando.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateIso.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha primero.", Toast.LENGTH_SHORT).show();
            return;
        }
        appointmentRepository.getAvailableSlots(asesorId, propertyId, selectedDateIso,
                new FirebaseAppointmentRepository.AvailableSlotsCallback() {
                    @Override
                    public void onSuccess(List<String> slots, FirebaseAppointmentRepository.Availability availability) {
                        if (slots.isEmpty()) {
                            Toast.makeText(AsesorReprogramarCitaActivity.this,
                                    "No hay horarios disponibles para esa fecha.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<String> labels = new ArrayList<>();
                        for (String slot : slots) labels.add(FirebaseAppointmentRepository.displayTime(slot));
                        new AlertDialog.Builder(AsesorReprogramarCitaActivity.this)
                                .setTitle("Nuevo horario")
                                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                                    selectedTime = slots.get(which);
                                    editNuevaHora.setText(labels.get(which));
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
        if (TextUtils.isEmpty(citaId) || TextUtils.isEmpty(selectedDateIso) || TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Selecciona nueva fecha y hora.", Toast.LENGTH_SHORT).show();
            return;
        }
        String motivo = editMotivo.getText().toString().trim();
        if (motivo.length() < 4) {
            Toast.makeText(this, "Ingresa un motivo de reprogramaciÃ³n.", Toast.LENGTH_SHORT).show();
            return;
        }
        appointmentRepository.rescheduleAppointment(citaId, selectedDateIso, selectedDateText,
                selectedTime, motivo, new FirebaseAppointmentRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AsesorReprogramarCitaActivity.this,
                                "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AsesorReprogramarCitaActivity.this,
                                "Error al reprogramar: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String displayDate(String iso) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso));
            return String.format(Locale.getDefault(), "%d %s %d", calendar.get(Calendar.DAY_OF_MONTH),
                    MESES[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR));
        } catch (Exception ignored) {
            return iso;
        }
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
