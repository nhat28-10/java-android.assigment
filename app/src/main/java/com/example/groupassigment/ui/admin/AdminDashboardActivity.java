package com.example.groupassigment.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassigment.R;
import com.example.groupassigment.repository.UserRepository;
import com.example.groupassigment.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvUserCount;
    private Button btnUserManagement;
    private SessionManager sessionManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, " Access denied\, Toast.LENGTH_SHORT).show();
 finish();
 return;
 }

 tvWelcome = findViewById(R.id.tvWelcome);
 tvUserCount = findViewById(R.id.tvUserCount);
 btnUserManagement = findViewById(R.id.btnUserManagement);

 btnUserManagement.setOnClickListener(new View.OnClickListener() {
 @Override
 public void onClick(View v) {
 startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class));
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
 tvWelcome.setText(\Welcome \ + sessionManager.getCurrentUser().getFullName());
 tvUserCount.setText(\Total Users: \ + userRepository.getUserCount());
 }
}
