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
        
        sessionManager = new SessionManager(this);
        
        // Kiểm tra quyền truy cập trước khi setContentView để tránh leak UI
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);
        authDbHelper = new AuthDbHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserCount = findViewById(R.id.tvUserCount);
        btnUserManagement = findViewById(R.id.btnUserManagement);
        btnLogout = findViewById(R.id.btnLogout);

        btnUserManagement.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class))
        );

        btnLogout.setOnClickListener(v -> logout());

        loadDashboard();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn() && sessionManager.isAdmin()) {
            loadDashboard();
        }
    }

    private void loadDashboard() {
        if (tvWelcome != null) tvWelcome.setText("Welcome " + sessionManager.getName());
        if (tvUserCount != null && authDbHelper != null) {
            tvUserCount.setText("Total Users: " + authDbHelper.getAllUsers().size());
        }
    }
}
