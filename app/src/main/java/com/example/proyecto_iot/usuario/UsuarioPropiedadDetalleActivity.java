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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectBusinessRules;

import java.util.ArrayList;

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
    
    private ViewPager2 vpPropertyGallery;
    private TextView tvImageCounter;
    private UsuarioProjectGalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedad_detalle);
        applyInsets();
        
        setupGallery();
        
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

        View contactAsesor = findViewById(R.id.btnContactarAsesor);
        if (contactAsesor != null) {
            contactAsesor.setOnClickListener(v -> contactarAsesor());
        }

        View mapCta = findViewById(R.id.btnPropertyMapAction);
        if (mapCta != null) {
            mapCta.setOnClickListener(v -> {
                Intent mapIntent = new Intent(this, UsuarioMapaExploracionActivity.class);
                String pId = getIntent() != null ? projectIdFromIntent(getIntent()) : "";
                if (UsuarioPropertyCatalog.ID_VILLA_LUMINARA.equals(pId)) {
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LAT, -12.1221);
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LNG, -77.0315);
                } else if (UsuarioPropertyCatalog.ID_REFUGIO_CELESTE.equals(pId)) {
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LAT, -12.0970);
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LNG, -77.0353);
                } else if (UsuarioPropertyCatalog.ID_CASA_MERIDIAN.equals(pId)) {
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LAT, -12.1030);
                    mapIntent.putExtra(UsuarioMapaExploracionActivity.EXTRA_FOCUS_LNG, -77.0163);
                }
                startActivity(mapIntent);
            });
        }
    }

    private void setupGallery() {
        vpPropertyGallery = findViewById(R.id.vpPropertyGallery);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        
        galleryAdapter = new UsuarioProjectGalleryAdapter(position -> {
            ArrayList<String> urls = new ArrayList<>(galleryAdapter.getItems());
            if (!urls.isEmpty()) {
                Intent intent = new Intent(this, UsuarioGaleriaCompletaActivity.class);
                intent.putStringArrayListExtra(UsuarioGaleriaCompletaActivity.EXTRA_IMAGE_URLS, urls);
                intent.putExtra(UsuarioGaleriaCompletaActivity.EXTRA_INITIAL_POSITION, position);
                startActivity(intent);
            }
        });
        
        if (vpPropertyGallery != null) {
            vpPropertyGallery.setAdapter(galleryAdapter);
            vpPropertyGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateImageCounter(position, galleryAdapter.getItemCount());
                }
            });
        }
    }

    private void updateImageCounter(int position, int total) {
        if (tvImageCounter != null) {
            if (total > 1) {
                tvImageCounter.setVisibility(View.VISIBLE);
                tvImageCounter.setText((position + 1) + " / " + total);
            } else {
                tvImageCounter.setVisibility(View.GONE);
            }
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
                
                new FirebaseDataRepository().readProjectImages(projectId, new FirebaseDataRepository.ProjectImagesCallback() {
                    @Override
                    public void onSuccess(java.util.List<String> imageUrls) {
                        if (imageUrls.isEmpty() && !detail.imageUrl.isEmpty()) {
                            imageUrls.add(detail.imageUrl);
                        }
                        galleryAdapter.setItems(imageUrls);
                        updateImageCounter(0, imageUrls.size());
                    }

                    @Override
                    public void onError(String message) {
                        if (!detail.imageUrl.isEmpty()) {
                            java.util.List<String> singleImage = new java.util.ArrayList<>();
                            singleImage.add(detail.imageUrl);
                            galleryAdapter.setItems(singleImage);
                            updateImageCounter(0, 1);
                        }
                    }
                });

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

        if (galleryAdapter != null && galleryAdapter.getItemCount() == 0) {
            java.util.List<String> urls = new java.util.ArrayList<>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                urls.add(imageUrl);
            }
            galleryAdapter.setItems(urls);
            updateImageCounter(0, urls.size());
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

    private void contactarAsesor() {
        String projectId = getIntent() != null ? projectIdFromIntent(getIntent()) : "";
        if (projectId.isEmpty()) return;

        com.example.proyecto_iot.AuthSessionManager sessionManager = new com.example.proyecto_iot.AuthSessionManager(this);
        String clienteUid = sessionManager.getUserId();
        if (clienteUid == null || clienteUid.isEmpty()) {
            Toast.makeText(this, "Debe iniciar sesion para contactar al asesor.", Toast.LENGTH_SHORT).show();
            return;
        }

        new com.example.proyecto_iot.data.FirebaseAppointmentRepository().getAdvisorForProject(projectId, new com.example.proyecto_iot.data.FirebaseAppointmentRepository.AdvisorCallback() {
            @Override
            public void onSuccess(com.example.proyecto_iot.data.FirebaseAppointmentRepository.Advisor adv) {
                com.example.proyecto_iot.data.FirebaseChatRepository chatRepo = new com.example.proyecto_iot.data.FirebaseChatRepository();
                com.example.proyecto_iot.data.FirebaseChatRepository.Advisor chatAdvisor = new com.example.proyecto_iot.data.FirebaseChatRepository.Advisor(
                        adv.uid, adv.name, "", "sa_profile_admin"
                );
                chatRepo.findOrCreateConversation(
                        clienteUid,
                        sessionManager.getUserName(),
                        chatAdvisor,
                        new com.example.proyecto_iot.data.FirebaseChatRepository.ConversationCallback() {
                            @Override
                            public void onSuccess(com.example.proyecto_iot.data.FirebaseChatRepository.Conversation conversation) {
                                Intent intent = new Intent(UsuarioPropiedadDetalleActivity.this, UsuarioChatDetalleActivity.class);
                                intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONVERSATION_ID, conversation.id);
                                intent.putExtra(UsuarioChatDetalleActivity.EXTRA_ASESOR_UID, conversation.asesorUid);
                                intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONTACT_NAME, conversation.asesorNombre);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(UsuarioPropiedadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioPropiedadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
