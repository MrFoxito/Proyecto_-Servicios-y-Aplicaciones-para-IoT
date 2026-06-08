package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;

public class UsuarioPropiedadDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_PROPERTY_ID = "extra_property_id";
    public static final String EXTRA_PROPERTY_TITLE = "extra_property_title";
    public static final String EXTRA_PROPERTY_PRICE = "extra_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";
    public static final String EXTRA_PROPERTY_IMAGE_URL = "extra_property_image_url";
    public static final String EXTRA_PROPERTY_DESCRIPTION = "extra_property_description";
    public static final String EXTRA_PROPERTY_DISTRICT = "extra_property_district";
    public static final String EXTRA_PROPERTY_DELIVERY_DATE = "extra_property_delivery_date";
    public static final String EXTRA_PROPERTY_MAP_LABEL = "extra_property_map_label";
    public static final String EXTRA_PROPERTY_BEDROOMS = "extra_property_bedrooms";
    public static final String EXTRA_PROPERTY_BATHROOMS = "extra_property_bathrooms";
    public static final String EXTRA_PROPERTY_AREA = "extra_property_area";
    public static final String EXTRA_PROPERTY_TYPOLOGIES = "extra_property_typologies";
    public static final String EXTRA_PROPERTY_AMENITIES = "extra_property_amenities";
    public static final String EXTRA_PROPERTY_STATUS = "extra_property_status";

    public static void putPropertyExtras(Intent intent, UsuarioPropertyListItem item) {
        intent.putExtra(EXTRA_PROPERTY_ID, item.getPropertyId());
        intent.putExtra(EXTRA_PROPERTY_TITLE, item.getTitle());
        intent.putExtra(EXTRA_PROPERTY_PRICE, item.getPrice());
        intent.putExtra(EXTRA_PROPERTY_LOCATION, item.getLocation());
        intent.putExtra(EXTRA_PROPERTY_IMAGE_URL, item.getImageUrl());
        intent.putExtra(EXTRA_PROPERTY_DESCRIPTION, item.getDescription());
        intent.putExtra(EXTRA_PROPERTY_DISTRICT, item.getDistrict());
        intent.putExtra(EXTRA_PROPERTY_DELIVERY_DATE, item.getDeliveryDate());
        intent.putExtra(EXTRA_PROPERTY_MAP_LABEL, item.getMapLabel());
        intent.putExtra(EXTRA_PROPERTY_BEDROOMS, item.getBedrooms());
        intent.putExtra(EXTRA_PROPERTY_BATHROOMS, item.getBathrooms());
        intent.putExtra(EXTRA_PROPERTY_AREA, item.getArea());
        intent.putExtra(EXTRA_PROPERTY_TYPOLOGIES, item.getTypologiesSummary());
        intent.putExtra(EXTRA_PROPERTY_AMENITIES, item.getAmenitiesSummary());
        intent.putExtra(EXTRA_PROPERTY_STATUS, item.getStatus());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedad_detalle);
        applyInsets();
        bindDynamicPropertyData();

        View back = findViewById(R.id.btnBackPropertyDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View schedule = findViewById(R.id.btnAgendarCita);
        if (schedule != null) {
            schedule.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioAgendarCitaActivity.class);
                intent.putExtra(
                        UsuarioAgendarCitaActivity.EXTRA_PROPERTY_TITLE,
                        readText(R.id.tvPropertyTopBarTitle, R.string.property_title)
                );
                intent.putExtra(
                        UsuarioAgendarCitaActivity.EXTRA_PROPERTY_LOCATION,
                        readText(R.id.propertyLocationText, R.string.property_location)
                );
                startActivity(intent);
            });
        }

        View separate = findViewById(R.id.btnSepararInmueble);
        if (separate != null) {
            separate.setOnClickListener(v -> {
                Intent payIntent = new Intent(this, UsuarioReservaPagoActivity.class);
                // Pasa los datos de la propiedad actual a la pantalla de pago
                payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_TITLE,
                        readText(R.id.propertyHeroTitle, R.string.property_title));
                payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_PRICE,
                        readText(R.id.propertyPriceText, R.string.property_price));
                payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_LOCATION,
                        readText(R.id.propertyLocationText, R.string.property_location));
                startActivity(payIntent);
            });
        }

        View mapCta = findViewById(R.id.btnPropertyMapAction);
        if (mapCta != null) {
            mapCta.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioMapaExploracionActivity.class)));
        }
    }

    private void bindDynamicPropertyData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID);
        String title = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String price = intent.getStringExtra(EXTRA_PROPERTY_PRICE);
        String location = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);
        String imageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL);
        String description = intent.getStringExtra(EXTRA_PROPERTY_DESCRIPTION);
        String district = intent.getStringExtra(EXTRA_PROPERTY_DISTRICT);
        String deliveryDate = intent.getStringExtra(EXTRA_PROPERTY_DELIVERY_DATE);
        String mapLabel = intent.getStringExtra(EXTRA_PROPERTY_MAP_LABEL);
        String typologies = intent.getStringExtra(EXTRA_PROPERTY_TYPOLOGIES);
        String amenities = intent.getStringExtra(EXTRA_PROPERTY_AMENITIES);
        String status = intent.getStringExtra(EXTRA_PROPERTY_STATUS);
        UsuarioPropertyCatalog.PropertyDetail detail = null;
        if (hasRemoteData(description, district, typologies, amenities, imageUrl)) {
            detail = createRemoteDetail(propertyId, title, price, location, description, district, deliveryDate, mapLabel, typologies, amenities, status);
        } else if (propertyId != null && !propertyId.trim().isEmpty()) {
            detail = UsuarioPropertyCatalog.getById(propertyId);
        }
        if (detail == null && title != null && !title.trim().isEmpty()) {
            detail = UsuarioPropertyCatalog.findByTitle(title);
        }
        if (detail == null) {
            detail = createFallbackDetail(title, price, location);
        }
        bindPropertyDetail(detail, imageUrl);
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.propertyDetailRoot);
        if (root == null) {
            return;
        }
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private String readText(int viewId, int fallbackRes) {
        TextView view = findViewById(viewId);
        if (view != null && view.getText() != null && view.getText().length() > 0) {
            return view.getText().toString();
        }
        return getString(fallbackRes);
    }

    private void bindPropertyDetail(UsuarioPropertyCatalog.PropertyDetail detail, String imageUrl) {
        bindText(R.id.tvPropertyTopBarTitle, detail.getTitle());
        bindText(R.id.propertyHeroBadge, detail.getBadge());
        bindText(R.id.propertyHeroTitle, detail.getTitle());
        bindText(R.id.propertyHeroSubtitle, detail.getShortSubtitle());
        bindText(R.id.propertyPriceText, detail.getPrice());
        bindText(R.id.propertyLocationText, detail.getLocation());
        bindText(R.id.propertyDistrictText, detail.getDistrict());
        bindText(R.id.propertyEtaText, detail.getEta());
        bindText(R.id.propertyAboutDescription, detail.getAboutDescription());
        bindText(R.id.propertyHighlightOne, detail.getHighlightOne());
        bindText(R.id.propertyHighlightTwo, detail.getHighlightTwo());
        bindText(R.id.propertyMapSummary, detail.getMapSummary());
        bindText(R.id.btnPropertyMapAction, detail.getMapCta());

        ImageView heroImage = findViewById(R.id.ivPropertyHero);
        if (heroImage != null) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(heroImage)
                        .load(imageUrl)
                        .centerCrop()
                        .into(heroImage);
            } else {
                heroImage.setImageResource(detail.getHeroImageResId());
            }
        }

        UsuarioPropertyCatalog.Amenity[] amenities = detail.getAmenities();
        bindAmenity(0, amenities);
        bindAmenity(1, amenities);
        bindAmenity(2, amenities);
        bindAmenity(3, amenities);
    }

    private void bindAmenity(int index, UsuarioPropertyCatalog.Amenity[] amenities) {
        UsuarioPropertyCatalog.Amenity amenity = index < amenities.length ? amenities[index] : null;
        int titleId;
        int descriptionId;
        int iconId;
        switch (index) {
            case 0:
                iconId = R.id.ivAmenityOneIcon;
                titleId = R.id.tvAmenityOneTitle;
                descriptionId = R.id.tvAmenityOneDescription;
                break;
            case 1:
                iconId = R.id.ivAmenityTwoIcon;
                titleId = R.id.tvAmenityTwoTitle;
                descriptionId = R.id.tvAmenityTwoDescription;
                break;
            case 2:
                iconId = R.id.ivAmenityThreeIcon;
                titleId = R.id.tvAmenityThreeTitle;
                descriptionId = R.id.tvAmenityThreeDescription;
                break;
            default:
                iconId = R.id.ivAmenityFourIcon;
                titleId = R.id.tvAmenityFourTitle;
                descriptionId = R.id.tvAmenityFourDescription;
                break;
        }
        ImageView iconView = findViewById(iconId);
        if (iconView != null && amenity != null) {
            iconView.setImageResource(amenity.getIconResId());
        }
        bindText(titleId, amenity != null ? amenity.getTitle() : "");
        bindText(descriptionId, amenity != null ? amenity.getDescription() : "");
    }

    private void bindText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null && value != null) {
            view.setText(value);
        }
    }

    private boolean hasRemoteData(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private UsuarioPropertyCatalog.PropertyDetail createRemoteDetail(
            String id,
            String title,
            String price,
            String location,
            String description,
            String district,
            String deliveryDate,
            String mapLabel,
            String typologies,
            String amenities,
            String status
    ) {
        String safeTitle = nonEmpty(title, getString(R.string.property_title));
        String safeStatus = nonEmpty(status, getString(R.string.property_badge_pre_sale));
        String safeTypologies = nonEmpty(typologies, "Tipologia por definir");
        String safeAmenities = nonEmpty(amenities, "Amenidades por definir");
        return new UsuarioPropertyCatalog.PropertyDetail(
                nonEmpty(id, "firebase_property"),
                safeStatus,
                safeTitle,
                nonEmpty(price, getString(R.string.property_price)),
                nonEmpty(location, getString(R.string.property_location)),
                nonEmpty(district, getString(R.string.property_location_detail)),
                safeStatus,
                safeTypologies,
                nonEmpty(deliveryDate, "Fecha por definir"),
                nonEmpty(description, "Informacion del inmueble en actualizacion."),
                safeTypologies,
                safeAmenities,
                nonEmpty(mapLabel, nonEmpty(location, getString(R.string.property_map_desc))),
                getString(R.string.property_map_cta),
                R.drawable.user_property_hero_real,
                amenitiesFromSummary(safeAmenities)
        );
    }

    private UsuarioPropertyCatalog.Amenity[] amenitiesFromSummary(String summary) {
        String[] names = summary == null ? new String[0] : summary.split(" · ");
        UsuarioPropertyCatalog.Amenity[] result = new UsuarioPropertyCatalog.Amenity[Math.max(1, Math.min(4, names.length))];
        for (int i = 0; i < result.length; i++) {
            String title = i < names.length && !names[i].trim().isEmpty() ? names[i].trim() : "Amenidad";
            result[i] = new UsuarioPropertyCatalog.Amenity(
                    amenityIcon(title),
                    title,
                    "Incluido en la propuesta del inmueble."
            );
        }
        return result;
    }

    private int amenityIcon(String title) {
        String normalized = title == null ? "" : title.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("pisc")) return R.drawable.ic_user_explore;
        if (normalized.contains("gim")) return R.drawable.ic_user_activity;
        if (normalized.contains("cowork") || normalized.contains("lounge")) return R.drawable.ic_user_chat;
        return R.drawable.ic_user_shield;
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private UsuarioPropertyCatalog.PropertyDetail createFallbackDetail(String title, String price, String location) {
        return new UsuarioPropertyCatalog.PropertyDetail(
                "fallback_property",
                "CURADURIA EDITORIAL",
                title != null && !title.trim().isEmpty() ? title : getString(R.string.property_title),
                price != null && !price.trim().isEmpty() ? price : getString(R.string.property_price),
                location != null && !location.trim().isEmpty() ? location : getString(R.string.property_location),
                getString(R.string.property_location_detail),
                getString(R.string.property_badge_pre_sale),
                getString(R.string.property_subtitle),
                getString(R.string.property_eta),
                getString(R.string.property_about_desc),
                getString(R.string.property_certification),
                getString(R.string.property_domotics),
                getString(R.string.property_map_desc),
                getString(R.string.property_map_cta),
                R.drawable.user_property_hero_real,
                new UsuarioPropertyCatalog.Amenity[] {
                        new UsuarioPropertyCatalog.Amenity(R.drawable.ic_user_activity, getString(R.string.feature_gym_title), getString(R.string.feature_gym_desc)),
                        new UsuarioPropertyCatalog.Amenity(R.drawable.ic_user_explore, getString(R.string.feature_pool_title), getString(R.string.feature_pool_desc)),
                        new UsuarioPropertyCatalog.Amenity(R.drawable.ic_user_chat, getString(R.string.feature_cowork_title), getString(R.string.feature_cowork_desc)),
                        new UsuarioPropertyCatalog.Amenity(R.drawable.ic_user_shield, getString(R.string.feature_security_title), getString(R.string.feature_security_desc))
                }
        );
    }
}
