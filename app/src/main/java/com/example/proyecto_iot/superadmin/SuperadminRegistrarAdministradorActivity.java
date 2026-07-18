package com.example.proyecto_iot.superadmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class SuperadminRegistrarAdministradorActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_registrar_administrador);
        setupCommonNavigation();

        View backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        View saveButton = findViewById(R.id.btnSaveAdmin);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> registerCompanyAndInvite(saveButton));
        }

        View uploadPhoto = findViewById(R.id.layoutUploadPhoto);
        if (uploadPhoto != null) {
            uploadPhoto.setOnClickListener(v ->
                    Toast.makeText(this, "Carga de foto local simulada (OK)", Toast.LENGTH_SHORT).show());
        }
    }

    private void registerCompanyAndInvite(View saveButton) {
        EditText nameInput = findViewById(R.id.etName);
        EditText descriptionInput = findViewById(R.id.etDescription);
        EditText emailInput = findViewById(R.id.etEmail);
        String name = text(nameInput);
        String description = text(descriptionInput);
        String email = text(emailInput).toLowerCase(Locale.ROOT);

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this,
                    "Complete el nombre de la inmobiliaria y el correo.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        setButtonEnabled(saveButton, false);
        new FirebaseDataRepository().createAdminInvitation(
                name,
                description,
                "",
                email,
                new FirebaseDataRepository.AdminInvitationCallback() {
                    @Override
                    public void onSuccess(String invitationId, String empresaId) {
                        sendEmailInvitation(saveButton, name, email, invitationId);
                    }

                    @Override
                    public void onError(String message) {
                        setButtonEnabled(saveButton, true);
                        Toast.makeText(SuperadminRegistrarAdministradorActivity.this,
                                message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void sendEmailInvitation(
            View saveButton,
            String companyName,
            String email,
            String invitationId
    ) {
        String continueUrl = new Uri.Builder()
                .scheme("https")
                .authority("iot-g3-c3fa2.firebaseapp.com")
                .path("invite_admin")
                .appendQueryParameter("invitationId", invitationId)
                .appendQueryParameter("email", email)
                .build()
                .toString();
        ActionCodeSettings settings = ActionCodeSettings.newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName("com.example.proyecto_iot", true, "1")
                .build();

        FirebaseAuth.getInstance().sendSignInLinkToEmail(email, settings)
                .addOnCompleteListener(task -> {
                    setButtonEnabled(saveButton, true);
                    if (task.isSuccessful()) {
                        SuperadminNotificationHelper.showAdminRegisteredNotification(this, companyName);
                        Toast.makeText(this,
                                "Inmobiliaria registrada. Invitación enviada a " + email,
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    Exception error = task.getException();
                    if (isEmailQuotaError(error)) {
                        showManualInvitationFallback(companyName, email, invitationId);
                    } else {
                        String message = error == null ? "Error desconocido" : error.getMessage();
                        Toast.makeText(this,
                                "La inmobiliaria quedó pendiente, pero no se pudo enviar el correo: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showManualInvitationFallback(
            String companyName,
            String email,
            String invitationId
    ) {
        String manualLink = new Uri.Builder()
                .scheme("proyectoiot")
                .authority("admin-invite")
                .appendQueryParameter("invitationId", invitationId)
                .appendQueryParameter("email", email)
                .build()
                .toString();
        new AlertDialog.Builder(this)
                .setTitle("Cuota de correos agotada")
                .setMessage("Firebase no puede enviar más enlaces hoy. La inmobiliaria quedó pendiente. Comparte la invitación manual con el administrador para que cree su cuenta desde la app.")
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Compartir invitación", (dialog, which) -> {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT,
                            "Invitación para administrar " + companyName);
                    share.putExtra(Intent.EXTRA_TEXT,
                            "Has sido invitado como administrador de " + companyName
                                    + ". Abre este enlace desde el teléfono con la app instalada:\n"
                                    + manualLink);
                    startActivity(Intent.createChooser(share, "Compartir invitación"));
                })
                .show();
    }

    private boolean isEmailQuotaError(Exception error) {
        if (error == null) return false;
        String text = (error.getClass().getSimpleName() + " " + error.getMessage())
                .toLowerCase(Locale.ROOT);
        return text.contains("quota")
                || text.contains("exceeded")
                || text.contains("too many requests");
    }

    private void setButtonEnabled(View button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.55f);
    }

    private String text(EditText input) {
        return input == null ? "" : input.getText().toString().trim();
    }
}
