package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.ArrayList;
import java.util.List;

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
        List<UsuarioPropertyListItem> properties =
                new LocalSchemaStorage(this).getUserPropertyListItems();
        renderProjectCards(properties);
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (projects.isEmpty()) {
                    return;
                }
                renderProjectCards(mergeProjects(projects, properties));
            }

            @Override
            public void onError(String message) {
                renderProjectCards(properties);
            }
        });
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

    private void renderProjectCards(List<UsuarioPropertyListItem> properties) {
        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.recyclerHomeFirebaseProjects);
        if (recycler != null) {
            recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            UsuarioPropertyListAdapter adapter = new UsuarioPropertyListAdapter(properties, this::openPropertyDetail);
            recycler.setAdapter(adapter);
        }
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
