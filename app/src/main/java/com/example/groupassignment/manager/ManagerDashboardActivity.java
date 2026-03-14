package com.example.groupassignment.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class ManagerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeManager;
    private TextView tvActiveProjects, tvTotalTasks, tvPendingReview, tvApprovalRate;
    private TextView tvInProgressCount, tvPendingCount, tvApprovedCount, tvRejectedCount;
    private Button btnProjects, btnDatasets, btnLogout;

    private ProjectDbHelper projectDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        projectDbHelper = new ProjectDbHelper(this);

        initViews();
        bindDashboardData();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindDashboardData();
    }

    private void initViews() {
        tvWelcomeManager = findViewById(R.id.tvWelcomeManager);

        tvActiveProjects = findViewById(R.id.tvActiveProjects);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvPendingReview = findViewById(R.id.tvPendingReview);
        tvApprovalRate = findViewById(R.id.tvApprovalRate);

        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvRejectedCount = findViewById(R.id.tvRejectedCount);

        btnProjects = findViewById(R.id.btnProjects);
        btnDatasets = findViewById(R.id.btnDatasets);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void bindDashboardData() {
        String managerName = "Manager";
        tvWelcomeManager.setText("Welcome back, " + managerName);

        List<ProjectItem> projects = projectDbHelper.getAllProjects();

        int totalProjects = projects.size();
        int activeProjects = 0;
        int pendingReview = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        int inProgressCount = 0;

        for (ProjectItem project : projects) {
            String status = project.getStatus() == null ? "" : project.getStatus().trim().toLowerCase(Locale.getDefault());
            String reviewStatus = project.getReviewStatus() == null ? "" : project.getReviewStatus().trim().toLowerCase(Locale.getDefault());

            if ("active".equals(status)) {
                activeProjects++;
                inProgressCount++;
            }

            if ("pending".equals(reviewStatus)) {
                pendingReview++;
            } else if ("approved".equals(reviewStatus)) {
                approvedCount++;
            } else if ("rejected".equals(reviewStatus)) {
                rejectedCount++;
            }
        }

        int reviewedTotal = pendingReview + approvedCount + rejectedCount;
        double approvalRate = reviewedTotal == 0 ? 0.0 : (approvedCount * 100.0 / reviewedTotal);

        tvActiveProjects.setText(String.valueOf(activeProjects));

        // Tạm thời dùng vị trí này để hiển thị tổng project cho đến khi bạn có bảng tasks thật
        tvTotalTasks.setText(String.valueOf(totalProjects));

        tvPendingReview.setText(String.valueOf(pendingReview));
        tvApprovalRate.setText(String.format(Locale.getDefault(), "%.1f%%", approvalRate));

        tvInProgressCount.setText(String.valueOf(inProgressCount));
        tvPendingCount.setText(String.valueOf(pendingReview));
        tvApprovedCount.setText(String.valueOf(approvedCount));
        tvRejectedCount.setText(String.valueOf(rejectedCount));
    }

    private void setupActions() {
        btnProjects.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, ManagerProjectsActivity.class);
            startActivity(intent);
        });

        btnDatasets.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, DatasetsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}