package com.example.groupassigment.models;

public class Project {
    private long id;
    private String name;
    private String description;
    private long managerId;
    private String status;
    private String createdAt;
    private String updatedAt;
    private int totalTasks;
    private int completedTasks;

    public Project() {}

    public Project(long id, String name, String description, long managerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.status = "ACTIVE";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getManagerId() { return managerId; }
    public void setManagerId(long managerId) { this.managerId = managerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public double getProgress() {
        if (totalTasks == 0) return 0;
        return (double) completedTasks / totalTasks * 100;
    }
}
