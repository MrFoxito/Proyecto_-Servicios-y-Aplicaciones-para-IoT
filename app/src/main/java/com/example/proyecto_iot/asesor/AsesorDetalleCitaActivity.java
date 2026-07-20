package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class AsesorDetalleCitaActivity extends BaseAsesorActivity {

    public static final String EXTRA_CITA_ID = "extra_cita_id";

    private Cita citaActual;
    private EventoCitaAdapter eventoCitaAdapter;
    private final FirebaseAppointmentRepository repository = new FirebaseAppointmentRepository();
    private ListenerRegistration citaListener;
    private ListenerRegistration eventosListener;
    private boolean operationInFlight;
    private boolean actionsBound;
    private String citaId;
    private final List<EventoCita> legacyEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_detalle_cita);

        setupBackButton();
        setupRecyclerView();

        citaId = getIntent().getStringExtra(EXTRA_CITA_ID);
        if (isEmpty(citaId)) {
            Toast.makeText(this, "ID de cita no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        citaId = citaId.trim();
        loadCitaData(citaId);
    }

    private void loadCitaData(String citaId) {
        if (citaListener != null) citaListener.remove();
        citaListener = FirebaseFirestore.getInstance().collection("citas").document(citaId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "No se pudo cargar la cita. Vuelve a intentarlo.", Toast.LENGTH_LONG).show();
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

                        String currentUid = authenticatedAdvisorUid();
                        if (isEmpty(currentUid) || !currentUid.equals(citaActual.getAsesorId())) {
                            Toast.makeText(this, "No tienes acceso a esta cita.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        readLegacyHistory(doc);

                        populateData();
                        setupActions();
                        loadProjectImage(citaActual.getProyectoId(), firstOf(doc,
                                "primaryImageUrl", "imageUrl", "imagenUrl", "propertyImageUrl"));
                        listenAppointmentEvents(citaActual.getId(), currentUid);
                    }
                });
    }

    private void listenAppointmentEvents(String citaId, String advisorUid) {
        if (eventosListener != null) eventosListener.remove();
        if (isEmpty(citaId) || isEmpty(advisorUid)) {
            renderHistoryError();
            return;
        }
        renderHistoryLoading();
        eventosListener = FirebaseFirestore.getInstance().collection("eventos_cita")
                .whereEqualTo("citaId", citaId)
                .whereEqualTo("asesorId", advisorUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (error != null) {
                        renderHistoryError();
                        return;
                    }
                    List<EventoCita> eventos = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            EventoCita evento = new EventoCita();
                            evento.setId(document.getId());
                            evento.setCitaId(firstOf(document, "citaId"));
                            evento.setTitulo(firstOf(document, "titulo", "title", "evento"));
                            evento.setDetalle(firstOf(document, "detalle", "detail", "motivo"));
                            evento.setTipo(firstOf(document, "tipo", "type"));
                            evento.setFechaHora(eventDate(document));
                            eventos.add(evento);
                        }
                    }
                    renderHistory(mergeEvents(eventos));
                });
    }

    @SuppressWarnings("unchecked")
    private void readLegacyHistory(DocumentSnapshot doc) {
        legacyEvents.clear();
        Object rawHistory = doc.get("historial");
        if (!(rawHistory instanceof List)) return;
        for (Object rawItem : (List<?>) rawHistory) {
            if (!(rawItem instanceof Map)) continue;
            Map<String, Object> item = (Map<String, Object>) rawItem;
            EventoCita event = new EventoCita();
            event.setId(valueOf(item.get("id")));
            event.setCitaId(firstNonEmpty(valueOf(item.get("citaId")), doc.getId()));
            event.setTitulo(firstNonEmpty(valueOf(item.get("titulo")), valueOf(item.get("title")), "Evento"));
            event.setDetalle(firstNonEmpty(valueOf(item.get("detalle")), valueOf(item.get("detail"))));
            event.setTipo(firstNonEmpty(valueOf(item.get("tipo")), valueOf(item.get("type")), "INFO"));
            event.setFechaHora(firstNonEmpty(valueOf(item.get("fechaHora")), valueOf(item.get("createdAtISO"))));
            legacyEvents.add(event);
        }
    }

    private List<EventoCita> mergeEvents(List<EventoCita> remoteEvents) {
        LinkedHashMap<String, EventoCita> merged = new LinkedHashMap<>();
        for (EventoCita event : legacyEvents) putEvent(merged, event);
        if (remoteEvents != null) for (EventoCita event : remoteEvents) putEvent(merged, event);
        List<EventoCita> events = new ArrayList<>(merged.values());
        events.sort(Comparator.comparing(EventoCita::getFechaHora,
                Comparator.nullsLast(String::compareTo)));
        return events;
    }

    private void putEvent(LinkedHashMap<String, EventoCita> target, EventoCita event) {
        if (event == null) return;
        String key = firstNonEmpty(event.getId(), event.getCitaId() + "|" + event.getTipo() + "|"
                + event.getTitulo() + "|" + event.getFechaHora());
        target.put(key, event);
    }

    private String valueOf(Object value) {
        if (value instanceof String) return ((String) value).trim();
        long millis = value instanceof Timestamp ? ((Timestamp) value).toDate().getTime()
                : value instanceof Number ? ((Number) value).longValue() : 0L;
        return millis > 0 ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(millis) : "";
    }

    private String eventDate(DocumentSnapshot document) {
        String iso = firstOf(document, "fechaHora", "createdAtISO", "updatedAtISO");
        if (!iso.isEmpty()) return iso;
        Object value = document.get("createdAt");
        long millis = value instanceof Timestamp ? ((Timestamp) value).toDate().getTime()
                : value instanceof Number ? ((Number) value).longValue() : 0L;
        return millis > 0 ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(millis) : "";
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

    private String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (!isEmpty(value)) return value.trim();
        }
        return "";
    }

    private void loadProjectImage(String projectId, String appointmentImageUrl) {
        ImageView image = findViewById(R.id.imgDetallePropiedad);
        ProjectImageLoader.load(image, appointmentImageUrl, R.drawable.as_project_placeholder);
        if (isEmpty(projectId)) return;
        FirebaseFirestore.getInstance().collection("proyectos").document(projectId).get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (doc.exists()) {
                        String url = firstOf(doc, "primaryImageUrl", "imageUrl", "imagenUrl", "propertyImageUrl");
                        if (!isEmpty(url)) {
                            ProjectImageLoader.load(image, url, R.drawable.as_project_placeholder);
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        RecyclerView rvHistorial = findViewById(R.id.rvHistorialCita);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        eventoCitaAdapter = new EventoCitaAdapter(null);
        rvHistorial.setAdapter(eventoCitaAdapter);
        renderHistoryLoading();
    }

    private void renderHistoryLoading() {
        TextView state = findViewById(R.id.txtHistorialEstado);
        TextView retry = findViewById(R.id.btnReintentarHistorial);
        if (state != null) {
            state.setText("Cargando actividad de la cita…");
            state.setVisibility(View.VISIBLE);
        }
        if (retry != null) retry.setVisibility(View.GONE);
    }

    private void renderHistory(List<EventoCita> events) {
        if (eventoCitaAdapter == null) return;
        eventoCitaAdapter.setEventos(events);
        TextView state = findViewById(R.id.txtHistorialEstado);
        TextView retry = findViewById(R.id.btnReintentarHistorial);
        if (state != null) {
            state.setText("Aún no hay actividad registrada para esta cita.");
            state.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (retry != null) retry.setVisibility(View.GONE);
    }

    private void renderHistoryError() {
        if (eventoCitaAdapter != null && !legacyEvents.isEmpty()) {
            renderHistory(mergeEvents(null));
        }
        TextView state = findViewById(R.id.txtHistorialEstado);
        TextView retry = findViewById(R.id.btnReintentarHistorial);
        if (state != null) {
            state.setText("No se pudo cargar la actividad reciente. Intenta nuevamente.");
            state.setVisibility(View.VISIBLE);
        }
        if (retry != null) {
            retry.setVisibility(View.VISIBLE);
            retry.setOnClickListener(v -> {
                if (citaActual != null) listenAppointmentEvents(citaActual.getId(), authenticatedAdvisorUid());
            });
        }
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

        renderHistory(mergeEvents(null));
    }

    private void updateButtonsVisibility() {
        String status = firstNonEmpty(citaActual.getEstado()).toLowerCase(Locale.ROOT);
        boolean isActive = status.equals("confirmada") || status.equals("reprogramada") || status.equals("pendiente");
        boolean canRegisterSeparation = isActive && !citaActual.isHasCierre();

        View separation = findViewById(R.id.btnRegistrarSeparacionDetalle);
        View manage = findViewById(R.id.btnGestionarCita);
        View panel = findViewById(R.id.actionPanel);
        if (separation != null) separation.setVisibility(canRegisterSeparation ? View.VISIBLE : View.GONE);
        if (manage != null) manage.setVisibility(isActive ? View.VISIBLE : View.GONE);
        if (panel != null) panel.setVisibility((canRegisterSeparation || isActive) ? View.VISIBLE : View.GONE);
    }

    private void setupActions() {
        if (actionsBound) return;
        actionsBound = true;
        findViewById(R.id.txtDetalleCliente).setOnClickListener(v -> openClientSummary());
        findViewById(R.id.imgDetallePropiedad).setOnClickListener(v -> openProjectSummary());
        findViewById(R.id.txtDetallePropiedad).setOnClickListener(v -> openProjectSummary());

        findViewById(R.id.btnRegistrarSeparacionDetalle).setOnClickListener(v -> {
            if (!canOperate()) return;
            Intent intent = new Intent(this, AsesorRegistrarSeparacionActivity.class);
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CITA_ID, citaActual.getId());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE, citaActual.getClienteNombre());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROYECTO, citaActual.getProyectoNombre());
            intent.putExtra(AsesorRegistrarSeparacionActivity.EXTRA_PROJECT_ID, citaActual.getProyectoId());
            startActivity(intent);
        });

        View manage = findViewById(R.id.btnGestionarCita);
        if (manage != null) manage.setOnClickListener(v -> showManageActions());

        findViewById(R.id.btnMensaje).setOnClickListener(v -> {
            if (isEmpty(citaActual.getClienteId()) || isEmpty(citaActual.getProyectoId())) {
                Toast.makeText(this, "Información de cliente no disponible para chat", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentAsesorId = firebaseUser == null ? "" : firebaseUser.getUid();
            if (isEmpty(currentAsesorId) || !currentAsesorId.equals(citaActual.getAsesorId())) {
                Toast.makeText(this, "Tu sesión no corresponde al asesor de esta cita.", Toast.LENGTH_LONG).show();
                return;
            }
            new FirebaseChatRepository().getProjectConversation(
                    citaActual.getClienteId(), currentAsesorId, citaActual.getProyectoId(),
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

        findViewById(R.id.btnEditarNota).setOnClickListener(v -> showEditNotaDialog());
    }

    private void showManageActions() {
        if (!canOperate()) return;
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_asesor_gestion_cita, null);
        content.findViewById(R.id.btnReprogramarCita).setOnClickListener(v -> {
            sheet.dismiss();
            if (!canOperate()) return;
            Intent intent = new Intent(this, AsesorReprogramarCitaActivity.class);
            intent.putExtra(EXTRA_CITA_ID, citaActual.getId());
            startActivity(intent);
        });
        content.findViewById(R.id.btnMarcarAtendida).setOnClickListener(v -> {
            sheet.dismiss();
            if (beginOperation()) repository.updateAttendance(citaActual.getId(), true, simpleOpCallback("Visita completada"));
        });
        content.findViewById(R.id.btnMarcarNoAsistio).setOnClickListener(v -> {
            sheet.dismiss();
            if (beginOperation()) repository.updateAttendance(citaActual.getId(), false, simpleOpCallback("Inasistencia registrada"));
        });
        content.findViewById(R.id.btnCancelarCita).setOnClickListener(v -> {
            sheet.dismiss();
            showCancelDialog();
        });
        sheet.setContentView(content);
        sheet.show();
    }

    private void showCancelDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Cancelar cita").setMessage("¿Por qué se cancela la cita?").setView(input)
                .setPositiveButton("Confirmar", (d, w) -> {
                    if (beginOperation()) {
                        repository.cancelAppointment(citaActual.getId(), input.getText().toString(), simpleOpCallback("Cita cancelada"));
                    }
                })
                .setNegativeButton("Volver", null).show();
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
        if (!beginOperation()) return;
        FirebaseFirestore.getInstance().collection("citas").document(citaActual.getId())
                .update("nota", nota)
                .addOnSuccessListener(aVoid -> {
                    operationInFlight = false;
                    citaActual.setNota(nota);
                    ((TextView)findViewById(R.id.txtDetalleNotas)).setText(isEmpty(nota) ? "Sin notas registradas." : nota);
                    Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    operationInFlight = false;
                    Toast.makeText(this, "Error al guardar nota", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyStatusTheme(TextView view, String status) {
        if (view == null) return;
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
            @Override public void onSuccess() {
                operationInFlight = false;
                Toast.makeText(AsesorDetalleCitaActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String error) {
                operationInFlight = false;
                Toast.makeText(AsesorDetalleCitaActivity.this, error, Toast.LENGTH_LONG).show();
            }
        };
    }

    private boolean canOperate() {
        return citaActual != null && !operationInFlight && authenticatedAdvisorUid().equals(citaActual.getAsesorId());
    }

    private boolean beginOperation() {
        if (!canOperate()) {
            Toast.makeText(this, "Espera a que termine la operación actual.", Toast.LENGTH_SHORT).show();
            return false;
        }
        operationInFlight = true;
        return true;
    }

    private String authenticatedAdvisorUid() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String firebaseUid = firebaseUser == null ? "" : firebaseUser.getUid();
        String cachedUid = AuthSessionManager.getInstance(this).getUid();
        if (isEmpty(firebaseUid) || (!isEmpty(cachedUid) && !firebaseUid.equals(cachedUid))) return "";
        return firebaseUid;
    }

    private void openClientSummary() {
        if (citaActual == null || isEmpty(citaActual.getClienteId())) return;
        Intent intent = new Intent(this, AsesorClienteDetalleActivity.class);
        intent.putExtra(AsesorClienteDetalleActivity.EXTRA_CLIENTE_ID, citaActual.getClienteId());
        intent.putExtra(AsesorClienteDetalleActivity.EXTRA_CLIENTE_NOMBRE, citaActual.getClienteNombre());
        intent.putExtra(AsesorClienteDetalleActivity.EXTRA_CITA_ID, citaActual.getId());
        startActivity(intent);
    }

    private void openProjectSummary() {
        if (citaActual == null || isEmpty(citaActual.getProyectoId())) return;
        Intent intent = new Intent(this, AsesorProyectoDetalleActivity.class);
        intent.putExtra(AsesorProyectoDetalleActivity.EXTRA_PROJECT_ID, citaActual.getProyectoId());
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (citaListener == null && !isEmpty(citaId)) loadCitaData(citaId);
    }

    @Override
    protected void onStop() {
        if (citaListener != null) {
            citaListener.remove();
            citaListener = null;
        }
        if (eventosListener != null) {
            eventosListener.remove();
            eventosListener = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (citaListener != null) citaListener.remove();
        if (eventosListener != null) eventosListener.remove();
        super.onDestroy();
    }
}
