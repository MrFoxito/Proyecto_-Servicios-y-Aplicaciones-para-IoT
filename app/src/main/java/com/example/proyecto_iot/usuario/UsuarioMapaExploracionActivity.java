package com.example.proyecto_iot.usuario;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.maps.MapsPlatformConfig;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity {
    public static final String EXTRA_FOCUS_PROJECT_ID = "focus_project_id";
    private final Map<PointAnnotation, UsuarioPropertyListItem> markerProjects = new HashMap<>();
    
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;
    
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
            statusView.setText("Configura MAPS_API_KEY en local.properties para activar Google Places.");
        }

        mapView = findViewById(R.id.userProjectsMap);
        mapboxMap = mapView.getMapboxMap();
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, style -> {
            AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(mapView);
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new com.mapbox.maps.plugin.annotation.AnnotationConfig());
            
            pointAnnotationManager.addClickListener(annotation -> {
                UsuarioPropertyListItem item = markerProjects.get(annotation);
                if (item != null) {
                    loadNearbyPlaces(item);
                    // Mostrar info simple o abrir. Aquí abrimos directamente o solo cargamos.
                    // Para replicar el comportamiento (1 click cargar places, 2 clicks/info click abrir),
                    // en Mapbox v11 es mejor abrir directamente o mostrar en la UI inferior y cargar places.
                    // Aquí la UI inferior se actualiza, y si se requiere abrir se puede dejar la lógica.
                }
                return true;
            });
            
            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(Point.fromLngLat(-77.0428, -12.0464))
                    .zoom(11.0)
                    .build();
            mapboxMap.setCamera(cameraPosition);
            
            loadProjects();
        });
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
        if (mapboxMap == null || pointAnnotationManager == null) {
            return;
        }
        pointAnnotationManager.deleteAll();
        markerProjects.clear();
        int count = 0;
        Point first = null;
        Point focused = null;
        String focusId = getIntent() == null ? "" : valueOr(getIntent().getStringExtra(EXTRA_FOCUS_PROJECT_ID));
        
        Bitmap icon = getBitmapFromDrawable(this, R.drawable.ic_location);
        
        for (UsuarioPropertyListItem item : projects) {
            if (!item.hasCoordinates()) {
                continue;
            }
            Point point = Point.fromLngLat(item.getLongitude(), item.getLatitude());
            
            if (icon != null) {
                PointAnnotationOptions options = new PointAnnotationOptions()
                    .withPoint(point)
                    .withTextField(item.getTitle())
                    .withTextOffset(Arrays.asList(0.0, 1.5))
                    .withIconImage(icon);
                    
                PointAnnotation marker = pointAnnotationManager.create(options);
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
            CameraOptions cam = new CameraOptions.Builder().center(focused).zoom(16.0).build();
            mapboxMap.setCamera(cam);
        } else if (first != null) {
            CameraOptions cam = new CameraOptions.Builder().center(first).zoom(12.0).build();
            mapboxMap.setCamera(cam);
        }
    }

    private void loadNearbyPlaces(UsuarioPropertyListItem project) {
        openProperty(project);
        
        if (placesClient == null || !project.hasCoordinates()) {
            nearbyView.setText("Google Places no está configurado.");
            return;
        }
        nearbyView.setText("Buscando puntos de interés cercanos…");
        com.google.android.gms.maps.model.LatLng center = new com.google.android.gms.maps.model.LatLng(project.getLatitude(), project.getLongitude());
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
                    Bitmap placesIcon = getBitmapFromDrawable(this, android.R.drawable.ic_menu_myplaces);
                    
                    for (Place place : response.getPlaces()) {
                        String name = place.getDisplayName();
                        if (name != null && !name.trim().isEmpty()) {
                            names.add(name);
                        }
                        com.google.android.gms.maps.model.LatLng location = place.getLocation();
                        if (location != null && pointAnnotationManager != null && placesIcon != null) {
                            Point point = Point.fromLngLat(location.longitude, location.latitude);
                            PointAnnotationOptions options = new PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(placesIcon);
                            pointAnnotationManager.create(options);
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
    
    private Bitmap getBitmapFromDrawable(Activity context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 48,
                drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 48, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
