package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.databinding.ActivityAdminCrearProyectoBinding;

public class AdminCrearProyectoActivity extends BaseAdminActivity {

    private ActivityAdminCrearProyectoBinding binding;
    private int contadorAreas = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCrearProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnPublicar.setOnClickListener(v -> finish());

        setupEstadoSelector(
                binding.tvEstadoPlanosCrear,
                binding.tvEstadoPlanosCrear,
                binding.tvEstadoPreventaCrear,
                binding.tvEstadoVentaCrear
        );

        binding.mapaProyectoCrear.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoCrear));
        binding.tvMapaProyectoCrear.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoCrear));
        binding.btnAgregarAreaComunCrear.setOnClickListener(v -> mostrarDialogoAreaComun());
    }

    private void setupEstadoSelector(TextView seleccionado, TextView... opciones) {
        for (TextView opcion : opciones) {
            opcion.setOnClickListener(v -> aplicarEstado((TextView) v, opciones));
        }
        aplicarEstado(seleccionado, opciones);
    }

    private void aplicarEstado(TextView seleccionado, TextView... opciones) {
        for (TextView opcion : opciones) {
            boolean activo = opcion == seleccionado;
            opcion.setBackgroundResource(activo ? R.drawable.bg_pill_active : android.R.color.transparent);
            opcion.setTextColor(activo ? Color.WHITE : Color.parseColor("#666666"));
            opcion.setTypeface(null, activo ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void mostrarDialogoMapa(TextView tvMapa) {
        EditText input = new EditText(this);
        input.setHint("Ej: Av. Santa Fe 455, Lomas de Santa Fe");
        input.setSingleLine(true);
        input.setPadding(32, 18, 32, 18);

        new AlertDialog.Builder(this)
                .setTitle("Editar ubicacion en mapa")
                .setMessage("Actualiza el punto o referencia visible del proyecto.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String ubicacion = input.getText().toString().trim();
                    if (ubicacion.isEmpty()) {
                        Toast.makeText(this, "Ubicacion del mapa conservada", Toast.LENGTH_SHORT).show();
                    } else {
                        tvMapa.setText("Mapa: " + ubicacion);
                    }
                })
                .show();
    }

    private void mostrarDialogoAreaComun() {
        EditText input = new EditText(this);
        input.setHint("Ej: Sala de cine");
        input.setSingleLine(true);
        input.setPadding(32, 18, 32, 18);

        new AlertDialog.Builder(this)
                .setTitle("Agregar area comun")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        nombre = "Area comun " + contadorAreas;
                    }
                    agregarAreaComun(nombre);
                    contadorAreas++;
                })
                .show();
    }

    private void agregarAreaComun(String nombre) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(nombre);
        checkBox.setChecked(true);
        checkBox.setTextColor(Color.parseColor("#0B1A24"));
        checkBox.setTextSize(12);
        checkBox.setBackgroundResource(R.drawable.bg_search_bar);
        checkBox.setPadding(8, 0, 8, 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(48)
        );
        params.setMargins(0, 0, 0, dpToPx(8));
        binding.contenedorAreasComunesCrear.addView(checkBox, params);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
