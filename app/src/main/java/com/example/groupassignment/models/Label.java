package com.example.groupassignment.models;

public class Label {
    private long id;
    private long projectId;
    private String name;
    private String color;
    private String description;
    private String createdAt;

    public Label() {}

    public Label(long id, long projectId, String name, String color) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.color = color;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
