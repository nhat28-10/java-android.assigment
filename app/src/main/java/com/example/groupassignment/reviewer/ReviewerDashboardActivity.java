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
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeReviewer;
    private TextView tvPendingReview;
    private TextView tvReviewed;
    private TextView tvTotalAssigned;
    private TextView tvWaitingSubmission;
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

        if (!sessionManager.isLoggedIn() || !sessionManager.isReviewer()) {
            finish();
            startActivity(com.example.groupassignment.utils.RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            return;
        }

        initViews();
        setupResultLauncher();
        btnLogout.setOnClickListener(v -> logout());
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
        tvWaitingSubmission = findViewById(R.id.tvWaitingSubmission);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        btnLogout = findViewById(R.id.btnLogout);
        layoutTaskList = findViewById(R.id.layoutReviewerTaskList);
    }

    private void setupResultLauncher() {
        taskDetailLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> bindDashboardData());
    }

    private void bindDashboardData() {
        String reviewerName = sessionManager.getName();
        if (reviewerName == null || reviewerName.trim().isEmpty()) {
            reviewerName = "Reviewer";
        }
        tvWelcomeReviewer.setText("Welcome back, " + reviewerName);

        int reviewerId = getCurrentReviewerId();
        taskDbHelper.refreshExpiredTasks();
        List<LogicalTaskItem> allTasks = taskDbHelper.getLogicalTasksForReviewer(reviewerId);

        List<LogicalTaskItem> waitingForSubmission = new ArrayList<>();
        List<LogicalTaskItem> pendingReview = new ArrayList<>();
        List<LogicalTaskItem> reviewedOrFinalized = new ArrayList<>();

        for (LogicalTaskItem task : allTasks) {
            if (task == null) {
                continue;
            }
            if (isWaitingForSubmission(task)) {
                waitingForSubmission.add(task);
            } else if (isPendingReview(task, reviewerId)) {
                pendingReview.add(task);
            } else {
                reviewedOrFinalized.add(task);
            }
        }

        tvTotalAssigned.setText(String.valueOf(allTasks.size()));
        tvWaitingSubmission.setText(String.valueOf(waitingForSubmission.size()));
        tvPendingReview.setText(String.valueOf(pendingReview.size()));
        tvReviewed.setText(String.valueOf(reviewedOrFinalized.size()));

        renderTaskSections(waitingForSubmission, pendingReview, reviewedOrFinalized);
    }

    private void renderTaskSections(List<LogicalTaskItem> waitingForSubmission,
                                    List<LogicalTaskItem> pendingReview,
                                    List<LogicalTaskItem> reviewedOrFinalized) {
        layoutTaskList.removeAllViews();
        boolean hasAnyTask = !waitingForSubmission.isEmpty() || !pendingReview.isEmpty() || !reviewedOrFinalized.isEmpty();
        tvEmptyTasks.setVisibility(hasAnyTask ? View.GONE : View.VISIBLE);
        if (!hasAnyTask) {
            return;
        }

        addTaskSection("Assigned / Waiting for submission", waitingForSubmission, false);
        addTaskSection("Pending review", pendingReview, true);
        addTaskSection("Reviewed / Finalized", reviewedOrFinalized, false);
    }

    private void addTaskSection(String title, List<LogicalTaskItem> tasks, boolean actionable) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        TextView header = new TextView(this);
        header.setText(String.format(Locale.getDefault(), "%s (%d)", title, tasks.size()));
        header.setTextSize(16f);
        header.setTextColor(getColor(R.color.text_primary));
        header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.topMargin = dp(10);
        headerParams.bottomMargin = dp(8);
        header.setLayoutParams(headerParams);
        layoutTaskList.addView(header);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (LogicalTaskItem item : tasks) {
            View card = inflater.inflate(R.layout.item_reviewer_task, layoutTaskList, false);
            bindTaskCard(card, item, actionable);
            layoutTaskList.addView(card);
        }
    }

    private void bindTaskCard(View card, LogicalTaskItem item, boolean actionable) {
        TextView tvTaskId = card.findViewById(R.id.tvTaskId);
        TextView tvTaskProject = card.findViewById(R.id.tvTaskProject);
        TextView tvTaskDataset = card.findViewById(R.id.tvTaskDataset);
        TextView tvTaskAnnotator = card.findViewById(R.id.tvTaskAnnotator);
        TextView tvTaskType = card.findViewById(R.id.tvTaskType);
        TextView tvTaskStatus = card.findViewById(R.id.tvTaskStatus);
        Button btnOpenTask = card.findViewById(R.id.btnOpenTask);

        tvTaskId.setText(item.getDisplaySourceName() + " • Round " + item.getRoundNumber());
        tvTaskProject.setText("Project: " + item.getProjectName());
        tvTaskDataset.setText("Dataset: " + item.getDatasetName() + " • Item: " + item.getDisplaySourceName());
        tvTaskAnnotator.setText("Annotator: " + item.getAnnotatorName());
        tvTaskType.setText("Votes: " + item.getApproveCount() + "/" + item.getRejectCount() + "/" + item.getPendingVotes());
        tvTaskStatus.setText("Status: " + item.getDisplayStatus());
        btnOpenTask.setText(actionable ? "Open Review" : "View Task");
        btnOpenTask.setOnClickListener(v -> openTaskDetail(item.getReferenceTaskId()));
    }

    private boolean isWaitingForSubmission(LogicalTaskItem task) {
        String status = normalize(task.getStatus());
        return !task.isFinalDecisionReached()
                && (TaskDbHelper.STATUS_ASSIGNED.equals(status)
                || TaskDbHelper.STATUS_IN_PROGRESS.equals(status)
                || TaskDbHelper.STATUS_REWORK_REQUIRED.equals(status));
    }

    private boolean isPendingReview(LogicalTaskItem task, int reviewerId) {
        String status = normalize(task.getStatus());
        return !task.isFinalDecisionReached()
                && (TaskDbHelper.STATUS_SUBMITTED.equals(status)
                || TaskDbHelper.STATUS_UNDER_REVIEW.equals(status))
                && taskDbHelper.canReviewerVote(task, reviewerId);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
