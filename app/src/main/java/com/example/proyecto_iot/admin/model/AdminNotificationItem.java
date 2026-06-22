package com.example.proyecto_iot.admin.model;

public class AdminNotificationItem {
    public enum Section { TODAY, YESTERDAY }
    public enum Type { PAYMENT, SEPARATION, DELIVERY, ACTION }

    private final String id;
    private final Section section;
    private final Type type;
    private final String title;
    private final String badge;
    private final String line1;
    private final String line2;
    private final String actionText;
    private final String separationId;
    private final String projectId;
    private final String notificationKind;
    private final long createdAt;

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
        this(id, section, type, title, badge, line1, line2, actionText, "", "", "", 0L);
    }

    public AdminNotificationItem(
            String id,
            Section section,
            Type type,
            String title,
            String badge,
            String line1,
            String line2,
            String actionText,
            String separationId,
            String projectId,
            String notificationKind,
            long createdAt
    ) {
        this.id = id;
        this.section = section;
        this.type = type;
        this.title = title;
        this.badge = badge;
        this.line1 = line1;
        this.line2 = line2;
        this.actionText = actionText;
        this.separationId = separationId == null ? "" : separationId;
        this.projectId = projectId == null ? "" : projectId;
        this.notificationKind = notificationKind == null ? "" : notificationKind;
        this.createdAt = createdAt;
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

    public String getSeparationId() {
        return separationId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getNotificationKind() {
        return notificationKind;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
