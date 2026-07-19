package com.example.proyecto_iot.maps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.libraries.places.api.Places;

public final class MapsPlatformConfig {
    private static final String API_KEY_METADATA = "com.google.android.geo.API_KEY";

    private MapsPlatformConfig() {
    }

    public static String apiKey(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            Bundle metadata = info.metaData;
            return metadata == null ? "" : valueOr(metadata.getString(API_KEY_METADATA));
        } catch (PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    public static boolean isConfigured(Context context) {
        return isApiKey(apiKey(context));
    }

    /** Android Google Maps keys are client-side keys and must be restricted in Google Cloud. */
    public static boolean isApiKey(String value) {
        String key = valueOr(value);
        return key.startsWith("AIza")
                && key.length() >= 30
                && !"DEFAULT_API_KEY".equals(key)
                && !key.contains("REEMPLAZA");
    }

    public static String configurationMessage() {
        return "Configura MAPS_API_KEY en local.properties y habilita Maps SDK for Android.";
    }

    public static boolean initializePlaces(Context context) {
        if (!isConfigured(context)) {
            return false;
        }
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context.getApplicationContext(), apiKey(context));
        }
        return true;
    }

    private static String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
