package com.example.proyecto_iot.usuario;

import java.util.ArrayList;
import java.util.List;

/** Pure presentation rules shared by Explore cards and their pagination trigger. */
public final class ExploreProjectPresentationPolicy {
    private ExploreProjectPresentationPolicy() { }

    public static String location(String district, String city) {
        String safeDistrict = value(district);
        String safeCity = value(city);
        if (!safeDistrict.isEmpty() && !safeCity.isEmpty()) return safeDistrict + ", " + safeCity;
        return safeDistrict.isEmpty() ? safeCity : safeDistrict;
    }

    public static String typologySummary(List<String> bedrooms, List<String> areas) {
        List<String> parts = new ArrayList<>();
        String bedroomPart = compact(bedrooms);
        String areaPart = compact(areas);
        if (!bedroomPart.isEmpty()) parts.add(bedroomPart + " dorm");
        if (!areaPart.isEmpty()) parts.add(areaPart);
        return join(parts);
    }

    public static boolean shouldLoadMore(
            boolean loading,
            boolean hasMore,
            int lastVisiblePosition,
            int itemCount
    ) {
        return !loading && hasMore && itemCount > 0
                && lastVisiblePosition >= Math.max(0, itemCount - 3);
    }

    private static String compact(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        List<String> clean = new ArrayList<>();
        for (String value : values) {
            String safe = value(value);
            if (!safe.isEmpty() && !clean.contains(safe)) clean.add(safe);
        }
        if (clean.isEmpty()) return "";
        return clean.size() == 1 ? clean.get(0) : clean.get(0) + "–" + clean.get(clean.size() - 1);
    }

    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) continue;
            if (result.length() > 0) result.append(" · ");
            result.append(value.trim());
        }
        return result.toString();
    }

    private static String value(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
