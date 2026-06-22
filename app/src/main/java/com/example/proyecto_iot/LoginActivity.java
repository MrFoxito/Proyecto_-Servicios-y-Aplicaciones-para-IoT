package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.asesor.AsesorHomeActivity;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.superadmin.SuperadminResumenActivity;
import com.example.proyecto_iot.usuario.UsuarioHomeActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        applySafeAreaInsets();

        AuthSessionManager sessionManager = new AuthSessionManager(this);
        setupAuthActions(sessionManager);
    }

    private void setupAuthActions(AuthSessionManager sessionManager) {
        EditText emailField = findViewById(R.id.inputEmail);
        EditText passwordField = findViewById(R.id.inputPassword);
        Button loginButton = findViewById(R.id.btnLogin);
        View registerButton = findViewById(R.id.btnOpenRegister);
        TextView forgotPassword = findViewById(R.id.txtForgotPassword);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contrasena", Toast.LENGTH_SHORT).show();
                return;
            }

            loginButton.setEnabled(false);
            new FirebaseDataRepository().signInOrCreateKnownDemoUser(
                    this,
                    email,
                    password,
                    new FirebaseDataRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(FirebaseDataRepository.UserProfile profile) {
                            loginButton.setEnabled(true);
                            sessionManager.markRegisteredAndLoggedIn(
                                    profile.uid,
                                    profile.nombre,
                                    profile.correo,
                                    profile.telefono,
                                    profile.rol
                            );
                            openHome(profile.rol);
                        }

                        @Override
                        public void onError(String message) {
                            loginButton.setEnabled(true);
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Recuperacion no implementada aun", Toast.LENGTH_SHORT).show()
        );
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

    private void applySafeAreaInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(android.R.id.content);
        if (root == null) return;

        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
