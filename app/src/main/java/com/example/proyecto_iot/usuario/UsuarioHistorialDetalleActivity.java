package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.proyecto_iot.R;

public class UsuarioHistorialDetalleActivity extends BaseUsuarioActivity {
    public static final String EXTRA_HISTORY_TITLE = "extra_history_title";
    public static final String EXTRA_HISTORY_DATE = "extra_history_date";
    public static final String EXTRA_HISTORY_STATUS = "extra_history_status";
    public static final String EXTRA_HISTORY_CODE = "extra_history_code";
    public static final String EXTRA_HISTORY_AMOUNT = "extra_history_amount";
    public static final String EXTRA_HISTORY_SUMMARY = "extra_history_summary";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_historial_detalle);
        setupUserBottomNav(R.id.navUserActivity);
        bindData();
        setupActions();
    }

    private void bindData() {
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
            primary.setOnClickListener(v -> openScreen(UsuarioChatsActivity.class));
        }

        View secondary = findViewById(R.id.btnHistorySecondaryAction);
        if (secondary != null) {
            secondary.setOnClickListener(v -> openScreen(UsuarioHomeActivity.class));
        }
    }
}
