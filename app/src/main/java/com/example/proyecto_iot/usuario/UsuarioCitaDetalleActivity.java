package com.example.proyecto_iot.usuario;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class UsuarioCitaDetalleActivity extends BaseUsuarioActivity {
    public static final String EXTRA_APPOINTMENT_ID = "extra_appointment_id";
    public static final String EXTRA_APPOINTMENT_PROJECT_ID = "extra_appointment_project_id";
    public static final String EXTRA_APPOINTMENT_TITLE = "extra_appointment_title";
    public static final String EXTRA_APPOINTMENT_STATUS = "extra_appointment_status";
    public static final String EXTRA_APPOINTMENT_DATE = "extra_appointment_date";
    public static final String EXTRA_APPOINTMENT_ADVISOR = "extra_appointment_advisor";
    public static final String EXTRA_APPOINTMENT_LOCATION = "extra_appointment_location";
    public static final String EXTRA_APPOINTMENT_NOTE = "extra_appointment_note";
    public static final String EXTRA_APPOINTMENT_CONFIRMED = "extra_appointment_confirmed";

    private boolean confirmed;
    private String appointmentId = "";
    private String projectId = "";
    private String appointmentStatus = "";
    private String appointmentAdvisorId = "";
    private String appointmentAdvisorName = "";
    private boolean chatRequestInProgress;
    private boolean cancellationRequestInProgress;
    private boolean appointmentAvailable = true;
    private TextView primaryAction;
    private TextView secondaryAction;
    private View cancelAction;
    private ListenerRegistration appointmentListener;
    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_detalle);
        setupUserBottomNav(R.id.navUserActivity);
        bindData();
        setupActions();
        reserveBottomNavigationSpace();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startAppointmentListener();
    }

    @Override
    protected void onStop() {
        if (appointmentListener != null) {
            appointmentListener.remove();
            appointmentListener = null;
        }
        super.onStop();
    }

    private void bindData() {
        confirmed = getIntent().getBooleanExtra(EXTRA_APPOINTMENT_CONFIRMED, true);
        appointmentId = getIntent().getStringExtra(EXTRA_APPOINTMENT_ID);
        if (appointmentId == null) appointmentId = "";
        projectId = valueOr(getIntent().getStringExtra(EXTRA_APPOINTMENT_PROJECT_ID));
        appointmentStatus = valueOr(getIntent().getStringExtra(EXTRA_APPOINTMENT_STATUS));
        appointmentAdvisorName = valueOr(getIntent().getStringExtra(EXTRA_APPOINTMENT_ADVISOR));

        bindText(R.id.tvAppointmentDetailTitle,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_TITLE),
                R.string.activity_card_1_title);
        bindText(R.id.tvAppointmentDetailStatus,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_STATUS),
                R.string.activity_card_1_status);
        bindText(R.id.tvAppointmentDetailDate,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_DATE),
                R.string.activity_card_1_datetime);
        bindText(R.id.tvAppointmentDetailAdvisor,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_ADVISOR),
                R.string.activity_card_1_advisor);
        bindText(R.id.tvAppointmentDetailLocation,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_LOCATION),
                R.string.activity_appointment_location_1);
        bindText(R.id.tvAppointmentDetailNote,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_NOTE),
                R.string.activity_appointment_note_1);

        updateAppointmentPresentation();
    }

    private void bindText(int viewId, String value, int fallbackRes) {
        TextView view = findViewById(viewId);
        if (view == null) {
            return;
        }
        if (value == null || value.trim().isEmpty()) {
            view.setText(fallbackRes);
            return;
        }
        view.setText(value);
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackAppointmentDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        primaryAction = findViewById(R.id.btnAppointmentPrimaryAction);
        if (primaryAction != null) {
            primaryAction.setOnClickListener(v -> openAppointmentChat());
        }

        secondaryAction = findViewById(R.id.btnAppointmentSecondaryAction);
        if (secondaryAction != null) {
            secondaryAction.setOnClickListener(v -> openAppointmentProjectMap());
        }

        cancelAction = findViewById(R.id.btnCancelAppointmentByUser);
        if (cancelAction != null) {
            cancelAction.setOnClickListener(v -> confirmCancellation(cancelAction));
        }
        updateActionState();
    }

    private void startAppointmentListener() {
        if (appointmentListener != null || appointmentId.isEmpty()) {
            if (appointmentId.isEmpty()) markAppointmentUnavailable("No se pudo identificar la cita.");
            return;
        }
        appointmentListener = FirebaseFirestore.getInstance().collection("citas").document(appointmentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (error != null) {
                        markAppointmentUnavailable("No se pudo actualizar la cita. Intenta nuevamente.");
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        markAppointmentUnavailable("Esta cita ya no está disponible.");
                        return;
                    }
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String clienteId = firstNonEmpty(snapshot.getString("clienteId"), snapshot.getString("clientId"));
                    if (user == null || !user.getUid().equals(clienteId)) {
                        markAppointmentUnavailable("No tienes permiso para ver esta cita.");
                        return;
                    }
                    appointmentAvailable = true;
                    bindAppointmentSnapshot(snapshot);
                });
    }

    private void bindAppointmentSnapshot(DocumentSnapshot snapshot) {
        String status = firstNonEmpty(snapshot.getString("estado"), "Pendiente");
        appointmentStatus = status;
        confirmed = "Confirmada".equalsIgnoreCase(status) || "Reprogramada".equalsIgnoreCase(status);
        if (!isCancellableStatus()) {
            cancellationRequestInProgress = false;
        }
        projectId = firstNonEmpty(snapshot.getString("propertyId"), snapshot.getString("projectId"),
                snapshot.getString("proyectoId"));
        appointmentAdvisorId = firstNonEmpty(snapshot.getString("asesorId"), snapshot.getString("advisorId"));
        appointmentAdvisorName = firstNonEmpty(snapshot.getString("asesorNombre"), appointmentAdvisorName, "Asesor");

        bindText(R.id.tvAppointmentDetailTitle,
                firstNonEmpty(snapshot.getString("inmuebleNombre"), snapshot.getString("proyectoNombre"), "Proyecto"),
                R.string.activity_card_1_title);
        bindText(R.id.tvAppointmentDetailStatus, status.toUpperCase(), R.string.activity_card_1_status);
        bindText(R.id.tvAppointmentDetailDate,
                firstNonEmpty(snapshot.getString("fechaTexto"), snapshot.getString("fechaISO")) + " "
                        + firstNonEmpty(snapshot.getString("hora")),
                R.string.activity_card_1_datetime);
        bindText(R.id.tvAppointmentDetailAdvisor, appointmentAdvisorName, R.string.activity_card_1_advisor);
        bindText(R.id.tvAppointmentDetailLocation, snapshot.getString("meetingPoint"),
                R.string.activity_appointment_location_1);
        bindText(R.id.tvAppointmentDetailNote, snapshot.getString("nota"), R.string.activity_appointment_note_1);
        updateAppointmentPresentation();
        updateActionState();
    }

    private void markAppointmentUnavailable(String message) {
        appointmentAvailable = false;
        chatRequestInProgress = false;
        updateActionState();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateAppointmentPresentation() {
        TextView badge = findViewById(R.id.tvAppointmentDetailStatus);
        if (badge != null) {
            int color = confirmed ? R.color.app_accent_gold : R.color.app_text_secondary;
            badge.setTextColor(ContextCompat.getColor(this, color));
        }
        bindText(R.id.tvAppointmentNextTitle,
                getString(confirmed
                        ? R.string.appointment_detail_next_title_confirmed
                        : R.string.appointment_detail_next_title_pending),
                R.string.appointment_detail_next_title_confirmed);
        bindText(R.id.tvAppointmentNextDescription,
                getString(confirmed
                        ? R.string.appointment_detail_next_description_confirmed
                        : R.string.appointment_detail_next_description_pending),
                R.string.appointment_detail_next_description_confirmed);
    }

    private void updateActionState() {
        boolean canOpenChat = appointmentAvailable && !appointmentId.isEmpty()
                && !projectId.isEmpty() && !appointmentAdvisorId.isEmpty() && !chatRequestInProgress;
        if (primaryAction != null) {
            primaryAction.setText(confirmed ? R.string.appointment_detail_primary_confirmed
                    : R.string.appointment_detail_primary_pending);
            primaryAction.setEnabled(canOpenChat);
            primaryAction.setAlpha(canOpenChat ? 1f : 0.5f);
        }
        boolean canOpenMap = appointmentAvailable && !projectId.isEmpty();
        if (secondaryAction != null) {
            secondaryAction.setText(confirmed ? R.string.appointment_detail_secondary_confirmed
                    : R.string.appointment_detail_secondary_pending);
            secondaryAction.setEnabled(canOpenMap);
            secondaryAction.setAlpha(canOpenMap ? 1f : 0.5f);
        }
        if (cancelAction != null) {
            boolean canCancel = appointmentAvailable && !appointmentId.isEmpty() && confirmed
                    && isCancellableStatus() && !cancellationRequestInProgress;
            cancelAction.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            cancelAction.setEnabled(canCancel);
        }
    }

    private void openAppointmentProjectMap() {
        if (projectId.isEmpty()) {
            Toast.makeText(this, "Esta cita no está vinculada a un proyecto con ubicación.", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(UsuarioMapaExploracionActivity.focusedProjectIntent(this, projectId));
    }

    private void openAppointmentChat() {
        if (!appointmentAvailable || projectId.isEmpty() || appointmentAdvisorId.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar el proyecto o asesor asociado a la cita.", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || valueOr(firebaseUser.getUid()).isEmpty()) {
            Toast.makeText(this, "Inicia sesión para contactar al asesor.", Toast.LENGTH_LONG).show();
            return;
        }
        String clienteUid = firebaseUser.getUid();
        String cachedUid = AuthSessionManager.getInstance(this).getUid();
        if (!valueOr(cachedUid).isEmpty() && !clienteUid.equals(cachedUid)) {
            Toast.makeText(this, "La sesión cambió. Vuelve a iniciar sesión antes de abrir el chat.", Toast.LENGTH_LONG).show();
            return;
        }
        if (chatRequestInProgress) return;
        chatRequestInProgress = true;
        updateActionState();
        appointmentRepository.getAppointmentChatContext(appointmentId, clienteUid,
                new FirebaseAppointmentRepository.AppointmentChatContextCallback() {
                    @Override
                    public void onSuccess(FirebaseAppointmentRepository.AppointmentChatContext context) {
                        if (isFinishing() || isDestroyed()) return;
                        // The advisor comes from the appointment itself. Do not replace it with a
                        // currently assigned advisor, because assignments can change after booking.
                        openProjectChat(context, clienteUid);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing() && !isDestroyed()) finishChatRequest(message);
                    }
                });
    }

    private void openProjectChat(FirebaseAppointmentRepository.AppointmentChatContext context, String clienteUid) {
        FirebaseChatRepository.ProjectChatContext project = new FirebaseChatRepository.ProjectChatContext(
                context.projectId,
                context.projectName,
                context.projectLocation,
                context.projectPrice,
                context.projectImageUrl
        );
        FirebaseChatRepository.Advisor advisor = new FirebaseChatRepository.Advisor(
                context.asesorId, context.asesorNombre, "", "", context.assignmentId);
        new FirebaseChatRepository().findOrCreateProjectConversation(
                clienteUid,
                AuthSessionManager.getInstance(this).getUserName(),
                advisor,
                project,
                new FirebaseChatRepository.ConversationCallback() {
                    @Override
                    public void onSuccess(FirebaseChatRepository.Conversation conversation) {
                        if (isFinishing() || isDestroyed()) return;
                        finishChatRequest(null);
                        Intent intent = new Intent(UsuarioCitaDetalleActivity.this,
                                UsuarioChatDetalleActivity.class);
                        UsuarioChatDetalleActivity.putConversationExtras(intent, conversation);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) finishChatRequest(message);
                    }
                });
    }

    private void finishChatRequest(String message) {
        chatRequestInProgress = false;
        updateActionState();
        if (message != null && !message.trim().isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isCancellableStatus() {
        return "CONFIRMADA".equalsIgnoreCase(appointmentStatus)
                || "REPROGRAMADA".equalsIgnoreCase(appointmentStatus);
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonEmpty(String... values) {
        if (values != null) {
            for (String value : values) {
                String normalized = valueOr(value);
                if (!normalized.isEmpty()) return normalized;
            }
        }
        return "";
    }

    /** Ensures the final action can be scrolled above the persistent bottom navigation. */
    private void reserveBottomNavigationSpace() {
        View scroll = findViewById(R.id.appointmentDetailScroll);
        View navItem = findViewById(R.id.navUserExplore);
        if (scroll == null || navItem == null || !(navItem.getParent() instanceof View)) return;

        View nav = (View) navItem.getParent();
        final int baseLeft = scroll.getPaddingLeft();
        final int baseTop = scroll.getPaddingTop();
        final int baseRight = scroll.getPaddingRight();
        final int baseBottom = scroll.getPaddingBottom();
        Runnable updatePadding = () -> scroll.setPadding(baseLeft, baseTop, baseRight,
                baseBottom + nav.getHeight() + dp(16));
        nav.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                updatePadding.run());
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(baseLeft, baseTop, baseRight,
                    baseBottom + nav.getHeight() + bars.bottom + dp(16));
            return insets;
        });
        scroll.post(updatePadding);
        ViewCompat.requestApplyInsets(scroll);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void confirmCancellation(View cancelAction) {
        EditText reason = new EditText(this);
        reason.setHint("Motivo de la cancelación (opcional)");
        new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("El horario volverá a estar disponible.")
                .setView(reason)
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar cita", (dialog, which) -> {
                    if (cancellationRequestInProgress) return;
                    cancellationRequestInProgress = true;
                    cancelAction.setEnabled(false);
                    appointmentRepository.cancelAppointment(appointmentId,
                            reason.getText().toString().trim(), new FirebaseAppointmentRepository.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(UsuarioCitaDetalleActivity.this,
                                            "Cita cancelada y horario liberado.", Toast.LENGTH_LONG).show();
                                    // The appointment listener updates the status and actions from Firestore.
                                }

                                @Override
                                public void onError(String message) {
                                    cancellationRequestInProgress = false;
                                    updateActionState();
                                    Toast.makeText(UsuarioCitaDetalleActivity.this,
                                            message == null || message.trim().isEmpty()
                                                    ? "No se pudo cancelar la cita."
                                                    : message,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }
}
