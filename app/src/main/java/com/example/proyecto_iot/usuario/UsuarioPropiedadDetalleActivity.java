package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.data.ProjectBusinessRules;

public class UsuarioPropiedadDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_PROPERTY_ID = "extra_property_id";
    public static final String EXTRA_PROPERTY_TITLE = "extra_property_title";
    public static final String EXTRA_PROPERTY_PRICE = "extra_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_property_location";
    public static final String EXTRA_PROPERTY_IMAGE_URL = "extra_property_image_url";
    public static final String EXTRA_PROPERTY_STATUS = "extra_property_status";
    public static final String EXTRA_PROPERTY_DELIVERY_DATE = "extra_property_delivery_date";
    public static final String EXTRA_PROPERTY_QR_VALUE = "extra_property_qr_value";

    private String propertyStatus = ProjectBusinessRules.STATUS_PLANOS;
    private String propertyDeliveryDate = "";
    private String propertyQrValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedad_detalle);
        applyInsets();
        bindDynamicPropertyData();
        loadRemoteProjectMetadata();

        View back = findViewById(R.id.btnBackPropertyDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View schedule = findViewById(R.id.btnAgendarCita);
        if (schedule != null) {
            schedule.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsuarioAgendarCitaActivity.class);
                intent.putExtra(
                        UsuarioAgendarCitaActivity.EXTRA_PROPERTY_ID,
                        getIntent() != null ? projectIdFromIntent(getIntent()) : ""
                );
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
            separate.setOnClickListener(v -> openPaymentIfAllowed());
        }

        View mapCta = findViewById(R.id.btnPropertyMapAction);
        if (mapCta != null) {
            mapCta.setOnClickListener(v -> {
                Intent mapIntent = new Intent(this, UsuarioMapaExploracionActivity.class);
                mapIntent.putExtra(
                        UsuarioMapaExploracionActivity.EXTRA_FOCUS_PROJECT_ID,
                        getIntent() == null ? "" : projectIdFromIntent(getIntent())
                );
                startActivity(mapIntent);
            });
        }
    }

    private void bindDynamicPropertyData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String propertyId = projectIdFromIntent(intent);
        String title = intent.getStringExtra(EXTRA_PROPERTY_TITLE);
        String price = intent.getStringExtra(EXTRA_PROPERTY_PRICE);
        String location = intent.getStringExtra(EXTRA_PROPERTY_LOCATION);
        String imageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL);
        propertyStatus = ProjectBusinessRules.normalizeStatus(intent.getStringExtra(EXTRA_PROPERTY_STATUS));
        propertyDeliveryDate = safe(intent.getStringExtra(EXTRA_PROPERTY_DELIVERY_DATE));
        propertyQrValue = safe(intent.getStringExtra(EXTRA_PROPERTY_QR_VALUE));
        UsuarioPropertyCatalog.PropertyDetail detail = null;
        if (propertyId != null && !propertyId.trim().isEmpty()) {
            detail = UsuarioPropertyCatalog.getById(propertyId);
        }
        if (detail == null && title != null && !title.trim().isEmpty()) {
            detail = UsuarioPropertyCatalog.findByTitle(title);
        }
        if (detail == null) {
            detail = createFallbackDetail(title, price, location);
        }
        bindPropertyDetail(detail, imageUrl);
        applyOperationRules();
    }

    private void loadRemoteProjectMetadata() {
        Intent intent = getIntent();
        String projectId = intent == null ? "" : projectIdFromIntent(intent);
        if (projectId.isEmpty()) {
            return;
        }
        new FirebaseDataRepository().readProjectDetail(projectId, new FirebaseDataRepository.ProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectDetail detail) {
                propertyStatus = detail.estadoProyecto;
                propertyDeliveryDate = detail.fechaEntrega;
                propertyQrValue = detail.qrValue;
                bindText(R.id.propertyHeroBadge, ProjectBusinessRules.displayStatus(detail.estadoProyecto));
                bindText(R.id.propertyHeroTitle, detail.nombre);
                bindText(R.id.tvPropertyTopBarTitle, detail.nombre);
                bindText(R.id.propertyPriceText, detail.precioDesde);
                bindText(R.id.propertyLocationText, detail.direccion);
                bindText(R.id.propertyDistrictText, detail.distrito);
                if (!detail.fechaEntrega.isEmpty()) {
                    bindText(R.id.propertyEtaText, "Entrega estimada: " + detail.fechaEntrega);
                }
                if (!detail.descripcion.isEmpty()) {
                    bindText(R.id.propertyAboutDescription, detail.descripcion);
                }
                ImageView heroImage = findViewById(R.id.ivPropertyHero);
                if (heroImage != null && !detail.imageUrl.isEmpty()) {
                    ProjectImageLoader.load(heroImage, detail.imageUrl, 0);
                }
                applyOperationRules();
            }

            @Override
            public void onError(String message) {
                applyOperationRules();
            }
        });
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
                ProjectImageLoader.load(heroImage, imageUrl, detail.getHeroImageResId());
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

    private void applyOperationRules() {
        View schedule = findViewById(R.id.btnAgendarCita);
        if (schedule != null) {
            schedule.setEnabled(ProjectBusinessRules.canScheduleAppointment(propertyStatus));
            schedule.setAlpha(schedule.isEnabled() ? 1f : 0.45f);
        }

        View separate = findViewById(R.id.btnSepararInmueble);
        if (separate != null) {
            boolean canSeparate = ProjectBusinessRules.canCreateSeparation(propertyStatus);
            separate.setAlpha(canSeparate ? 1f : 0.45f);
        }
    }

    private void openPaymentIfAllowed() {
        if (!ProjectBusinessRules.canCreateSeparation(propertyStatus)) {
            Toast.makeText(this, "Este proyecto esta en planos. Aun no permite separacion.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent payIntent = new Intent(this, UsuarioReservaPagoActivity.class);
        payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_ID,
                getIntent() != null ? projectIdFromIntent(getIntent()) : "");
        payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_TITLE,
                readText(R.id.propertyHeroTitle, R.string.property_title));
        payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_PRICE,
                readText(R.id.propertyPriceText, R.string.property_price));
        payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_LOCATION,
                readText(R.id.propertyLocationText, R.string.property_location));
        payIntent.putExtra(UsuarioReservaPagoActivity.EXTRA_PROPERTY_STATUS, propertyStatus);
        startActivity(payIntent);
    }

    private String projectIdFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "app".equalsIgnoreCase(data.getScheme()) && "proyecto".equalsIgnoreCase(data.getHost())) {
            String id = data.getLastPathSegment();
            if (id != null && !id.trim().isEmpty()) {
                return id.trim();
            }
        }
        return safe(intent.getStringExtra(EXTRA_PROPERTY_ID));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
