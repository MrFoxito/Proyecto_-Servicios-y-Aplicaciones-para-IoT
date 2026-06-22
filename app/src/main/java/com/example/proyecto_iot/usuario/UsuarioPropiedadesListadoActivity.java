package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.ArrayList;
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
        UsuarioPropertyListAdapter adapter = new UsuarioPropertyListAdapter(buildPropertyItems(), this::openPropertyDetail);
        recyclerView.setAdapter(adapter);
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (projects.isEmpty()) {
                    return;
                }
                adapter.setItems(mergeProjects(projects, buildPropertyItems()));
            }

            @Override
            public void onError(String message) {
                adapter.setItems(buildPropertyItems());
            }
        });
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

    private List<UsuarioPropertyListItem> mergeProjects(
            List<UsuarioPropertyListItem> primary,
            List<UsuarioPropertyListItem> fallback
    ) {
        List<UsuarioPropertyListItem> merged = new ArrayList<>(primary);
        for (UsuarioPropertyListItem localItem : fallback) {
            boolean exists = false;
            for (UsuarioPropertyListItem item : merged) {
                if (item.getTitle().equalsIgnoreCase(localItem.getTitle())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                merged.add(localItem);
            }
        }
        return merged;
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, item.getPropertyId());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, item.getTitle());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, item.getPrice());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, item.getLocation());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_IMAGE_URL, item.getImageUrl());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_STATUS, item.getEstadoProyecto());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_DELIVERY_DATE, item.getFechaEntrega());
        intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_QR_VALUE, item.getQrValue());
        startActivity(intent);
    }
}
