package com.example.proyecto_iot.admin;

import android.os.Bundle;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.AccountRepository;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.databinding.ActivityAdminResumenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Dashboard principal del Administrador.
 * Muestra informacion de la empresa y accesos rapidos.
 */
public class AdminResumenActivity extends BaseAdminActivity {

    private ActivityAdminResumenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResumenBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBottomNavigation();
        loadCompany();
        checkDeliveryDueProjects();

        binding.btnNotificaciones.setOnClickListener(v -> openScreen(AdminNotificacionesActivity.class));
        binding.btnEditProfile.setOnClickListener(v -> openScreen(AdminEditarEmpresaActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompany();
        checkDeliveryDueProjects();
    }

    private void loadCompany() {
        new AccountRepository().loadCompany(currentUid(), new AccountRepository.CompanyCallback() {
            @Override
            public void onSuccess(String empresaId, String companyName, String address, String email, String phone,
                                  String primaryImageUrl, String secondaryImageUrl) {
                binding.tvAdminCompanyEmail.setText(email.isEmpty() ? "Correo pendiente" : email);
                binding.tvAdminCompanyPhone.setText(phone.isEmpty() ? "Telefono pendiente" : phone);
                binding.tvAdminCompanyAddress.setText(companyName.isEmpty() ? "Nombre pendiente" : companyName);
                ProjectImageLoader.load(binding.ivCompanyPrimaryImage, primaryImageUrl, R.drawable.sa_profile_admin);
                ProjectImageLoader.load(binding.ivCompanySecondaryImage, secondaryImageUrl, R.drawable.sa_profile_asesor_1);
            }

            @Override
            public void onSuccess(String empresaId, String address, String email, String phone,
                                  String primaryImageUrl, String secondaryImageUrl) {
                binding.tvAdminCompanyEmail.setText(email.isEmpty() ? "Correo pendiente" : email);
                binding.tvAdminCompanyPhone.setText(phone.isEmpty() ? "Telefono pendiente" : phone);
                binding.tvAdminCompanyAddress.setText(address.isEmpty() ? "Completa el perfil de tu empresa" : address);
                ProjectImageLoader.load(binding.ivCompanyPrimaryImage, primaryImageUrl, R.drawable.sa_profile_admin);
                ProjectImageLoader.load(binding.ivCompanySecondaryImage, secondaryImageUrl, R.drawable.sa_profile_asesor_1);
            }

            @Override
            public void onError(String message) {
                binding.tvAdminCompanyAddress.setText("No se pudo cargar la empresa");
            }
        });
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null && !user.getUid().trim().isEmpty()) {
            return user.getUid();
        }
        return AuthSessionManager.getInstance(this).getUid();
    }

    private void checkDeliveryDueProjects() {
        new FirebaseDataRepository().checkDeliveryDueProjectNotifications(new FirebaseDataRepository.DeliveryReminderCallback() {
            @Override
            public void onSuccess(java.util.List<FirebaseDataRepository.ProjectDetail> dueProjects) {
                // The notification screen listens to Firestore.
            }

            @Override
            public void onError(String message) {
                // Keep the dashboard usable if the background check fails.
            }
        });
    }
}
