package com.example.groupassigment.models;

public class Task {
    private long id;
    private long datasetId;
    private long projectId;
    private long assignedTo;
    private String status;
    private String labelingData;
    private String labels;
    private String reviewerComment;
    private String assignedAt;
    private String completedAt;
    private String reviewedAt;

    public Task() {}

    public Task(long id, long datasetId, long projectId, long assignedTo) {
        this.id = id;
        this.datasetId = datasetId;
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.status = "PENDING";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDatasetId() { return datasetId; }
    public void setDatasetId(long datasetId) { this.datasetId = datasetId; }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(long assignedTo) { this.assignedTo = assignedTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLabelingData() { return labelingData; }
    public void setLabelingData(String labelingData) { this.labelingData = labelingData; }

    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }

    public String getReviewerComment() { return reviewerComment; }
    public void setReviewerComment(String reviewerComment) { this.reviewerComment = reviewerComment; }

    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isInProgress() { return "IN_PROGRESS".equals(status); }
    public boolean isSubmitted() { return "SUBMITTED".equals(status); }
    public boolean isApproved() { return "APPROVED".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }
}
