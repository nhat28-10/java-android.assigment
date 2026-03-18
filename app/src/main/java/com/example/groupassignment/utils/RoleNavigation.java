package com.example.groupassignment.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.groupassignment.annotator.AnnotatorOverviewActivity;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.manager.ManagerDashboardActivity;
import com.example.groupassignment.reviewer.ReviewerDashboardActivity;
import com.example.groupassignment.ui.admin.AdminDashboardActivity;

public final class RoleNavigation {

    private RoleNavigation() {
    }

    @NonNull
    public static Intent buildHomeIntent(@NonNull Context context, String role) {
        Intent intent;

        if (isRole(role, "admin")) {
            intent = new Intent(context, AdminDashboardActivity.class);
        } else if (isRole(role, "manager")) {
            intent = new Intent(context, ManagerDashboardActivity.class);
        } else if (isRole(role, "reviewer")) {
            intent = new Intent(context, ReviewerDashboardActivity.class);
        } else if (isRole(role, "annotator")) {
            intent = new Intent(context, AnnotatorOverviewActivity.class);
        } else {
            intent = new Intent(context, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static void redirectToHome(@NonNull Activity activity, String role) {
        activity.startActivity(buildHomeIntent(activity, role));
        activity.finish();
    }

    public static boolean isRole(String role, @NonNull String expectedRole) {
        return expectedRole.equalsIgnoreCase(normalizeRole(role));
    }

    @NonNull
    public static String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }
}
