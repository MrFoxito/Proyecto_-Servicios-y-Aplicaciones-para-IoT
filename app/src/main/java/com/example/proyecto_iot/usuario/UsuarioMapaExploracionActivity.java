package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class UsuarioMapaExploracionActivity extends BaseUsuarioActivity {

    public static final String EXTRA_FOCUS_LAT = "extra_focus_lat";
    public static final String EXTRA_FOCUS_LNG = "extra_focus_lng";

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_mapa_exploracion);
        
        double focusLat = getIntent().getDoubleExtra(EXTRA_FOCUS_LAT, -12.0464);
        double focusLng = getIntent().getDoubleExtra(EXTRA_FOCUS_LNG, -77.0428);
        double focusZoom = getIntent().hasExtra(EXTRA_FOCUS_LAT) ? 15.0 : 12.0;

        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.getMapboxMap().loadStyleUri(Style.STANDARD, style -> {
                CameraOptions cameraPosition = new CameraOptions.Builder()
                        .center(Point.fromLngLat(focusLng, focusLat)) // Lima, Peru by default
                        .zoom(focusZoom)
                        .build();
                mapView.getMapboxMap().setCamera(cameraPosition);
                addDynamicMarkers();
            });
        }
        
        setupUserBottomNav(R.id.navUserExplore);
        setupActions();
    }

    private void addDynamicMarkers() {
        AnnotationPlugin annotationApi = mapView.getPlugin("mapbox-annotations");
        if (annotationApi != null) {
            PointAnnotationManager manager = (PointAnnotationManager) annotationApi.createAnnotationManager(AnnotationType.PointAnnotation, new AnnotationConfig());
            
            boolean hasFocus = getIntent().hasExtra(EXTRA_FOCUS_LAT);
            if (hasFocus) {
                double focusLat = getIntent().getDoubleExtra(EXTRA_FOCUS_LAT, -12.0464);
                double focusLng = getIntent().getDoubleExtra(EXTRA_FOCUS_LNG, -77.0428);
                addMarker(manager, focusLng, focusLat, 40, Color.parseColor("#E63946")); // Big red pin
            } else {
                addMarker(manager, -77.0315, -12.1221, 24, Color.parseColor("#000000")); // Villa Luminara
                addMarker(manager, -77.0353, -12.0970, 24, Color.parseColor("#000000")); // Refugio Celeste
                addMarker(manager, -77.0163, -12.1030, 24, Color.parseColor("#000000")); // Casa Meridian
            }
        }
    }

    private void addMarker(PointAnnotationManager manager, double lng, double lat, int radius, int colorHex) {
        Bitmap bitmap = createPinBitmap(radius, colorHex);
        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(bitmap);
        manager.create(options);
    }

    private Bitmap createPinBitmap(int radius, int colorHex) {
        int diameter = radius * 2;
        Bitmap bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(colorHex);
        canvas.drawCircle(radius, radius, radius - 4, paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(radius, radius, radius - 4, paint);
        return bitmap;
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackMapExplore);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View openList = findViewById(R.id.btnOpenListFromMap);
        if (openList != null) {
            openList.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));
        }

        bindPropertyCard(
                R.id.mapPropertyCard1,
                UsuarioPropertyCatalog.ID_VILLA_LUMINARA,
                getString(R.string.home_featured_property),
                getString(R.string.home_featured_price),
                getString(R.string.home_featured_location)
        );
        bindPropertyCard(
                R.id.mapPropertyCard2,
                UsuarioPropertyCatalog.ID_REFUGIO_CELESTE,
                getString(R.string.home_popular_property_2),
                getString(R.string.home_popular_price_2),
                getString(R.string.property_list_location_two)
        );
    }

    private void bindPropertyCard(int viewId, String propertyId, String title, String price, String location) {
        View card = findViewById(viewId);
        if (card == null) {
            return;
        }
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, propertyId);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, title);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, price);
            intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location);
            startActivity(intent);
        });
    }
}
