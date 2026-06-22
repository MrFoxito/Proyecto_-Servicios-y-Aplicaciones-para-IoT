package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectMediaRepository;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.example.proyecto_iot.data.AccountRepository;
import com.example.proyecto_iot.databinding.ActivityAdminEditarEmpresaBinding;

/**
 * Vista para editar los datos corporativos de la empresa inmobiliaria.
 */
public class AdminEditarEmpresaActivity extends BaseAdminActivity {

    private ActivityAdminEditarEmpresaBinding binding;
    private AdminLocalStorage adminLocalStorage;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private int nextCompanyImageSlot = 0;
    private final Uri[] selectedCompanyUris = new Uri[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarEmpresaBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);

        setupImagePicker();
        setupBackButton();
        restoreCompanyProfile();
        loadCompanyFromFirebase();

        binding.btnAgregarImagenEmpresa.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        binding.ivEmpresaImagenPrincipal.setOnClickListener(v -> {
            nextCompanyImageSlot = 0;
            imagePickerLauncher.launch(new String[]{"image/*"});
        });
        binding.ivEmpresaImagenSecundaria.setOnClickListener(v -> {
            nextCompanyImageSlot = 1;
            imagePickerLauncher.launch(new String[]{"image/*"});
        });
        binding.btnCompletarConfig.setOnClickListener(v -> confirmSaveCompanyProfile());
        binding.btnCancelar.setOnClickListener(v -> closeWithAnimation());
    }

    private void loadCompanyFromFirebase() {
        String uid = AuthSessionManager.getInstance(this).getUid();
        new AccountRepository().loadCompany(uid, new AccountRepository.CompanyCallback() {
            @Override
            public void onSuccess(String empresaId, String address, String email, String phone,
                                  String primaryImageUrl, String secondaryImageUrl) {
                binding.etDireccion.setText(address);
                binding.etCorreo.setText(email);
                binding.etTelefono.setText(phone);
                if (!primaryImageUrl.isEmpty()) loadCompanyImage(primaryImageUrl, 0);
                if (!secondaryImageUrl.isEmpty()) loadCompanyImage(secondaryImageUrl, 1);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarEmpresaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                return;
            }
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some providers grant only temporary read access.
            }

            int selectedSlot = nextCompanyImageSlot;
            adminLocalStorage.saveCompanyImageUri(selectedSlot, uri.toString());
            selectedCompanyUris[selectedSlot] = uri;
            if (selectedSlot == 0) {
                binding.ivEmpresaImagenPrincipal.setImageURI(uri);
                nextCompanyImageSlot = 1;
            } else {
                binding.ivEmpresaImagenSecundaria.setImageURI(uri);
                nextCompanyImageSlot = 0;
            }
            Toast.makeText(this, "Imagen agregada desde galeria", Toast.LENGTH_SHORT).show();
        });
    }

    private void restoreCompanyProfile() {
        String[] profile = adminLocalStorage.getCompanyProfile();
        binding.etDireccion.setText(profile[0]);
        binding.etCorreo.setText(profile[1]);
        binding.etTelefono.setText(profile[2]);

        String primaryUri = adminLocalStorage.getCompanyImageUri(0);
        String secondaryUri = adminLocalStorage.getCompanyImageUri(1);
        if (!primaryUri.isEmpty()) {
            loadCompanyImage(primaryUri, 0);
        }
        if (!secondaryUri.isEmpty()) {
            loadCompanyImage(secondaryUri, 1);
        }
    }

    private void loadCompanyImage(String imageUri, int slot) {
        if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
            Glide.with(slot == 0 ? binding.ivEmpresaImagenPrincipal : binding.ivEmpresaImagenSecundaria)
                    .load(imageUri)
                    .centerCrop()
                    .into(slot == 0 ? binding.ivEmpresaImagenPrincipal : binding.ivEmpresaImagenSecundaria);
        } else if (slot == 0) {
            binding.ivEmpresaImagenPrincipal.setImageURI(Uri.parse(imageUri));
        } else {
            binding.ivEmpresaImagenSecundaria.setImageURI(Uri.parse(imageUri));
        }
    }

    private void confirmSaveCompanyProfile() {
        new AlertDialog.Builder(this)
                .setTitle("Completar configuracion")
                .setMessage("Deseas guardar la configuracion corporativa de la empresa?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> saveCompanyProfile())
                .show();
    }

    private void saveCompanyProfile() {
        String address = binding.etDireccion.getText().toString().trim();
        String email = binding.etCorreo.getText().toString().trim();
        String phone = binding.etTelefono.getText().toString().trim();
        String uid = AuthSessionManager.getInstance(this).getUid();
        new AccountRepository().updateCompany(uid, address, email, phone, new AccountRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                adminLocalStorage.saveCompanyProfile(address, email, phone);
                uploadCompanyImageSlot(0);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarEmpresaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadCompanyImageSlot(int slot) {
        if (slot >= selectedCompanyUris.length) {
            Toast.makeText(this, "Configuracion de empresa guardada correctamente", Toast.LENGTH_SHORT).show();
            closeWithAnimation();
            return;
        }
        Uri uri = selectedCompanyUris[slot];
        if (uri == null) {
            uploadCompanyImageSlot(slot + 1);
            return;
        }

        String adminId = AuthSessionManager.getInstance(this).getUid();
        new ProjectMediaRepository(this).uploadCompanyImage(adminId, uri, new SupabaseStorageRepository.UploadCallback() {
            @Override
            public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                new FirebaseDataRepository().saveCompanyImage(slot, result, new FirebaseDataRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        adminLocalStorage.saveCompanyImageUri(slot, result.publicUrl);
                        uploadCompanyImageSlot(slot + 1);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminEditarEmpresaActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarEmpresaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void closeWithAnimation() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
