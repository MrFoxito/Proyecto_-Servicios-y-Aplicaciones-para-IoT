package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.List;

public class UsuarioPropiedadesListadoActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedades_listado);
        setupUserBottomNav(R.id.navUserExplore);
        setupPropertyList();
        setupActions();
    }

    private void setupPropertyList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerPropertyList);
        if (recyclerView == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UsuarioPropertyListAdapter(buildPropertyItems(), this::openPropertyDetail));
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
    }

    private List<UsuarioPropertyListItem> buildPropertyItems() {
        return new LocalSchemaStorage(this).getUserPropertyListItems();
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, item.getPropertyId());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, item.getTitle());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, item.getPrice());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, item.getLocation());
        startActivity(intent);
    }
}
