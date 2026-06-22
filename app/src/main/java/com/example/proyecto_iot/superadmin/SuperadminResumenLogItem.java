package com.example.proyecto_iot.superadmin;

public class SuperadminResumenLogItem {
    private final String label;
    private final String message;
    private final int accentColor;
    private final int labelColor;
    private final String dateIso;

    public SuperadminResumenLogItem(String label, String message, int accentColor, int labelColor,
                                    String dateIso) {
        this.label = label;
        this.message = message;
        this.accentColor = accentColor;
        this.labelColor = labelColor;
        this.dateIso = dateIso;
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

    public String getDateIso() {
        return dateIso;
    }
}

