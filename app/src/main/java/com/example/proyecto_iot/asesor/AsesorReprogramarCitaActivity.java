package com.example.proyecto_iot.asesor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.entity.Cita;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    private FirebaseFirestore db;
    private AuthSessionManager sessionManager;
    private FirebaseAppointmentRepository repository;

    private String citaId = "";
    private Cita citaActual;

    private ImageView imgPropiedad;
    private TextView txtProyectoTag, txtPropiedadNombre, txtPropiedadUbicacion, txtCitaStatus;
    private TextView txtAvatarCliente, txtNombreCliente, txtInfoCliente;
    private TextView txtFechaActual, txtHoraActual;
    private EditText editNuevaFecha, editNuevaHora, editMotivo;

    private String selectedDateIso = "";
    private String selectedTime = "";
    private List<Cita> citasDelDia = new ArrayList<>();

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);

        db = FirebaseFirestore.getInstance();
        sessionManager = AuthSessionManager.getInstance(this);
        repository = new FirebaseAppointmentRepository();

        citaId = getIntent().getStringExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID);
        if (citaId == null || citaId.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió ID de cita", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadCitaData();
    }

    private void initViews() {
        imgPropiedad = findViewById(R.id.imgPropiedadReprogramar);
        txtProyectoTag = findViewById(R.id.txtProyectoTag);
        txtPropiedadNombre = findViewById(R.id.txtPropiedadNombre);
        txtPropiedadUbicacion = findViewById(R.id.txtPropiedadUbicacion);
        txtCitaStatus = findViewById(R.id.txtCitaStatus);

        txtAvatarCliente = findViewById(R.id.txtAvatarCliente);
        txtNombreCliente = findViewById(R.id.txtNombreCliente);
        txtInfoCliente = findViewById(R.id.txtInfoCliente);

        txtFechaActual = findViewById(R.id.txtFechaActualValue);
        txtHoraActual = findViewById(R.id.txtHoraActualValue);

        editNuevaFecha = findViewById(R.id.editNuevaFecha);
        editNuevaHora = findViewById(R.id.editNuevaHora);
        editMotivo = findViewById(R.id.editMotivoReprogramacion);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        editNuevaFecha.setOnClickListener(v -> showDatePicker());
        editNuevaHora.setOnClickListener(v -> showAvailableTimes());

        findViewById(R.id.btnConfirmarReprogramacion).setOnClickListener(v -> confirmarReprogramacion());
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> finish());
    }

    private void loadCitaData() {
        db.collection("citas").document(citaId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        citaActual = doc.toObject(Cita.class);
                        if (citaActual != null) {
                            citaActual.setId(doc.getId());
                            populateUI();
                            loadProjectData(citaActual.getProyectoId());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void populateUI() {
        txtPropiedadNombre.setText(citaActual.getProyectoNombre());
        txtProyectoTag.setText(citaActual.getProyectoNombre() != null ? citaActual.getProyectoNombre().toUpperCase() : "PROYECTO");
        txtPropiedadUbicacion.setText(citaActual.getMeetingPoint() != null ? citaActual.getMeetingPoint() : "Ubicación del proyecto");

        String status = citaActual.getEstado();
        txtCitaStatus.setText(status != null ? status.toUpperCase() : "CONFIRMADA");
        applyStatusStyle(txtCitaStatus, status);

        txtNombreCliente.setText(citaActual.getClienteNombre());
        txtInfoCliente.setText("Cliente vinculado al proyecto");
        if (citaActual.getClienteNombre() != null && !citaActual.getClienteNombre().isEmpty()) {
            txtAvatarCliente.setText(citaActual.getClienteNombre().substring(0, 1).toUpperCase());
        }

        txtFechaActual.setText(citaActual.getFechaFormateada());
        txtHoraActual.setText(citaActual.getHoraFormateada());
    }

    private void applyStatusStyle(TextView view, String status) {
        if (status == null) status = "";
        switch (status.toLowerCase()) {
            case "confirmada":
            case "reprogramada":
                view.setBackgroundResource(R.drawable.as_status_green);
                view.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            default:
                view.setBackgroundResource(R.drawable.as_chip_light);
                view.setTextColor(Color.parseColor("#68727B"));
                break;
        }
    }

    private void loadProjectData(String projectId) {
        if (projectId == null || projectId.isEmpty()) return;
        db.collection("proyectos").document(projectId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String url = doc.getString("primaryImageUrl");
                        if (url == null) url = doc.getString("imageUrl");
                        ProjectImageLoader.load(imgPropiedad, url, R.drawable.as_property_09);
                        
                        String ubicacion = doc.getString("direccion");
                        if (ubicacion == null) ubicacion = doc.getString("distrito");
                        if (ubicacion != null) txtPropiedadUbicacion.setText(ubicacion);
                    }
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editNuevaFecha.setText(String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, MESES[month], year));
            editNuevaHora.setText("");
            selectedTime = "";
            loadCitasDelDia(selectedDateIso);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void loadCitasDelDia(String fechaISO) {
        String asesorId = sessionManager.getUid();
        db.collection("citas")
                .whereEqualTo("asesorId", asesorId)
                .whereEqualTo("fechaISO", fechaISO)
                .get()
                .addOnSuccessListener(query -> {
                    citasDelDia.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Cita c = doc.toObject(Cita.class);
                        if (c != null) citasDelDia.add(c);
                    }
                });
    }

    private void showAvailableTimes() {
        if (selectedDateIso.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha primero", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> slots = new ArrayList<>();
        for (int h = 9; h < 18; h++) {
            for (int m = 0; m < 60; m += 30) {
                String hStr = String.format(Locale.US, "%02d:%02d", h, m);
                if (!isSlotOccupied(hStr)) {
                    slots.add(formatTimeDisplay(hStr));
                }
            }
        }

        if (slots.isEmpty()) {
            Toast.makeText(this, "No hay horarios disponibles para este día", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar nuevo horario")
                .setItems(slots.toArray(new String[0]), (dialog, which) -> {
                    selectedTime = convertTo24h(slots.get(which));
                    editNuevaHora.setText(slots.get(which));
                })
                .show();
    }

    private boolean isSlotOccupied(String time) {
        for (Cita c : citasDelDia) {
            if (time.equals(c.getHora())) return true;
        }
        return false;
    }

    private String formatTimeDisplay(String time) {
        try {
            String[] p = time.split(":");
            int h = Integer.parseInt(p[0]);
            String ampm = h >= 12 ? "PM" : "AM";
            int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
            return String.format(Locale.getDefault(), "%d:%s %s", h12, p[1], ampm);
        } catch (Exception e) { return time; }
    }

    private String convertTo24h(String displayTime) {
        try {
            String[] parts = displayTime.split(" ");
            String[] hhmm = parts[0].split(":");
            int h = Integer.parseInt(hhmm[0]);
            if (parts[1].equals("PM") && h < 12) h += 12;
            if (parts[1].equals("AM") && h == 12) h = 0;
            return String.format(Locale.US, "%02d:%02d", h, Integer.parseInt(hhmm[1]));
        } catch (Exception e) { return ""; }
    }

    private void confirmarReprogramacion() {
        if (selectedDateIso.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        String motivo = editMotivo.getText().toString().trim();
        if (motivo.isEmpty()) {
            Toast.makeText(this, "Ingresa un motivo", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevaFechaTexto = editNuevaFecha.getText().toString();
        
        // Usamos el repositorio para asegurar transaccionalidad con slots y escritura en historial
        repository.rescheduleAppointment(citaId, selectedDateIso, nuevaFechaTexto, selectedTime, motivo, 
            new FirebaseAppointmentRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AsesorReprogramarCitaActivity.this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AsesorReprogramarCitaActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            });
    }
}
