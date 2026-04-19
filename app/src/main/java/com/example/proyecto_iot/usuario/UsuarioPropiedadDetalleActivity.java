package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;

public class UsuarioPropiedadDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_PROPERTY_TITLE = "extra_property_title";
    public static final String EXTRA_PROPERTY_PRICE = "extra_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedad_detalle);
        applyInsets();
        bindDynamicPropertyData();

        View back = findViewById(R.id.btnBackPropertyDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View schedule = findViewById(R.id.btnAgendarCita);
        if (schedule != null) {
            schedule.setOnClickListener(v ->
                    Toast.makeText(this, R.string.property_schedule_sent, Toast.LENGTH_SHORT).show());
        }

        View separate = findViewById(R.id.btnSepararInmueble);
        if (separate != null) {
            separate.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioReservaPagoActivity.class)));
        }
    }

    private void bindDynamicPropertyData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String title = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String price = intent.getStringExtra(EXTRA_PROPERTY_PRICE);
        String location = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);

        TextView titleView = findViewById(R.id.propertyHeroTitle);
        TextView priceView = findViewById(R.id.propertyPriceText);
        TextView locationView = findViewById(R.id.propertyLocationText);

        if (titleView != null && title != null && !title.trim().isEmpty()) {
            titleView.setText(title);
        }
        if (priceView != null && price != null && !price.trim().isEmpty()) {
            priceView.setText(price);
        }
        if (locationView != null && location != null && !location.trim().isEmpty()) {
            locationView.setText(location);
        }
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.propertyDetailRoot);
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
