package com.example.proyecto_iot.usuario;

public class UsuarioChatListItem {

    private final String name;
    private final String message;
    private final String time;
    private final int avatarResId;
    private final String initials;
    private final boolean usesInitials;
    private final boolean unread;
    private final boolean favorite;
    private final String conversationId;
    private final String asesorUid;
    private final long lastMessageAt;

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
        this(name, message, time, avatarResId, initials, usesInitials, unread, favorite, "", "", 0L);
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
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarResId = avatarResId;
        this.initials = initials;
        this.usesInitials = usesInitials;
        this.unread = unread;
        this.favorite = favorite;
        this.conversationId = conversationId;
        this.asesorUid = asesorUid;
        this.lastMessageAt = lastMessageAt;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

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
}
