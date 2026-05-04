package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.ArrayList;
import java.util.List;

public class UsuarioActividadActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_actividad);
        setupUserBottomNav(R.id.navUserActivity);
        setupAppointments();
        setupTramites();
        setupHistory();
    }

    private void setupAppointments() {
        RecyclerView recyclerView = findViewById(R.id.recyclerAppointments);
        if (recyclerView == null) {
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new UsuarioAppointmentAdapter(buildAppointments(), this::openAppointmentDetail));
    }

    private void setupTramites() {
        RecyclerView recyclerView = findViewById(R.id.recyclerTramites);
        if (recyclerView == null) {
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new UsuarioTramiteAdapter(buildTramites(), this::openTramiteDetail));
    }

    private void setupHistory() {
        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        if (recyclerView == null) {
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new UsuarioHistoryAdapter(buildHistory(), this::openHistoryDetail));
    }

    private List<UsuarioAppointmentItem> buildAppointments() {
        List<UsuarioAppointmentItem> items = new ArrayList<>();
        items.add(new UsuarioAppointmentItem(
                getString(R.string.activity_card_1_title),
                getString(R.string.activity_card_1_status),
                getString(R.string.activity_card_1_datetime),
                getString(R.string.activity_card_1_advisor),
                R.drawable.user_featured_house,
                getString(R.string.activity_appointment_location_1),
                getString(R.string.activity_appointment_note_1),
                true
        ));
        items.add(new UsuarioAppointmentItem(
                getString(R.string.activity_card_2_title),
                getString(R.string.activity_appointment_status_2),
                getString(R.string.activity_card_2_datetime),
                getString(R.string.activity_card_2_advisor),
                R.drawable.user_popular_2,
                getString(R.string.activity_appointment_location_2),
                getString(R.string.activity_appointment_note_2),
                false
        ));
        return items;
    }

    private List<UsuarioTramiteItem> buildTramites() {
        List<UsuarioTramiteItem> items = new ArrayList<>();
        items.add(new UsuarioTramiteItem(
                getString(R.string.activity_sep_1_title),
                getString(R.string.activity_sep_1_id),
                getString(R.string.activity_sep_1_state),
                getString(R.string.activity_sep_1_note),
                getString(R.string.activity_sep_1_due),
                true
        ));
        items.add(new UsuarioTramiteItem(
                getString(R.string.activity_sep_2_title),
                getString(R.string.activity_sep_2_id),
                getString(R.string.activity_sep_2_state),
                getString(R.string.activity_sep_2_note),
                getString(R.string.activity_tramite_secondary_due),
                false
        ));
        return items;
    }

    private List<UsuarioHistoryItem> buildHistory() {
        List<UsuarioHistoryItem> items = new ArrayList<>();
        items.add(new UsuarioHistoryItem(
                getString(R.string.activity_history_1_badge),
                getString(R.string.activity_history_1_title),
                getString(R.string.activity_history_1_date),
                getString(R.string.activity_history_1_summary),
                getString(R.string.history_detail_status_default),
                getString(R.string.history_detail_code_default),
                getString(R.string.history_detail_amount_default)
        ));
        return items;
    }

    private void openAppointmentDetail(UsuarioAppointmentItem item) {
        Intent intent = new Intent(this, UsuarioCitaDetalleActivity.class);
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_TITLE, item.getTitle());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_STATUS, item.getStatus());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_DATE, item.getDateTime());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_ADVISOR, item.getAdvisor());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_LOCATION, item.getLocation());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_NOTE, item.getNote());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_CONFIRMED, item.isConfirmed());
        startActivity(intent);
    }

    private void openTramiteDetail(UsuarioTramiteItem item) {
        Intent intent = new Intent(this, UsuarioTramiteDetalleActivity.class);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_TITLE, item.getTitle());
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_ID, item.getId());
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_STATUS, item.getStatus());
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_NOTE, item.getNote());
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_DUE, item.getDue());
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_CAN_PAY, item.canPay());
        startActivity(intent);
    }

    private void openHistoryDetail(UsuarioHistoryItem item) {
        Intent intent = new Intent(this, UsuarioHistorialDetalleActivity.class);
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_TITLE, item.getTitle());
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_DATE, item.getDate());
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_STATUS, item.getStatus());
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_CODE, item.getCode());
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_AMOUNT, item.getAmount());
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_SUMMARY, item.getSummary());
        startActivity(intent);
    }
}
