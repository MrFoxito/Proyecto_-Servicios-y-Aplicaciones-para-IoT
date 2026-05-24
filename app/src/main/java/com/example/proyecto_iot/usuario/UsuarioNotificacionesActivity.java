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
        return new LocalSchemaStorage(this).getUserNotifications();
    }

    private void openNotificationAction(UsuarioNotificationItem item) {
        if (item.getActionType() == UsuarioNotificationItem.ACTION_PAYMENT) {
            startActivity(new Intent(this, UsuarioReservaPagoActivity.class));
            return;
        }

        Intent intent = new Intent(this, UsuarioCitaDetalleActivity.class);
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_TITLE, getString(R.string.activity_card_1_title));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_STATUS, getString(R.string.activity_card_1_status));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_DATE, getString(R.string.activity_card_1_datetime));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_ADVISOR, getString(R.string.activity_card_1_advisor));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_LOCATION, getString(R.string.activity_appointment_location_1));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_NOTE, getString(R.string.activity_appointment_note_1));
        intent.putExtra(UsuarioCitaDetalleActivity.EXTRA_APPOINTMENT_CONFIRMED, true);
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
