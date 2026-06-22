package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;

import android.content.Intent;
import android.os.Bundle;

public class AsesorPagoAprobadoActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_pago_aprobado);

        setupBackButton();
        findViewById(R.id.btnVolverDashboardSeparaciones).setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorSeparacionesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }
}
