package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectImageLoader;

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
        renderProjectCards(new ArrayList<>());
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (isFinishing() || isDestroyed()) return;
                renderProjectCards(projects);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                renderProjectCards(new ArrayList<>());
            }
        });
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
            card.setVisibility(View.VISIBLE);
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
            card.setVisibility(View.GONE);
            card.setOnClickListener(null);
        }
    }

    private void bindPopularRow(int rowId, int labelId, int titleId, int metaId, int priceId,
                                 List<UsuarioPropertyListItem> properties, int index) {
        View row = findViewById(rowId);
        if (row == null) return;

        if (index < properties.size()) {
            row.setVisibility(View.VISIBLE);
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
            row.setVisibility(View.GONE);
            row.setOnClickListener(null);
        }
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item));
    }

    private void bindImage(ImageView imageView, UsuarioPropertyListItem item) {
        ProjectImageLoader.load(imageView, item.getImageUrl(), item.getImageResId());
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

        View scanQr = findViewById(R.id.btnHomeQrScan);
        if (scanQr != null) {
            scanQr.setOnClickListener(v ->
                    startActivity(new Intent(this, QrScannerActivity.class)));
        }
    }
}
