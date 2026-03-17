package com.example.groupassignment.models;

public class User {
    private long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;

    public User() {}

    public User(long id, String username, String email, String fullName, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isAdmin() { return role == UserRole.ADMIN; }
    public boolean isManager() { return role == UserRole.MANAGER; }
    public boolean isAnnotator() { return role == UserRole.ANNOTATOR; }
    public boolean isReviewer() { return role == UserRole.REVIEWER; }
}
