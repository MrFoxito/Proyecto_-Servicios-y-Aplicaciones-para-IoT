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

import com.example.proyecto_iot.data.FirebaseDataRepository;
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
        View register = findViewById(R.id.btnRegisterAccount);
        View openLogin = findViewById(R.id.txtOpenLogin);
        View backButton = findViewById(R.id.btnBackRegister);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (openLogin != null) {
            openLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }

        if (register != null) {
            register.setOnClickListener(v -> {
                if (fullName == null || email == null || phone == null
                        || password == null || confirmPassword == null) {
                    Toast.makeText(this, "Error al leer los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name       = fullName.getText().toString().trim();
                String mail       = email.getText().toString().trim();
                String phoneValue = phone.getText().toString().trim();
                String pass       = password.getText().toString().trim();
                String passConfirm = confirmPassword.getText().toString().trim();

                if (name.isEmpty() || mail.isEmpty() || phoneValue.isEmpty()
                        || pass.isEmpty() || passConfirm.isEmpty()) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(passConfirm)) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pass.length() < 6) {
                    Toast.makeText(this, "La contrasena debe tener minimo 6 caracteres", Toast.LENGTH_LONG).show();
                    return;
                }

                register.setEnabled(false);
                new FirebaseDataRepository().registerClient(
                        name,
                        mail,
                        phoneValue,
                        pass,
                        new FirebaseDataRepository.ProfileCallback() {
                            @Override
                            public void onSuccess(FirebaseDataRepository.UserProfile profile) {
                                register.setEnabled(true);
                                AuthSessionManager sessionManager = new AuthSessionManager(RegisterActivity.this);
                                sessionManager.markRegisteredAndLoggedIn(
                                        profile.uid,
                                        profile.nombre,
                                        profile.correo,
                                        profile.telefono,
                                        profile.rol
                                );
                                Toast.makeText(RegisterActivity.this, "Cuenta creada en Firebase", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, UsuarioHomeActivity.class));
                                finishAffinity();
                            }

                            @Override
                            public void onError(String message) {
                                register.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            });
        }
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
