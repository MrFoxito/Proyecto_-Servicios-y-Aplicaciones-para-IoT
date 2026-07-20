package com.example.proyecto_iot.asesor;

/** Pure image-selection rules for separation presentation. */
public final class SeparationProjectImagePolicy {
    private SeparationProjectImagePolicy() { }

    public static String select(String primaryImageUrl, String imageUrl, String imagenUrl,
                                String propertyImageUrl, String galleryImageUrl) {
        return firstValid(primaryImageUrl, imageUrl, imagenUrl, propertyImageUrl, galleryImageUrl);
    }

    public static boolean isUsableImageUrl(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        return trimmed.startsWith("https://") || trimmed.startsWith("http://")
                || trimmed.startsWith("data:image/");
    }

    private static String firstValid(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (isUsableImageUrl(value)) return value.trim();
        }
        return "";
    }
}
