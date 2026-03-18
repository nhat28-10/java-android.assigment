package com.example.groupassignment.annotator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;

public class AnnotatorOverviewActivity extends AppCompatActivity {

    private TextView tvAnnotatorWelcome;
    private TextView tvAnnotatorTaskSummary;
    private Button btnOpenTask;
    private Button btnLogout;

    private SessionManager sessionManager;
    private TaskDbHelper taskDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_overview);

        sessionManager = new SessionManager(this);
        taskDbHelper = new TaskDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAnnotator()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            startActivity(RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            finish();
            return;
        }

        tvAnnotatorWelcome = findViewById(R.id.tvAnnotatorWelcome);
        tvAnnotatorTaskSummary = findViewById(R.id.tvAnnotatorTaskSummary);
        btnOpenTask = findViewById(R.id.btnOpenTask);
        btnLogout = findViewById(R.id.btnLogout);

        btnOpenTask.setOnClickListener(v -> openNextTask());
        btnLogout.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindTaskSummary();
    }

    private void bindTaskSummary() {
        String annotatorName = sessionManager.getName();
        if (annotatorName == null || annotatorName.trim().isEmpty()) {
            annotatorName = "Annotator";
        }
        tvAnnotatorWelcome.setText("Annotator Overview\n" + annotatorName);

        int annotatorId = getCurrentAnnotatorId();
        taskDbHelper.seedDemoTasksForAnnotator(annotatorId);

        List<TaskItem> allTasks = taskDbHelper.getTasksForAnnotator(annotatorId);
        List<TaskItem> pendingTasks = taskDbHelper.getPendingTasksForAnnotator(annotatorId);

        tvAnnotatorTaskSummary.setText(
                "Assigned tasks: " + allTasks.size() + "\nPending work: " + pendingTasks.size()
        );

        btnOpenTask.setEnabled(!allTasks.isEmpty());
    }

    private void openNextTask() {
        int annotatorId = getCurrentAnnotatorId();
        List<TaskItem> pendingTasks = taskDbHelper.getPendingTasksForAnnotator(annotatorId);
        List<TaskItem> allTasks = taskDbHelper.getTasksForAnnotator(annotatorId);

        TaskItem selectedTask = !pendingTasks.isEmpty() ? pendingTasks.get(0) : (!allTasks.isEmpty() ? allTasks.get(0) : null);
        if (selectedTask == null) {
            Toast.makeText(this, "No tasks available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AnnotatorTaskActivity.class);
        intent.putExtra("task_id", selectedTask.getId());
        startActivity(intent);
    }

    private int getCurrentAnnotatorId() {
        long sessionUserId = sessionManager.getUserId();
        if (sessionUserId > 0) {
            return (int) sessionUserId;
        }

        String email = sessionManager.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            AuthDbHelper authDbHelper = new AuthDbHelper(this);
            User currentUser = authDbHelper.getUserByEmail(email);
            if (currentUser != null) {
                return (int) currentUser.getId();
            }
        }

        return 0;
    }

    private void logout() {
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
