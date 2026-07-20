package com.example.proyecto_iot.usuario;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Pure search helpers so project suggestions behave consistently and can be unit tested. */
public final class ProjectSearchPolicy {
    private ProjectSearchPolicy() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}+", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean matches(String searchableText, String query) {
        String normalizedQuery = normalize(query);
        return !normalizedQuery.isEmpty() && normalize(searchableText).contains(normalizedQuery);
    }

    public static <T> List<T> filter(
            List<T> source,
            String query,
            int limit,
            Function<T, String> searchableText
    ) {
        List<T> matches = new ArrayList<>();
        if (source == null || limit <= 0 || searchableText == null) return matches;
        for (T item : source) {
            if (matches(searchableText.apply(item), query)) {
                matches.add(item);
                if (matches.size() == limit) break;
            }
        }
        return matches;
    }
}
