package com.example.proyecto_iot.superadmin;

public class SuperadminResumenLogItem {
    private final String label;
    private final String message;
    private final int accentColor;
    private final int labelColor;

    public SuperadminResumenLogItem(String label, String message, int accentColor, int labelColor) {
        this.label = label;
        this.message = message;
        this.accentColor = accentColor;
        this.labelColor = labelColor;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getLabelColor() {
        return labelColor;
    }
}

