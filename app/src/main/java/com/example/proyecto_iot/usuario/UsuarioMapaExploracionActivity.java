package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.maps.MapsPlatformConfig;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity implements OnMapReadyCallback {
    public static final String EXTRA_FOCUS_PROJECT_ID = "focus_project_id";
    private final Map<Marker, UsuarioPropertyListItem> markerProjects = new HashMap<>();
    private GoogleMap googleMap;
    private TextView statusView;
    private TextView nearbyView;
    private PlacesClient placesClient;
    private boolean mapsConfigured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_mapa_exploracion);
        setupUserBottomNav(R.id.navUserExplore);

        statusView = findViewById(R.id.tvMapStatus);
        nearbyView = findViewById(R.id.tvNearbyPlaces);
        findViewById(R.id.btnBackMapExplore).setOnClickListener(v -> finish());
        findViewById(R.id.btnOpenListFromMap).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));

        mapsConfigured = MapsPlatformConfig.initializePlaces(this);
        if (mapsConfigured) {
            placesClient = Places.createClient(this);
        } else {
            statusView.setText("Configura MAPS_API_KEY en local.properties para activar el mapa.");
        }

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userProjectsMap);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-12.0464, -77.0428), 11f));
        googleMap.setOnInfoWindowClickListener(marker -> {
            UsuarioPropertyListItem item = markerProjects.get(marker);
            if (item != null) {
                openProperty(item);
            }
        });
        googleMap.setOnMarkerClickListener(marker -> {
            UsuarioPropertyListItem item = markerProjects.get(marker);
            if (item != null) {
                loadNearbyPlaces(item);
            }
            return false;
        });
        loadProjects();
    }

    private void loadProjects() {
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                renderProjects(projects);
            }

            @Override
            public void onError(String message) {
                statusView.setText(message);
            }
        });
    }

    private void renderProjects(List<UsuarioPropertyListItem> projects) {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();
        markerProjects.clear();
        int count = 0;
        LatLng first = null;
        LatLng focused = null;
        String focusId = getIntent() == null ? "" : valueOr(getIntent().getStringExtra(EXTRA_FOCUS_PROJECT_ID));
        for (UsuarioPropertyListItem item : projects) {
            if (!item.hasCoordinates()) {
                continue;
            }
            LatLng point = new LatLng(item.getLatitude(), item.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(item.getTitle())
                    .snippet(item.getLocation() + " · " + item.getPrice()));
            if (marker != null) {
                markerProjects.put(marker, item);
            }
            if (first == null) {
                first = point;
            }
            if (focusId.equals(item.getPropertyId())) {
                focused = point;
            }
            count++;
        }
        if (mapsConfigured) {
            statusView.setText(count == 0
                    ? "No hay proyectos con coordenadas válidas."
                    : count + " proyectos cargados desde Firestore.");
        }
        if (focused != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focused, 16f));
        } else if (first != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(first, 12f));
        }
    }

    private void loadNearbyPlaces(UsuarioPropertyListItem project) {
        if (placesClient == null || !project.hasCoordinates()) {
            nearbyView.setText("Google Places no está configurado.");
            return;
        }
        nearbyView.setText("Buscando puntos de interés cercanos…");
        LatLng center = new LatLng(project.getLatitude(), project.getLongitude());
        CircularBounds bounds = CircularBounds.newInstance(center, 1500);
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.LOCATION,
                Place.Field.PRIMARY_TYPE
        );
        SearchNearbyRequest request = SearchNearbyRequest.builder(bounds, fields)
                .setIncludedTypes(Arrays.asList("school", "hospital", "bus_station", "shopping_mall"))
                .setMaxResultCount(8)
                .build();
        placesClient.searchNearby(request)
                .addOnSuccessListener(response -> {
                    List<String> names = new ArrayList<>();
                    for (Place place : response.getPlaces()) {
                        String name = place.getDisplayName();
                        if (name != null && !name.trim().isEmpty()) {
                            names.add(name);
                        }
                        LatLng location = place.getLocation();
                        if (location != null) {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(name == null ? "Punto de interés" : name)
                                    .snippet("Lugar cercano · Google Places"));
                        }
                    }
                    nearbyView.setText(names.isEmpty()
                            ? "No se encontraron puntos cercanos. Datos de lugares: Google."
                            : "Cerca de " + project.getTitle() + ": " + android.text.TextUtils.join(", ", names)
                            + ". Datos de lugares: Google.");
                })
                .addOnFailureListener(error ->
                        nearbyView.setText("No se pudieron consultar puntos cercanos: " + error.getMessage()));
    }

    private void openProperty(UsuarioPropertyListItem item) {
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

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
