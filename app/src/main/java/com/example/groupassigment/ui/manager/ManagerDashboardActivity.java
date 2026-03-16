package com.example.groupassigment.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassigment.R;
import com.example.groupassigment.repository.ProjectRepository;
import com.example.groupassigment.utils.SessionManager;

public class ManagerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvProjectCount;
    private Button btnProjectManagement;
    private SessionManager sessionManager;
    private ProjectRepository projectRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        sessionManager = new SessionManager(this);
        projectRepository = new ProjectRepository(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, " Access denied\, Toast.LENGTH_SHORT).show();
 finish();
 return;
 }

 tvWelcome = findViewById(R.id.tvWelcome);
 tvProjectCount = findViewById(R.id.tvProjectCount);
 btnProjectManagement = findViewById(R.id.btnProjectManagement);

 btnProjectManagement.setOnClickListener(new View.OnClickListener() {
 @Override
 public void onClick(View v) {
 startActivity(new Intent(ManagerDashboardActivity.this, ProjectManagementActivity.class));
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
 tvProjectCount.setText(\Total Projects: \ + projectRepository.getProjectCount());
 }
}
