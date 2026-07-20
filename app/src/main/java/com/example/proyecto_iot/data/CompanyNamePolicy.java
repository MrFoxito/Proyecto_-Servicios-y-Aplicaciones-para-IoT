package com.example.proyecto_iot.data;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Centraliza la prioridad y la comparación de nombres corporativos sin modificar
 * los valores originales que se muestran en la interfaz.
 */
public final class CompanyNamePolicy {
    private CompanyNamePolicy() {
    }

    public static String resolve(
            String companyName,
            String companyAlias,
            String userName,
            String userLegacyName,
            String invitationName
    ) {
        return firstNonEmpty(companyName, companyAlias, userName, userLegacyName, invitationName);
    }

    public static boolean matches(String first, String second) {
        String normalizedFirst = comparable(first);
        String normalizedSecond = comparable(second);
        return !normalizedFirst.isEmpty() && normalizedFirst.equals(normalizedSecond);
    }

    public static boolean shouldBackfill(String existingValue, String candidate) {
        return clean(existingValue).isEmpty() && !clean(candidate).isEmpty();
    }

    public static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isEmpty()) return cleaned;
        }
        return "";
    }

    private static String comparable(String value) {
        String cleaned = clean(value).toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) return "";
        return Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("\\s+", " ");
    }
}
