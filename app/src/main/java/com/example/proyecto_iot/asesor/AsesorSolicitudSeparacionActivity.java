package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import android.os.Bundle;
import android.widget.TextView;

public class AsesorSolicitudSeparacionActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_solicitud_separacion);

        setupBackButton();
        setupDynamicData();

        findViewById(R.id.btnAprobarPagoSolicitud).setOnClickListener(v -> openScreen(AsesorPagoAprobadoActivity.class));
        findViewById(R.id.btnRechazarSolicitud).setOnClickListener(v -> showPendingToast());
    }

    private void setupDynamicData() {
        // Obtenemos referencias a los campos
        TextView txtReservaId = findViewById(R.id.txtSolReservaId);
        TextView txtCliente = findViewById(R.id.txtSolCliente);
        TextView txtClienteSub = findViewById(R.id.txtSolClienteSub);
        TextView txtProyecto = findViewById(R.id.txtSolProyecto);
        TextView txtPropiedad = findViewById(R.id.txtSolPropiedad);
        TextView txtPrecioTotal = findViewById(R.id.txtSolPrecioTotal);
        TextView txtMontoSeparacion = findViewById(R.id.txtSolMontoSeparacion);

        // Simulamos datos coherentes traídos de la base de datos o el intent
        txtReservaId.setText("#EE-2024-1025");
        txtCliente.setText("Alicia Velarde");
        txtClienteSub.setText("a.velarde@empresa.com");
        txtProyecto.setText("Altos del Bosque");
        txtPropiedad.setText("Penthouse Tipo 1");
        txtPrecioTotal.setText("$480,000");
        txtMontoSeparacion.setText("$20,000");
    }
}
