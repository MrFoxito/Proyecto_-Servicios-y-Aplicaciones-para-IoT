package com.example.proyecto_iot.asesor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatting and parsing shared by the advisor's separation price presentation. */
public final class SeparationPricingPolicy {
    private SeparationPricingPolicy() { }

    public static boolean hasAmount(double amount) {
        return amount > 0d && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    public static double number(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        String raw = value == null ? "" : String.valueOf(value).trim();
        if (raw.isEmpty()) return 0d;
        String normalized = raw.replaceAll("[^0-9,.-]", "");
        if (normalized.contains(",") && normalized.contains(".")) normalized = normalized.replace(",", "");
        else if (normalized.contains(",")) normalized = normalized.replace(",", ".");
        try { return Double.parseDouble(normalized); } catch (NumberFormatException ignored) { return 0d; }
    }

    public static String formatPen(double amount) {
        if (!hasAmount(amount)) return "";
        DecimalFormat formatter = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "S/ " + formatter.format(amount);
    }

    public static String display(String snapshotText, double snapshotAmount, String fallbackText) {
        String text = value(snapshotText);
        if (!text.isEmpty() && number(text) > 0d) return text;
        if (hasAmount(snapshotAmount)) return formatPen(snapshotAmount);
        text = value(fallbackText);
        return number(text) > 0d ? text : "Monto no configurado";
    }

    public static String value(String value) { return value == null ? "" : value.trim(); }
}
