package com.example.proyecto_iot.usuario;

public class UsuarioHistoryItem {

    private final String badge;
    private final String title;
    private final String date;
    private final String summary;
    private final String status;
    private final String code;
    private final String amount;

    public UsuarioHistoryItem(
            String badge,
            String title,
            String date,
            String summary,
            String status,
            String code,
            String amount
    ) {
        this.badge = badge;
        this.title = title;
        this.date = date;
        this.summary = summary;
        this.status = status;
        this.code = code;
        this.amount = amount;
    }

    public String getBadge() {
        return badge;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getSummary() {
        return summary;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getAmount() {
        return amount;
    }
}
