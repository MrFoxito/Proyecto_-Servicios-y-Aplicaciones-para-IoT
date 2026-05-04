package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyecto_iot.R;

public class UsuarioMetodosPagoActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_metodos_pago);
        setupUserBottomNav(R.id.navUserProfile);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPaymentMethods);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View addMethod = findViewById(R.id.btnAddPaymentMethod);
        if (addMethod != null) {
            addMethod.setOnClickListener(v ->
                    Toast.makeText(this, R.string.profile_payment_add_toast, Toast.LENGTH_SHORT).show());
        }
    }
}
