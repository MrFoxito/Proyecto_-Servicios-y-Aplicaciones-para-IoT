package com.example.proyecto_iot.usuario;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.maps.MapsPlatformConfig;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Google Maps project explorer. It intentionally does not request device location. */
public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity {
    public static final String EXTRA_FOCUS_PROJECT_ID = "focus_project_id";

    /** Opens the map in single-project mode. Without an id, the global explorer is preserved. */
    public static Intent focusedProjectIntent(Context context, String projectId) {
        Intent intent = new Intent(context, UsuarioMapaExploracionActivity.class);
        if (projectId != null && !projectId.trim().isEmpty()) {
            intent.putExtra(EXTRA_FOCUS_PROJECT_ID, projectId.trim());
        }
        return intent;
    }

    private final Map<Marker, UsuarioPropertyListItem> markerProjects = new HashMap<>();
    private FrameLayout mapHost;
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView statusView;
    private TextView nearbyView;
    private boolean destroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_mapa_exploracion);
        setupUserBottomNav(R.id.navUserExplore);
        statusView = findViewById(R.id.tvMapStatus);
        nearbyView = findViewById(R.id.tvNearbyPlaces);
        mapHost = findViewById(R.id.userProjectsMap);
        findViewById(R.id.btnBackMapExplore).setOnClickListener(v -> finish());
        findViewById(R.id.btnOpenListFromMap).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));
        if (MapsPlatformConfig.isConfigured(this)) initializeMap(savedInstanceState);
        else showMapFallback(MapsPlatformConfig.configurationMessage());
    }

    private void initializeMap(Bundle state) {
        try {
            mapView = new MapView(this);
            mapView.onCreate(state);
            mapHost.addView(mapView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mapView.getMapAsync(map -> {
                if (destroyed || isFinishing() || isDestroyed()) return;
                googleMap = map;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setOnMarkerClickListener(marker -> {
                    UsuarioPropertyListItem item = markerProjects.get(marker);
                    if (item != null) openProperty(item);
                    return item != null;
                });
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-12.0464, -77.0428), 11f));
                loadProjects();
            });
        } catch (RuntimeException error) {
            showMapFallback("No fue posible iniciar Google Maps. Usa la lista de proyectos.");
        }
    }

    private void loadProjects() {
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (!destroyed && !isFinishing() && !isDestroyed()) renderProjects(projects);
            }
            @Override public void onError(String message) {
                if (!destroyed && statusView != null) statusView.setText(message);
            }
        });
    }

    private void renderProjects(List<UsuarioPropertyListItem> projects) {
        if (googleMap == null) return;
        googleMap.clear();
        markerProjects.clear();
        String focusId = getIntent() == null ? "" : value(getIntent().getStringExtra(EXTRA_FOCUS_PROJECT_ID));
        boolean focusRequested = !focusId.isEmpty();
        LatLng first = null;
        LatLng focused = null;
        int count = 0;
        for (UsuarioPropertyListItem item : projects) {
            if (focusRequested && !focusId.equals(item.getPropertyId())) continue;
            if (!item.hasCoordinates()) continue;
            LatLng point = new LatLng(item.getLatitude(), item.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(point).title(item.getTitle()));
            if (marker != null) markerProjects.put(marker, item);
            if (first == null) first = point;
            if (focusId.equals(item.getPropertyId())) focused = point;
            count++;
        }
        if (focusRequested && focused == null) {
            if (statusView != null) statusView.setText("El proyecto seleccionado no existe o no tiene coordenadas válidas.");
            if (nearbyView != null) nearbyView.setText("No se mostrarán otros proyectos en este mapa.");
            return;
        }
        if (statusView != null) statusView.setText(count == 0 ? "No hay proyectos con coordenadas válidas." : count + " proyectos cargados.");
        if (nearbyView != null) nearbyView.setText("Toca un proyecto en el mapa para ver sus detalles.");
        LatLng target = focused != null ? focused : first;
        if (target != null) googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, focused != null ? 16f : 12f));
    }

    private void openProperty(UsuarioPropertyListItem item) { startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item)); }

    private void showMapFallback(String message) {
        if (statusView != null) statusView.setText(message);
        if (nearbyView != null) nearbyView.setText("La navegación por lista continúa disponible.");
        if (mapHost == null) return;
        mapHost.removeAllViews();
        TextView fallback = new TextView(this);
        fallback.setText(message);
        fallback.setTextColor(ContextCompat.getColor(this, R.color.app_text_secondary));
        fallback.setGravity(Gravity.CENTER);
        fallback.setPadding(32, 32, 32, 32);
        mapHost.addView(fallback, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private String value(String input) { return input == null ? "" : input.trim(); }
    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { if (mapView != null) mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { destroyed = true; markerProjects.clear(); if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
}
