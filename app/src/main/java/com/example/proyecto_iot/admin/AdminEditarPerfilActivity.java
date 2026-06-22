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
import com.example.proyecto_iot.data.ProjectMediaRepository;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.example.proyecto_iot.data.AccountContext;
import com.example.proyecto_iot.data.AccountRepository;
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
    private boolean completionRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        completionRequired = getIntent().getBooleanExtra("require_profile_completion", false);
        setupAvatarPicker();

        loadProfile();
        restoreAvatar();

        binding.btnBack.setOnClickListener(v -> closeOrRequireCompletion());
        binding.btnDescartar.setOnClickListener(v -> closeOrRequireCompletion());
        binding.fabCambiarFoto.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        binding.ivAvatarPerfil.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        binding.etNacimiento.setOnClickListener(v -> showBirthDatePicker());

        binding.btnGuardar.setOnClickListener(v -> confirmSaveProfile());
    }

    private void loadProfile() {
        new AccountRepository().load(AuthSessionManager.getInstance(this).getUid(), new AccountRepository.Callback() {
            @Override
            public void onSuccess(AccountContext account) {
                binding.etNombre.setText(account.nombreCompleto);
                binding.etTelefono.setText(account.telefono);
                binding.etEmail.setText(account.email);
                if (!account.avatarUrl.isEmpty()) {
                    adminLocalStorage.saveAdminProfileAvatarUri(account.avatarUrl);
                    Glide.with(binding.ivAvatarPerfil).load(account.avatarUrl).centerCrop().into(binding.ivAvatarPerfil);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarPerfilActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
            saveProfileFields();
            return;
        }

        String uid = AuthSessionManager.getInstance(this).getUid();
        new ProjectMediaRepository(this).uploadUserAvatar(uid, selectedAvatarUri, new SupabaseStorageRepository.UploadCallback() {
            @Override
            public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                new FirebaseDataRepository().saveCurrentUserAvatar(result, new FirebaseDataRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        adminLocalStorage.saveAdminProfileAvatarUri(result.publicUrl);
                        saveProfileFields();
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

    private void saveProfileFields() {
        String uid = AuthSessionManager.getInstance(this).getUid();
        new AccountRepository().updateProfile(
                uid,
                binding.etNombre.getText().toString(),
                binding.etEmail.getText().toString(),
                binding.etTelefono.getText().toString(),
                binding.etDni.getText().toString(),
                binding.etNacimiento.getText().toString(),
                new AccountRepository.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        AuthSessionManager.getInstance(AdminEditarPerfilActivity.this).updateUserData(
                                binding.etNombre.getText().toString(),
                                binding.etEmail.getText().toString(),
                                binding.etTelefono.getText().toString()
                        );
                        Toast.makeText(AdminEditarPerfilActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_LONG).show();
                        if (completionRequired) {
                            Intent intent = new Intent(AdminEditarPerfilActivity.this, AdminHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminEditarPerfilActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void closeOrRequireCompletion() {
        if (completionRequired) {
            Toast.makeText(this, "Completa nombre, correo y teléfono para continuar.", Toast.LENGTH_LONG).show();
            return;
        }
        finish();
    }
}
