package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;

public class UsuarioCitaDetalleActivity extends BaseUsuarioActivity {
    public static final String EXTRA_APPOINTMENT_TITLE = "extra_appointment_title";
    public static final String EXTRA_APPOINTMENT_STATUS = "extra_appointment_status";
    public static final String EXTRA_APPOINTMENT_DATE = "extra_appointment_date";
    public static final String EXTRA_APPOINTMENT_ADVISOR = "extra_appointment_advisor";
    public static final String EXTRA_APPOINTMENT_LOCATION = "extra_appointment_location";
    public static final String EXTRA_APPOINTMENT_NOTE = "extra_appointment_note";
    public static final String EXTRA_APPOINTMENT_CONFIRMED = "extra_appointment_confirmed";

    private boolean confirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_detalle);
        setupUserBottomNav(R.id.navUserActivity);
        bindData();
        setupActions();
    }

    private void bindData() {
        confirmed = getIntent().getBooleanExtra(EXTRA_APPOINTMENT_CONFIRMED, true);

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

        TextView primaryAction = findViewById(R.id.btnAppointmentPrimaryAction);
        if (primaryAction != null) {
            primaryAction.setText(confirmed
                    ? R.string.appointment_detail_primary_confirmed
                    : R.string.appointment_detail_primary_pending);
            primaryAction.setOnClickListener(v -> openScreen(UsuarioChatsActivity.class));
        }

        TextView secondaryAction = findViewById(R.id.btnAppointmentSecondaryAction);
        if (secondaryAction != null) {
            secondaryAction.setText(confirmed
                    ? R.string.appointment_detail_secondary_confirmed
                    : R.string.appointment_detail_secondary_pending);
            secondaryAction.setOnClickListener(v -> openScreen(UsuarioMapaExploracionActivity.class));
        }
    }
}
