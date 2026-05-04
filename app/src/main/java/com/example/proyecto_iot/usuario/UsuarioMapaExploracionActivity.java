package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_mapa_exploracion);
        setupUserBottomNav(R.id.navUserExplore);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackMapExplore);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View openList = findViewById(R.id.btnOpenListFromMap);
        if (openList != null) {
            openList.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));
        }

        bindPropertyCard(
                R.id.mapPropertyCard1,
                UsuarioPropertyCatalog.ID_VILLA_LUMINARA,
                getString(R.string.home_featured_property),
                getString(R.string.home_featured_price),
                getString(R.string.home_featured_location)
        );
        bindPropertyCard(
                R.id.mapPropertyCard2,
                UsuarioPropertyCatalog.ID_REFUGIO_CELESTE,
                getString(R.string.home_popular_property_2),
                getString(R.string.home_popular_price_2),
                getString(R.string.property_list_location_two)
        );
    }

    private void bindPropertyCard(int viewId, String propertyId, String title, String price, String location) {
        View card = findViewById(viewId);
        if (card == null) {
            return;
        }
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, propertyId);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, title);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, price);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location);
            startActivity(intent);
        });
    }
}
