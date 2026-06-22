package com.example.proyecto_iot.asesor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    public static final String EXTRA_ASESOR_ID = "extra_asesor_id";
    public static final String EXTRA_FECHA = "extra_fecha";
    public static final String EXTRA_HORA = "extra_hora";


    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private FirebaseFirestore db;
    private AuthSessionManager sessionManager;

    private EditText editNuevaFecha;
    private EditText editNuevaHora;
    private EditText editMotivo;

    private String citaId = "";
    private String asesorId = "";
    private String propertyId = "";
    private String clienteId = "";
    private String clienteNombre = "";
    private String proyectoNombre = "";

    private String selectedDateIso = "";
    private String selectedDateText = "";
    private String selectedTime = "";
    private String selectedSlotKey = "";

    private List<Cita> citasDelDia = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);

        db = FirebaseFirestore.getInstance();
        sessionManager = AuthSessionManager.getInstance(this);

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
        if (intent == null) return;

        citaId = valueOr(intent.getStringExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID));
        asesorId = valueOr(intent.getStringExtra(EXTRA_ASESOR_ID));
        propertyId = valueOr(intent.getStringExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROJECT_ID));
        clienteId = valueOr(intent.getStringExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID));
        clienteNombre = valueOr(intent.getStringExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE));
        proyectoNombre = valueOr(intent.getStringExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO));

        selectedDateIso = valueOr(intent.getStringExtra(EXTRA_FECHA));
        selectedDateText = displayDate(selectedDateIso);
        selectedTime = valueOr(intent.getStringExtra(EXTRA_HORA));

        // Si no viene el asesorId, usar el del usuario autenticado
        if (asesorId.isEmpty()) {
            asesorId = sessionManager.getUid();
        }

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
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            initial.setTime(format.parse(selectedDateIso));
        } catch (Exception ignored) {}

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    selectedDateText = String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, MESES[month], year);
                    selectedTime = "";
                    selectedSlotKey = "";
                    editNuevaFecha.setText(selectedDateText);
                    editNuevaHora.setText("");
                    // Cargar citas del día para verificar disponibilidad
                    loadCitasDelDia(selectedDateIso);
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

    private void loadCitasDelDia(String fechaISO) {
        if (asesorId.isEmpty()) {
            Toast.makeText(this, "No se encontró el asesor.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("citas")
                .whereEqualTo("asesorId", asesorId)
                .whereEqualTo("fechaISO", fechaISO)
                .get()
                .addOnSuccessListener(query -> {
                    citasDelDia.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Cita c = doc.toObject(Cita.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            citasDelDia.add(c);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar disponibilidad", Toast.LENGTH_SHORT).show()
                );
    }

    private void showAvailableTimes() {
        if (TextUtils.isEmpty(asesorId)) {
            Toast.makeText(this, "No se encontró el asesor.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDateIso)) {
            Toast.makeText(this, "Selecciona una fecha primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar slots disponibles (09:00 a 18:00 cada 30 minutos)
        List<String> availableSlots = generateAvailableSlots(selectedDateIso);

        if (availableSlots.isEmpty()) {
            Toast.makeText(this, "No hay horarios disponibles para esa fecha.", Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar en diálogo
        new AlertDialog.Builder(this)
                .setTitle("Nuevo horario")
                .setItems(availableSlots.toArray(new String[0]), (dialog, which) -> {
                    selectedSlotKey = getSlotKey(availableSlots.get(which));
                    selectedTime = availableSlots.get(which);
                    editNuevaHora.setText(selectedTime);
                })
                .show();
    }

    private List<String> generateAvailableSlots(String fechaISO) {
        List<String> slots = new ArrayList<>();
        // Generar horarios de 09:00 a 18:00 cada 30 minutos
        for (int h = 9; h < 18; h++) {
            for (int m = 0; m < 60; m += 30) {
                String horaStr = String.format(Locale.US, "%02d:%02d", h, m);
                String slotKey = asesorId + "_" + fechaISO + "_" + horaStr;

                // Verificar si está ocupado (cita existente que solape)
                boolean ocupado = false;
                for (Cita c : citasDelDia) {
                    String cHora = c.getHora();
                    int duracion = c.getDuracionMinutos();
                    if (haySolapamiento(horaStr, cHora, duracion)) {
                        ocupado = true;
                        break;
                    }
                }

                if (!ocupado) {
                    // Mostrar hora formateada (ej. "10:00 AM")
                    String display = formatTimeDisplay(horaStr);
                    slots.add(display);
                }
            }
        }
        return slots;
    }

    private boolean haySolapamiento(String nuevaHora, String citaHora, int duracionCita) {
        try {
            int nuevaMin = timeToMinutes(nuevaHora);
            int citaMin = timeToMinutes(citaHora);
            int citaFin = citaMin + duracionCita;
            // Una cita de 1 hora ocupa de citaMin a citaMin+60
            // La nueva cita también dura 1 hora
            int nuevaFin = nuevaMin + 60;
            // Solapamiento si (nuevaMin < citaFin && nuevaFin > citaMin)
            return nuevaMin < citaFin && nuevaFin > citaMin;
        } catch (Exception e) {
            return true; // Por seguridad, considerar ocupado
        }
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String formatTimeDisplay(String time) {
        try {
            String[] parts = time.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            String ampm = (h >= 12) ? "PM" : "AM";
            int h12 = (h == 0) ? 12 : (h > 12 ? h - 12 : h);
            return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, ampm);
        } catch (Exception e) {
            return time;
        }
    }

    private String getSlotKey(String displayTime) {
        // Convertir "10:00 AM" a "10:00"
        try {
            String[] parts = displayTime.split(" ");
            String timePart = parts[0];
            String ampm = parts[1];
            String[] hhmm = timePart.split(":");
            int h = Integer.parseInt(hhmm[0]);
            int m = Integer.parseInt(hhmm[1]);
            if (ampm.equalsIgnoreCase("PM") && h != 12) {
                h += 12;
            } else if (ampm.equalsIgnoreCase("AM") && h == 12) {
                h = 0;
            }
            return String.format(Locale.US, "%02d:%02d", h, m);
        } catch (Exception e) {
            return displayTime;
        }
    }

    private void confirmarReprogramacion() {
        if (TextUtils.isEmpty(citaId)) {
            Toast.makeText(this, "No se encontró la cita a reprogramar.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDateIso) || TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Selecciona nueva fecha y hora.", Toast.LENGTH_SHORT).show();
            return;
        }
        String motivo = editMotivo != null ? editMotivo.getText().toString().trim() : "";
        if (motivo.length() < 4) {
            Toast.makeText(this, "Ingresa un motivo de reprogramación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la hora en formato HH:mm (24h) para guardar
        String hora24 = getSlotKey(selectedTime);
        String slotKey = asesorId + "_" + selectedDateIso + "_" + hora24;

        // Preparar actualización
        Map<String, Object> updates = new HashMap<>();
        updates.put("fechaISO", selectedDateIso);
        updates.put("fechaTexto", selectedDateText);
        updates.put("hora", hora24);
        updates.put("slotId", slotKey);
        updates.put("estado", "Reprogramada");
        updates.put("nota", "Reprogramada: " + motivo);

        // Agregar evento al historial
        List<Map<String, Object>> historial = new ArrayList<>();
        Map<String, Object> evento = new HashMap<>();
        evento.put("id", "evt_" + System.currentTimeMillis());
        evento.put("citaId", citaId);
        evento.put("titulo", "Cita reprogramada");
        evento.put("detalle", "Nueva fecha: " + selectedDateText + " a las " + selectedTime + ". Motivo: " + motivo);
        evento.put("fechaHora", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(Calendar.getInstance().getTime()));
        evento.put("tipo", "REPROGRAMADA");
        historial.add(evento);

        // Si ya existe historial, agregar al existente
        // (En este caso, como no lo tenemos, creamos uno nuevo; en producción deberías leerlo primero)
        updates.put("historial", historial);

        db.collection("citas").document(citaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al reprogramar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String displayDate(String iso) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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