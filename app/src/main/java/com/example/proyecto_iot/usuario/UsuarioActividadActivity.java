package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

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
        return new LocalSchemaStorage(this).getUserAppointments();
    }

    private List<UsuarioTramiteItem> buildTramites() {
        return new LocalSchemaStorage(this).getUserTramites();
    }

    private List<UsuarioHistoryItem> buildHistory() {
        return new LocalSchemaStorage(this).getUserHistory();
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
