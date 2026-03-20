package com.example.groupassignment.annotator;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;

public class AnnotatorOverviewActivity extends AppCompatActivity {

    private TextView tvAnnotatorWelcome;
    private TextView tvAnnotatorTaskSummary;
    private TextView tvEmptyTasks;
    private Button btnOpenTask;
    private Button btnLogout;
    private LinearLayout layoutAnnotatorTaskList;
    private SessionManager sessionManager;
    private TaskDbHelper taskDbHelper;
    private List<LogicalTaskItem> logicalTasks;

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
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        btnOpenTask = findViewById(R.id.btnOpenTask);
        btnLogout = findViewById(R.id.btnLogout);
        layoutAnnotatorTaskList = findViewById(R.id.layoutAnnotatorTaskList);

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
        taskDbHelper.refreshExpiredTasks();
        logicalTasks = taskDbHelper.getLogicalTasksForAnnotator(annotatorId);

        int assigned = 0;
        int inProgress = 0;
        int underReview = 0;
        int rework = 0;
        int approved = 0;
        int rejected = 0;
        int overdue = 0;
        for (LogicalTaskItem task : logicalTasks) {
            String status = task.getDisplayStatus();
            if (TaskDbHelper.STATUS_ASSIGNED.equals(status)) {
                assigned++;
            } else if (TaskDbHelper.STATUS_IN_PROGRESS.equals(status)) {
                inProgress++;
            } else if (TaskDbHelper.STATUS_SUBMITTED.equals(status) || TaskDbHelper.STATUS_UNDER_REVIEW.equals(status)) {
                underReview++;
            } else if (TaskDbHelper.STATUS_REWORK_REQUIRED.equals(status)) {
                rework++;
            } else if (TaskDbHelper.STATUS_APPROVED_FINAL.equals(status)) {
                approved++;
            } else if (TaskDbHelper.STATUS_REJECTED_FINAL.equals(status)) {
                rejected++;
            } else if (TaskDbHelper.STATUS_OVERDUE.equals(status)) {
                overdue++;
            }
        }

        tvAnnotatorTaskSummary.setText(
                "Assigned: " + assigned
                        + "\nIn progress: " + inProgress
                        + "\nUnder review: " + underReview
                        + "\nRework required: " + rework
                        + "\nApproved final: " + approved
                        + "\nRejected final: " + rejected
                        + "\nOverdue: " + overdue
        );

        btnOpenTask.setEnabled(!logicalTasks.isEmpty());
        renderTaskList();
    }

    private void renderTaskList() {
        layoutAnnotatorTaskList.removeAllViews();
        if (logicalTasks == null || logicalTasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyTasks.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (LogicalTaskItem item : logicalTasks) {
            View card = inflater.inflate(R.layout.item_reviewer_task, layoutAnnotatorTaskList, false);
            TextView tvTaskId = card.findViewById(R.id.tvTaskId);
            TextView tvTaskProject = card.findViewById(R.id.tvTaskProject);
            TextView tvTaskDataset = card.findViewById(R.id.tvTaskDataset);
            TextView tvTaskAnnotator = card.findViewById(R.id.tvTaskAnnotator);
            TextView tvTaskType = card.findViewById(R.id.tvTaskType);
            TextView tvTaskStatus = card.findViewById(R.id.tvTaskStatus);
            Button btnOpenTaskCard = card.findViewById(R.id.btnOpenTask);

            tvTaskId.setText("Task Round #" + item.getRoundNumber());
            tvTaskProject.setText("Project: " + item.getProjectName());
            tvTaskDataset.setText("Dataset: " + item.getDatasetName());
            tvTaskAnnotator.setText("Labels: " + (item.getLabelsRaw().isEmpty() ? "N/A" : item.getLabelsRaw().replace("||", ", ")));
            tvTaskType.setText("Votes: " + item.getApproveCount() + " approve / " + item.getRejectCount() + " reject / " + item.getPendingVotes() + " pending");
            tvTaskStatus.setText("Status: " + item.getDisplayStatus());
            btnOpenTaskCard.setText("Open Task");
            btnOpenTaskCard.setOnClickListener(v -> openTask(item.getReferenceTaskId()));
            layoutAnnotatorTaskList.addView(card);
        }
    }

    private void openNextTask() {
        if (logicalTasks == null || logicalTasks.isEmpty()) {
            Toast.makeText(this, "No tasks available", Toast.LENGTH_SHORT).show();
            return;
        }
        LogicalTaskItem selectedTask = logicalTasks.get(0);
        for (LogicalTaskItem item : logicalTasks) {
            if (taskDbHelper.isTaskEditable(item)) {
                selectedTask = item;
                break;
            }
        }
        openTask(selectedTask.getReferenceTaskId());
    }

    private void openTask(int taskId) {
        Intent intent = new Intent(this, AnnotatorTaskActivity.class);
        intent.putExtra("task_id", taskId);
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
