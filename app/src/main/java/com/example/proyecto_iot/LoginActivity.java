package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.asesor.AsesorHomeActivity;
import com.example.proyecto_iot.superadmin.SuperadminResumenActivity;
import com.example.proyecto_iot.usuario.UsuarioHomeActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        applySafeAreaInsets();

        AuthSessionManager sessionManager = new AuthSessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openHome(resolveRoleTargetByName(sessionManager.getRole()));
            return;
        }

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

            showRoleDialog(sessionManager);
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Recuperacion no implementada aun", Toast.LENGTH_SHORT).show()
        );
    }

    private void showRoleDialog(AuthSessionManager sessionManager) {
        String[] options = new String[]{
                "Entrar como usuario",
                "Entrar como asesor",
                "Entrar como admin",
                "Entrar como super admin"
        };

        new AlertDialog.Builder(this)
                .setTitle("Selecciona como entrar")
                .setItems(options, (dialog, which) -> {
                    Pair<String, Class<?>> target = resolveRoleTarget(which);
                    sessionManager.markLoggedIn(target.first);
                    openHome(target.second);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private Pair<String, Class<?>> resolveRoleTarget(int option) {
        switch (option) {
            case 0:
                return new Pair<>(AuthSessionManager.ROLE_USER, UsuarioHomeActivity.class);
            case 1:
                return new Pair<>(AuthSessionManager.ROLE_ASESOR, AsesorHomeActivity.class);
            case 2:
                return new Pair<>(AuthSessionManager.ROLE_ADMIN, AdminHomeActivity.class);
            case 3:
            default:
                return new Pair<>(AuthSessionManager.ROLE_SUPERADMIN, SuperadminResumenActivity.class);
        }
    }

    private Class<?> resolveRoleTargetByName(String role) {
        switch (role) {
            case AuthSessionManager.ROLE_USER:
                return UsuarioHomeActivity.class;
            case AuthSessionManager.ROLE_ASESOR:
                return AsesorHomeActivity.class;
            case AuthSessionManager.ROLE_ADMIN:
                return AdminHomeActivity.class;
            case AuthSessionManager.ROLE_SUPERADMIN:
            default:
                return SuperadminResumenActivity.class;
        }
    }

    private void openHome(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    private void applySafeAreaInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(android.R.id.content);
        if (root == null) {
            return;
        }

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
