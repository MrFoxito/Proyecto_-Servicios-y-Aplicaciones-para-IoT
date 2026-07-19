package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.example.proyecto_iot.entity.Cita;
import com.example.proyecto_iot.entity.EventoCita;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    public static final String EXTRA_CITA_ID = "extra_cita_id";

    private Cita citaActual;
    private EventoCitaAdapter eventoCitaAdapter;
    private final FirebaseAppointmentRepository repository = new FirebaseAppointmentRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        setupBackButton();
        setupRecyclerView();

        String citaId = getIntent().getStringExtra(EXTRA_CITA_ID);
        if (citaId == null || citaId.isEmpty()) {
            Toast.makeText(this, "ID de cita no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCitaData(citaId);
    }

    private void loadCitaData(String citaId) {
        FirebaseFirestore.getInstance().collection("citas").document(citaId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "La cita ya no esta disponible en tu agenda.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        return;
                    }

                    citaActual = doc.toObject(Cita.class);
                    if (citaActual != null) {
                        citaActual.setId(doc.getId());
                        
                        // Robustez en mapeo de campos (Firestore puede variar nombres)
                        if (isEmpty(citaActual.getClienteId())) {
                            citaActual.setClienteId(firstOf(doc, "clienteId", "clientId", "clienteUid", "uidCliente"));
                        }
                        if (isEmpty(citaActual.getAsesorId())) {
                            citaActual.setAsesorId(firstOf(doc, "asesorId", "advisorId", "asesorUid", "uidAsesor"));
                        }
                        if (isEmpty(citaActual.getProyectoId())) {
                            citaActual.setProyectoId(firstOf(doc, "proyectoId", "propertyId", "projectId"));
                        }
                        if (isEmpty(citaActual.getClienteNombre())) {
                            citaActual.setClienteNombre(firstOf(doc, "clienteNombre", "clientName", "nombreCliente"));
                        }
                        if (isEmpty(citaActual.getProyectoNombre())) {
                            citaActual.setProyectoNombre(firstOf(doc, "proyectoNombre", "inmuebleNombre", "projectName"));
                        }

                        // Cargar historial
                        List<Map<String, Object>> histData = (List<Map<String, Object>>) doc.get("historial");
                        if (histData != null) {
                            List<EventoCita> listaEventos = new ArrayList<>();
                            for (Map<String, Object> m : histData) {
                                EventoCita ev = new EventoCita();
                                ev.setTitulo((String)m.get("titulo"));
                                ev.setDetalle((String)m.get("detalle"));
                                ev.setFechaHora((String)m.get("fechaHora"));
                                listaEventos.add(ev);
                            }
                            citaActual.setHistorial(listaEventos);
                        }

                        populateData();
                        setupActions();
                        loadProjectImage(citaActual.getProyectoId());
                    }
                });
    }

    private String firstOf(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String val = doc.getString(key);
            if (val != null && !val.trim().isEmpty()) return val.trim();
        }
        return "";
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void loadProjectImage(String projectId) {
        if (isEmpty(projectId)) return;
        FirebaseFirestore.getInstance().collection("proyectos").document(projectId).get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (doc.exists()) {
                        String url = doc.getString("primaryImageUrl");
                        if (isEmpty(url)) url = doc.getString("imageUrl");
                        if (!isEmpty(url)) {
                            ProjectImageLoader.load(
                                    (ImageView) findViewById(R.id.imgDetallePropiedad),
                                    url,
                                    R.drawable.as_property_01
                            );
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        RecyclerView rvHistorial = findViewById(R.id.rvHistorialCita);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        eventoCitaAdapter = new EventoCitaAdapter(null);
        rvHistorial.setAdapter(eventoCitaAdapter);
    }

    private void populateData() {
        if (citaActual == null) return;

        TextView txtProyecto = findViewById(R.id.txtDetalleProyecto);
        TextView txtPropiedad = findViewById(R.id.txtDetallePropiedad);
        TextView txtCliente = findViewById(R.id.txtDetalleCliente);
        TextView txtStatusImg = findViewById(R.id.txtDetalleStatusImg);
        TextView txtStatusLabel = findViewById(R.id.txtDetalleStatus);
        TextView txtFecha = findViewById(R.id.txtDetalleFecha);
        TextView txtHora = findViewById(R.id.txtDetalleHora);
        TextView txtDia = findViewById(R.id.txtDetalleDia);
        TextView txtDireccion = findViewById(R.id.txtDetalleDireccion);
        TextView txtNotas = findViewById(R.id.txtDetalleNotas);
        TextView txtAvatar = findViewById(R.id.txtDetalleAvatar);

        txtProyecto.setText(!isEmpty(citaActual.getProyectoNombre()) ? citaActual.getProyectoNombre().toUpperCase() : "PROYECTO");
        txtPropiedad.setText(citaActual.getProyectoNombre());
        txtCliente.setText(citaActual.getClienteNombre());
        txtFecha.setText(citaActual.getFechaFormateada());
        txtHora.setText(citaActual.getHoraFormateada());
        txtDireccion.setText(!isEmpty(citaActual.getMeetingPoint()) ? citaActual.getMeetingPoint() : "Ubicación del Proyecto");
        txtNotas.setText(!isEmpty(citaActual.getNota()) ? citaActual.getNota() : "Sin notas registradas.");

        if (!isEmpty(citaActual.getClienteNombre())) {
            String[] parts = citaActual.getClienteNombre().split(" ");
            String initials = parts.length > 1 ? (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)) : parts[0].substring(0,1);
            txtAvatar.setText(initials.toUpperCase());
        }

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar cal = Calendar.getInstance();
            cal.setTime(fmt.parse(citaActual.getFechaISO()));
            txtDia.setText(new SimpleDateFormat("EEEE", new Locale("es", "ES")).format(cal.getTime()));
        } catch (Exception ignored) {}

        applyStatusTheme(txtStatusImg, citaActual.getEstado());
        applyStatusTheme(txtStatusLabel, citaActual.getEstado());
        updateButtonsVisibility();

        if (citaActual.getHistorial() != null) {
            eventoCitaAdapter.setEventos(citaActual.getHistorial());
        }
    }

    private void updateButtonsVisibility() {
        String status = citaActual.getEstado().toLowerCase();
        boolean isActive = status.equals("confirmada") || status.equals("reprogramada") || status.equals("pendiente");

        findViewById(R.id.btnRegistrarSeparacionDetalle).setVisibility(citaActual.isHasCierre() ? View.GONE : View.VISIBLE);
        findViewById(R.id.btnReprogramarCita).setVisibility(isActive ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnCancelarCita).setVisibility(isActive ? View.VISIBLE : View.GONE);

        View panelAsistencia = findViewById(R.id.btnMarcarAtendida).getParent() instanceof View ? (View)findViewById(R.id.btnMarcarAtendida).getParent() : null;
        if (panelAsistencia != null) {
            panelAsistencia.setVisibility(isActive ? View.VISIBLE : View.GONE);
        }
    }

    private void setupActions() {
        findViewById(R.id.btnRegistrarSeparacionDetalle).setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorRegistrarSeparacionActivity.class);
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CITA_ID, citaActual.getId());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE, citaActual.getClienteNombre());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO, citaActual.getProyectoNombre());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROJECT_ID, citaActual.getProyectoId());
            startActivity(intent);
        });

        findViewById(R.id.btnReprogramarCita).setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorReprogramarCitaActivity.class);
            intent.putExtra(EXTRA_CITA_ID, citaActual.getId());
            startActivity(intent);
        });

        findViewById(R.id.btnMarcarAtendida).setOnClickListener(v -> repository.updateAttendance(citaActual.getId(), true, simpleOpCallback("Visita completada")));
        findViewById(R.id.btnMarcarNoAsistio).setOnClickListener(v -> repository.updateAttendance(citaActual.getId(), false, simpleOpCallback("Inasistencia registrada")));

        findViewById(R.id.btnCancelarCita).setOnClickListener(v -> {
            EditText input = new EditText(this);
            new AlertDialog.Builder(this).setTitle("Cancelar Cita").setMessage("¿Por qué se cancela la cita?").setView(input)
                    .setPositiveButton("Confirmar", (d, w) -> repository.cancelAppointment(citaActual.getId(), input.getText().toString(), simpleOpCallback("Cita cancelada")))
                    .setNegativeButton("Cerrar", null).show();
        });

        findViewById(R.id.btnMensaje).setOnClickListener(v -> {
            if (isEmpty(citaActual.getClienteId())) {
                Toast.makeText(this, "Información de cliente no disponible para chat", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentAsesorId = firebaseUser == null ? "" : firebaseUser.getUid();
            if (isEmpty(currentAsesorId) || !currentAsesorId.equals(citaActual.getAsesorId())) {
                Toast.makeText(this, "Tu sesión no corresponde al asesor de esta cita.", Toast.LENGTH_LONG).show();
                return;
            }
            new FirebaseChatRepository().getAppointmentConversation(
                    citaActual.getClienteId(), currentAsesorId, citaActual.getId(),
                    new FirebaseChatRepository.ConversationCallback() {
                        @Override
                        public void onSuccess(FirebaseChatRepository.Conversation conversation) {
                            if (isFinishing() || isDestroyed()) return;
                            Intent intent = new Intent(AsesorDetalleCitaActivity.this, AsesorChatIndividualActivity.class);
                            intent.putExtra("conversationId", conversation.id);
                            intent.putExtra("clienteId", citaActual.getClienteId());
                            intent.putExtra("clienteNombre", citaActual.getClienteNombre());
                            intent.putExtra("projectName", conversation.projectName);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(String message) {
                            if (!isFinishing()) Toast.makeText(AsesorDetalleCitaActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        View btnLlamar = findViewById(R.id.btnLlamar);
        if (btnLlamar != null) {
            btnLlamar.setOnClickListener(v -> {
                Toast.makeText(this, "Iniciando llamada con " + citaActual.getClienteNombre(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:999000999")));
            });
        }

        findViewById(R.id.btnEditarNota).setOnClickListener(v -> showEditNotaDialog());
    }

    private void showEditNotaDialog() {
        EditText input = new EditText(this);
        input.setPadding(40, 40, 40, 40);
        if (!isEmpty(citaActual.getNota())) {
            input.setText(citaActual.getNota());
        }

        new AlertDialog.Builder(this)
                .setTitle("Notas de la Cita")
                .setMessage("Actualiza los detalles u observaciones de esta visita:")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevaNota = input.getText().toString().trim();
                    saveNotaToFirebase(nuevaNota);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveNotaToFirebase(String nota) {
        FirebaseFirestore.getInstance().collection("citas").document(citaActual.getId())
                .update("nota", nota)
                .addOnSuccessListener(aVoid -> {
                    citaActual.setNota(nota);
                    ((TextView)findViewById(R.id.txtDetalleNotas)).setText(isEmpty(nota) ? "Sin notas registradas." : nota);
                    Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar nota", Toast.LENGTH_SHORT).show());
    }

    private void applyStatusTheme(TextView view, String status) {
        if (isEmpty(status)) status = "Pendiente";
        view.setText(status.toUpperCase());
        switch (status.toLowerCase()) {
            case "confirmada":
            case "atendida":
                view.setBackgroundResource(R.drawable.as_status_green);
                view.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "cancelada":
            case "no asistio":
                view.setBackgroundResource(R.drawable.as_status_pending);
                view.setTextColor(Color.parseColor("#9B1C1C"));
                break;
            default:
                view.setBackgroundResource(R.drawable.as_chip_light);
                view.setTextColor(Color.parseColor("#68727B"));
                break;
        }
    }

    private FirebaseAppointmentRepository.OperationCallback simpleOpCallback(String msg) {
        return new FirebaseAppointmentRepository.OperationCallback() {
            @Override public void onSuccess() { Toast.makeText(AsesorDetalleCitaActivity.this, msg, Toast.LENGTH_SHORT).show(); }
            @Override public void onError(String error) { Toast.makeText(AsesorDetalleCitaActivity.this, error, Toast.LENGTH_LONG).show(); }
        };
    }
}
