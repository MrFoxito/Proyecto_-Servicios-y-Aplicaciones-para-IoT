package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AsesorEditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etEmail, etTelefono, etDescripcion;
    private TextInputLayout lyNombre, lyApellido;
    private ImageView imgAvatar;
    private Button btnCambiarFoto;
    private MaterialButton btnGuardar, btnCancelar;

    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private SupabaseStorageRepository storageRepository;
    private String userId;
    private Uri selectedImageUri;

    // Launcher moderno para seleccionar imagen
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .placeholder(R.drawable.sa_profile_asesor_1)
                                .into(imgAvatar);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_editar_perfil);

        // Inicializar managers
        sessionManager = AuthSessionManager.getInstance(this);
        db = FirebaseFirestore.getInstance();
        storageRepository = new SupabaseStorageRepository(this);
        userId = sessionManager.getUid();

        // Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etDescripcion = findViewById(R.id.etDescripcion);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnGuardar = findViewById(R.id.btnGuardarPerfilAsesor);
        btnCancelar = findViewById(R.id.btnCancelarEditarPerfilAsesor);
        lyNombre = findViewById(R.id.lyNombre);
        lyApellido = findViewById(R.id.lyApellido);

        // Cargar datos actuales
        loadUserData();

        // Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnCambiarFoto.setOnClickListener(v -> openImagePicker());

        btnGuardar.setOnClickListener(v -> saveChanges());

        btnCancelar.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Nombre completo (solo lectura)
                        String nombres = doc.getString("nombres");
                        String apellidos = doc.getString("apellidos");
                        if (nombres != null) {
                            etNombre.setText(nombres.trim());
                            etNombre.setFocusable(false);
                            etNombre.setClickable(false);
                            etNombre.setLongClickable(false);
                        } else lyNombre.setHelperTextEnabled(false);
                        if (apellidos != null) {
                            etApellido.setText(apellidos.trim());
                            etApellido.setFocusable(false);
                            etApellido.setClickable(false);
                            etApellido.setLongClickable(false);
                        } else lyApellido.setHelperTextEnabled(false);


                        // Correo (solo lectura)
                        String email = doc.getString("email");
                        etEmail.setText(email != null ? email : "");

                        // Teléfono (editable)
                        String telefono = doc.getString("telefono");
                        etTelefono.setText(telefono != null ? telefono : "");

                        // Descripción (editable)
                        String descripcion = doc.getString("descripcion");
                        etDescripcion.setText(descripcion != null ? descripcion : "");

                        // Avatar (cargar desde URL si existe)
                        String avatarUrl = doc.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.sa_profile_asesor_1)
                                    .error(R.drawable.sa_profile_asesor_1)
                                    .into(imgAvatar);
                        } else {
                            // Fallback a avatarKey local
                            String avatarKey = doc.getString("avatarKey");
                            if (avatarKey != null) {
                                int resId = getResources().getIdentifier(avatarKey, "drawable", getPackageName());
                                if (resId != 0) {
                                    imgAvatar.setImageResource(resId);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Perfil no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveChanges() {
        String telefono = etTelefono.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (telefono.isEmpty()) {
            Toast.makeText(this, "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón para evitar doble clic
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Actualizar datos en Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("telefono", telefono);
        updates.put("descripcion", descripcion);

        db.collection("usuarios").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Si hay imagen seleccionada, subir a Supabase
                    if (selectedImageUri != null) {
                        uploadAvatarToSupabase(selectedImageUri);
                    } else {
                        // Sin imagen nueva, solo actualización de datos
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar Cambios");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar Cambios");
                });
    }

    private void uploadAvatarToSupabase(Uri imageUri) {
        storageRepository.uploadUserAvatar(userId, imageUri, new SupabaseStorageRepository.UploadCallback() {
            @Override
            public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                // Obtener URL pública de la imagen subida
                String publicUrl = result.publicUrl;

                // Actualizar avatarUrl en Firestore
                db.collection("usuarios").document(userId)
                        .update("avatarUrl", publicUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AsesorEditarPerfilActivity.this,
                                    "Perfil y foto actualizados", Toast.LENGTH_SHORT).show();
                            btnGuardar.setEnabled(true);
                            btnGuardar.setText("Guardar Cambios");
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AsesorEditarPerfilActivity.this,
                                    "Error al guardar URL de foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnGuardar.setEnabled(true);
                            btnGuardar.setText("Guardar Cambios");
                        });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorEditarPerfilActivity.this,
                        "Error al subir foto: " + message, Toast.LENGTH_SHORT).show();
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar Cambios");
            }
        });
    }
}