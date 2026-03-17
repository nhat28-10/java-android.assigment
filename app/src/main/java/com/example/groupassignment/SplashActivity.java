package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.annotator.AnnotatorOverviewActivity;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.manager.ManagerDashboardActivity;
import com.example.groupassignment.reviewer.ReviewerDashboardActivity;
import com.example.groupassignment.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String role = sessionManager.getRole();

        if ("manager".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, ManagerDashboardActivity.class));
        } else if ("annotator".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, AnnotatorOverviewActivity.class));
        } else if ("reviewer".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, ReviewerDashboardActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}