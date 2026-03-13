package com.example.groupassignment.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.groupassignment.manager.ProjectsActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.utils.SessionManager;

public class ManagerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeManager;
    private TextView tvActiveProjects, tvTotalTasks, tvPendingReview, tvApprovalRate;
    private TextView tvInProgressCount, tvPendingCount, tvApprovedCount, tvRejectedCount;
    private Button btnProjects, btnDatasets, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        initViews();
        bindMockData();
        setupActions();
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

    private void bindMockData() {
        String managerName = "Manager";
        tvWelcomeManager.setText("Welcome back, " + managerName);

        tvActiveProjects.setText("8");
        tvTotalTasks.setText("156");
        tvPendingReview.setText("18");
        tvApprovalRate.setText("84.5%");

        tvInProgressCount.setText("12");
        tvPendingCount.setText("18");
        tvApprovedCount.setText("94");
        tvRejectedCount.setText("17");
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