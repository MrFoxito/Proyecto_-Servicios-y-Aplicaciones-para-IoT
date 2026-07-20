package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.RoleUiHelper;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseAsesorActivity extends AppCompatActivity {

    private boolean validatingAccess;

    @Override
    protected void onStart() {
        super.onStart();
        if (validatingAccess || isFinishing()) return;
        validatingAccess = true;
        AuthSessionManager.getInstance(this).resolveCurrentAccess(new AuthSessionManager.AccessListener() {
            @Override public void onAllowed(com.google.firebase.auth.FirebaseUser user, String role) {
                validatingAccess = false;
                if (!AuthSessionManager.ROLE_ASESOR.equals(role)) blockAccess("Esta ruta es solo para asesores.");
            }
            @Override public void onBlocked(String status) {
                validatingAccess = false;
                blockAccess("Tu cuenta de asesor está " + status + ".");
            }
            @Override public void onError(String message) {
                validatingAccess = false;
                // A transient Firestore failure must not clear a valid Firebase
                // session while the advisor is interacting with a form.
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    blockAccess("No se pudo validar tu acceso. Inicia sesión nuevamente.");
                    return;
                }
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(BaseAsesorActivity.this,
                            "No se pudo verificar tu acceso por el momento. Intenta nuevamente al restablecer la conexión.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void blockAccess(String message) {
        if (isFinishing() || isDestroyed()) return;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        RoleUiHelper.applyRoleChrome(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        RoleUiHelper.applyRoleChrome(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        RoleUiHelper.applyRoleChrome(this);
    }

    protected void setupBottomNavigation(int activeNavId) {
        setupNavClick(R.id.navMiAgenda, AsesorMiAgendaActivity.class);
        setupNavClick(R.id.navSeparaciones, AsesorSeparacionesActivity.class);
        setupNavClick(R.id.navChats, AsesorChatsActivity.class);
        setupNavClick(R.id.navPerfil, AsesorPerfilActivity.class);
        applyActiveNav(activeNavId);
    }

    protected void openScreen(Class<?> destination) {
        if (!getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    protected void setupBackButton() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }
    }

    protected void showPendingToast() {
        Toast.makeText(this, "Vista secundaria pendiente", Toast.LENGTH_SHORT).show();
    }

    private void setupNavClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                if (!getClass().equals(destination)) {
                    Intent intent = new Intent(this, destination);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }

    private void applyActiveNav(int activeNavId) {
        int[][] navItems = {
                {R.id.navMiAgenda, R.id.iconMiAgenda, R.id.labelMiAgenda},
                {R.id.navSeparaciones, R.id.iconSeparaciones, R.id.labelSeparaciones},
                {R.id.navChats, R.id.iconChats, R.id.labelChats},
                {R.id.navPerfil, R.id.iconPerfil, R.id.labelPerfil}
        };

        for (int[] item : navItems) {
            View container = findViewById(item[0]);
            ImageView icon = findViewById(item[1]);
            TextView label = findViewById(item[2]);
            boolean active = item[0] == activeNavId;

            if (container != null) {
                if (active) {
                    container.setBackgroundResource(R.drawable.ad_pill_active);
                } else {
                    container.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            if (icon != null) {
                icon.setColorFilter(active ? Color.WHITE : Color.parseColor("#9AA3AF"));
            }
            if (label != null) {
                label.setTextColor(active ? Color.WHITE : Color.parseColor("#9AA3AF"));
            }
        }
    }
}
