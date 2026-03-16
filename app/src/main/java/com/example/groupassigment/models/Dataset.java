package com.example.groupassigment.models;

public class Dataset {
    private long id;
    private long projectId;
    private String name;
    private String description;
    private String dataPath;
    private int totalItems;
    private int labeledItems;
    private String createdAt;
    private String updatedAt;

    public Dataset() {}

    public Dataset(long id, long projectId, String name, String description) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDataPath() { return dataPath; }
    public void setDataPath(String dataPath) { this.dataPath = dataPath; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getLabeledItems() { return labeledItems; }
    public void setLabeledItems(int labeledItems) { this.labeledItems = labeledItems; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public double getProgress() {
        if (totalItems == 0) return 0;
        return (double) labeledItems / totalItems * 100;
    }
}
