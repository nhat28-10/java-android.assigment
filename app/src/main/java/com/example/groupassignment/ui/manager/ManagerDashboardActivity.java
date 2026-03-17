package com.example.groupassignment.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassignment.R;
import com.example.groupassignment.repository.ProjectRepository;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.utils.SessionManager;

public class ManagerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvProjectCount;
    private Button btnProjects, btnDatasets, btnLogout;
    private SessionManager sessionManager;
    private ProjectRepository projectRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        sessionManager = new SessionManager(this);
        projectRepository = new ProjectRepository(this);

        if (!sessionManager.isLoggedIn() || (!sessionManager.isManager() && !sessionManager.isAdmin())) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcomeManager);
        tvProjectCount = findViewById(R.id.tvActiveProjects);
        btnProjects = findViewById(R.id.btnProjects);
        btnDatasets = findViewById(R.id.btnDatasets);
        btnLogout = findViewById(R.id.btnLogout);

        btnProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerDashboardActivity.this, ProjectManagementActivity.class));
            }
        });

        btnDatasets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManagerDashboardActivity.this, "Datasets feature", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.logout();
                Toast.makeText(ManagerDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagerDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        loadDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        tvWelcome.setText("Welcome " + sessionManager.getName());
    }
}
