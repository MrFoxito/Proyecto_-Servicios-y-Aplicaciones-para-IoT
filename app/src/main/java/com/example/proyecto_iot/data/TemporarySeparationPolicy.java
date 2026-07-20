package com.example.proyecto_iot.data;

import java.util.Locale;

/** Shared presentation and compatibility policy for the temporary unit hold lifecycle. */
public final class TemporarySeparationPolicy {
    public static final String PENDING_PAYMENT = "SEPARACION_PENDIENTE_PAGO";
    public static final String PAYMENT_VERIFICATION = "PAGO_EN_VERIFICACION";
    public static final String CONFIRMED = "SEPARACION_CONFIRMADA";
    public static final String EXPIRED = "SEPARACION_VENCIDA";
    public static final String CANCELLED = "SEPARACION_CANCELADA";

    private TemporarySeparationPolicy() { }

    public static boolean isBlockingLock(String lockStatus, long expiresAtMillis, long nowMillis) {
        String status = safe(lockStatus);
        if (PAYMENT_VERIFICATION.equals(status) || CONFIRMED.equals(status)) return true;
        return "RETENIDA_TEMPORALMENTE".equals(status) && expiresAtMillis > nowMillis;
    }

    public static String availabilityLabel(String lockStatus) {
        String status = safe(lockStatus);
        if (PAYMENT_VERIFICATION.equals(status)) return "Pago en verificación";
        if (CONFIRMED.equals(status)) return "Separación confirmada";
        return "Temporalmente separada";
    }

    public static String normalizeOperationalStatus(String operationalStatus, String legacyStatus) {
        String status = safe(operationalStatus);
        if (!status.isEmpty()) return status;
        String legacy = safe(legacyStatus).toUpperCase(Locale.ROOT);
        if ("APROBADA".equals(legacy) || "CONFIRMADA".equals(legacy)) return CONFIRMED;
        if ("RECHAZADA".equals(legacy) || "CANCELADA".equals(legacy)) return CANCELLED;
        if ("PAGADA".equals(legacy) || "PENDIENTE".equals(legacy)) return PENDING_PAYMENT;
        return PENDING_PAYMENT;
    }

    public static String displayLabel(String status) {
        switch (safe(status)) {
            case PENDING_PAYMENT:
                return "Unidad retenida temporalmente";
            case PAYMENT_VERIFICATION:
                return "Pago en verificación";
            case CONFIRMED:
                return "Separación confirmada";
            case EXPIRED:
                return "Separación vencida";
            case CANCELLED:
                return "Separación cancelada";
            default:
                return "Disponible";
        }
    }

    public static boolean canCancel(String status) {
        String normalized = safe(status);
        return PENDING_PAYMENT.equals(normalized) || PAYMENT_VERIFICATION.equals(normalized);
    }

    public static boolean canSubmitExternalPayment(String status) {
        return PENDING_PAYMENT.equals(safe(status));
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
