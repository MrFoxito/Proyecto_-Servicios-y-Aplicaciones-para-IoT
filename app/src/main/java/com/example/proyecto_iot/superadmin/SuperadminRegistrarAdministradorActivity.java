package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import android.widget.EditText;

public class SuperadminRegistrarAdministradorActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_registrar_administrador);
        setupCommonNavigation();

        if (findViewById(R.id.btnSaveAdmin) != null) {
            findViewById(R.id.btnSaveAdmin).setOnClickListener(v -> {
                EditText etName = findViewById(R.id.etName);
                EditText etDescription = findViewById(R.id.etDescription);
                EditText etEmail = findViewById(R.id.etEmail);

                String name = etName != null ? etName.getText().toString().trim() : "";
                String description = etDescription != null ? etDescription.getText().toString().trim() : "";
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Por favor complete el nombre de la inmobiliaria y el correo", Toast.LENGTH_SHORT).show();
                    return;
                }

                LocalSchemaStorage storage = new LocalSchemaStorage(this);
                storage.addInmobiliaria(name, description, "default_photo_url", email);
                
                // Reutilizando la notificación del sistema
                SuperadminNotificationHelper.showAdminRegisteredNotification(this, name);

                Toast.makeText(this, "Inmobiliaria registrada. Invitación enviada a " + email, Toast.LENGTH_LONG).show();
                finish();
            });
        }
        
        if (findViewById(R.id.layoutUploadPhoto) != null) {
            findViewById(R.id.layoutUploadPhoto).setOnClickListener(v -> {
                Toast.makeText(this, "Carga de foto local simulada (OK)", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
