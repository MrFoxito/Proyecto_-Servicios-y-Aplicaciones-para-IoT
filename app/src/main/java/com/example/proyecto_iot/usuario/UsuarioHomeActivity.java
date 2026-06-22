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
        bindFeaturedCard(R.id.featuredPrimaryCard, properties, 0);
        bindFeaturedCard(R.id.featuredSecondaryCard, properties, 2);

        bindPopularRow(
                R.id.popularRow1,
                R.id.tvPopularLabel1, R.id.tvPopularTitle1,
                R.id.tvPopularMeta1, R.id.tvPopularPrice1,
                properties, 1
        );
        bindPopularRow(
                R.id.popularRow2,
                R.id.tvPopularLabel2, R.id.tvPopularTitle2,
                R.id.tvPopularMeta2, R.id.tvPopularPrice2,
                properties, 3
        );
    }

    private void bindFeaturedCard(int cardId, List<UsuarioPropertyListItem> properties, int index) {
        View card = findViewById(cardId);
        if (card == null) return;

        if (index < properties.size()) {
            UsuarioPropertyListItem item = properties.get(index);
            ImageView img = card.findViewWithTag("heroImage");
            if (img == null && card instanceof android.view.ViewGroup) {
                img = findFirstImageView((android.view.ViewGroup) card);
            }
            if (img != null) {
                bindImage(img, item);
            }
            card.setOnClickListener(v -> openPropertyDetail(item));
        } else {
            card.setOnClickListener(null);
        }
    }

    private void bindPopularRow(int rowId, int labelId, int titleId, int metaId, int priceId,
                                 List<UsuarioPropertyListItem> properties, int index) {
        View row = findViewById(rowId);
        if (row == null) return;

        if (index < properties.size()) {
            UsuarioPropertyListItem item = properties.get(index);

            setText(labelId, item.getLabel());
            setText(titleId, item.getTitle());
            setText(metaId, item.getLocation());
            setText(priceId, item.getPrice());

            ImageView img = null;
            if (row instanceof android.view.ViewGroup) {
                img = findFirstImageView((android.view.ViewGroup) row);
            }
            if (img != null) {
                bindImage(img, item);
            }

            row.setOnClickListener(v -> openPropertyDetail(item));
        } else {
            row.setOnClickListener(null);
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

    private void bindImage(ImageView imageView, UsuarioPropertyListItem item) {
        if (!item.getImageUrl().isEmpty()) {
            Glide.with(imageView)
                    .load(item.getImageUrl())
                    .centerCrop()
                    .into(imageView);
        } else if (item.getImageResId() != 0) {
            imageView.setImageResource(item.getImageResId());
        }
    }

    private void setText(int viewId, String value) {
        TextView tv = findViewById(viewId);
        if (tv != null && value != null && !value.isEmpty()) {
            tv.setText(value);
        }
    }

    private ImageView findFirstImageView(android.view.ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ImageView) return (ImageView) child;
            if (child instanceof android.view.ViewGroup) {
                ImageView found = findFirstImageView((android.view.ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
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
