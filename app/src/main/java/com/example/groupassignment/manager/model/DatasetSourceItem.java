package com.example.groupassignment.manager.model;

import java.io.Serializable;

public class DatasetSourceItem implements Serializable {
    private int id;
    private int datasetId;
    private int projectId;
    private String itemName;
    private String itemPathOrContent;
    private String mimeType;
    private String itemType;
    private String status;
    private long sizeBytes = -1L;
    private String createdAt;
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDatasetId() { return datasetId; }
    public void setDatasetId(int datasetId) { this.datasetId = datasetId; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemPathOrContent() { return itemPathOrContent; }
    public void setItemPathOrContent(String itemPathOrContent) { this.itemPathOrContent = itemPathOrContent; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
