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
        bindProjectClick(
                R.id.featuredPrimaryCard,
                getString(R.string.home_featured_property),
                getString(R.string.home_featured_price),
                getString(R.string.home_featured_location)
        );
        bindProjectClick(
                R.id.featuredSecondaryCard,
                getString(R.string.home_popular_property_2),
                getString(R.string.home_popular_price_2),
                "Malibu, CA"
        );
        bindProjectClick(
                R.id.popularRow1,
                getString(R.string.home_popular_property_1),
                getString(R.string.home_popular_price_1),
                "Brooklyn, NY"
        );
        bindProjectClick(
                R.id.popularRow2,
                getString(R.string.home_popular_property_2),
                getString(R.string.home_popular_price_2),
                "Malibu, CA"
        );
    }

    private void bindProjectClick(int viewId, String title, String price, String location) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, title);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, price);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location);
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

        View openMap = findViewById(R.id.btnOpenMap);
        if (openMap != null) {
            openMap.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioMapaExploracionActivity.class)));
        }
    }
}
