package com.example.proyecto_iot.maps;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_ADDRESS = "map_address";
    public static final String EXTRA_DISTRICT = "map_district";
    public static final String EXTRA_LATITUDE = "map_latitude";
    public static final String EXTRA_LONGITUDE = "map_longitude";

    private double latitude = -12.0464;
    private double longitude = -77.0428;
    private String address = "";
    private String district = "";
    private GoogleMap googleMap;
    private Marker marker;
    private TextView addressView;
    private View confirmButton;
    private final ExecutorService geocodingExecutor = Executors.newSingleThreadExecutor();
    private int geocodingRequestId;

    private final ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    LatLng location = place.getLocation();
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

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.projectLocationMap);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }

        if (!MapsPlatformConfig.initializePlaces(this)) {
            findViewById(R.id.btnSearchPlace).setEnabled(false);
            Toast.makeText(this, "Agrega MAPS_API_KEY en local.properties para usar Google Maps y Places.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(point -> {
            latitude = point.latitude;
            longitude = point.longitude;
            address = "";
            district = "";
            updateMap(false);
            resolveAddressFromCoordinates(false);
        });
        updateMap(true);
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
        if (googleMap == null) {
            return;
        }
        LatLng point = new LatLng(latitude, longitude);
        if (marker == null) {
            marker = googleMap.addMarker(new MarkerOptions().position(point).draggable(true));
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override public void onMarkerDragStart(Marker marker) { }
                @Override public void onMarkerDrag(Marker marker) { }
                @Override public void onMarkerDragEnd(Marker dragged) {
                    latitude = dragged.getPosition().latitude;
                    longitude = dragged.getPosition().longitude;
                    address = "";
                    district = "";
                    updateAddressText();
                    resolveAddressFromCoordinates(false);
                }
            });
        } else {
            marker.setPosition(point);
        }
        if (moveCamera) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
        }
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
                // Keep a clear coordinate fallback if the geocoder is temporarily unavailable.
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
