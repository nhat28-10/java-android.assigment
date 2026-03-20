package com.example.groupassignment.reviewer.model;

public class TaskItem {
    private int id;
    private int projectId;
    private String projectName;
    private int datasetId;
    private String datasetName;
    private int annotatorId;
    private String annotatorName;
    private int reviewerId;
    private String reviewerName;
    private String type;
    private String status;
    private String submittedAt;
    private String reviewedAt;
    private String annotationResult;
    private String reviewComments;
    private String rejectionReason;
    private String logicalTaskKey;
    private int roundNumber;
    private String projectDeadline;
    private String projectGuidelines;
    private String projectLabels;
    private String voteDecision;
    private String finalStatus;
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public int getDatasetId() { return datasetId; }
    public void setDatasetId(int datasetId) { this.datasetId = datasetId; }
    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public int getAnnotatorId() { return annotatorId; }
    public void setAnnotatorId(int annotatorId) { this.annotatorId = annotatorId; }
    public String getAnnotatorName() { return annotatorName; }
    public void setAnnotatorName(String annotatorName) { this.annotatorName = annotatorName; }
    public int getReviewerId() { return reviewerId; }
    public void setReviewerId(int reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getAnnotationResult() { return annotationResult; }
    public void setAnnotationResult(String annotationResult) { this.annotationResult = annotationResult; }
    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getLogicalTaskKey() { return logicalTaskKey; }
    public void setLogicalTaskKey(String logicalTaskKey) { this.logicalTaskKey = logicalTaskKey; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public String getProjectDeadline() { return projectDeadline; }
    public void setProjectDeadline(String projectDeadline) { this.projectDeadline = projectDeadline; }
    public String getProjectGuidelines() { return projectGuidelines; }
    public void setProjectGuidelines(String projectGuidelines) { this.projectGuidelines = projectGuidelines; }
    public String getProjectLabels() { return projectLabels; }
    public void setProjectLabels(String projectLabels) { this.projectLabels = projectLabels; }
    public String getVoteDecision() { return voteDecision; }
    public void setVoteDecision(String voteDecision) { this.voteDecision = voteDecision; }
    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isReviewed() {
        return voteDecision != null && !voteDecision.trim().isEmpty();
    }
}
