package com.example.proyecto_iot;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.asesor.AsesorHomeActivity;
import com.example.proyecto_iot.superadmin.SuperadminResumenActivity;
import com.example.proyecto_iot.usuario.UsuarioHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private AuthSessionManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthSessionManager.getInstance(this);

        if (authManager.getCurrentFirebaseUser() != null && authManager.isLoggedIn()) {
            openHome(authManager.getRole());
            return;
        }

        etEmail = findViewById(R.id.inputEmail);
        etPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnLoginGoogle);
        TextView tvRegister = findViewById(R.id.btnOpenRegister);
        TextView tvForgot = findViewById(R.id.txtForgotPassword);

        // Login con correo/contraseña
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            authManager.loginWithEmail(email, password, new AuthSessionManager.AuthListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        openHome(authManager.getRole());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        btnGoogle.setOnClickListener(v ->
                authManager.startGoogleSignIn(this, new AuthSessionManager.AuthListener() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        runOnUiThread(() -> openHome(authManager.getRole()));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                        );
                    }
                })
        );

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Primero ingresa tu correo", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Revisa tu correo para restablecer la contraseña",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() :
                                    "Error al enviar el correo";
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void openHome(String rol) {
        Class<?> destination;
        switch (rol) {
            case AuthSessionManager.ROLE_ADMIN:
                destination = AdminHomeActivity.class;
                break;
            case AuthSessionManager.ROLE_ASESOR:
                destination = AsesorHomeActivity.class;
                break;
            case AuthSessionManager.ROLE_SUPERADMIN:
                destination = SuperadminResumenActivity.class;
                break;
            default:
                destination = UsuarioHomeActivity.class;
                break;
        }
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}