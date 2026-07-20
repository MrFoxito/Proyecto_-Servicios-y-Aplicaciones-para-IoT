package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.entity.Separacion;

import java.util.Locale;

/** Pure presentation rules for the advisor's separation list. */
public final class SeparationPresentationPolicy {
    private SeparationPresentationPolicy() { }

    public static String canonicalProjectId(String propertyId, String projectId, String proyectoId) {
        return first(propertyId, projectId, proyectoId);
    }

    public static boolean isManagedPen(Separacion separation) {
        if (separation == null) return false;
        String currency = value(separation.getCurrency()).toUpperCase(Locale.ROOT);
        if ("PEN".equals(currency)) return true;
        return currency.isEmpty() && value(separation.getMontoTexto()).startsWith("S/");
    }

    public static boolean isManagedStatus(Separacion separation) {
        String status = separation == null ? "" : value(separation.getEstado());
        return "pagada".equalsIgnoreCase(status) || "aprobada".equalsIgnoreCase(status);
    }

    public static double penAmount(Separacion separation) {
        if (!isManagedPen(separation)) return 0d;
        if (separation.getAmount() > 0d) return separation.getAmount();
        String raw = value(separation.getMontoTexto()).replaceAll("[^0-9,.-]", "");
        if (raw.contains(",") && raw.contains(".")) raw = raw.replace(",", "");
        else if (raw.contains(",")) raw = raw.replace(",", ".");
        try { return Double.parseDouble(raw); } catch (Exception ignored) { return 0d; }
    }

    private static String first(String... values) {
        if (values == null) return "";
        for (String value : values) if (!value(value).isEmpty()) return value(value);
        return "";
    }

    private static String value(String value) { return value == null ? "" : value.trim(); }
}
