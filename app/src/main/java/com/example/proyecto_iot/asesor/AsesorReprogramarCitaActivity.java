package com.example.proyecto_iot.asesor;
 
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.Cita;
import com.example.proyecto_iot.entity.EventoCita;
 
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
 
public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {
 
    private String citaId;
    private EditText editFecha, editHora, editMotivo;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);
 
        citaId = getIntent().getStringExtra("extra_cita_id");
 
        editFecha = findViewById(R.id.editNuevaFecha);
        editHora = findViewById(R.id.editNuevaHora);
        editMotivo = findViewById(R.id.editMotivoReprogramacion);
 
        setupBackButton();
        setupPickers();
 
        findViewById(R.id.btnConfirmarReprogramacion).setOnClickListener(v -> confirmarReprogramacion());
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
 
    private void setupPickers() {
        editFecha.setFocusable(false);
        editFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                editFecha.setText(sdf.format(selected.getTime()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
 
        editHora.setFocusable(false);
        editHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selected.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                editHora.setText(sdf.format(selected.getTime()));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            tpd.show();
        });
    }
 
    private void confirmarReprogramacion() {
        if (citaId == null || citaId.isEmpty()) {
            Toast.makeText(this, "Error: Cita no especificada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
 
        String nuevaFecha = editFecha.getText().toString().trim();
        String nuevaHora = editHora.getText().toString().trim();
        String motivo = editMotivo.getText().toString().trim();
 
        if (nuevaFecha.isEmpty() || nuevaHora.isEmpty()) {
            Toast.makeText(this, "Completa fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
 
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        boolean updated = storage.updateCitaStatusAndDetails(citaId, "Reprogramada", nuevaFecha, nuevaHora);
 
        if (updated) {
            String eventoId = UUID.randomUUID().toString();
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            String fechaActual = isoFmt.format(Calendar.getInstance().getTime());
 
            EventoCita evento = new EventoCita(
                    eventoId,
                    citaId,
                    "Cita reprogramada",
                    "Motivo: " + (motivo.isEmpty() ? "No especificado" : motivo) + ". Nuevo horario: " + nuevaFecha + ", " + nuevaHora,
                    fechaActual,
                    "REPROGRAMADA"
            );
            storage.addEventoCita(evento);
 
            Toast.makeText(this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se pudo actualizar la cita", Toast.LENGTH_SHORT).show();
        }
 
        // Volver al detalle
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
