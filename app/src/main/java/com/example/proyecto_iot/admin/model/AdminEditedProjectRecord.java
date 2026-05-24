package com.example.proyecto_iot.admin.model;

public class AdminEditedProjectRecord {
    private final String projectName;
    private final String status;
    private final String editedAt;

    public AdminEditedProjectRecord(String projectName, String status, String editedAt) {
        this.projectName = projectName;
        this.status = status;
        this.editedAt = editedAt;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getStatus() {
        return status;
    }

    public String getEditedAt() {
        return editedAt;
    }
}
