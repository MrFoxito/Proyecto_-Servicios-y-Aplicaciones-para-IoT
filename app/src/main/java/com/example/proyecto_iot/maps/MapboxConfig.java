package com.example.proyecto_iot.maps;

import android.content.Context;

import com.example.proyecto_iot.R;

/** Validates the public Mapbox token packaged from ignored local.properties. */
public final class MapboxConfig {
    private MapboxConfig() {
    }

    public static boolean isConfigured(Context context) {
        return isPublicToken(context.getString(R.string.mapbox_access_token));
    }

    public static boolean isPublicToken(String value) {
        String token = value == null ? "" : value.trim();
        return token.startsWith("pk.")
                && token.length() > 20
                && !token.contains("REEMPLAZA")
                && !token.equals("YOUR_MAPBOX_ACCESS_TOKEN");
    }

    public static String configurationMessage() {
        return "Configura MAPBOX_ACCESS_TOKEN en local.properties para mostrar el mapa.";
    }
}
