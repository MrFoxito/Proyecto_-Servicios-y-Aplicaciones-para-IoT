package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

public class UsuarioHomeActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_home);
        setupUserBottomNav(R.id.navUserExplore);
        setupProjectClicks();
        setupHeaderActions();
        setupExploreActions();
    }

    private void setupProjectClicks() {
        bindProjectClick(R.id.featuredPrimaryCard, UsuarioPropertyCatalog.ID_VILLA_LUMINARA);
        bindProjectClick(R.id.featuredSecondaryCard, UsuarioPropertyCatalog.ID_REFUGIO_CELESTE);
        bindProjectClick(R.id.popularRow1, UsuarioPropertyCatalog.ID_IRON_WORKS);
        bindProjectClick(R.id.popularRow2, UsuarioPropertyCatalog.ID_REFUGIO_CELESTE);
    }

    private void bindProjectClick(int viewId, String propertyId) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, propertyId);
            startActivity(intent);
        });
    }

    private void setupHeaderActions() {
        View notifications = findViewById(R.id.btnExploreNotifications);
        if (notifications != null) {
            notifications.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioNotificacionesActivity.class)));
        }
    }

    private void setupExploreActions() {
        View seeAll = findViewById(R.id.btnSeeAllProperties);
        if (seeAll != null) {
            seeAll.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));
        }
    }
}
