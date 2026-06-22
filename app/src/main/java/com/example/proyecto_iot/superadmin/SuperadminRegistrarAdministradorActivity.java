package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.data.FirebaseDataRepository;
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

                FirebaseDataRepository repo = new FirebaseDataRepository();
                repo.addInmobiliaria(name, description, "default_photo_url", email, new FirebaseDataRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        com.google.firebase.auth.ActionCodeSettings actionCodeSettings =
                                com.google.firebase.auth.ActionCodeSettings.newBuilder()
                                        .setUrl("https://iot-g3-c3fa2.firebaseapp.com/invite_admin?email=" + email + "&inmobiliaria=" + name)
                                        .setHandleCodeInApp(true)
                                        .setAndroidPackageName(
                                                "com.example.proyecto_iot",
                                                true, /* installIfNotAvailable */
                                                "12"    /* minimumVersion */)
                                        .build();

                        com.google.firebase.auth.FirebaseAuth.getInstance().sendSignInLinkToEmail(email, actionCodeSettings)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        SuperadminNotificationHelper.showAdminRegisteredNotification(SuperadminRegistrarAdministradorActivity.this, name);
                                        Toast.makeText(SuperadminRegistrarAdministradorActivity.this, "Inmobiliaria registrada. Invitación enviada a " + email, Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(SuperadminRegistrarAdministradorActivity.this, "Error enviando invitación: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(SuperadminRegistrarAdministradorActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
        
        if (findViewById(R.id.layoutUploadPhoto) != null) {
            findViewById(R.id.layoutUploadPhoto).setOnClickListener(v -> {
                Toast.makeText(this, "Carga de foto local simulada (OK)", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
