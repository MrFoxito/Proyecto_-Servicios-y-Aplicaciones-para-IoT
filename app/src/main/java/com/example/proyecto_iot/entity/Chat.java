package com.example.proyecto_iot.entity;

public class Chat {
    private String id;

    private String clienteId;
    private String asesorId;
    private long lastMessageTime;
    private String lastMessagePreview;

    private String userName;
    private String lastMessage;
    private String time;
    private int profileImageRes;
    private String initials; // For when there is no image
    private boolean unread;

    public Chat(String id, String userName, String lastMessage, String time, int profileImageRes, String initials, boolean unread) {
        this.id = id;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.profileImageRes = profileImageRes;
        this.initials = initials;
        this.unread = unread;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getProfileImageRes() { return profileImageRes; }
    public void setProfileImageRes(int profileImageRes) { this.profileImageRes = profileImageRes; }

    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }

    public boolean isUnread() { return unread; }
    public void setUnread(boolean unread) { this.unread = unread; }
}
