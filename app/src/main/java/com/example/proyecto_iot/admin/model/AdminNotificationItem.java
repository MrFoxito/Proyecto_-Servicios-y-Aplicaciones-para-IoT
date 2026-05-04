package com.example.proyecto_iot.admin.model;

public class AdminNotificationItem {
    public enum Section { TODAY, YESTERDAY }
    public enum Type { PAYMENT, SEPARATION, ACTION }

    private final String id;
    private final Section section;
    private final Type type;
    private final String title;
    private final String badge;
    private final String line1;
    private final String line2;
    private final String actionText;

    public AdminNotificationItem(
            String id,
            Section section,
            Type type,
            String title,
            String badge,
            String line1,
            String line2,
            String actionText
    ) {
        this.id = id;
        this.section = section;
        this.type = type;
        this.title = title;
        this.badge = badge;
        this.line1 = line1;
        this.line2 = line2;
        this.actionText = actionText;
    }

    public String getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBadge() {
        return badge;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getActionText() {
        return actionText;
    }
}
