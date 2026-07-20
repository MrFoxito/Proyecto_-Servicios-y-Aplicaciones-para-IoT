package com.example.proyecto_iot.usuario;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Local-only, account-scoped search history. It never writes to Firebase. */
public final class RecentProjectSearchStore {
    private static final String PREFS_NAME = "usuario_project_search_history";
    private static final String KEY_PREFIX = "queries_";
    private final SharedPreferences preferences;
    private final String userKey;

    public RecentProjectSearchStore(@NonNull Context context, String uid) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userKey = KEY_PREFIX + (uid == null || uid.trim().isEmpty() ? "local" : uid.trim());
    }

    public List<String> get() {
        List<String> result = new ArrayList<>();
        String raw = preferences.getString(userKey, "[]");
        try {
            JSONArray array = new JSONArray(raw == null ? "[]" : raw);
            for (int index = 0; index < array.length(); index++) {
                result.add(array.optString(index, ""));
            }
        } catch (Exception ignored) {
            // A corrupt local value is treated as an empty private history.
        }
        return RecentProjectSearchPolicy.add(result, "");
    }

    public List<String> record(String query) {
        List<String> updated = RecentProjectSearchPolicy.add(get(), query);
        JSONArray array = new JSONArray();
        for (String value : updated) array.put(value);
        preferences.edit().putString(userKey, array.toString()).apply();
        return updated;
    }
}
