package com.example.proyecto_iot.superadmin;

public class SuperadminResumenLogItem {
    private final String label;
    private final String message;
    private final int accentColor;
    private final int labelColor;
    private final String dateIso;
    private final String time;

    public SuperadminResumenLogItem(String label, String message, int accentColor, int labelColor,
                                    String dateIso, String time) {
        this.label = label;
        this.message = message;
        this.accentColor = accentColor;
        this.labelColor = labelColor;
        this.dateIso = dateIso;
        this.time = time;
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

    public String getTime() {
        return time;
    }
}

