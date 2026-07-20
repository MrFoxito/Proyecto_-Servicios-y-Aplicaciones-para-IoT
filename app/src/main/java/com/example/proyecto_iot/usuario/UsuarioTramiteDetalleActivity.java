package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UsuarioTramiteDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_TRAMITE_TITLE = "extra_tramite_title";
    public static final String EXTRA_TRAMITE_ID = "extra_tramite_id";
    public static final String EXTRA_TRAMITE_STATUS = "extra_tramite_status";
    public static final String EXTRA_TRAMITE_NOTE = "extra_tramite_note";
    public static final String EXTRA_TRAMITE_DUE = "extra_tramite_due";
    public static final String EXTRA_TRAMITE_CAN_PAY = "extra_tramite_can_pay";
    public static final String EXTRA_TRAMITE_PROJECT_ID = "extra_tramite_project_id";

    private boolean canPay;
    private String statusValue;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_tramite_detalle);
        applyInsets();
        bindData();
        setupActions();
    }

    private void bindData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String title = intent.getStringExtra(EXTRA_TRAMITE_TITLE);
        String id = intent.getStringExtra(EXTRA_TRAMITE_ID);
        statusValue = intent.getStringExtra(EXTRA_TRAMITE_STATUS);
        String note = intent.getStringExtra(EXTRA_TRAMITE_NOTE);
        String due = intent.getStringExtra(EXTRA_TRAMITE_DUE);
        canPay = intent.getBooleanExtra(EXTRA_TRAMITE_CAN_PAY, false);
        projectId = intent.getStringExtra(EXTRA_TRAMITE_PROJECT_ID);

        bindText(R.id.tvTramiteDetailTitle, title, R.string.activity_sep_1_title);
        bindText(R.id.tvTramiteDetailId, id, R.string.activity_sep_1_id);
        bindText(R.id.tvTramiteDetailStatus, statusValue, R.string.activity_sep_1_state);
        bindText(R.id.tvTramiteDetailNote, note, R.string.activity_sep_1_note);
        bindText(R.id.tvTramiteDetailDue, due, R.string.activity_sep_1_due);
        bindDetailState();

    }

    private void bindDetailState() {
        TextView eyebrow = findViewById(R.id.tvTramiteDetailEyebrow);
        TextView statusView = findViewById(R.id.tvTramiteDetailStatus);
        TextView nextTitle = findViewById(R.id.tvTramiteNextTitle);
        TextView nextDescription = findViewById(R.id.tvTramiteNextDescription);
        TextView supportTitle = findViewById(R.id.tvTramiteSupportTitle);
        TextView supportDescription = findViewById(R.id.tvTramiteSupportDescription);

        if (statusView != null) {
            int color = canPay ? R.color.app_accent_gold : R.color.app_text_secondary;
            statusView.setTextColor(ContextCompat.getColor(this, color));
        }

        if (canPay) {
            bindText(R.id.tvTramiteDetailEyebrow, getString(R.string.tramite_detail_summary_ready), R.string.tramite_detail_summary_ready);
            bindText(R.id.tvTramiteNextTitle, getString(R.string.tramite_detail_next_title_pay), R.string.tramite_detail_next_title_pay);
            bindText(R.id.tvTramiteNextDescription, getString(R.string.tramite_detail_next_description_pay), R.string.tramite_detail_next_description_pay);
            bindText(R.id.tvTramiteSupportTitle, getString(R.string.tramite_detail_support_title_pay), R.string.tramite_detail_support_title_pay);
            bindText(R.id.tvTramiteSupportDescription, getString(R.string.tramite_detail_support_description_pay), R.string.tramite_detail_support_description_pay);
            return;
        }

        bindText(R.id.tvTramiteDetailEyebrow, getString(R.string.tramite_detail_summary_review), R.string.tramite_detail_summary_review);
        bindText(R.id.tvTramiteNextTitle, getString(R.string.tramite_detail_next_title_review), R.string.tramite_detail_next_title_review);
        bindText(R.id.tvTramiteNextDescription, getString(R.string.tramite_detail_next_description_review), R.string.tramite_detail_next_description_review);
        bindText(R.id.tvTramiteSupportTitle, getString(R.string.tramite_detail_support_title_review), R.string.tramite_detail_support_title_review);
        bindText(R.id.tvTramiteSupportDescription, getString(R.string.tramite_detail_support_description_review), R.string.tramite_detail_support_description_review);
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
        View back = findViewById(R.id.btnBackTramiteDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        TextView actionButton = findViewById(R.id.btnTramitePrimaryAction);
        if (actionButton == null) {
            return;
        }

        if (canPay) {
            actionButton.setText("CANCELAR SEPARACIÓN");
            if (actionButton instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton mb = (com.google.android.material.button.MaterialButton) actionButton;
                mb.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DC2626")));
                mb.setTextColor(android.graphics.Color.WHITE);
            }
            actionButton.setOnClickListener(v -> confirmCancellation());
        } else {
            actionButton.setText(R.string.tramite_detail_action_contact);
            if (actionButton instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton mb = (com.google.android.material.button.MaterialButton) actionButton;
                mb.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(this, R.color.app_brand_navy));
                mb.setTextColor(android.graphics.Color.WHITE);
            }
            actionButton.setOnClickListener(v -> contactAdvisorForProject());
        }
    }

    private void confirmCancellation() {
        if (isFinishing() || isDestroyed()) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancelar separación")
                .setMessage("¿Estás seguro que deseas cancelar esta separación? La unidad será liberada.")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, cancelar", (dialog, which) -> cancelSeparation())
                .show();
    }

    private void cancelSeparation() {
        if (isFinishing() || isDestroyed()) return;
        String separacionId = getIntent() != null ? getIntent().getStringExtra(EXTRA_TRAMITE_ID) : "";
        if (separacionId == null || separacionId.trim().isEmpty()) {
            Toast.makeText(this, "ID de separación no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView btn = findViewById(R.id.btnTramitePrimaryAction);
        if (btn != null) btn.setEnabled(false);

        new com.example.proyecto_iot.data.FirebaseSeparationRepository().cancelTemporarySeparation(
                separacionId, 
                new com.example.proyecto_iot.data.FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String id) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(UsuarioTramiteDetalleActivity.this, "Separación cancelada. La unidad fue liberada.", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                if (btn != null) btn.setEnabled(true);
                Toast.makeText(UsuarioTramiteDetalleActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void contactAdvisorForProject() {
        if (isFinishing() || isDestroyed()) return;
        String clientUid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        String clientName = AuthSessionManager.getInstance(this).getUserName();
        if (clientUid.isEmpty()) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (projectId != null && !projectId.trim().isEmpty()) {
            openProjectChatForProject(clientUid, clientName, projectId);
            return;
        }

        String separacionId = getIntent() != null ? getIntent().getStringExtra(EXTRA_TRAMITE_ID) : "";
        if (separacionId != null && !separacionId.trim().isEmpty()) {
            FirebaseFirestore.getInstance().collection("separaciones").document(separacionId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (snapshot.exists()) {
                            String foundProjectId = snapshot.getString("propertyId");
                            if (foundProjectId == null || foundProjectId.trim().isEmpty()) foundProjectId = snapshot.getString("projectId");
                            if (foundProjectId == null || foundProjectId.trim().isEmpty()) foundProjectId = snapshot.getString("proyectoId");
                            if (foundProjectId != null && !foundProjectId.trim().isEmpty()) {
                                openProjectChatForProject(clientUid, clientName, foundProjectId);
                                return;
                            }
                        }
                        Toast.makeText(UsuarioTramiteDetalleActivity.this, "No se encontró el proyecto asociado a esta separación.", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing()) Toast.makeText(UsuarioTramiteDetalleActivity.this, "Error al cargar datos del proyecto.", Toast.LENGTH_SHORT).show();
                    });
            return;
        }
        Toast.makeText(this, "Información de proyecto no disponible para esta separación.", Toast.LENGTH_SHORT).show();
    }

    private void openProjectChatForProject(String clientUid, String clientName, String resolvedProjectId) {
        FirebaseAppointmentRepository appointmentRepo = new FirebaseAppointmentRepository();
        FirebaseChatRepository chatRepo = new FirebaseChatRepository();
        appointmentRepo.getAdvisorsForProject(resolvedProjectId, new FirebaseAppointmentRepository.AdvisorsCallback() {
            @Override
            public void onSuccess(List<FirebaseAppointmentRepository.Advisor> advisors) {
                if (isFinishing() || isDestroyed() || advisors.isEmpty()) {
                    if (!isFinishing()) Toast.makeText(UsuarioTramiteDetalleActivity.this, "Este proyecto no tiene un asesor asignado.", Toast.LENGTH_LONG).show();
                    return;
                }
                FirebaseAppointmentRepository.Advisor advisor = advisors.get(0);
                FirebaseChatRepository.Advisor chatAdvisor = new FirebaseChatRepository.Advisor(
                        advisor.uid, advisor.name, "", "sa_profile_asesor_1", advisor.assignmentId);
                FirebaseChatRepository.ProjectChatContext context = new FirebaseChatRepository.ProjectChatContext(
                        resolvedProjectId,
                        getIntent() != null && getIntent().getStringExtra(EXTRA_TRAMITE_TITLE) != null ? getIntent().getStringExtra(EXTRA_TRAMITE_TITLE) : "Proyecto",
                        "", "", ""
                );
                chatRepo.findOrCreateProjectConversation(clientUid, clientName, chatAdvisor, context, new FirebaseChatRepository.ConversationCallback() {
                    @Override
                    public void onSuccess(FirebaseChatRepository.Conversation conversation) {
                        if (isFinishing() || isDestroyed()) return;
                        Intent intent = new Intent(UsuarioTramiteDetalleActivity.this, UsuarioChatDetalleActivity.class);
                        UsuarioChatDetalleActivity.putConversationExtras(intent, conversation);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) Toast.makeText(UsuarioTramiteDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (!isFinishing()) Toast.makeText(UsuarioTramiteDetalleActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.tramiteDetailRoot);
        if (root == null) {
            return;
        }

        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
