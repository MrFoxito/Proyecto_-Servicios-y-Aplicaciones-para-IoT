package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import org.json.JSONObject;

public class UsuarioDatosPersonalesActivity extends BaseUsuarioActivity {

    private EditText etName, etEmail, etPhone, etCity, etPrefContact;
    private TextView tvSummaryName, tvSummaryContact;
    private AuthSessionManager session;
    private LocalSchemaStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_datos_personales);
        setupUserBottomNav(R.id.navUserProfile);

        session = new AuthSessionManager(this);
        storage = new LocalSchemaStorage(this);

        bindViews();
        loadUserData();
        setupActions();
    }

    private void bindViews() {
        etName = findViewById(R.id.etPersonalName);
        etEmail = findViewById(R.id.etPersonalEmail);
        etPhone = findViewById(R.id.etPersonalPhone);
        etCity = findViewById(R.id.etPersonalCity);
        etPrefContact = findViewById(R.id.etPersonalPrefContact);
        tvSummaryName = findViewById(R.id.tvPersonalSummaryName);
        tvSummaryContact = findViewById(R.id.tvPersonalSummaryContact);
    }

    private void loadUserData() {
        // Primero intenta leer del storage por userId
        String userId = session.getUserId();
        JSONObject user = storage.getUserById(userId);

        String name = session.getUserName();
        String email = session.getUserEmail();
        String phone = session.getUserPhone();
        String city = "";

        if (user != null) {
            String nombres = user.optString("nombres", "");
            String apellidos = user.optString("apellidos", "");
            String fullName = (nombres + " " + apellidos).trim();
            if (!fullName.isEmpty()) name = fullName;
            if (!user.optString("email", "").isEmpty()) email = user.optString("email");
            if (!user.optString("telefono", "").isEmpty()) phone = user.optString("telefono");
            city = user.optString("ciudad", "");
        }

        if (etName != null) etName.setText(name);
        if (etEmail != null) etEmail.setText(email);
        if (etPhone != null) etPhone.setText(phone);
        if (etCity != null) etCity.setText(city);
        if (etPrefContact != null && !phone.isEmpty()) etPrefContact.setText(phone);

        // Actualiza el resumen visual
        if (tvSummaryName != null) tvSummaryName.setText(name.isEmpty() ? getString(R.string.profile_personal_name_value) : name);
        if (tvSummaryContact != null) tvSummaryContact.setText(phone.isEmpty() ? getString(R.string.profile_personal_pref_contact_value) : phone);
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPersonalData);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View save = findViewById(R.id.btnSavePersonalData);
        if (save != null) {
            save.setOnClickListener(v -> saveUserData());
        }
    }

    private void saveUserData() {
        String name = etName != null ? etName.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        String city = etCity != null ? etCity.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "El nombre y el correo son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guarda en AuthSessionManager (sesión activa)
        session.updateUserProfile(name, email, phone);

        // Guarda en LocalSchemaStorage (colección usuarios)
        String userId = session.getUserId();
        if (!userId.isEmpty()) {
            storage.updateUsuario(userId, name, email, phone, city);
        }

        // Actualiza el resumen visual
        if (tvSummaryName != null) tvSummaryName.setText(name);
        if (tvSummaryContact != null) tvSummaryContact.setText(phone.isEmpty() ? email : phone);

        Toast.makeText(this, R.string.profile_personal_save_toast, Toast.LENGTH_SHORT).show();
    }
}
