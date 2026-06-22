package com.example.proyecto_iot.maps;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ProjectMapPreviewController {
    private final Runnable clickAction;
    private GoogleMap googleMap;
    private Marker marker;
    private double latitude;
    private double longitude;

    public ProjectMapPreviewController(
            FragmentActivity activity,
            @IdRes int fragmentId,
            double latitude,
            double longitude,
            Runnable clickAction
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.clickAction = clickAction;
        SupportMapFragment fragment = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentById(fragmentId);
        if (fragment != null) {
            fragment.getMapAsync(map -> {
                try {
                    googleMap = map;
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.getUiSettings().setCompassEnabled(false);
                    if (clickAction != null) {
                        googleMap.getUiSettings().setAllGesturesEnabled(false);
                        googleMap.setOnMapClickListener(point -> clickAction.run());
                        googleMap.setOnMarkerClickListener(selected -> {
                            clickAction.run();
                            return true;
                        });
                    }
                    render();
                } catch (RuntimeException ignored) {
                    googleMap = null;
                    marker = null;
                }
            });
        }
    }

    public void showLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        render();
    }

    private void render() {
        if (googleMap == null || Double.isNaN(latitude) || Double.isNaN(longitude)
                || Double.isInfinite(latitude) || Double.isInfinite(longitude)) {
            return;
        }
        try {
            LatLng point = new LatLng(latitude, longitude);
            if (marker == null) {
                marker = googleMap.addMarker(new MarkerOptions().position(point));
            } else {
                marker.setPosition(point);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
        } catch (RuntimeException ignored) {
            marker = null;
        }
    }
}
