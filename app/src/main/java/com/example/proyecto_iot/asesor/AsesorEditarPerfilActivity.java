package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import org.json.JSONObject;

public class AsesorEditarPerfilActivity extends BaseAsesorActivity {

    private EditText edtNombre, edtEmail, edtTelefono, edtCargo, edtBio;
    private AuthSessionManager session;
    private LocalSchemaStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_editar_perfil);

        session = new AuthSessionManager(this);
        storage = new LocalSchemaStorage(this);

        initViews();
        loadCurrentData();

        setupBackButton();
        findViewById(R.id.btnGuardarPerfilAsesor).setOnClickListener(v -> saveProfileChanges());
        findViewById(R.id.btnCancelarEditarPerfilAsesor).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void initViews() {
        edtNombre = findViewById(R.id.edtEditarNombre);
        edtEmail = findViewById(R.id.edtEditarEmail);
        edtTelefono = findViewById(R.id.edtEditarTelefono);
        edtCargo = findViewById(R.id.edtEditarCargo);
        edtBio = findViewById(R.id.edtEditarBio);
    }

    private void loadCurrentData() {
        JSONObject user = storage.getUserById(session.getUserId());
        if (user != null) {
            String fullName = user.optString("nombres") + " " + user.optString("apellidos");
            edtNombre.setText(fullName);
            edtEmail.setText(user.optString("email"));
            edtTelefono.setText(user.optString("telefono"));
            edtCargo.setText(user.optString("cargo", "Asesor Senior de Boutique"));
            edtBio.setText(user.optString("bio", "Curando experiencias residenciales de lujo por mas de una decada. Dedicado a encontrar el alma dentro de la estructura."));
        } else {
            // Fallback to session if storage fails
            edtNombre.setText(session.getUserName());
            edtEmail.setText(session.getUserEmail());
            edtTelefono.setText(session.getUserPhone());
        }
    }

    private void saveProfileChanges() {
        String name = edtNombre.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtTelefono.getText().toString().trim();
        String cargo = edtCargo.getText().toString().trim();
        String bio = edtBio.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nombre y Email son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Persistir en LocalSchemaStorage
        storage.updateAdvisorProfile(session.getUserId(), name, email, phone, bio, cargo);

        // Actualizar AuthSessionManager
        session.updateUserProfile(name, email, phone);

        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
