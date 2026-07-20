package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;
import java.util.List;

public class UsuarioActividadActivity extends BaseUsuarioActivity {

    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();
    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
    private ListenerRegistration appointmentsListener;
    private ListenerRegistration separationsListener;
    private ListenerRegistration historyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_actividad);
        setupUserBottomNav(R.id.navUserActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRealtimeListeners();
    }

    @Override
    protected void onPause() {
        stopRealtimeListeners();
        super.onPause();
    }

    private String getClienteId() {
        return AuthSessionManager.getInstance(this).getUid();
    }

    private void startRealtimeListeners() {
        if (appointmentsListener == null) {
            listenAppointments();
        }
        if (historyListener == null) {
            listenHistory();
        }
        if (separationsListener == null) {
            listenTramites();
        }
    }

    private void stopRealtimeListeners() {
        if (appointmentsListener != null) {
            appointmentsListener.remove();
            appointmentsListener = null;
        }
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }
        if (separationsListener != null) {
            separationsListener.remove();
            separationsListener = null;
        }
    }

    private void listenAppointments() {
        RecyclerView recyclerView = findViewById(R.id.recyclerAppointments);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        appointmentsListener = appointmentRepository.listenUserAppointments(getClienteId(), new FirebaseAppointmentRepository.UserAppointmentsCallback() {
            @Override
            public void onSuccess(List<UsuarioAppointmentItem> items) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioAppointmentAdapter(items, UsuarioActividadActivity.this::openAppointmentDetail));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioAppointmentAdapter(Collections.emptyList(),
                        UsuarioActividadActivity.this::openAppointmentDetail));
                Toast.makeText(UsuarioActividadActivity.this,
                        "No se pudieron actualizar las citas. Intenta nuevamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenTramites() {
        RecyclerView recyclerView = findViewById(R.id.recyclerTramites);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        separationsListener = separationRepository.listenUserSeparations(getClienteId(), new FirebaseSeparationRepository.UserTramitesCallback() {
            @Override
            public void onSuccess(List<UsuarioTramiteItem> items) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioTramiteAdapter(items, UsuarioActividadActivity.this::openTramiteDetail));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioTramiteAdapter(Collections.emptyList(),
                        UsuarioActividadActivity.this::openTramiteDetail));
                Toast.makeText(UsuarioActividadActivity.this,
                        "No se pudieron cargar las separaciones. Intenta nuevamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenHistory() {
        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        if (recyclerView == null) return;
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }
        
        historyListener = appointmentRepository.listenUserHistory(getClienteId(), new FirebaseAppointmentRepository.UserHistoryCallback() {
            @Override
            public void onSuccess(List<UsuarioHistoryItem> items) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioHistoryAdapter(items, UsuarioActividadActivity.this::openHistoryDetail));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                recyclerView.setAdapter(new UsuarioHistoryAdapter(Collections.emptyList(),
                        UsuarioActividadActivity.this::openHistoryDetail));
                Toast.makeText(UsuarioActividadActivity.this,
                        "No se pudo actualizar el historial. Intenta nuevamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openAppointmentDetail(UsuarioAppointmentItem item) {
        Intent intent = new Intent(this, UsuarioCitaDetalleActivity.class);
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_ID, item.getAppointmentId());
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_PROJECT_ID, item.getProjectId());
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
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_PROJECT_ID, item.getProjectId());
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
        intent.putExtra(UsuarioHistorialDetalleActivity.EXTRA_HISTORY_CITA_ID, item.getCitaId());
        startActivity(intent);
    }
}
