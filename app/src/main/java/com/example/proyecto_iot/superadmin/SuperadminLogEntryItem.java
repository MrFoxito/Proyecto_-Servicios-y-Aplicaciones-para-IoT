package com.example.proyecto_iot.superadmin;

public class SuperadminLogEntryItem {
    private final int accentColor;
    private final int iconResId;
    private final int iconTint;
    private final String title;
    private final String subtitle;
    private final String time;
    private final String detail;
    private final String status;
    private final int statusColor;

    public SuperadminLogEntryItem(int accentColor, int iconResId, int iconTint, String title, String subtitle,
                                  String time, String detail, String status, int statusColor) {
        this.accentColor = accentColor;
        this.iconResId = iconResId;
        this.iconTint = iconTint;
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
        this.detail = detail;
        this.status = status;
        this.statusColor = statusColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getIconTint() {
        return iconTint;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTime() {
        return time;
    }

    public String getDetail() {
        return detail;
    }

    public String getStatus() {
        return status;
    }

    public int getStatusColor() {
        return statusColor;
    }
}

