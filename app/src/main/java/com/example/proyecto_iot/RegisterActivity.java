package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.admin.AdminHomeActivity;
import com.example.proyecto_iot.asesor.AsesorHomeActivity;
import com.example.proyecto_iot.superadmin.SuperadminResumenActivity;
import com.example.proyecto_iot.usuario.UsuarioHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private AuthSessionManager authManager;
    private TextInputEditText inputNombres, inputApellidos, inputEmail, inputPhone, inputPassword, inputConfirmPassword;
    private MaterialButton btnRegister, btnRegisterGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        applySafeAreaInsets();

        authManager = AuthSessionManager.getInstance(this);

        // Bindings
        inputNombres = findViewById(R.id.inputNombres);
        inputApellidos = findViewById(R.id.inputApellidos);
        inputEmail = findViewById(R.id.inputRegisterEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputPassword = findViewById(R.id.inputRegisterPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegisterAccount);

        // Botón de registro con correo
        btnRegister.setOnClickListener(v -> {
            String nombres = inputNombres.getText().toString().trim();
            String apellidos = inputApellidos.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String confirm = inputConfirmPassword.getText().toString().trim();

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                    password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_LONG).show();
                return;
            }

            btnRegister.setEnabled(false);
            authManager.registerWithEmail(email, password, nombres, apellidos, phone,
                    new AuthSessionManager.AuthListener() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            btnRegister.setEnabled(true);
                            openHome(authManager.getRole());
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnRegister.setEnabled(true);
                            runOnUiThread(() ->
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                            );
                        }
                    });
        });

        // Botón para ir a login
        findViewById(R.id.txtOpenLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Botón de retroceso
        findViewById(R.id.btnBackRegister).setOnClickListener(v -> finish());
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void applySafeAreaInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}