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

import com.example.proyecto_iot.usuario.UsuarioHomeActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        applySafeAreaInsets();

        setupActions();
    }

    private void setupActions() {
        EditText fullName = findViewById(R.id.inputFullName);
        EditText email = findViewById(R.id.inputRegisterEmail);
        EditText phone = findViewById(R.id.inputPhone);
        EditText password = findViewById(R.id.inputRegisterPassword);
        EditText confirmPassword = findViewById(R.id.inputConfirmPassword);
        Button register = findViewById(R.id.btnRegisterAccount);
        TextView openLogin = findViewById(R.id.txtOpenLogin);
        Button backButton = findViewById(R.id.btnBackRegister);

        backButton.setOnClickListener(v -> finish());

        openLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        register.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String phoneValue = phone.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String passConfirm = confirmPassword.getText().toString().trim();

            if (name.isEmpty() || mail.isEmpty() || phoneValue.isEmpty() || pass.isEmpty() || passConfirm.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(passConfirm)) {
                Toast.makeText(this, "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthSessionManager sessionManager = new AuthSessionManager(this);
            sessionManager.markRegisteredAndLoggedIn();

            startActivity(new Intent(this, UsuarioHomeActivity.class));
            finishAffinity();
        });
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
