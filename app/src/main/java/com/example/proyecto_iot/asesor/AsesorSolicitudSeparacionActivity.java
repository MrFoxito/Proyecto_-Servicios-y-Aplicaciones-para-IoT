package com.example.proyecto_iot.asesor;
 
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.Separacion;
 
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
 
public class AsesorSolicitudSeparacionActivity extends BaseAsesorActivity {
 
    private String separacionId;
    private Separacion separacionActual;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_solicitud_separacion);
 
        separacionId = getIntent().getStringExtra("extra_separacion_id");
 
        setupBackButton();
        setupDynamicData();
 
        findViewById(R.id.btnAprobarPagoSolicitud).setOnClickListener(v -> {
            if (separacionActual != null) {
                new LocalSchemaStorage(this).updateSeparacionStatus(separacionActual.getId(), "Aprobado");
                Toast.makeText(this, "Pago aprobado correctamente", Toast.LENGTH_SHORT).show();
            }
            openScreen(AsesorPagoAprobadoActivity.class);
        });
 
        findViewById(R.id.btnRechazarSolicitud).setOnClickListener(v -> {
            if (separacionActual != null) {
                new LocalSchemaStorage(this).updateSeparacionStatus(separacionActual.getId(), "Rechazado");
                Toast.makeText(this, "Solicitud de separación rechazada", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }
 
    private void setupDynamicData() {
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        if (separacionId != null && !separacionId.isEmpty()) {
            separacionActual = storage.getSeparacionById(separacionId);
        }
 
        if (separacionActual == null) {
            java.util.List<Separacion> list = storage.getAdvisorSeparaciones();
            if (!list.isEmpty()) {
                separacionActual = list.get(0);
            }
        }
 
        if (separacionActual == null) {
            return;
        }
 
        // Obtenemos referencias a los campos
        TextView txtReservaId = findViewById(R.id.txtSolReservaId);
        TextView txtCliente = findViewById(R.id.txtSolCliente);
        TextView txtClienteSub = findViewById(R.id.txtSolClienteSub); // Nota: En el XML original puede ser txtSolClienteSub
        if (txtClienteSub == null) {
            txtClienteSub = findViewById(R.id.txtSolClienteSub);
        }
        TextView txtProyecto = findViewById(R.id.txtSolProyecto);
        TextView txtPropiedad = findViewById(R.id.txtSolPropiedad);
        TextView txtPrecioTotal = findViewById(R.id.txtSolPrecioTotal);
        TextView txtMontoSeparacion = findViewById(R.id.txtSolMontoSeparacion);
 
        // Formateamos los datos
        String clienteEmail = separacionActual.getClientName().toLowerCase().replace(" ", ".") + "@mail.com";
        String propName = separacionActual.getPropertyName();
        String projName = propName.contains(" - ") ? propName.split(" - ")[0] : "Proyecto Inmobiliario";
 
        txtReservaId.setText("ID: " + separacionActual.getId());
        txtCliente.setText(separacionActual.getClientName());
        if (txtClienteSub != null) {
            txtClienteSub.setText(clienteEmail);
        }
        txtProyecto.setText(projName);
        txtPropiedad.setText(propName);
        txtPrecioTotal.setText("$450,000"); // Precio de simulación
        txtMontoSeparacion.setText(separacionActual.getPrice());
    }
}
