package com.example.groupassignment.manager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetItem implements Serializable {

    private int id;
    private String name;
    private String description;
    private String type;
    private int totalItems;
    private int approvedItems;
    private int submittedItems;
    private int pendingAnnotationItems;
    private int rejectedItems;
    private String createdAt;
    private int projectId;
    private String imageUris; // Stores pipe-separated URIs

    public DatasetItem() {
        this.imageUris = "";
    }

    public DatasetItem(int id,
                       String name,
                       String description,
                       String type,
                       int totalItems,
                       int approvedItems,
                       int submittedItems,
                       int pendingAnnotationItems,
                       int rejectedItems,
                       String createdAt) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.totalItems = totalItems;
        this.approvedItems = approvedItems;
        this.submittedItems = submittedItems;
        this.pendingAnnotationItems = pendingAnnotationItems;
        this.rejectedItems = rejectedItems;
        this.createdAt = createdAt;
        this.projectId = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getApprovedItems() {
        return approvedItems;
    }

    public void setApprovedItems(int approvedItems) {
        this.approvedItems = approvedItems;
    }

    public int getSubmittedItems() {
        return submittedItems;
    }

    public void setSubmittedItems(int submittedItems) {
        this.submittedItems = submittedItems;
    }

    public int getPendingAnnotationItems() {
        return pendingAnnotationItems;
    }

    public void setPendingAnnotationItems(int pendingAnnotationItems) {
        this.pendingAnnotationItems = pendingAnnotationItems;
    }

    public int getRejectedItems() {
        return rejectedItems;
    }

    public void setRejectedItems(int rejectedItems) {
        this.rejectedItems = rejectedItems;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getImageUris() {
        return imageUris == null ? "" : imageUris;
    }

    public void setImageUris(String imageUris) {
        this.imageUris = imageUris;
    }

    public List<String> getImageUriList() {
        if (imageUris == null || imageUris.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(imageUris.split("\\|")));
    }

    public void setImageUriList(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            this.imageUris = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uris.size(); i++) {
            sb.append(uris.get(i));
            if (i < uris.size() - 1) sb.append("|");
        }
        this.imageUris = sb.toString();
    }

    public boolean isAssignedToProject() {
        return projectId > 0;
    }

    public int getProgressPercent() {
        if (totalItems <= 0) return 0;
        return Math.min(100, Math.round((approvedItems * 100f) / totalItems));
    }

    public boolean isReadyForAi() {
        return totalItems > 0 && approvedItems >= totalItems;
    }

    public String getStatusCode() {
        if (totalItems <= 0 || approvedItems == 0) {
            if (pendingAnnotationItems > 0 || submittedItems > 0) {
                return "annotating";
            }
            return "not_started";
        }

        int progress = getProgressPercent();
        if (progress >= 100) {
            return "ready";
        }

        if (submittedItems > 0) {
            return "under_review";
        }

        return "annotating";
    }

    public String getStatusLabel() {
        switch (getStatusCode()) {
            case "ready":
                return "Ready for AI Training";
            case "under_review":
                return "Under Review";
            case "annotating":
                return "Annotating";
            case "not_started":
            default:
                return "Not Started";
        }
    }
}
