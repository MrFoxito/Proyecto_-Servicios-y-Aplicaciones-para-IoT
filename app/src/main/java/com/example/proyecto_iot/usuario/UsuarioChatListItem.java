package com.example.proyecto_iot.usuario;

public class UsuarioChatListItem {

    private final String name;
    private final String message;
    private final String previewText;
    private final String time;
    private final int avatarResId;
    private final String initials;
    private final boolean usesInitials;
    private final boolean unread;
    private final boolean favorite;
    private final String conversationId;
    private final String asesorUid;
    private final long lastMessageAt;
    private final String projectId;
    private final String projectName;
    private final String projectLocation;
    private final String projectPrice;
    private final String projectImageUrl;

    public UsuarioChatListItem(
            String name,
            String message,
            String time,
            int avatarResId,
            String initials,
            boolean usesInitials,
            boolean unread,
            boolean favorite
    ) {
        this(name, message, "", time, avatarResId, initials, usesInitials, unread, favorite,
                "", "", 0L, "", "", "", "", "");
    }

    public UsuarioChatListItem(
            String name,
            String message,
            String time,
            int avatarResId,
            String initials,
            boolean usesInitials,
            boolean unread,
            boolean favorite,
            String conversationId,
            String asesorUid,
            long lastMessageAt
    ) {
        this(name, message, "", time, avatarResId, initials, usesInitials, unread, favorite,
                conversationId, asesorUid, lastMessageAt, "", "", "", "", "");
    }

    public UsuarioChatListItem(
            String name, String message, String previewText, String time, int avatarResId, String initials,
            boolean usesInitials, boolean unread, boolean favorite, String conversationId,
            String asesorUid, long lastMessageAt, String projectId, String projectName,
            String projectLocation, String projectPrice, String projectImageUrl
    ) {
        this.name = name;
        this.message = message;
        this.previewText = previewText;
        this.time = time;
        this.avatarResId = avatarResId;
        this.initials = initials;
        this.usesInitials = usesInitials;
        this.unread = unread;
        this.favorite = favorite;
        this.conversationId = conversationId;
        this.asesorUid = asesorUid;
        this.lastMessageAt = lastMessageAt;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectLocation = projectLocation;
        this.projectPrice = projectPrice;
        this.projectImageUrl = projectImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getSecondaryText() { return message; }

    public String getPreviewText() { return previewText; }

    public String getTime() {
        return time;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getInitials() {
        return initials;
    }

    public boolean usesInitials() {
        return usesInitials;
    }

    public boolean isUnread() {
        return unread;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getAsesorUid() {
        return asesorUid;
    }

    public long getLastMessageAt() {
        return lastMessageAt;
    }

    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getProjectLocation() { return projectLocation; }
    public String getProjectPrice() { return projectPrice; }
    public String getProjectImageUrl() { return projectImageUrl; }
}
