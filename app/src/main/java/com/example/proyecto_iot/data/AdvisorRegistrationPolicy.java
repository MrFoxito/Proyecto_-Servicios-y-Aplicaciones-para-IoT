package com.example.proyecto_iot.data;

import java.util.Locale;

/** Shared status rules for advisor applications and legacy advisor profiles. */
public final class AdvisorRegistrationPolicy {
    public static final String ROLE_ASESOR = "asesor";
    public static final String STATUS_ACTIVE = "activo";
    public static final String STATUS_PENDING = "pendiente";

    private AdvisorRegistrationPolicy() {
    }

    public static boolean isAdvisor(String role) {
        return ROLE_ASESOR.equals(normalize(role));
    }

    public static String initialStatus(String role) {
        return isAdvisor(role) ? STATUS_PENDING : STATUS_ACTIVE;
    }

    /** Existing advisor profiles without a status remain active for backward compatibility. */
    public static boolean isActiveAdvisor(String role, String status) {
        return isAdvisor(role) && (normalize(status).isEmpty() || STATUS_ACTIVE.equals(normalize(status)));
    }

    public static boolean blocksAdvisorAccess(String role, String status) {
        return isAdvisor(role) && !isActiveAdvisor(role, status);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
