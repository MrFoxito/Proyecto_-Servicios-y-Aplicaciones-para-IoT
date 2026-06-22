package com.example.proyecto_iot.admin;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.RoleUiHelper;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * Clase base para todas las actividades del rol Admin.
 * Centraliza la navegación del Bottom Navigation y comportamientos comunes.
 */
public abstract class BaseAdminActivity extends AppCompatActivity {

    protected void setContentView(ViewBinding binding) {
        setContentView(binding.getRoot());
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

    /**
     * Configura la navegación del Bottom Navigation Bar.
     * Debe llamarse en onCreate() de cada Activity principal.
     */
    protected void setupBottomNavigation() {
        setupNavClick(R.id.navInicio, AdminResumenActivity.class);
        setupNavClick(R.id.navProyectos, AdminProyectosActivity.class);
        setupNavClick(R.id.navAsesores, AdminAsesoresActivity.class);
        setupNavClick(R.id.navReportes, AdminReportesActivity.class);
        setupNavClick(R.id.navPerfil, AdminPerfilActivity.class);
    }

    private void setupNavClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                if (!getClass().equals(destination)) {
                    Intent intent = new Intent(this, destination);
                    // Sin animación para navegación principal (se siente nativa)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }

    /**
     * Abre una pantalla secundaria (detalle, edición, etc.)
     * con animación de deslizamiento suave.
     */
    protected void openScreen(Class<?> destination) {
        if (!getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    /**
     * Configura el botón de retroceso con animación inversa.
     */
    protected void setupBackButton() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }
    }
}
