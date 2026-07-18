package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseSuperadminActivity extends AppCompatActivity {

    protected void setupCommonNavigation() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        applySafeAreaInsets();
        SuperadminNotificationHelper.setup(this);

        setupBottomNav();
        setupProfileMenu();

        // Botones adicionales existentes
        setupClick(R.id.btnRegisterAdmin, SuperadminRegistrarAdministradorActivity.class);
        setupClick(R.id.btnViewAdvisorRequests, SuperadminAprobacionAsesoresActivity.class);
        setupClick(R.id.btnGoLogs, SuperadminLogsActivity.class);

        // Ya no necesitamos sobreescribir el back press para el drawer
    }

    private void setupClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> openScreen(destination));
        }
    }

    protected void openScreen(Class<?> destination) {
        if (!getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            overridePendingTransition(0, 0); // Opcional: transición sin animación
        }
    }

    private void applySafeAreaInsets() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View mainContent = content.getChildAt(0);
        final int initialLeft = mainContent.getPaddingLeft();
        final int initialTop = mainContent.getPaddingTop();
        final int initialRight = mainContent.getPaddingRight();
        final int initialBottom = mainContent.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialLeft + bars.left,
                    initialTop + bars.top,
                    initialRight + bars.right,
                    initialBottom + bars.bottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(mainContent);
    }

    private void setupBottomNav() {
        View bottomNav = findViewById(R.id.superadminBottomNav);
        if (bottomNav == null) return;

        setupClick(R.id.navDashboard, SuperadminResumenActivity.class);
        setupClick(R.id.navUsuarios, SuperadminGestionUsuariosActivity.class);
        setupClick(R.id.navAgencias, SuperadminReportesGlobalesActivity.class);
        setupClick(R.id.navReportes, SuperadminReportesUsuariosActivity.class);
        setupClick(R.id.navLogs, SuperadminLogsActivity.class);

        updateActiveBottomNavState();
    }

    private void updateActiveBottomNavState() {
        int activeId = -1;
        if (this instanceof SuperadminResumenActivity) activeId = R.id.navDashboard;
        else if (this instanceof SuperadminGestionUsuariosActivity) activeId = R.id.navUsuarios;
        else if (this instanceof SuperadminReportesGlobalesActivity) activeId = R.id.navAgencias;
        else if (this instanceof SuperadminReportesUsuariosActivity) activeId = R.id.navReportes;
        else if (this instanceof SuperadminLogsActivity) activeId = R.id.navLogs;

        int[] ids = {R.id.navDashboard, R.id.navUsuarios, R.id.navAgencias, R.id.navReportes, R.id.navLogs};
        for (int id : ids) {
            ViewGroup tab = findViewById(id);
            if (tab == null) continue;

            ImageView icon = (ImageView) tab.getChildAt(0);
            TextView text = (TextView) tab.getChildAt(1);
            boolean isActive = (id == activeId);

            if (isActive) {
                tab.setBackgroundResource(R.drawable.ad_pill_active);
                icon.setColorFilter(android.graphics.Color.WHITE);
                text.setTextColor(android.graphics.Color.WHITE);
            } else {
                tab.setBackgroundResource(android.R.color.transparent);
                icon.setColorFilter(android.graphics.Color.parseColor("#9AA3AF"));
                text.setTextColor(android.graphics.Color.parseColor("#9AA3AF"));
            }
        }
    }

    private void setupProfileMenu() {
        View profilePic = findViewById(R.id.ivDashboardProfile);
        if (profilePic == null) {
            profilePic = findViewById(R.id.ivHeaderProfile);
        }

        if (profilePic != null) {
            profilePic.setOnClickListener(v -> showProfileMenu(v));
        }

        loadUserProfile();
    }

    private void showProfileMenu(View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Rol: Superadmin").setEnabled(false);
        popup.getMenu().add(0, 2, 0, "Cerrar Sesión");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 2) {
                AuthSessionManager.getInstance(this).logout();
                Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            FirebaseFirestore.getInstance().collection("usuarios")
                    .whereEqualTo("correo", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ImageView ivDashboardPhoto = findViewById(R.id.ivDashboardProfile);
                            ImageView ivHeaderPhoto = findViewById(R.id.ivHeaderProfile);
                            
                            if ("superadmin@estate.pe".equalsIgnoreCase(email)) {
                                if (ivDashboardPhoto != null) ivDashboardPhoto.setImageResource(R.drawable.sa_profile_superadmin);
                                if (ivHeaderPhoto != null) ivHeaderPhoto.setImageResource(R.drawable.sa_profile_superadmin);
                            }
                        }
                    });
        }
    }
}


