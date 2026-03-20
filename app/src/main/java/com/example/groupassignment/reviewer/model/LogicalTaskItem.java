package com.example.groupassignment.reviewer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogicalTaskItem implements Serializable {
    private int referenceTaskId;
    private String logicalTaskKey;
    private int roundNumber;
    private int projectId;
    private String projectName;
    private int datasetId;
    private String datasetName;
    private int annotatorId;
    private String annotatorName;
    private String type;
    private String status;
    private String finalStatus;
    private String submittedAt;
    private String reviewedAt;
    private String annotationResult;
    private String guidelines;
    private String labelsRaw;
    private String deadline;
    private int reviewerCount;
    private int approveCount;
    private int rejectCount;
    private int pendingVotes;
    private boolean deadlinePassed;
    private boolean finalDecisionReached;
    private final List<ReviewerVoteItem> reviewerVotes = new ArrayList<>();

    public int getReferenceTaskId() { return referenceTaskId; }
    public void setReferenceTaskId(int referenceTaskId) { this.referenceTaskId = referenceTaskId; }
    public String getLogicalTaskKey() { return logicalTaskKey; }
    public void setLogicalTaskKey(String logicalTaskKey) { this.logicalTaskKey = logicalTaskKey; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getAnnotationResult() { return annotationResult; }
    public void setAnnotationResult(String annotationResult) { this.annotationResult = annotationResult; }
    public String getGuidelines() { return guidelines; }
    public void setGuidelines(String guidelines) { this.guidelines = guidelines; }
    public String getLabelsRaw() { return labelsRaw; }
    public void setLabelsRaw(String labelsRaw) { this.labelsRaw = labelsRaw; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public int getReviewerCount() { return reviewerCount; }
    public void setReviewerCount(int reviewerCount) { this.reviewerCount = reviewerCount; }
    public int getApproveCount() { return approveCount; }
    public void setApproveCount(int approveCount) { this.approveCount = approveCount; }
    public int getRejectCount() { return rejectCount; }
    public void setRejectCount(int rejectCount) { this.rejectCount = rejectCount; }
    public int getPendingVotes() { return pendingVotes; }
    public void setPendingVotes(int pendingVotes) { this.pendingVotes = pendingVotes; }
    public boolean isDeadlinePassed() { return deadlinePassed; }
    public void setDeadlinePassed(boolean deadlinePassed) { this.deadlinePassed = deadlinePassed; }
    public boolean isFinalDecisionReached() { return finalDecisionReached; }
    public void setFinalDecisionReached(boolean finalDecisionReached) { this.finalDecisionReached = finalDecisionReached; }
    public List<ReviewerVoteItem> getReviewerVotes() { return reviewerVotes; }

    public String getDisplayStatus() {
        if (finalStatus != null && !finalStatus.trim().isEmpty() && !finalStatus.equalsIgnoreCase(status)) {
            return finalStatus;
        }
        return status;
    }
}
