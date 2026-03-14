package com.example.groupassignment.manager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProjectItem implements Serializable {

    private int id;

    private String name;
    private String description;
    private String status;
    private String reviewStatus;
    private int reviewerCount;
    private int annotatorCount;
    private String lastUpdated;

    private String guidelines;
    private String reviewMode;
    private double sampleRate;
    private String deadline;
    private String exportFormat;

    private List<String> labels;
    private List<String> datasets;
    private List<String> annotators;
    private List<String> reviewers;

    public ProjectItem() {
        labels = new ArrayList<>();
        datasets = new ArrayList<>();
        annotators = new ArrayList<>();
        reviewers = new ArrayList<>();
    }

    public ProjectItem(String name, String description, String status, String reviewStatus,
                       int reviewerCount, int annotatorCount, String lastUpdated) {
        this();
        this.name = name;
        this.description = description;
        this.status = status;
        this.reviewStatus = reviewStatus;
        this.reviewerCount = reviewerCount;
        this.annotatorCount = annotatorCount;
        this.lastUpdated = lastUpdated;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public int getReviewerCount() {
        return reviewerCount;
    }

    public void setReviewerCount(int reviewerCount) {
        this.reviewerCount = reviewerCount;
    }

    public int getAnnotatorCount() {
        return annotatorCount;
    }

    public void setAnnotatorCount(int annotatorCount) {
        this.annotatorCount = annotatorCount;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }

    public String getReviewMode() {
        return reviewMode;
    }

    public void setReviewMode(String reviewMode) {
        this.reviewMode = reviewMode;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    public List<String> getAnnotators() {
        return annotators;
    }

    public void setAnnotators(List<String> annotators) {
        this.annotators = annotators;
    }

    public List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
    }
}