package com.example.proyecto_iot.maps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.proyecto_iot.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.Annotation;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Mapbox location picker used by administrator project create and edit flows. */
public class ProjectLocationPickerActivity extends AppCompatActivity {
    public static final String EXTRA_ADDRESS = "map_address";
    public static final String EXTRA_DISTRICT = "map_district";
    public static final String EXTRA_LATITUDE = "map_latitude";
    public static final String EXTRA_LONGITUDE = "map_longitude";

    private double latitude = -12.0464;
    private double longitude = -77.0428;
    private String address = "";
    private String district = "";
    private MapView mapView;
    private PointAnnotationManager annotationManager;
    private PointAnnotation marker;
    private FrameLayout mapHost;
    private TextView addressView;
    private View confirmButton;
    private final ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private int geocodingRequestId;

    private final ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    com.google.android.gms.maps.model.LatLng location = place.getLocation();
                    if (location != null) {
                        latitude = location.latitude;
                        longitude = location.longitude;
                        address = first(place.getFormattedAddress(), place.getDisplayName());
                        district = inferDistrict(address);
                        updateMap(true);
                        if (district.isEmpty()) resolveAddressFromCoordinates(true);
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Toast.makeText(this, first(status.getStatusMessage(), "No se pudo buscar la direcciÃ³n."), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_location_picker);
        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, latitude);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, longitude);
        address = first(getIntent().getStringExtra(EXTRA_ADDRESS));
        district = first(getIntent().getStringExtra(EXTRA_DISTRICT));
        addressView = findViewById(R.id.tvSelectedAddress);
        confirmButton = findViewById(R.id.btnConfirmLocation);
        mapHost = findViewById(R.id.projectLocationMap);
        findViewById(R.id.btnBackMapPicker).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchPlace).setOnClickListener(v -> openAutocomplete());
        confirmButton.setOnClickListener(v -> returnLocation());
        updateAddressText();

        if (MapboxConfig.isConfigured(this)) initializeMap();
        else showMapFallback(MapboxConfig.configurationMessage());
        if (!MapsPlatformConfig.initializePlaces(this)) {
            findViewById(R.id.btnSearchPlace).setEnabled(false);
        }
    }

    private void initializeMap() {
        try {
            mapView = new MapView(this);
            mapHost.addView(mapView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                if (isFinishing() || isDestroyed()) return;
                AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
                annotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);
                annotationManager.addDragListener(new OnPointAnnotationDragListener() {
                    @Override public void onAnnotationDragStarted(Annotation<?> annotation) { }
                    @Override public void onAnnotationDrag(Annotation<?> annotation) { }
                    @Override public void onAnnotationDragFinished(Annotation<?> annotation) {
                        if (annotation instanceof PointAnnotation) {
                            Point point = ((PointAnnotation) annotation).getPoint();
                            selectPoint(point, false);
                        }
                    }
                });
                GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
                gestures.addOnMapClickListener(point -> {
                    selectPoint(point, false);
                    return true;
                });
                updateMap(true);
            });
        } catch (RuntimeException error) {
            showMapFallback("No fue posible iniciar Mapbox.");
        }
    }

    private void selectPoint(Point point, boolean keepAddress) {
        latitude = point.latitude();
        longitude = point.longitude();
        if (!keepAddress) {
            address = "";
            district = "";
        }
        updateMap(false);
        resolveAddressFromCoordinates(keepAddress);
    }

    private void updateMap(boolean moveCamera) {
        updateAddressText();
        if (annotationManager == null) return;
        Point point = Point.fromLngLat(longitude, latitude);
        if (marker == null) {
            marker = annotationManager.create(new PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(markerBitmap())
                    .withDraggable(true));
        } else {
            marker.setPoint(point);
            annotationManager.update(marker);
        }
        if (moveCamera) mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(16.0).build());
    }

    private void showMapFallback(String message) {
        if (address.isEmpty()) address = coordinateAddress(latitude, longitude);
        updateAddressText();
        if (mapHost == null) return;
        mapHost.removeAllViews();
        TextView fallback = new TextView(this);
        fallback.setText(message + "\nSe guardarÃ¡ la ubicaciÃ³n por coordenadas.");
        fallback.setGravity(android.view.Gravity.CENTER);
        fallback.setPadding(32, 32, 32, 32);
        fallback.setTextColor(ContextCompat.getColor(this, R.color.app_text_secondary));
        mapHost.addView(fallback, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void openAutocomplete() {
        if (!MapsPlatformConfig.initializePlaces(this)) {
            Toast.makeText(this, "La bÃºsqueda requiere MAPS_API_KEY y Places API (New). Puedes seleccionar directamente en Mapbox.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, Arrays.asList(
                Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION))
                .setCountries(Arrays.asList("PE")).build(this);
        autocompleteLauncher.launch(intent);
    }

    private void updateAddressText() {
        if (addressView != null) {
            String label = address.isEmpty() ? "Buscando direcciÃ³n exacta..." : address;
            addressView.setText(label + "\n" + String.format(Locale.US, "Lat %.5f, Lng %.5f", latitude, longitude));
        }
    }

    private void returnLocation() {
        if (address.isEmpty()) {
            Toast.makeText(this, "Espera mientras se obtiene la direcciÃ³n exacta.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_ADDRESS, address);
        data.putExtra(EXTRA_DISTRICT, district);
        data.putExtra(EXTRA_LATITUDE, latitude);
        data.putExtra(EXTRA_LONGITUDE, longitude);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void resolveAddressFromCoordinates(boolean keepCurrentAddress) {
        int requestId = ++geocodingRequestId;
        double requestedLat = latitude;
        double requestedLng = longitude;
        String currentAddress = address;
        setResolvingAddress(true);
        geocodingExecutor.execute(() -> {
            String resolvedAddress = "";
            String resolvedDistrict = "";
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
                List<Address> results = geocoder.getFromLocation(requestedLat, requestedLng, 1);
                if (results != null && !results.isEmpty()) {
                    Address result = results.get(0);
                    resolvedAddress = first(result.getAddressLine(0), result.getThoroughfare(), result.getFeatureName());
                    resolvedDistrict = first(inferDistrict(resolvedAddress), inferDistrict(result.getSubLocality()), inferDistrict(result.getLocality()));
                }
            } catch (IOException | IllegalArgumentException ignored) { }
            String finalAddress = keepCurrentAddress ? first(currentAddress, resolvedAddress, coordinateAddress(requestedLat, requestedLng))
                    : first(resolvedAddress, coordinateAddress(requestedLat, requestedLng));
            String finalDistrict = first(resolvedDistrict, inferDistrict(finalAddress));
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || requestId != geocodingRequestId) return;
                address = finalAddress;
                district = finalDistrict;
                setResolvingAddress(false);
                updateAddressText();
            });
        });
    }

    private void setResolvingAddress(boolean resolving) {
        confirmButton.setEnabled(!resolving);
        confirmButton.setAlpha(resolving ? .55f : 1f);
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

    private String coordinateAddress(double lat, double lng) { return String.format(Locale.US, "UbicaciÃ³n %.5f, %.5f", lat, lng); }
    private String inferDistrict(String value) {
        String normalized = normalize(value);
        String[] districts = {"Miraflores", "San Isidro", "Santiago de Surco", "Barranco", "Pueblo Libre", "San Miguel", "Jesus Maria", "Magdalena del Mar", "La Molina", "Lince", "Cercado de Lima", "San Borja", "Surquillo", "Chorrillos", "Los Olivos"};
        for (String candidate : districts) if (normalized.contains(normalize(candidate))) return candidate;
        return "";
    }
    private String normalize(String value) { return Normalizer.normalize(first(value), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT); }
    private String first(String... values) { for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim(); return ""; }

    @Override protected void onDestroy() {
        geocodingRequestId++;
        geocodingExecutor.shutdownNow();
        if (annotationManager != null) annotationManager.deleteAll();
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
    }
}
