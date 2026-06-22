package com.example.proyecto_iot.data;

import com.example.proyecto_iot.R;

import java.util.Locale;

public final class AmenityIconResolver {
    private AmenityIconResolver() {
    }

    public static int resolve(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (normalized.contains("cowork")) return R.drawable.ic_admin_laptop;
        if (normalized.contains("pisc")) return R.drawable.ic_admin_pool;
        if (normalized.contains("gim")) return R.drawable.ic_amenity_gym;
        if (normalized.contains("bbq") || normalized.contains("parr")) return R.drawable.ic_amenity_bbq;
        if (normalized.contains("pet")) return R.drawable.ic_amenity_pet;
        if (normalized.contains("seguridad")) return R.drawable.ic_amenity_security;
        if (normalized.contains("estacion")) return R.drawable.ic_amenity_parking;
        if (normalized.contains("bici")) return R.drawable.ic_amenity_bike;
        if (normalized.contains("terraza")) return R.drawable.ic_amenity_terrace;
        if (normalized.contains("juegos")) return R.drawable.ic_amenity_playground;
        if (normalized.contains("lobby") || normalized.contains("lounge")) return R.drawable.ic_amenity_lobby;
        return R.drawable.ic_home;
    }
}
