package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.RoleUiHelper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseAsesorActivity extends AppCompatActivity {

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
//                {R.id.navMiAgenda, R.id.iconMiAgenda, R.id.labelMiAgenda},
//                {R.id.navSeparaciones, R.id.iconSeparaciones, R.id.labelSeparaciones},
//                {R.id.navChats, R.id.iconChats, R.id.labelChats},
//                {R.id.navPerfil, R.id.iconPerfil, R.id.labelPerfil}
                {R.id.navMiAgenda, R.id.iconMiAgenda},
                {R.id.navSeparaciones, R.id.iconSeparaciones},
                {R.id.navChats, R.id.iconChats},
                {R.id.navPerfil, R.id.iconPerfil}
        };

        for (int[] item : navItems) {
            View container = findViewById(item[0]);
            ImageView icon = findViewById(item[1]);
//            TextView label = findViewById(item[2]);
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
//            if (label != null) {
//                label.setTextColor(active ? Color.WHITE : Color.parseColor("#9AA3AF"));
//            }
        }
    }
}
