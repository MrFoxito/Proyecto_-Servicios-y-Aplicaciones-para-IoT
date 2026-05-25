package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.List;

public class UsuarioNotificacionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_notificaciones);
        applyInsets();
        setupActions();
        setupNotificationList();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackNotifications);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
    }

    private void setupNotificationList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerNotifications);
        if (recyclerView == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UsuarioNotificationAdapter(buildNotifications(), this::openNotificationAction));
    }

    private List<UsuarioNotificationItem> buildNotifications() {
        String clienteId = new AuthSessionManager(this).getUserId();
        return new LocalSchemaStorage(this).getUserNotifications(clienteId);
    }

    private void openNotificationAction(UsuarioNotificationItem item) {
        if (item.getActionType() == UsuarioNotificationItem.ACTION_PAYMENT) {
            if (item.hasTramiteData()) {
                Intent intent = new Intent(this, UsuarioTramiteDetalleActivity.class);
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_TITLE, item.getTramiteTitle());
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_ID, item.getTramiteId());
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_STATUS, item.getTramiteStatus());
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_NOTE, item.getTramiteNote());
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_DUE, item.getTramiteDue());
                intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_CAN_PAY, item.canPayTramite());
                startActivity(intent);
            } else {
                startActivity(new Intent(this, UsuarioActividadActivity.class));
            }
            return;
        }

        Intent intent = new Intent(this, UsuarioCitaDetalleActivity.class);

        // Si la notificación tiene datos reales de cita, los usa
        if (item.hasCitaData()) {
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_TITLE,    item.getCitaTitle());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_STATUS,   item.getCitaStatus());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_DATE,     item.getCitaDate());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_ADVISOR,  item.getCitaAdvisor());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_LOCATION, item.getCitaLocation());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_NOTE,     item.getCitaNote());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_CONFIRMED, item.isCitaConfirmed());
        } else {
            // Fallback con strings genéricos si no hay cita en el storage
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_TITLE,    item.getTitle());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_STATUS,   "CONFIRMADA");
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_DATE,     item.getTime());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_ADVISOR,  "Elena Valdes");
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_LOCATION, "Lobby principal");
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_NOTE,     item.getBody());
            intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_CONFIRMED, true);
        }
        startActivity(intent);
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.notificationsRoot);
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
