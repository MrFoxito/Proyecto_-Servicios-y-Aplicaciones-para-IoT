package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;

import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.List;

public class UsuarioActividadActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_actividad);
        setupUserBottomNav(R.id.navUserActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga cada vez que la pantalla vuelve a ser visible
        loadAppointments();
        loadTramites();
        loadHistory();
    }

    private String getClienteId() {
        return new AuthSessionManager(this).getUserId();
    }

    private void loadAppointments() {
        RecyclerView recyclerView = findViewById(R.id.recyclerAppointments);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        new FirebaseAppointmentRepository().readUserAppointments(getClienteId(), new FirebaseAppointmentRepository.UserAppointmentsCallback() {
            @Override
            public void onSuccess(List<UsuarioAppointmentItem> items) {
                recyclerView.setAdapter(new UsuarioAppointmentAdapter(items, UsuarioActividadActivity.this::openAppointmentDetail));
            }

            @Override
            public void onError(String message) {
                // Fallback to local if error or empty (optional, but requested to connect to firebase)
                List<UsuarioAppointmentItem> items = new LocalSchemaStorage(UsuarioActividadActivity.this).getUserAppointments(getClienteId());
                recyclerView.setAdapter(new UsuarioAppointmentAdapter(items, UsuarioActividadActivity.this::openAppointmentDetail));
            }
        });
    }

    private void loadTramites() {
        RecyclerView recyclerView = findViewById(R.id.recyclerTramites);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        new com.example.proyecto_iot.data.FirebaseSeparationRepository().readUserSeparations(getClienteId(), new com.example.proyecto_iot.data.FirebaseSeparationRepository.UserTramitesCallback() {
            @Override
            public void onSuccess(List<UsuarioTramiteItem> items) {
                recyclerView.setAdapter(new UsuarioTramiteAdapter(items, UsuarioActividadActivity.this::openTramiteDetail));
            }

            @Override
            public void onError(String message) {
                List<UsuarioTramiteItem> items = new LocalSchemaStorage(UsuarioActividadActivity.this).getUserTramites(getClienteId());
                recyclerView.setAdapter(new UsuarioTramiteAdapter(items, UsuarioActividadActivity.this::openTramiteDetail));
            }
        });
    }

    private void loadHistory() {
        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        new FirebaseAppointmentRepository().readUserHistory(getClienteId(), new FirebaseAppointmentRepository.UserHistoryCallback() {
            @Override
            public void onSuccess(List<UsuarioHistoryItem> items) {
                recyclerView.setAdapter(new UsuarioHistoryAdapter(items, UsuarioActividadActivity.this::openHistoryDetail));
            }

            @Override
            public void onError(String message) {
                List<UsuarioHistoryItem> items = new LocalSchemaStorage(UsuarioActividadActivity.this).getUserHistory(getClienteId());
                recyclerView.setAdapter(new UsuarioHistoryAdapter(items, UsuarioActividadActivity.this::openHistoryDetail));
            }
        });
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
