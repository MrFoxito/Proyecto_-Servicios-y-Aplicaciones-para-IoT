package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectVisualEditorAdapter;
import com.example.proyecto_iot.admin.model.AdminProjectVisualItem;
import com.example.proyecto_iot.databinding.ActivityAdminEditarProyectoBinding;

import java.util.Arrays;

public class AdminEditarProyectoActivity extends BaseAdminActivity {

    private ActivityAdminEditarProyectoBinding binding;
    private int contadorAreas = 4;
    private AdminProjectVisualEditorAdapter visualAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnGuardar.setOnClickListener(v -> finish());
        setupVisualGallery();

        setupEstadoSelector(
                binding.tvEstadoVentaEditar,
                binding.tvEstadoPlanosEditar,
                binding.tvEstadoPreventaEditar,
                binding.tvEstadoVentaEditar
        );

        binding.mapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoEditar));
        binding.tvMapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoEditar));
        binding.btnAgregarAreaComunEditar.setOnClickListener(v -> mostrarDialogoAreaComun());
        binding.btnGestionarTipologiasEditar.setOnClickListener(v -> mostrarDialogoTipologia());
    }

    private void setupVisualGallery() {
        visualAdapter = new AdminProjectVisualEditorAdapter(item ->
                Toast.makeText(this, item.getActionLabel() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
        binding.rvMaterialVisualEditar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvMaterialVisualEditar.setAdapter(visualAdapter);
        visualAdapter.setItems(Arrays.asList(
                new AdminProjectVisualItem("Portada principal", "Cambiar foto", R.drawable.sa_profile_admin, true),
                new AdminProjectVisualItem("Fachada", "Cambiar foto", R.drawable.sa_profile_admin, true),
                new AdminProjectVisualItem("Lobby", "Cambiar foto", R.drawable.sa_profile_admin, true),
                new AdminProjectVisualItem("Amenidades", "Cambiar foto", R.drawable.sa_profile_admin, true),
                new AdminProjectVisualItem("Rooftop", "Cambiar foto", R.drawable.sa_profile_admin, true)
        ));
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
            opcion.setTextColor(activo ? Color.WHITE : Color.parseColor("#9AA3AF"));
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

    private void mostrarDialogoTipologia() {
        EditText input = new EditText(this);
        input.setHint("Nombre de tipologia");
        input.setSingleLine(true);
        input.setPadding(32, 18, 32, 18);

        new AlertDialog.Builder(this)
                .setTitle("Gestionar tipologia")
                .setMessage("Actualiza el nombre de la primera tipologia editable.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (!nombre.isEmpty()) {
                        View tipologiaView = findViewById(R.id.etTipologiaNombre1);
                        if (tipologiaView instanceof EditText) {
                            ((EditText) tipologiaView).setText(nombre);
                        }
                    }
                })
                .show();
    }

    private void agregarAreaComun(String nombre) {
        TextView chip = new TextView(this);
        chip.setText(nombre + "   x");
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(10);
        chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chip.setBackgroundResource(R.drawable.bg_pill_active);
        chip.setPadding(dpToPx(16), 0, dpToPx(16), 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(32)
        );
        params.setMargins(0, 0, dpToPx(8), 0);
        binding.contenedorAreasComunesEditar.addView(
                chip,
                Math.max(0, binding.contenedorAreasComunesEditar.getChildCount() - 1),
                params
        );
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
