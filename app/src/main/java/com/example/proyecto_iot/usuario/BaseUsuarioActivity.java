package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;

public abstract class BaseUsuarioActivity extends AppCompatActivity {

    private int currentNavId = -1;

    protected void setupUserBottomNav(int selectedItemId) {
        this.currentNavId = selectedItemId;
        applySafeAreaInsets();

        setupNavClick(R.id.navUserExplore, UsuarioHomeActivity.class);
        setupNavClick(R.id.navUserActivity, UsuarioActividadActivity.class);
        setupNavClick(R.id.navUserChats, UsuarioChatsActivity.class);
        setupNavClick(R.id.navUserProfile, UsuarioPerfilActivity.class);

        setSelected(selectedItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentNavId != -1) {
            setSelected(currentNavId);
        }
    }

    private void setupNavClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                setSelected(viewId);
                onUserNavSelected(viewId, destination);
            });
        }
    }

    private void setSelected(int selectedItemId) {
        View explore = findViewById(R.id.navUserExplore);
        View activity = findViewById(R.id.navUserActivity);
        View chats = findViewById(R.id.navUserChats);
        View profile = findViewById(R.id.navUserProfile);

        if (explore != null) {
            explore.setSelected(selectedItemId == R.id.navUserExplore);
        }
        if (activity != null) {
            activity.setSelected(selectedItemId == R.id.navUserActivity);
        }
        if (chats != null) {
            chats.setSelected(selectedItemId == R.id.navUserChats);
        }
        if (profile != null) {
            profile.setSelected(selectedItemId == R.id.navUserProfile);
        }
    }

    protected void openScreen(Class<?> destination) {
        if (!getClass().equals(destination)) {
            Intent intent = new Intent(this, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    protected void onUserNavSelected(int viewId, Class<?> destination) {
        openScreen(destination);
    }

    private void applySafeAreaInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        final int rootLeft = root.getPaddingLeft();
        final int rootTop = root.getPaddingTop();
        final int rootRight = root.getPaddingRight();

        View navItem = findViewById(R.id.navUserExplore);
        View navRoot = null;
        if (navItem != null && navItem.getParent() instanceof View) {
            navRoot = (View) navItem.getParent();
        }
        final View nav = navRoot;
        final int navBottom = nav != null ? nav.getPaddingBottom() : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(rootLeft + bars.left, rootTop + bars.top, rootRight + bars.right, v.getPaddingBottom());
            if (nav != null) {
                nav.setPadding(nav.getPaddingLeft(), nav.getPaddingTop(), nav.getPaddingRight(), navBottom + bars.bottom);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
