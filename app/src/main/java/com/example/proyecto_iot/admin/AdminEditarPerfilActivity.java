package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.databinding.ActivityAdminEditarPerfilBinding;

public class AdminEditarPerfilActivity extends BaseAdminActivity {

    private ActivityAdminEditarPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding);

        binding.etNombre.setText("Administrador Editorial");
        binding.etTelefono.setText("+52 55 1234 5678");
        binding.etEmail.setText("admin@editorialestate.com");
        binding.etDni.setText("45678912-K");
        binding.etNacimiento.setText("15/05/1985");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDescartar.setOnClickListener(v -> finish());

        binding.btnGuardar.setOnClickListener(v -> {
            String nombre = binding.etNombre.getText().toString();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
