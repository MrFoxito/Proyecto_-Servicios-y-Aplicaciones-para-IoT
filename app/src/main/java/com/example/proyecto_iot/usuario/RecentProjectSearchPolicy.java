package com.example.proyecto_iot.usuario;

import java.util.ArrayList;
import java.util.List;

/** Pure rules for the small, private history shown on the search screen. */
public final class RecentProjectSearchPolicy {
    public static final int MAX_RECENT_SEARCHES = 5;

    private RecentProjectSearchPolicy() { }

    public static List<String> add(List<String> existing, String query) {
        List<String> result = new ArrayList<>();
        String cleanQuery = clean(query);
        if (cleanQuery.isEmpty()) return copy(existing);
        result.add(cleanQuery);
        if (existing != null) {
            String normalizedQuery = ProjectSearchPolicy.normalize(cleanQuery);
            for (String value : existing) {
                String cleanValue = clean(value);
                if (cleanValue.isEmpty()
                        || normalizedQuery.equals(ProjectSearchPolicy.normalize(cleanValue))
                        || result.size() >= MAX_RECENT_SEARCHES) {
                    continue;
                }
                result.add(cleanValue);
            }
        }
        return result;
    }

    public static String clean(String query) {
        if (query == null) return "";
        String value = query.trim().replaceAll("\\s+", " ");
        return value.length() > 80 ? value.substring(0, 80) : value;
    }

    private static List<String> copy(List<String> source) {
        List<String> copy = new ArrayList<>();
        if (source == null) return copy;
        for (String value : source) {
            String clean = clean(value);
            if (!clean.isEmpty() && copy.size() < MAX_RECENT_SEARCHES) copy.add(clean);
        }
        return copy;
    }
}
