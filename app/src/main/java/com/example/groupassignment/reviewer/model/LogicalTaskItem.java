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
    private int datasetItemId;
    private String datasetItemName;
    private String sourceUri;
    private String sourceDisplayName;
    private String sourceMimeType;
    private int annotatorId;
    private String annotatorName;
    private String type;
    private String status;
    private String finalStatus;
    private String assignedAt;
    private String startedAt;
    private String submittedAt;
    private String reviewedAt;
    private String annotationResult;
    private String annotationLabelsRaw;
    private String annotationPayload;
    private String guidelines;
    private String projectLabelsRaw;
    private String deadline;
    private int reviewerCount;
    private int approveCount;
    private int rejectCount;
    private int pendingVotes;
    private boolean deadlinePassed;
    private boolean finalDecisionReached;
    private String autoRejectedAt;
    private String updatedAt;
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
    public int getDatasetItemId() { return datasetItemId; }
    public void setDatasetItemId(int datasetItemId) { this.datasetItemId = datasetItemId; }
    public String getDatasetItemName() { return datasetItemName; }
    public void setDatasetItemName(String datasetItemName) { this.datasetItemName = datasetItemName; }
    public String getSourceUri() { return sourceUri; }
    public void setSourceUri(String sourceUri) { this.sourceUri = sourceUri; }
    public String getSourceDisplayName() { return sourceDisplayName; }
    public void setSourceDisplayName(String sourceDisplayName) { this.sourceDisplayName = sourceDisplayName; }
    public String getSourceMimeType() { return sourceMimeType; }
    public void setSourceMimeType(String sourceMimeType) { this.sourceMimeType = sourceMimeType; }
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
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getAnnotationResult() { return annotationResult; }
    public void setAnnotationResult(String annotationResult) { this.annotationResult = annotationResult; }
    public String getAnnotationLabelsRaw() { return annotationLabelsRaw; }
    public void setAnnotationLabelsRaw(String annotationLabelsRaw) { this.annotationLabelsRaw = annotationLabelsRaw; }
    public String getAnnotationPayload() { return annotationPayload; }
    public void setAnnotationPayload(String annotationPayload) { this.annotationPayload = annotationPayload; }
    public String getGuidelines() { return guidelines; }
    public void setGuidelines(String guidelines) { this.guidelines = guidelines; }
    public String getProjectLabelsRaw() { return projectLabelsRaw; }
    public void setProjectLabelsRaw(String projectLabelsRaw) { this.projectLabelsRaw = projectLabelsRaw; }
    public String getLabelsRaw() { return projectLabelsRaw; }
    public void setLabelsRaw(String labelsRaw) { this.projectLabelsRaw = labelsRaw; }
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
    public String getAutoRejectedAt() { return autoRejectedAt; }
    public void setAutoRejectedAt(String autoRejectedAt) { this.autoRejectedAt = autoRejectedAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<ReviewerVoteItem> getReviewerVotes() { return reviewerVotes; }

    public String getDisplayStatus() {
        if (finalStatus != null && !finalStatus.trim().isEmpty() && !finalStatus.equalsIgnoreCase(status)) {
            return finalStatus;
        }
        return status;
    }

    public String getDisplaySourceName() {
        if (sourceDisplayName != null && !sourceDisplayName.trim().isEmpty()) {
            return sourceDisplayName;
        }
        if (datasetItemName != null && !datasetItemName.trim().isEmpty()) {
            return datasetItemName;
        }
        return datasetName;
    }
}
