package com.example.proyecto_iot.superadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CompletarRegistroAdminActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String emailExtra = "";
    private String invitationId = "";
    private View completeButton;
    private boolean invitationVerified;
    private boolean manualInvitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completar_registro_admin);

        auth = FirebaseAuth.getInstance();
        completeButton = findViewById(R.id.btnCompletarRegistro);
        setCompleteEnabled(false);

        AuthSessionManager.getInstance(this).clearLocalSession();
        auth.signOut();
        processInvitation(getIntent());
        completeButton.setOnClickListener(v -> saveAndEnter());
    }

    private void processInvitation(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Toast.makeText(this, "No se recibió una invitación válida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Uri data = intent.getData();
        String link = data.toString();

        if ("proyectoiot".equalsIgnoreCase(data.getScheme())
                && "admin-invite".equalsIgnoreCase(data.getHost())) {
            manualInvitation = true;
            emailExtra = value(data.getQueryParameter("email"));
            invitationId = value(data.getQueryParameter("invitationId"));
            if (emailExtra.isEmpty() || invitationId.isEmpty()) {
                Toast.makeText(this, "La invitación manual está incompleta.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            invitationVerified = true;
            setCompleteEnabled(true);
            showInvitationEmail();
            Toast.makeText(this,
                    "Invitación recibida. Completa tus datos para crear la cuenta.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!auth.isSignInWithEmailLink(link)) {
            Toast.makeText(this, "El enlace de invitación no es válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailExtra = value(data.getQueryParameter("email"));
        invitationId = value(data.getQueryParameter("invitationId"));
        if (emailExtra.isEmpty() || invitationId.isEmpty()) {
            String continueUrl = data.getQueryParameter("continueUrl");
            if (continueUrl == null) {
                String nestedLink = data.getQueryParameter("link");
                if (nestedLink != null) {
                    continueUrl = Uri.parse(nestedLink).getQueryParameter("continueUrl");
                }
            }
            if (continueUrl != null) {
                Uri continueUri = Uri.parse(continueUrl);
                emailExtra = value(continueUri.getQueryParameter("email"));
                invitationId = value(continueUri.getQueryParameter("invitationId"));
            }
        }
        showInvitationEmail();
        if (emailExtra.isEmpty() || invitationId.isEmpty()) {
            Toast.makeText(this, "No se encontró el correo o la invitación.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        auth.signInWithEmailLink(emailExtra, link).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                invitationVerified = true;
                setCompleteEnabled(true);
                Toast.makeText(this,
                        "Enlace verificado correctamente. Ingresa tus datos.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "El enlace expiró o ya fue usado. Solicita una nueva invitación.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void saveAndEnter() {
        if (!invitationVerified || (!manualInvitation && auth.getCurrentUser() == null)) {
            Toast.makeText(this, "Espera a que la invitación sea verificada.", Toast.LENGTH_LONG).show();
            return;
        }
        TextInputEditText namesInput = findViewById(R.id.etNombres);
        TextInputEditText surnamesInput = findViewById(R.id.etApellidos);
        TextInputEditText phoneInput = findViewById(R.id.etTelefono);
        TextInputEditText passwordInput = findViewById(R.id.etPassword);
        String names = text(namesInput);
        String surnames = text(surnamesInput);
        String phone = text(phoneInput);
        String password = text(passwordInput);

        if (names.isEmpty() || surnames.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        setCompleteEnabled(false);
        if (manualInvitation) {
            auth.createUserWithEmailAndPassword(emailExtra, password).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    completeProfile(task.getResult().getUser(), names, surnames, phone);
                } else {
                    setCompleteEnabled(true);
                    String message = task.getException() == null
                            ? "No se pudo crear la cuenta."
                            : task.getException().getMessage();
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        user.updatePassword(password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                completeProfile(user, names, surnames, phone);
            } else {
                setCompleteEnabled(true);
                String message = task.getException() == null
                        ? "No se pudo establecer la contraseña."
                        : task.getException().getMessage();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void completeProfile(FirebaseUser user, String names, String surnames, String phone) {
        FirebaseDataRepository.UserProfile profile = new FirebaseDataRepository.UserProfile(
                user.getUid(), names, surnames, emailExtra, phone, "admin"
        );
        new FirebaseDataRepository().completeAdminInvitation(
                invitationId,
                profile,
                new FirebaseDataRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        AuthSessionManager.getInstance(CompletarRegistroAdminActivity.this)
                                .saveUserSession(
                                        user.getUid(),
                                        names + " " + surnames,
                                        emailExtra,
                                        phone,
                                        AuthSessionManager.ROLE_ADMIN
                                );
                        Intent home = new Intent(
                                CompletarRegistroAdminActivity.this,
                                AdminHomeActivity.class
                        );
                        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(home);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        setCompleteEnabled(true);
                        Toast.makeText(CompletarRegistroAdminActivity.this,
                                message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void showInvitationEmail() {
        TextView view = findViewById(R.id.tvInmobiliariaName);
        view.setText(emailExtra.isEmpty() ? "Completa tu perfil" : emailExtra);
    }

    private void setCompleteEnabled(boolean enabled) {
        completeButton.setEnabled(enabled);
        completeButton.setAlpha(enabled ? 1f : 0.55f);
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
