package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;

public class UsuarioTramiteDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_TRAMITE_TITLE = "extra_tramite_title";
    public static final String EXTRA_TRAMITE_ID = "extra_tramite_id";
    public static final String EXTRA_TRAMITE_STATUS = "extra_tramite_status";
    public static final String EXTRA_TRAMITE_NOTE = "extra_tramite_note";
    public static final String EXTRA_TRAMITE_DUE = "extra_tramite_due";
    public static final String EXTRA_TRAMITE_CAN_PAY = "extra_tramite_can_pay";

    private boolean canPay;
    private String statusValue;

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
            actionButton.setText(R.string.tramite_detail_action_pay);
            actionButton.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioReservaPagoActivity.class)));
        } else {
            actionButton.setText(R.string.tramite_detail_action_contact);
            actionButton.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioChatsActivity.class)));
        }
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
