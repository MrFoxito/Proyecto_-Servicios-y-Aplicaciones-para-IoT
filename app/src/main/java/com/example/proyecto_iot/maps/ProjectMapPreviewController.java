package com.example.proyecto_iot.maps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.proyecto_iot.R;
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

public class ProjectMapPreviewController {
    private final Runnable clickAction;
    private final FragmentActivity activity;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;
    private PointAnnotation marker;
    private double latitude;
    private double longitude;

    public ProjectMapPreviewController(
            FragmentActivity activity,
            @IdRes int viewId,
            double latitude,
            double longitude,
            Runnable clickAction
    ) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.clickAction = clickAction;
        this.mapView = activity.findViewById(viewId);
        
        if (mapView != null) {
            this.mapboxMap = mapView.getMapboxMap();
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, style -> {
                if (clickAction != null) {
                    GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
                    if (gestures != null) {
                        gestures.setScrollEnabled(false);
                        gestures.setPinchToZoomEnabled(false);
                        gestures.setDoubleTapToZoomInEnabled(false);
                        gestures.setDoubleTouchToZoomOutEnabled(false);
                        gestures.addOnMapClickListener(point -> {
                            clickAction.run();
                            return true;
                        });
                    }
                }
                
                AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(mapView);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new com.mapbox.maps.plugin.annotation.AnnotationConfig());
                
                if (clickAction != null) {
                    pointAnnotationManager.addClickListener(pointAnnotation -> {
                        clickAction.run();
                        return true;
                    });
                }
                
                render();
            });
        }
    }

    public void showLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        render();
    }

    private void render() {
        if (mapboxMap == null || Double.isNaN(latitude) || Double.isNaN(longitude)
                || Double.isInfinite(latitude) || Double.isInfinite(longitude)) {
            return;
        }
        try {
            Point point = Point.fromLngLat(longitude, latitude);
            
            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(point)
                    .zoom(16.0)
                    .build();
            mapboxMap.setCamera(cameraPosition);
            
            if (pointAnnotationManager != null && activity != null) {
                if (marker != null) {
                    marker.setPoint(point);
                    pointAnnotationManager.update(marker);
                } else {
                    Bitmap icon = getBitmapFromDrawable(activity, android.R.drawable.ic_menu_myplaces);
                    if (icon == null) {
                        icon = getBitmapFromDrawable(activity, R.drawable.ic_location);
                    }
                    if (icon != null) {
                        PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(icon);
                        marker = pointAnnotationManager.create(options);
                    }
                }
            }
        } catch (RuntimeException ignored) {
            marker = null;
        }
    }
    
    private Bitmap getBitmapFromDrawable(FragmentActivity context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 48,
                drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 48, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
