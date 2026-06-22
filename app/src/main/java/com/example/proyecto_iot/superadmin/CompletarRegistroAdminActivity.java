package com.example.proyecto_iot.superadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CompletarRegistroAdminActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String emailExtra = "";
    private String inmobiliariaExtra = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completar_registro_admin);

        auth = FirebaseAuth.getInstance();
        
        // Desconectar cualquier sesión anterior (ej: Superadmin) para evitar errores cruzados
        auth.signOut();

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String link = data.toString();

            if (auth.isSignInWithEmailLink(link)) {
                emailExtra = data.getQueryParameter("email");
                inmobiliariaExtra = data.getQueryParameter("inmobiliaria");

                if (emailExtra == null) {
                    // Intentar extraer de continueUrl si es el enlace crudo de Firebase
                    String continueUrl = data.getQueryParameter("continueUrl");
                    if (continueUrl == null) {
                        // A veces viene anidado en el parámetro 'link'
                        String nestedLink = data.getQueryParameter("link");
                        if (nestedLink != null) {
                            Uri nestedUri = Uri.parse(nestedLink);
                            continueUrl = nestedUri.getQueryParameter("continueUrl");
                        }
                    }
                    if (continueUrl != null) {
                        Uri continueUri = Uri.parse(continueUrl);
                        emailExtra = continueUri.getQueryParameter("email");
                        inmobiliariaExtra = continueUri.getQueryParameter("inmobiliaria");
                    }
                }

                TextView tvInmo = findViewById(R.id.tvInmobiliariaName);
                if (inmobiliariaExtra != null && !inmobiliariaExtra.isEmpty()) {
                    tvInmo.setText("Inmobiliaria: " + inmobiliariaExtra + "\n" + emailExtra);
                } else {
                    tvInmo.setText(emailExtra != null ? emailExtra : "Completa tu perfil");
                }

                if (emailExtra != null && !emailExtra.isEmpty()) {
                    auth.signInWithEmailLink(emailExtra, link)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Enlace verificado correctamente. Ingresa tus datos.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Error al verificar el enlace: El enlace ya expiró o fue usado. Vuelve a enviar la invitación.", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                } else {
                    Toast.makeText(this, "No se encontró el email en el enlace", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }

        findViewById(R.id.btnCompletarRegistro).setOnClickListener(v -> guardarYEntrar());
    }

    private void guardarYEntrar() {
        TextInputEditText etNombres = findViewById(R.id.etNombres);
        TextInputEditText etApellidos = findViewById(R.id.etApellidos);
        TextInputEditText etTelefono = findViewById(R.id.etTelefono);
        TextInputEditText etPassword = findViewById(R.id.etPassword);

        String nombres = etNombres.getText() != null ? etNombres.getText().toString().trim() : "";
        String apellidos = etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (nombres.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay una sesión activa. Usa el enlace del correo.", Toast.LENGTH_SHORT).show();
            return;
        }

        user.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDataRepository.UserProfile profile = new FirebaseDataRepository.UserProfile(
                                user.getUid(),
                                nombres + " " + apellidos,
                                emailExtra,
                                telefono,
                                "admin"
                        );

                        FirebaseDataRepository repo = new FirebaseDataRepository();
                        repo.saveUserProfile(profile, new FirebaseDataRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(CompletarRegistroAdminActivity.this, "Registro completado con éxito", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CompletarRegistroAdminActivity.this, LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(CompletarRegistroAdminActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Error al establecer contraseña: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
