package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

public class UsuarioReservaPagoActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_TITLE    = "extra_reserva_property_title";
    public static final String EXTRA_PROPERTY_PRICE    = "extra_reserva_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_reserva_property_location";

    private String propertyTitle;
    private String propertyPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_reserva_pago);
        applyInsets();
        bindPropertyData();
        setupActions();
    }

    private void bindPropertyData() {
        Intent intent = getIntent();
        propertyTitle    = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_TITLE)    : null;
        propertyPrice    = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_PRICE)    : null;
        String propertyLocation = intent != null ? intent.getStringExtra(EXTRA_PROPERTY_LOCATION) : null;

        // Fallbacks si no vienen datos
        if (propertyTitle == null || propertyTitle.isEmpty()) {
            propertyTitle = getString(R.string.reserva_title);
        }
        if (propertyPrice == null || propertyPrice.isEmpty()) {
            propertyPrice = getString(R.string.reserva_property_value);
        }

        // Muestra el título de la propiedad en el hero
        TextView tvTitle = findViewById(R.id.tvReservaPropertyTitle);
        if (tvTitle != null) tvTitle.setText(propertyTitle);

        // Muestra el precio real
        TextView tvPrice = findViewById(R.id.tvReservaPropertyValue);
        if (tvPrice != null) tvPrice.setText(propertyPrice);

        // Muestra la ubicación si viene
        if (propertyLocation != null && !propertyLocation.isEmpty()) {
            TextView tvLocation = findViewById(R.id.tvReservaPropertyLocation);
            if (tvLocation != null) tvLocation.setText(propertyLocation);
        }
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackReservaPago);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View pay = findViewById(R.id.btnProcederPago);
        if (pay != null) {
            pay.setOnClickListener(v -> procesarPago());
        }
    }

    private void procesarPago() {
        AuthSessionManager session = new AuthSessionManager(this);
        String clienteId = session.getUserId();
        LocalSchemaStorage storage = new LocalSchemaStorage(this);

        // 1. Crea el trámite (Separaciones en curso)
        String tramiteId = storage.addTramite(clienteId, propertyTitle, propertyPrice);

        // 2. Crea el historial (Historial reciente)
        storage.addHistorial(clienteId, propertyTitle, propertyPrice, tramiteId);

        // 3. Capa 1 — notificación in-app (aparece en la pantalla de Notificaciones)
        storage.addNotificacionPago(clienteId, propertyTitle, propertyPrice, tramiteId);

        // 4. Capa 2 — notificación push del sistema Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 102);
            }
        }
        Intent tapIntent = new Intent(this, UsuarioActividadActivity.class);
        NotificationHelper.notifyPago(this, propertyTitle, propertyPrice, tapIntent);

        // 5. Feedback al usuario
        Toast.makeText(this, "Pago procesado correctamente", Toast.LENGTH_SHORT).show();

        // 6. Navega a Mi Actividad donde verá los nuevos items al instante
        Intent intent = new Intent(this, UsuarioActividadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.reservaPagoRoot);
        if (root == null) return;
        final int left   = root.getPaddingLeft();
        final int top    = root.getPaddingTop();
        final int right  = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top,
                    right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
