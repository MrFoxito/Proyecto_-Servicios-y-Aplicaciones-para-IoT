package com.example.proyecto_iot.usuario;

public class UsuarioNotificationItem {

    public static final int TYPE_APPROVAL = 1;
    public static final int TYPE_VISIT = 2;
    public static final int ACTION_PAYMENT = 1;
    public static final int ACTION_APPOINTMENT = 2;

    private final int type;
    private final String time;
    private final String title;
    private final String body;
    private final String ctaLabel;
    private final int actionType;

    public UsuarioNotificationItem(int type, String time, String title, String body, String ctaLabel, int actionType) {
        this.type = type;
        this.time = time;
        this.title = title;
        this.body = body;
        this.ctaLabel = ctaLabel;
        this.actionType = actionType;
    }

    public int getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getCtaLabel() {
        return ctaLabel;
    }

    public int getActionType() {
        return actionType;
    }
}
