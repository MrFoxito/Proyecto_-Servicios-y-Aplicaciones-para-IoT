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

    private View drawerOverlay;

    protected void setupCommonNavigation() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        applySafeAreaInsets();
        initDrawerOverlay();
        SuperadminNotificationHelper.setup(this);

        View menuButton = findViewById(R.id.btnOpenMenu);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> showDrawer(true));
        }

        setupClick(R.id.drawerDashboard, SuperadminResumenActivity.class);
        setupClick(R.id.drawerUsuarios, SuperadminGestionUsuariosActivity.class);
        setupClick(R.id.drawerAgencias, SuperadminReportesGlobalesActivity.class);
        setupClick(R.id.drawerReportes, SuperadminReportesUsuariosActivity.class);
        setupClick(R.id.drawerLogs, SuperadminLogsActivity.class);

        setupClick(R.id.btnRegisterAdmin, SuperadminRegistrarAdministradorActivity.class);
        setupClick(R.id.btnViewAdvisorRequests, SuperadminAprobacionAsesoresActivity.class);
        setupClick(R.id.btnGoLogs, SuperadminLogsActivity.class);

        setupClick(R.id.navAgencias, SuperadminReportesGlobalesActivity.class);
        setupClick(R.id.navClientes, SuperadminReportesUsuariosActivity.class);

        View logout = findViewById(R.id.drawerLogout);
        if (logout != null) {
            logout.setOnClickListener(v -> {
                AuthSessionManager.getInstance(this).logout();
                Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerOverlay != null && drawerOverlay.getVisibility() == View.VISIBLE) {
                    showDrawer(false);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setupClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> openScreen(destination));
        }
    }

    protected void openScreen(Class<?> destination) {
        showDrawer(false);
        if (!getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
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

    private void initDrawerOverlay() {
        View menuButton = findViewById(R.id.btnOpenMenu);
        if (menuButton == null || drawerOverlay != null) {
            return;
        }

        ViewGroup content = findViewById(android.R.id.content);
        drawerOverlay = getLayoutInflater().inflate(R.layout.view_superadmin_drawer_overlay, content, false);

        View scrim = drawerOverlay.findViewById(R.id.drawerScrim);
        if (scrim != null) {
            scrim.setOnClickListener(v -> showDrawer(false));
        }

        setupOverlayClick(drawerOverlay, R.id.drawerDashboard, SuperadminResumenActivity.class);
        setupOverlayClick(drawerOverlay, R.id.drawerUsuarios, SuperadminGestionUsuariosActivity.class);
        setupOverlayClick(drawerOverlay, R.id.drawerAgencias, SuperadminReportesGlobalesActivity.class);
        setupOverlayClick(drawerOverlay, R.id.drawerReportes, SuperadminReportesUsuariosActivity.class);
        setupOverlayClick(drawerOverlay, R.id.drawerLogs, SuperadminLogsActivity.class);

        View logout = drawerOverlay.findViewById(R.id.drawerLogout);
        if (logout != null) {
            logout.setOnClickListener(v -> {
                AuthSessionManager.getInstance(this).logout();
                Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        View panel = drawerOverlay.findViewById(R.id.drawerPanel);
        if (panel != null) {
            final int initialTop = panel.getPaddingTop();
            final int initialBottom = panel.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(panel, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), initialTop + bars.top, v.getPaddingRight(), initialBottom + bars.bottom);
                return insets;
            });
        }

        drawerOverlay.setVisibility(View.GONE);
        content.addView(drawerOverlay);
        ViewCompat.requestApplyInsets(drawerOverlay);

        // Load profile dynamically
        loadUserProfile(drawerOverlay);
        updateActiveDrawerState(drawerOverlay);
    }

    private void updateActiveDrawerState(View overlay) {
        int activeId = -1;
        if (this instanceof SuperadminResumenActivity) activeId = R.id.drawerDashboard;
        else if (this instanceof SuperadminGestionUsuariosActivity) activeId = R.id.drawerUsuarios;
        else if (this instanceof SuperadminReportesGlobalesActivity) activeId = R.id.drawerAgencias;
        else if (this instanceof SuperadminReportesUsuariosActivity) activeId = R.id.drawerReportes;
        else if (this instanceof SuperadminLogsActivity) activeId = R.id.drawerLogs;

        int[] ids = {R.id.drawerDashboard, R.id.drawerUsuarios, R.id.drawerAgencias, R.id.drawerReportes, R.id.drawerLogs};
        for (int id : ids) {
            android.widget.Button btn = overlay.findViewById(id);
            if (btn == null) continue;
            boolean isActive = (id == activeId);
            if (isActive) {
                btn.setBackgroundResource(R.drawable.sa_drawer_active);
                btn.setTextColor(android.graphics.Color.WHITE);
            } else {
                btn.setBackgroundResource(android.R.color.transparent);
                btn.setTextColor(android.graphics.Color.parseColor("#64748B"));
            }
            android.graphics.drawable.Drawable[] drawables = btn.getCompoundDrawablesRelative();
            if (drawables[0] != null) {
                drawables[0].setTint(isActive ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#94A3B8"));
            }
        }
    }

    private void loadUserProfile(View overlay) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            FirebaseFirestore.getInstance().collection("usuarios")
                    .whereEqualTo("correo", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            com.google.firebase.firestore.DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String firstName = doc.getString("nombres");
                            String lastName = doc.getString("apellidos");
                            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                            fullName = fullName.trim();
                            if (fullName.isEmpty()) fullName = "Super Admin";

                            TextView tvDrawerName = overlay.findViewById(R.id.tvDrawerName);
                            if (tvDrawerName != null) {
                                tvDrawerName.setText(fullName);
                            }
                            
                            ImageView ivDrawerPhoto = overlay.findViewById(R.id.ivDrawerPhoto);
                            ImageView ivDashboardPhoto = findViewById(R.id.ivDashboardProfile);
                            ImageView ivHeaderPhoto = findViewById(R.id.ivHeaderProfile);
                            
                            if ("superadmin@estate.pe".equalsIgnoreCase(email)) {
                                if (ivDrawerPhoto != null) ivDrawerPhoto.setImageResource(R.drawable.sa_profile_superadmin);
                                if (ivDashboardPhoto != null) ivDashboardPhoto.setImageResource(R.drawable.sa_profile_superadmin);
                                if (ivHeaderPhoto != null) ivHeaderPhoto.setImageResource(R.drawable.sa_profile_superadmin);
                            }
                        }
                    });
        }
    }

    private void setupOverlayClick(View root, int viewId, Class<?> destination) {
        View view = root.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> openScreen(destination));
        }
    }

    private void showDrawer(boolean show) {
        if (drawerOverlay == null) {
            return;
        }
        drawerOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}


