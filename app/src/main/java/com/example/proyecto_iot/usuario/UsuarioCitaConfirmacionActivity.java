package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.NotificationHelper;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

public class UsuarioCitaConfirmacionActivity extends AppCompatActivity {

    private static final int REQ_NOTIF_PERMISSION = 101;

    // Guardamos los datos para usarlos en el callback del permiso
    private String pendingPropertyTitle;
    private String pendingDate;
    private String pendingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cita_confirmacion);
        NotificationHelper.init(this);
        applyInsets();
        bindData();
        setupActions();
    }

    private void bindData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String propertyTitle    = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_PROPERTY_TITLE);
        String propertyLocation = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_PROPERTY_LOCATION);
        String date             = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_DATE);
        String time             = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_TIME);
        String note             = intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_NOTE);

        bindText(R.id.tvAppointmentConfirmPropertyTitle,    propertyTitle);
        bindText(R.id.tvAppointmentConfirmPropertyLocation, propertyLocation);
        bindText(R.id.tvAppointmentConfirmDate,    date);
        bindText(R.id.tvAppointmentConfirmTime,    time);
        bindText(R.id.tvAppointmentConfirmContact, intent.getStringExtra(UsuarioAgendarCitaActivity.EXTRA_APPOINTMENT_CONTACT));
        bindText(R.id.tvAppointmentConfirmNote,    note);

        AuthSessionManager session = new AuthSessionManager(this);
        String clienteId = session.getUserId();
        LocalSchemaStorage storage = new LocalSchemaStorage(this);

        // Guarda la cita en el storage
        storage.addCita(
                clienteId,
                session.getUserName(),
                propertyTitle,
                date,
                time,
                "Lobby principal - " + (propertyLocation != null ? propertyLocation : ""),
                note,
                "Elena Valdes",
                "user_featured_house"
        );

        // Capa 1 — notificación in-app (siempre, no depende de permisos)
        storage.addNotificacion(
                clienteId,
                "visit",
                "Cita registrada",
                "Tu visita a " + (propertyTitle != null ? propertyTitle : "la propiedad")
                        + " esta programada para el " + (date != null ? date : "")
                        + " a las " + (time != null ? time : "") + ".",
                "",
                "appointment"
        );

        // Guardamos para usar en el push (después del permiso)
        pendingPropertyTitle = propertyTitle != null ? propertyTitle : "Propiedad";
        pendingDate          = date != null ? date : "";
        pendingTime          = time != null ? time : "";

        // Capa 2 — notificación push del sistema
        launchPushNotification();
    }

    /**
     * Lanza la notificación push.
     * Si el permiso no está concedido en Android 13+, lo solicita primero.
     * El push se lanza en onRequestPermissionsResult si el usuario lo aprueba.
     */
    private void launchPushNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicita el permiso — el push se lanzará en el callback
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF_PERMISSION);
                return;
            }
        }
        // Permiso ya concedido (o Android < 13) — lanza directo
        firePush();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Usuario concedió el permiso — lanza el push ahora
            firePush();
        }
        // Si denegó, la notificación in-app ya fue guardada igual
    }

    private void firePush() {
        Intent tapIntent = new Intent(this, UsuarioActividadActivity.class);
        NotificationHelper.notifyCita(this, pendingPropertyTitle, pendingDate, pendingTime, tapIntent);
    }

    private void bindText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null && value != null && !value.trim().isEmpty()) {
            view.setText(value);
        }
    }

    private void setupActions() {
        View goActivity = findViewById(R.id.btnGoToActivityFromAppointment);
        if (goActivity != null) {
            goActivity.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioActividadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View continueExplore = findViewById(R.id.btnContinueExploreFromAppointment);
        if (continueExplore != null) {
            continueExplore.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.appointmentConfirmRoot);
        if (root == null) return;

        final int left   = root.getPaddingLeft();
        final int top    = root.getPaddingTop();
        final int right  = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}

