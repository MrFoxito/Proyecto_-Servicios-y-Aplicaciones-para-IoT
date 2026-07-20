package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private AuthSessionManager authManager;
    private TextInputEditText inputNombres, inputApellidos, inputEmail, inputPhone, inputPassword, inputConfirmPassword;
    private MaterialButton btnRegister, btnRegisterGoogle;
    private RadioGroup inputAccountType;
    private TextInputLayout layoutRequestedCompany;
    private MaterialAutoCompleteTextView inputRequestedCompany;
    private final List<CompanyOption> activeCompanies = new ArrayList<>();
    private CompanyOption selectedCompany;
    private boolean loadingCompanies;

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
        inputAccountType = findViewById(R.id.inputAccountType);
        layoutRequestedCompany = findViewById(R.id.layoutRequestedCompany);
        inputRequestedCompany = findViewById(R.id.inputRequestedCompany);

        inputAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean advisorApplication = checkedId == R.id.radioAdvisor;
            layoutRequestedCompany.setVisibility(advisorApplication ? View.VISIBLE : View.GONE);
            if (!advisorApplication) {
                selectedCompany = null;
                inputRequestedCompany.setText("", false);
            } else {
                loadActiveCompanies();
            }
        });
        inputRequestedCompany.setOnItemClickListener((parent, view, position, id) ->
                selectedCompany = (CompanyOption) parent.getItemAtPosition(position));

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

            boolean advisorApplication = inputAccountType.getCheckedRadioButtonId() == R.id.radioAdvisor;
            if (advisorApplication && selectedCompany == null) {
                inputRequestedCompany.setError("Selecciona una inmobiliaria activa");
                return;
            }

            AuthSessionManager.RegistrationRequest request = advisorApplication
                    ? AuthSessionManager.RegistrationRequest.advisor(selectedCompany.id, selectedCompany.name)
                    : AuthSessionManager.RegistrationRequest.client();
            btnRegister.setEnabled(false);
            authManager.registerWithEmail(email, password, nombres, apellidos, phone, request,
                    new AuthSessionManager.AuthListener() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            btnRegister.setEnabled(true);
                            if (advisorApplication) {
                                authManager.logout();
                                Toast.makeText(RegisterActivity.this,
                                        "Solicitud enviada. Un superadministrador debe aprobar tu cuenta antes de que puedas ingresar como asesor.",
                                        Toast.LENGTH_LONG).show();
                                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(login);
                                finish();
                            } else {
                                openHome(AuthSessionManager.ROLE_CLIENTE);
                            }
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

    private void loadActiveCompanies() {
        if (loadingCompanies || !activeCompanies.isEmpty()) return;
        loadingCompanies = true;
        inputRequestedCompany.setEnabled(false);
        FirebaseFirestore.getInstance().collection("empresas")
                // Supports legacy active-state casing while the rules still expose only active agencies.
                .whereIn("estado", Arrays.asList("activo", "ACTIVO", "Activo"))
                .get()
                .addOnSuccessListener(snapshot -> {
                    activeCompanies.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                        String name = firstNonEmpty(document.getString("nombre"),
                                document.getString("empresaNombre"), document.getString("inmobiliariaNombre"));
                        if (!name.isEmpty()) activeCompanies.add(new CompanyOption(document.getId(), name));
                    }
                    ArrayAdapter<CompanyOption> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, activeCompanies);
                    inputRequestedCompany.setAdapter(adapter);
                    inputRequestedCompany.setEnabled(true);
                    if (activeCompanies.isEmpty()) {
                        inputRequestedCompany.setError("No hay inmobiliarias activas disponibles");
                    }
                    loadingCompanies = false;
                })
                .addOnFailureListener(error -> {
                    loadingCompanies = false;
                    inputRequestedCompany.setEnabled(true);
                    Log.e("RegisterActivity", "No se pudieron cargar las inmobiliarias activas", error);
                    String message = "No se pudieron cargar las inmobiliarias. Intenta nuevamente.";
                    if (error instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreError = (FirebaseFirestoreException) error;
                        if (firestoreError.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            message = "No hay permiso para consultar inmobiliarias activas. Verifica que las reglas de Firestore publicadas incluyan el acceso de registro.";
                        } else if (firestoreError.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                            message = "No se pudo conectar con Firestore. Revisa tu conexión e inténtalo nuevamente.";
                        }
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private static final class CompanyOption {
        final String id;
        final String name;

        CompanyOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
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
