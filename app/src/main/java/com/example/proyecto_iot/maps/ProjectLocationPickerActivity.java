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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;
    private PointAnnotation marker;
    
    private TextView addressView;
    private View confirmButton;
    private final ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private int geocodingRequestId;

    private final ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    com.google.android.gms.maps.model.LatLng location = place.getLocation();
                    if (location != null) {
                        latitude = location.latitude;
                        longitude = location.longitude;
                        address = valueOr(place.getFormattedAddress(), place.getDisplayName());
                        district = inferDistrict(address);
                        updateMap(true);
                        if (district.isEmpty()) {
                            resolveAddressFromCoordinates(true);
                        }
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_location_picker);

        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, latitude);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, longitude);
        address = valueOr(getIntent().getStringExtra(EXTRA_ADDRESS));
        district = valueOr(getIntent().getStringExtra(EXTRA_DISTRICT));
        addressView = findViewById(R.id.tvSelectedAddress);
        confirmButton = findViewById(R.id.btnConfirmLocation);
        updateAddressText();

        findViewById(R.id.btnBackMapPicker).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchPlace).setOnClickListener(v -> openAutocomplete());
        confirmButton.setOnClickListener(v -> returnLocation());

        mapView = findViewById(R.id.projectLocationMap);
        mapboxMap = mapView.getMapboxMap();
        
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, style -> {
            AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(mapView);
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new com.mapbox.maps.plugin.annotation.AnnotationConfig());
            
            pointAnnotationManager.addDragListener(new com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener() {
                @Override public void onAnnotationDragStarted(com.mapbox.maps.plugin.annotation.Annotation<?> annotation) { }
                @Override public void onAnnotationDrag(com.mapbox.maps.plugin.annotation.Annotation<?> annotation) { }
                @Override public void onAnnotationDragFinished(com.mapbox.maps.plugin.annotation.Annotation<?> annotation) {
                    PointAnnotation pointAnnotation = (PointAnnotation) annotation;
                    latitude = pointAnnotation.getPoint().latitude();
                    longitude = pointAnnotation.getPoint().longitude();
                    address = "";
                    district = "";
                    updateAddressText();
                    resolveAddressFromCoordinates(false);
                }
            });

            GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
            gestures.addOnMapClickListener(point -> {
                latitude = point.latitude();
                longitude = point.longitude();
                address = "";
                district = "";
                updateMap(false);
                resolveAddressFromCoordinates(false);
                return true;
            });
            
            updateMap(true);
        });

        if (!MapsPlatformConfig.initializePlaces(this)) {
            findViewById(R.id.btnSearchPlace).setEnabled(false);
            Toast.makeText(this, "Agrega MAPS_API_KEY en local.properties para usar Google Places.", Toast.LENGTH_LONG).show();
        }
    }

    private void openAutocomplete() {
        if (!MapsPlatformConfig.initializePlaces(this)) {
            return;
        }
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION
        );
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(Arrays.asList("PE"))
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void updateMap(boolean moveCamera) {
        updateAddressText();
        if (mapboxMap == null) return;
        
        Point point = Point.fromLngLat(longitude, latitude);
        
        if (pointAnnotationManager != null) {
            if (marker == null) {
                Bitmap icon = getBitmapFromDrawable(this, R.drawable.ic_location);
                if (icon != null) {
                    PointAnnotationOptions options = new PointAnnotationOptions()
                        .withPoint(point)
                        .withDraggable(true)
                        .withIconImage(icon);
                    marker = pointAnnotationManager.create(options);
                }
            } else {
                marker.setPoint(point);
                pointAnnotationManager.update(marker);
            }
        }
        
        if (moveCamera) {
            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(point)
                    .zoom(16.0)
                    .build();
            mapboxMap.setCamera(cameraPosition);
        }
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

    private void updateAddressText() {
        if (addressView == null) {
            return;
        }
        String label = address.isEmpty() ? "Buscando dirección exacta..." : address;
        addressView.setText(label + "\n" + String.format(Locale.US, "Lat %.5f, Lng %.5f", latitude, longitude));
    }

    private void returnLocation() {
        if (address.isEmpty()) {
            Toast.makeText(this, "Espera mientras se obtiene la dirección exacta.", Toast.LENGTH_SHORT).show();
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
        final int requestId = ++geocodingRequestId;
        final double requestedLatitude = latitude;
        final double requestedLongitude = longitude;
        final String currentAddress = address;
        setResolvingAddress(true);

        geocodingExecutor.execute(() -> {
            String resolvedAddress = "";
            String resolvedDistrict = "";
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
                if (Geocoder.isPresent()) {
                    List<Address> results = geocoder.getFromLocation(
                            requestedLatitude,
                            requestedLongitude,
                            1
                    );
                    if (results != null && !results.isEmpty()) {
                        Address result = results.get(0);
                        resolvedAddress = valueOr(result.getAddressLine(0), joinAddress(result));
                        resolvedDistrict = valueOr(
                                inferDistrict(resolvedAddress),
                                inferDistrict(result.getSubLocality()),
                                inferDistrict(result.getLocality()),
                                inferDistrict(result.getSubAdminArea())
                        );
                    }
                }
            } catch (IOException | IllegalArgumentException ignored) {
            }

            final String finalAddress = keepCurrentAddress
                    ? valueOr(currentAddress, resolvedAddress, coordinateAddress(requestedLatitude, requestedLongitude))
                    : valueOr(resolvedAddress, coordinateAddress(requestedLatitude, requestedLongitude));
            final String finalDistrict = valueOr(resolvedDistrict, inferDistrict(finalAddress));
            runOnUiThread(() -> {
                if (requestId != geocodingRequestId) {
                    return;
                }
                address = finalAddress;
                district = finalDistrict;
                setResolvingAddress(false);
                updateAddressText();
            });
        });
    }

    private void setResolvingAddress(boolean resolving) {
        if (confirmButton != null) {
            confirmButton.setEnabled(!resolving);
            confirmButton.setAlpha(resolving ? 0.55f : 1f);
        }
    }

    private String joinAddress(Address result) {
        return valueOr(
                result.getThoroughfare(),
                result.getFeatureName(),
                result.getSubLocality(),
                result.getLocality()
        );
    }

    private String coordinateAddress(double lat, double lng) {
        return String.format(Locale.US, "Ubicación %.5f, %.5f", lat, lng);
    }

    private String inferDistrict(String value) {
        if (value == null) {
            return "";
        }
        String[] districts = {"Miraflores", "San Isidro", "Santiago de Surco", "Barranco", "Pueblo Libre",
                "San Miguel", "Jesus Maria", "Magdalena del Mar", "La Molina", "Lince",
                "Cercado de Lima", "San Borja", "Surquillo", "Chorrillos", "Los Olivos"};
        String normalizedValue = normalizeForComparison(value);
        for (String candidate : districts) {
            if (normalizedValue.contains(normalizeForComparison(candidate))) {
                return candidate;
            }
        }
        return "";
    }

    private String normalizeForComparison(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private String valueOr(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        geocodingRequestId++;
        geocodingExecutor.shutdownNow();
        super.onDestroy();
    }
}
