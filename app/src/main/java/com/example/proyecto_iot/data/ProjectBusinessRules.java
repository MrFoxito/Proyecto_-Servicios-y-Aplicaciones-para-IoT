package com.example.proyecto_iot.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class ProjectBusinessRules {
    public static final String STATUS_PLANOS = "en_planos";
    public static final String STATUS_PREVENTA = "preventa";
    public static final String STATUS_VENTA = "venta";

    private ProjectBusinessRules() {
    }

    public static String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        if (value.contains("preventa")) {
            return STATUS_PREVENTA;
        }
        if (value.contains("venta") && !value.contains("preventa")) {
            return STATUS_VENTA;
        }
        return STATUS_PLANOS;
    }

    public static String displayStatus(String status) {
        String normalized = normalizeStatus(status);
        if (STATUS_PREVENTA.equals(normalized)) {
            return "EN PREVENTA";
        }
        if (STATUS_VENTA.equals(normalized)) {
            return "EN VENTA";
        }
        return "EN PLANOS";
    }

    public static boolean canScheduleAppointment(String status) {
        return STATUS_PLANOS.equals(normalizeStatus(status))
                || STATUS_PREVENTA.equals(normalizeStatus(status))
                || STATUS_VENTA.equals(normalizeStatus(status));
    }

    public static boolean canCreateSeparation(String status) {
        String normalized = normalizeStatus(status);
        return STATUS_PREVENTA.equals(normalized) || STATUS_VENTA.equals(normalized);
    }

    public static String qrValue(String projectId) {
        return "app://proyecto/" + safeProjectId(projectId);
    }

    public static String safeProjectId(String projectId) {
        return projectId == null ? "" : projectId.trim();
    }

    public static String deliveryIsoFromDisplay(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return output.format(input.parse(displayDate.trim()));
        } catch (Exception ignored) {
            return "";
        }
    }

    public static long deliveryMillisFromDisplay(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            return 0L;
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return input.parse(displayDate.trim()).getTime();
        } catch (ParseException ignored) {
            return 0L;
        }
    }

    public static boolean deliveryDateReached(long deliveryMillis, String status) {
        return deliveryMillis > 0L
                && deliveryMillis <= System.currentTimeMillis()
                && !STATUS_VENTA.equals(normalizeStatus(status));
    }
}
