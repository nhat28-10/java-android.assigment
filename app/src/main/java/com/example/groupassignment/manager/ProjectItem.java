package com.example.groupassignment.manager;

public class ProjectItem {
    private final String name;
    private final String description;
    private final String status;
    private final String reviewer;
    private final String annotator;
    private final String updatedAt;

    public ProjectItem(String name, String description, String status,
                       String reviewer, String annotator, String updatedAt) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.reviewer = reviewer;
        this.annotator = annotator;
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getAnnotator() {
        return annotator;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}