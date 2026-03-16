package com.example.groupassigment.models;

/**
 * User roles in the system
 */
public enum UserRole {
    ADMIN("Admin"),
    MANAGER("Manager"),
    ANNOTATOR("Annotator"),
    REVIEWER("Reviewer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
