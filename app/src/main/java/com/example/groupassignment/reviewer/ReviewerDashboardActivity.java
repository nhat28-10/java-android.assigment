package com.example.groupassignment.reviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;

public class ReviewerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeReviewer;
    private TextView tvPendingReview;
    private TextView tvReviewed;
    private TextView tvTotalAssigned;
    private TextView tvEmptyTasks;
    private Button btnLogout;
    private LinearLayout layoutTaskList;

    private TaskDbHelper taskDbHelper;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> taskDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer_dashboard);

        taskDbHelper = new TaskDbHelper(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupResultLauncher();
        setupActions();
        bindDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindDashboardData();
    }

    private void initViews() {
        tvWelcomeReviewer = findViewById(R.id.tvWelcomeReviewer);
        tvPendingReview = findViewById(R.id.tvPendingReview);
        tvReviewed = findViewById(R.id.tvReviewed);
        tvTotalAssigned = findViewById(R.id.tvTotalAssigned);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        btnLogout = findViewById(R.id.btnLogout);
        layoutTaskList = findViewById(R.id.layoutReviewerTaskList);
    }

    private void setupResultLauncher() {
        taskDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        bindDashboardData();
                    }
                }
        );
    }

    private void setupActions() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void bindDashboardData() {
        String reviewerName = sessionManager.getName();
        if (reviewerName == null || reviewerName.trim().isEmpty()) {
            reviewerName = "Reviewer";
        }
        tvWelcomeReviewer.setText("Welcome back, " + reviewerName);

        int reviewerId = getCurrentReviewerId();
        taskDbHelper.ensureReviewerTasksSeeded(reviewerId);

        List<TaskItem> allTasks = taskDbHelper.getTasksForReviewer(reviewerId);
        List<TaskItem> pendingTasks = taskDbHelper.getPendingReviewTasksForReviewer(reviewerId);
        List<TaskItem> reviewedTasks = taskDbHelper.getReviewedTasksForReviewer(reviewerId);

        tvPendingReview.setText(String.valueOf(pendingTasks.size()));
        tvReviewed.setText(String.valueOf(reviewedTasks.size()));
        tvTotalAssigned.setText(String.valueOf(allTasks.size()));

        renderTaskList(allTasks);
    }

    private void renderTaskList(List<TaskItem> tasks) {
        layoutTaskList.removeAllViews();

        if (tasks == null || tasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyTasks.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (TaskItem item : tasks) {
            View card = inflater.inflate(R.layout.item_reviewer_task, layoutTaskList, false);

            TextView tvTaskId = card.findViewById(R.id.tvTaskId);
            TextView tvTaskProject = card.findViewById(R.id.tvTaskProject);
            TextView tvTaskDataset = card.findViewById(R.id.tvTaskDataset);
            TextView tvTaskAnnotator = card.findViewById(R.id.tvTaskAnnotator);
            TextView tvTaskType = card.findViewById(R.id.tvTaskType);
            TextView tvTaskStatus = card.findViewById(R.id.tvTaskStatus);
            Button btnOpenTask = card.findViewById(R.id.btnOpenTask);

            tvTaskId.setText("Task #" + item.getId());
            tvTaskProject.setText("Project: " + item.getProjectName());
            tvTaskDataset.setText("Dataset: " + item.getDatasetName());
            tvTaskAnnotator.setText("Annotator: " + item.getAnnotatorName());
            tvTaskType.setText("Type: " + item.getType());
            tvTaskStatus.setText("Status: " + item.getStatus());

            btnOpenTask.setOnClickListener(v -> openTaskDetail(item.getId()));
            layoutTaskList.addView(card);
        }
    }

    private void openTaskDetail(int taskId) {
        Intent intent = new Intent(this, ReviewerTaskActivity.class);
        intent.putExtra(ReviewerTaskActivity.EXTRA_TASK_ID, taskId);
        taskDetailLauncher.launch(intent);
    }

    private int getCurrentReviewerId() {
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

        return 1;
    }

    private void logout() {
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
