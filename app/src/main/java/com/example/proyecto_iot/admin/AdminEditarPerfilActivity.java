package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.example.proyecto_iot.databinding.ActivityAdminEditarPerfilBinding;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminEditarPerfilActivity extends BaseAdminActivity {

    private ActivityAdminEditarPerfilBinding binding;
    private ActivityResultLauncher<String[]> avatarPickerLauncher;
    private AdminLocalStorage adminLocalStorage;
    private Uri selectedAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        setupAvatarPicker();

        binding.etNombre.setText("Administrador Editorial");
        binding.etTelefono.setText("+52 55 1234 5678");
        binding.etEmail.setText("admin@editorialestate.com");
        binding.etDni.setText("45678912-K");
        binding.etNacimiento.setText("15/05/1985");
        restoreAvatar();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDescartar.setOnClickListener(v -> finish());
        binding.fabCambiarFoto.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        binding.ivAvatarPerfil.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        binding.etNacimiento.setOnClickListener(v -> showBirthDatePicker());

        binding.btnGuardar.setOnClickListener(v -> confirmSaveProfile());
    }

    private void setupAvatarPicker() {
        avatarPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                return;
            }
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some providers grant only temporary read access.
            }
            adminLocalStorage.saveAdminProfileAvatarUri(uri.toString());
            selectedAvatarUri = uri;
            binding.ivAvatarPerfil.setImageURI(uri);
            Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        });
    }

    private void restoreAvatar() {
        String avatarUri = adminLocalStorage.getAdminProfileAvatarUri();
        if (!avatarUri.isEmpty()) {
            if (avatarUri.startsWith("http://") || avatarUri.startsWith("https://")) {
                Glide.with(binding.ivAvatarPerfil).load(avatarUri).centerCrop().into(binding.ivAvatarPerfil);
            } else {
                binding.ivAvatarPerfil.setImageURI(Uri.parse(avatarUri));
            }
        }
    }

    private void showBirthDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .parse(binding.etNacimiento.getText().toString()));
        } catch (Exception ignored) {
            // Keeps today's date if the field cannot be parsed.
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    binding.etNacimiento.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void confirmSaveProfile() {
        String nombre = binding.etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Guardar cambios")
                .setMessage("Deseas actualizar los datos del perfil?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> saveProfileWithSupabaseAvatar())
                .show();
    }

    private void saveProfileWithSupabaseAvatar() {
        if (selectedAvatarUri == null) {
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = AuthSessionManager.getInstance(this).getUid();
        new SupabaseStorageRepository(this).uploadUserAvatar(uid, selectedAvatarUri, new SupabaseStorageRepository.UploadCallback() {
            @Override
            public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                new FirebaseDataRepository().saveCurrentUserAvatar(result, new FirebaseDataRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        adminLocalStorage.saveAdminProfileAvatarUri(result.publicUrl);
                        Toast.makeText(AdminEditarPerfilActivity.this, "Perfil actualizado con imagen en Supabase", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminEditarPerfilActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarPerfilActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
