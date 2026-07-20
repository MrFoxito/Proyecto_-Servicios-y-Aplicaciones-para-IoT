package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;

/** Read-only client context deliberately sourced from the appointment, not from arbitrary profiles. */
public class AsesorClienteDetalleActivity extends BaseAsesorActivity {
    public static final String EXTRA_CLIENTE_ID = "extra_cliente_id";
    public static final String EXTRA_CLIENTE_NOMBRE = "extra_cliente_nombre";
    public static final String EXTRA_CITA_ID = "extra_cita_id";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_cliente_detalle);
        setupBackButton();
        String clientId = getIntent().getStringExtra(EXTRA_CLIENTE_ID);
        String name = getIntent().getStringExtra(EXTRA_CLIENTE_NOMBRE);
        String appointmentId = getIntent().getStringExtra(EXTRA_CITA_ID);
        if (clientId == null || clientId.trim().isEmpty() || appointmentId == null || appointmentId.trim().isEmpty()) {
            Toast.makeText(this, "No se pudo abrir el contexto del cliente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ((TextView) findViewById(R.id.txtClienteDetalleNombre)).setText(
                name == null || name.trim().isEmpty() ? "Cliente" : name.trim());
        ((TextView) findViewById(R.id.txtClienteDetalleContexto)).setText(
                "Relacionado con la cita " + appointmentId);
        ((TextView) findViewById(R.id.txtClienteDetalleMensaje)).setText(
                "Los datos de contacto se protegen y solo están disponibles dentro de la conversación autorizada.");
    }
}
