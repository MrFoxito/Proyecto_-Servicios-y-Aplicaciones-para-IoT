package com.example.proyecto_iot.admin.model;

public class AdminAssignmentRecord {
    private final String id;
    private final String projectTitle;
    private final String projectLocation;
    private final String projectNeighborhood;
    private final String projectStatus;
    private final String advisorName;
    private final String assignedAt;

    public AdminAssignmentRecord(
            String id,
            String projectTitle,
            String projectLocation,
            String projectNeighborhood,
            String projectStatus,
            String advisorName,
            String assignedAt
    ) {
        this.id = id;
        this.projectTitle = projectTitle;
        this.projectLocation = projectLocation;
        this.projectNeighborhood = projectNeighborhood;
        this.projectStatus = projectStatus;
        this.advisorName = advisorName;
        this.assignedAt = assignedAt;
    }

    public String getId() {
        return id;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getProjectLocation() {
        return projectLocation;
    }

    public String getProjectNeighborhood() {
        return projectNeighborhood;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getAssignedAt() {
        return assignedAt;
    }
}
