package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UsuarioHistorialDetalleActivity extends BaseUsuarioActivity {
    public static final String EXTRA_HISTORY_TITLE = "extra_history_title";
    public static final String EXTRA_HISTORY_DATE = "extra_history_date";
    public static final String EXTRA_HISTORY_STATUS = "extra_history_status";
    public static final String EXTRA_HISTORY_CODE = "extra_history_code";
    public static final String EXTRA_HISTORY_AMOUNT = "extra_history_amount";
    public static final String EXTRA_HISTORY_SUMMARY = "extra_history_summary";
    public static final String EXTRA_HISTORY_CITA_ID = "extra_history_cita_id";

    private String appointmentId = "";
    private boolean chatRequestInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_historial_detalle);
        setupUserBottomNav(R.id.navUserActivity);
        bindData();
        setupActions();
    }

    private void bindData() {
        appointmentId = getIntent().getStringExtra(EXTRA_HISTORY_CITA_ID);
        if (appointmentId == null) appointmentId = "";

        bindText(R.id.tvHistoryDetailTitle,
                getIntent().getStringExtra(EXTRA_HISTORY_TITLE),
                R.string.activity_history_1_title);
        bindText(R.id.tvHistoryDetailDate,
                getIntent().getStringExtra(EXTRA_HISTORY_DATE),
                R.string.activity_history_1_date);
        bindText(R.id.tvHistoryDetailStatus,
                getIntent().getStringExtra(EXTRA_HISTORY_STATUS),
                R.string.history_detail_status_default);
        bindText(R.id.tvHistoryDetailCode,
                getIntent().getStringExtra(EXTRA_HISTORY_CODE),
                R.string.history_detail_code_default);
        bindText(R.id.tvHistoryDetailAmount,
                getIntent().getStringExtra(EXTRA_HISTORY_AMOUNT),
                R.string.history_detail_amount_default);
        bindText(R.id.tvHistoryDetailSummary,
                getIntent().getStringExtra(EXTRA_HISTORY_SUMMARY),
                R.string.activity_history_1_summary);

        TextView primaryBtn = findViewById(R.id.btnHistoryPrimaryAction);
        if (primaryBtn != null) {
            primaryBtn.setText("CONTACTAR AL ASESOR");
        }
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
        View back = findViewById(R.id.btnBackHistoryDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View primary = findViewById(R.id.btnHistoryPrimaryAction);
        if (primary != null) {
            primary.setOnClickListener(v -> openAppointmentChat());
        }

        View secondary = findViewById(R.id.btnHistorySecondaryAction);
        if (secondary != null) {
            secondary.setOnClickListener(v -> openScreen(UsuarioHomeActivity.class));
        }
    }

    private void openAppointmentChat() {
        if (appointmentId.isEmpty()) {
            Toast.makeText(this, "Esta vista no está vinculada a una cita real.", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getUid().trim().isEmpty()) {
            Toast.makeText(this, "Inicia sesión para contactar al asesor.", Toast.LENGTH_LONG).show();
            return;
        }
        String clienteUid = firebaseUser.getUid();
        String cachedUid = AuthSessionManager.getInstance(this).getUid();
        if (cachedUid != null && !cachedUid.trim().isEmpty() && !clienteUid.equals(cachedUid)) {
            Toast.makeText(this, "La sesión cambió. Vuelve a iniciar sesión antes de abrir el chat.", Toast.LENGTH_LONG).show();
            return;
        }
        if (chatRequestInProgress) return;
        chatRequestInProgress = true;
        setPrimaryActionEnabled(false);

        FirebaseFirestore.getInstance().collection("citas").document(appointmentId).get()
                .addOnSuccessListener(document -> {
                    if (isFinishing()) return;
                    if (!document.exists()) {
                        finishChatRequest("La cita ya no está disponible.");
                        return;
                    }
                    String projectId = document.getString("proyectoId");
                    if (projectId == null || projectId.trim().isEmpty()) projectId = document.getString("projectId");
                    if (projectId == null || projectId.trim().isEmpty()) projectId = document.getString("propertyId");

                    if (projectId == null || projectId.trim().isEmpty()) {
                        finishChatRequest("No se pudo identificar el proyecto asociado a la cita.");
                        return;
                    }

                    final String finalProjectId = projectId;
                    new FirebaseAppointmentRepository().getAdvisorsForProject(projectId,
                            new FirebaseAppointmentRepository.AdvisorsCallback() {
                                @Override
                                public void onSuccess(List<FirebaseAppointmentRepository.Advisor> advisors) {
                                    if (isFinishing()) return;
                                    if (advisors.isEmpty()) {
                                        finishChatRequest("Este proyecto no tiene asesores activos asignados.");
                                    } else {
                                        openProjectChat(finalProjectId, advisors.get(0), clienteUid);
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    if (!isFinishing()) finishChatRequest(message);
                                }
                            });
                })
                .addOnFailureListener(error -> {
                    if (!isFinishing()) finishChatRequest("Error al consultar la cita: " + error.getMessage());
                });
    }

    private void openProjectChat(String projectId, FirebaseAppointmentRepository.Advisor selectedAdvisor, String clienteUid) {
        FirebaseChatRepository.ProjectChatContext project = new FirebaseChatRepository.ProjectChatContext(
                projectId,
                getIntent().getStringExtra(EXTRA_HISTORY_TITLE),
                "",
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
                        if (isFinishing()) return;
                        finishChatRequest(null);
                        Intent intent = new Intent(UsuarioHistorialDetalleActivity.this,
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
        View primary = findViewById(R.id.btnHistoryPrimaryAction);
        if (primary != null) {
            primary.setEnabled(enabled);
            primary.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
}
