package com.example.proyecto_iot.data;

import java.net.URI;

/** Pure validation rule for remote user avatar URLs. */
public final class ProfileAvatarPolicy {
    private ProfileAvatarPolicy() { }

    public static boolean isValidRemoteUrl(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().contains(" ")) return false;
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            return ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }
}
