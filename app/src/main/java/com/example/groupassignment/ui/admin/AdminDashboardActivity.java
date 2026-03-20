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
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnAllUsers, btnCreateUser, btnActivityLogs, btnLogout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        btnAllUsers = findViewById(R.id.btnAllUsers);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnActivityLogs = findViewById(R.id.btnActivityLogs);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome " + sessionManager.getName());

        // Nút chính: Xem danh sách và Bật/Tắt trạng thái
        btnAllUsers.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AllUsersActivity.class))
        );

        // Nút phụ: Chỉ dùng để thêm mới
        btnCreateUser.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class))
        );

        btnActivityLogs.setOnClickListener(v -> 
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
