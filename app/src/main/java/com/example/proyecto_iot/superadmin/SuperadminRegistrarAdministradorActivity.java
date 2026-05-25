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
                EditText etEmail = findViewById(R.id.etEmail);
                EditText etPhone = findViewById(R.id.etPhone);
                EditText etAgency = findViewById(R.id.etAgency);
                EditText etPassword = findViewById(R.id.etPassword);

                String name = etName != null ? etName.getText().toString().trim() : "";
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                String agency = etAgency != null ? etAgency.getText().toString().trim() : "The Editorial Estate";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "admin123";

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Por favor complete nombre y correo", Toast.LENGTH_SHORT).show();
                    return;
                }

                LocalSchemaStorage storage = new LocalSchemaStorage(this);
                storage.addAdministrador(name, email, phone, agency, password);
                SuperadminNotificationHelper.showAdminRegisteredNotification(this, name);

                Toast.makeText(this, "Administrador registrado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}


