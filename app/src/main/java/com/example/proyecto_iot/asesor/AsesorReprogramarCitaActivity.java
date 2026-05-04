package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
// import com.example.proyecto_iot.entity.Cita;
// import com.example.proyecto_iot.entity.EventoCita;

import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;
import java.util.UUID;

public class AsesorReprogramarCitaActivity extends BaseAsesorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_reprogramar_cita);

        setupBackButton();
        findViewById(R.id.btnConfirmarReprogramacion).setOnClickListener(v -> confirmarReprogramacion());
        findViewById(R.id.btnCancelarReprogramacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void confirmarReprogramacion() {
        // COMENTADO POR AHORA PARA NO USAR REPOSITORIO
        /*
        Cita cita = CitaRepository.getInstance().getCitaActual();
        if (cita != null) {
            // Actualizar cita con los datos (simulados) de la UI
            cita.setDate("2024-10-26");
            cita.setTime("03:30 PM");
            cita.setStatus("Reprogramada");

            // Agregar evento al historial
            String eventoId = UUID.randomUUID().toString();
            String fechaActual = CitaRepository.toIsoFormat(Calendar.getInstance());
            EventoCita evento = new EventoCita(
                    eventoId,
                    cita.getId(),
                    "Cita reprogramada",
                    "Motivo: Cliente con conflicto de horario en la mañana. Nuevo horario: 26 Oct, 03:30 PM.",
                    fechaActual,
                    "REPROGRAMADA"
            );
            cita.addEvento(evento);
        }
        */

        Toast.makeText(this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show();
        
        // Volver al detalle
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
