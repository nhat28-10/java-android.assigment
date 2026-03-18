package com.example.groupassignment.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvUserCount;
    private Button btnUserManagement, btnLogout;
    private SessionManager sessionManager;
    private AuthDbHelper authDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        authDbHelper = new AuthDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserCount = findViewById(R.id.tvUserCount);
        btnUserManagement = findViewById(R.id.btnUserManagement);
        btnLogout = findViewById(R.id.btnLogout);

        btnUserManagement.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class))
        );

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
        tvUserCount.setText("Total Users: " + authDbHelper.getAllUsers().size());
    }
}
