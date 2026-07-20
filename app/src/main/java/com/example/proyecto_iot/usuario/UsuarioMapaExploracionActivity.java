package com.example.proyecto_iot.usuario;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.maps.MapboxConfig;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mapbox project explorer. It intentionally does not request device location. */
public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity {
    public static final String EXTRA_FOCUS_PROJECT_ID = "focus_project_id";

    public static Intent focusedProjectIntent(Context context, String projectId) {
        Intent intent = new Intent(context, UsuarioMapaExploracionActivity.class);
        if (projectId != null && !projectId.trim().isEmpty()) {
            intent.putExtra(EXTRA_FOCUS_PROJECT_ID, projectId.trim());
        }
        return intent;
    }

    private final Map<String, UsuarioPropertyListItem> markerProjects = new HashMap<>();
    private FrameLayout mapHost;
    private MapView mapView;
    private PointAnnotationManager annotationManager;
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
        if (MapboxConfig.isConfigured(this)) initializeMap();
        else showMapFallback(MapboxConfig.configurationMessage());
    }

    private void initializeMap() {
        try {
            mapView = new MapView(this);
            mapHost.addView(mapView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                if (destroyed || isFinishing() || isDestroyed()) return;
                AnnotationPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
                annotationManager = PointAnnotationManagerKt.createPointAnnotationManager(plugin, null);
                annotationManager.addClickListener(annotation -> {
                    UsuarioPropertyListItem item = markerProjects.get(annotation.getId());
                    if (item != null) openProperty(item);
                    return item != null;
                });
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(Point.fromLngLat(-77.0428, -12.0464)).zoom(11.0).build());
                loadProjects();
            });
        } catch (RuntimeException error) {
            showMapFallback("No fue posible iniciar Mapbox. Usa la lista de proyectos.");
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
        if (annotationManager == null) return;
        annotationManager.deleteAll();
        markerProjects.clear();
        String focusId = getIntent() == null ? "" : value(getIntent().getStringExtra(EXTRA_FOCUS_PROJECT_ID));
        boolean focusRequested = !focusId.isEmpty();
        Point first = null;
        Point focused = null;
        int count = 0;
        for (UsuarioPropertyListItem item : projects) {
            if (focusRequested && !focusId.equals(item.getPropertyId())) continue;
            if (!item.hasCoordinates()) continue;
            Point point = Point.fromLngLat(item.getLongitude(), item.getLatitude());
            PointAnnotation marker = annotationManager.create(new PointAnnotationOptions()
                    .withPoint(point).withIconImage(markerBitmap()));
            markerProjects.put(marker.getId(), item);
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
        if (nearbyView != null) nearbyView.setText("Toca el pin de un proyecto para ver sus detalles.");
        Point target = focused != null ? focused : first;
        if (target != null) mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(target).zoom(focused != null ? 16.0 : 12.0).build());
    }

    private void openProperty(UsuarioPropertyListItem item) {
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item));
    }

    private Bitmap markerBitmap() {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location, getTheme());
        if (drawable == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        int width = Math.max(32, drawable.getIntrinsicWidth());
        int height = Math.max(32, drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

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

    @Override protected void onDestroy() {
        destroyed = true;
        markerProjects.clear();
        if (annotationManager != null) annotationManager.deleteAll();
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
    }
}
