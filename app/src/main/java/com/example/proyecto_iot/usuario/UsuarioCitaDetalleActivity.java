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
    private boolean chatRequestInProgress;
    private TextView primaryAction;
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

    private void bindData() {
        confirmed = getIntent().getBooleanExtra(EXTRA_APPOINTMENT_CONFIRMED, true);
        appointmentId = getIntent().getStringExtra(EXTRA_APPOINTMENT_ID);
        if (appointmentId == null) appointmentId = "";
        projectId = valueOr(getIntent().getStringExtra(EXTRA_APPOINTMENT_PROJECT_ID));
        appointmentStatus = valueOr(getIntent().getStringExtra(EXTRA_APPOINTMENT_STATUS));

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
            primaryAction.setText(confirmed
                    ? R.string.appointment_detail_primary_confirmed
                    : R.string.appointment_detail_primary_pending);
            primaryAction.setEnabled(!appointmentId.isEmpty());
            primaryAction.setAlpha(appointmentId.isEmpty() ? 0.5f : 1f);
            primaryAction.setOnClickListener(v -> openAppointmentChat());
        }

        TextView secondaryAction = findViewById(R.id.btnAppointmentSecondaryAction);
        if (secondaryAction != null) {
            secondaryAction.setText(confirmed
                    ? R.string.appointment_detail_secondary_confirmed
                    : R.string.appointment_detail_secondary_pending);
            secondaryAction.setEnabled(!projectId.isEmpty());
            secondaryAction.setAlpha(projectId.isEmpty() ? 0.5f : 1f);
            secondaryAction.setOnClickListener(v -> openAppointmentProjectMap());
        }

        View cancelAction = findViewById(R.id.btnCancelAppointmentByUser);
        boolean canCancel = !appointmentId.isEmpty() && confirmed && isCancellableStatus();
        if (cancelAction != null) {
            cancelAction.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            cancelAction.setOnClickListener(v -> confirmCancellation(cancelAction));
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
        if (projectId.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar el proyecto asociado a la cita.", Toast.LENGTH_LONG).show();
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
        setPrimaryActionEnabled(false);

        appointmentRepository.getAdvisorsForProject(projectId,
                new FirebaseAppointmentRepository.AdvisorsCallback() {
                    @Override
                    public void onSuccess(java.util.List<FirebaseAppointmentRepository.Advisor> advisors) {
                        if (isFinishing() || isDestroyed()) return;
                        if (advisors.isEmpty()) {
                            finishChatRequest("Este proyecto no tiene asesores activos asignados.");
                        } else {
                            openProjectChat(advisors.get(0), clienteUid);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) finishChatRequest(message);
                    }
                });
    }

    private void openProjectChat(FirebaseAppointmentRepository.Advisor selectedAdvisor, String clienteUid) {
        FirebaseChatRepository.ProjectChatContext project = new FirebaseChatRepository.ProjectChatContext(
                projectId,
                getIntent().getStringExtra(EXTRA_APPOINTMENT_TITLE),
                getIntent().getStringExtra(EXTRA_APPOINTMENT_LOCATION),
                "",
                ""
        );
        FirebaseChatRepository.Advisor advisor = new FirebaseChatRepository.Advisor(
                selectedAdvisor.uid, selectedAdvisor.name, "", "sa_profile_asesor_1",
                selectedAdvisor.assignmentId);
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
        setPrimaryActionEnabled(true);
        if (message != null && !message.trim().isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void setPrimaryActionEnabled(boolean enabled) {
        if (primaryAction != null) {
            primaryAction.setEnabled(enabled);
            primaryAction.setAlpha(enabled ? 1f : 0.5f);
        }
    }

    private boolean isCancellableStatus() {
        return "CONFIRMADA".equalsIgnoreCase(appointmentStatus)
                || "REPROGRAMADA".equalsIgnoreCase(appointmentStatus);
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
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
                    cancelAction.setEnabled(false);
                    appointmentRepository.cancelAppointment(appointmentId,
                            reason.getText().toString().trim(), new FirebaseAppointmentRepository.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(UsuarioCitaDetalleActivity.this,
                                            "Cita cancelada y horario liberado.", Toast.LENGTH_LONG).show();
                                    TextView status = findViewById(R.id.tvAppointmentDetailStatus);
                                    if (status != null) status.setText("CANCELADA");
                                    appointmentStatus = "CANCELADA";
                                    confirmed = false;
                                    cancelAction.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(String message) {
                                    cancelAction.setEnabled(true);
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
