package com.example.proyecto_iot.maps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.proyecto_iot.R;
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
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

/** Safe Mapbox preview shared by the administrator project create, edit and detail screens. */
public class ProjectMapPreviewController {
    private final Runnable clickAction;
    private final FragmentActivity activity;
    private final FrameLayout host;
    private MapView mapView;
    private PointAnnotationManager annotationManager;
    private PointAnnotation marker;
    private double latitude;
    private double longitude;
    private boolean destroyed;

    public ProjectMapPreviewController(FragmentActivity activity, @IdRes int viewId,
                                       double latitude, double longitude, Runnable clickAction) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.clickAction = clickAction;
        this.host = activity.findViewById(viewId);
        initialize();
    }

    private void initialize() {
        if (host == null) return;
        if (!MapboxConfig.isConfigured(activity)) {
            showFallback(MapboxConfig.configurationMessage());
            return;
        }
        try {
            mapView = new MapView(activity);
            host.addView(mapView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                if (destroyed || activity.isFinishing() || activity.isDestroyed()) return;
                AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
                annotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);
                if (clickAction != null) {
                    annotationManager.addClickListener(annotation -> {
                        clickAction.run();
                        return true;
                    });
                    GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
                    gestures.addOnMapClickListener(point -> {
                        clickAction.run();
                        return true;
                    });
                }
                render();
            });
        } catch (RuntimeException error) {
            showFallback("No fue posible iniciar Mapbox. La ubicaciÃ³n sigue disponible en el formulario.");
        }
    }

    public void showLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        render();
    }

    private void render() {
        if (destroyed || annotationManager == null || !validCoordinates(latitude, longitude)) return;
        Point point = Point.fromLngLat(longitude, latitude);
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(16.0).build());
        if (marker == null) {
            marker = annotationManager.create(new PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(markerBitmap()));
        } else {
            marker.setPoint(point);
            annotationManager.update(marker);
        }
    }

    public void release() {
        destroyed = true;
        marker = null;
        if (annotationManager != null) {
            annotationManager.deleteAll();
            annotationManager = null;
        }
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
    }

    private Bitmap markerBitmap() {
        Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_location, activity.getTheme());
        if (drawable == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        int width = Math.max(32, drawable.getIntrinsicWidth());
        int height = Math.max(32, drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void showFallback(String message) {
        if (host == null) return;
        host.removeAllViews();
        TextView fallback = new TextView(activity);
        fallback.setText(message);
        fallback.setTextColor(ContextCompat.getColor(activity, R.color.app_text_secondary));
        fallback.setGravity(Gravity.CENTER);
        fallback.setPadding(32, 32, 32, 32);
        host.addView(fallback, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private boolean validCoordinates(double lat, double lng) {
        return !Double.isNaN(lat) && !Double.isNaN(lng) && !Double.isInfinite(lat)
                && !Double.isInfinite(lng) && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
