package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

public class UsuarioPropiedadesListadoActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedades_listado);
        setupUserBottomNav(R.id.navUserExplore);
        setupActions();
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPropertiesList);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View openMap = findViewById(R.id.btnOpenMapFromList);
        if (openMap != null) {
            openMap.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioMapaExploracionActivity.class)));
        }

        bindPropertyCard(
                R.id.propertyListCard1,
                getString(R.string.home_featured_property),
                getString(R.string.home_featured_price),
                getString(R.string.home_featured_location)
        );
        bindPropertyCard(
                R.id.propertyListCard2,
                getString(R.string.home_popular_property_1),
                getString(R.string.home_popular_price_1),
                getString(R.string.property_list_location_one)
        );
        bindPropertyCard(
                R.id.propertyListCard3,
                getString(R.string.home_popular_property_2),
                getString(R.string.home_popular_price_2),
                getString(R.string.property_list_location_two)
        );
        bindPropertyCard(
                R.id.propertyListCard4,
                getString(R.string.property_list_title_four),
                getString(R.string.property_list_price_four),
                getString(R.string.property_list_location_four)
        );
    }

    private void bindPropertyCard(int viewId, String title, String price, String location) {
        View card = findViewById(viewId);
        if (card == null) {
            return;
        }
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, title);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, price);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location);
            startActivity(intent);
        });
    }
}

