package com.example.groupassignment.reviewer.model;

import java.io.Serializable;

public class TaskAnnotationItem implements Serializable {
    private int id;
    private int taskId;
    private int projectId;
    private int datasetId;
    private int datasetItemId;
    private int annotatorId;
    private String annotatorName;
    private String datasetName;
    private String datasetItemName;
    private String labelName;
    private String labelValueOrPayload;
    private int roundNumber;
    private String createdAt;
    private String taskStatus;
    private String finalStatus;
    private String reviewerSummary;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public int getDatasetId() { return datasetId; }
    public void setDatasetId(int datasetId) { this.datasetId = datasetId; }
    public int getDatasetItemId() { return datasetItemId; }
    public void setDatasetItemId(int datasetItemId) { this.datasetItemId = datasetItemId; }
    public int getAnnotatorId() { return annotatorId; }
    public void setAnnotatorId(int annotatorId) { this.annotatorId = annotatorId; }
    public String getAnnotatorName() { return annotatorName; }
    public void setAnnotatorName(String annotatorName) { this.annotatorName = annotatorName; }
    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public String getDatasetItemName() { return datasetItemName; }
    public void setDatasetItemName(String datasetItemName) { this.datasetItemName = datasetItemName; }
    public String getLabelName() { return labelName; }
    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelValueOrPayload() { return labelValueOrPayload; }
    public void setLabelValueOrPayload(String labelValueOrPayload) { this.labelValueOrPayload = labelValueOrPayload; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
    public String getReviewerSummary() { return reviewerSummary; }
    public void setReviewerSummary(String reviewerSummary) { this.reviewerSummary = reviewerSummary; }
}
