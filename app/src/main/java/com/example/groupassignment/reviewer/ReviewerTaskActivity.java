package com.example.groupassignment.reviewer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.utils.SessionManager;

public class ReviewerTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvTaskTitle;
    private TextView tvProjectName;
    private TextView tvDatasetName;
    private TextView tvAnnotatorName;
    private TextView tvTaskType;
    private TextView tvTaskStatus;
    private TextView tvSubmittedAt;
    private TextView tvReviewedAt;
    private TextView tvTaskLabels;
    private TextView tvVoteSummary;
    private TextView tvAnnotationResult;
    private EditText edtReviewComment;
    private EditText edtRejectionReason;
    private Button btnApprove;
    private Button btnReject;

    private TaskDbHelper taskDbHelper;
    private int taskId;
    private LogicalTaskItem taskItem;
    private SessionManager sessionManager;
    private int currentReviewerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer_task);

        taskDbHelper = new TaskDbHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isReviewer()) {
            finish();
            startActivity(com.example.groupassignment.utils.RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            return;
        }

        currentReviewerId = resolveReviewerId();
        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        initViews();
        setupActions();
        loadTask();
    }

    private void initViews() {
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDatasetName = findViewById(R.id.tvDatasetName);
        tvAnnotatorName = findViewById(R.id.tvAnnotatorName);
        tvTaskType = findViewById(R.id.tvTaskType);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvSubmittedAt = findViewById(R.id.tvSubmittedAt);
        tvReviewedAt = findViewById(R.id.tvReviewedAt);
        tvTaskLabels = findViewById(R.id.tvTaskLabels);
        tvVoteSummary = findViewById(R.id.tvVoteSummary);
        tvAnnotationResult = findViewById(R.id.tvAnnotationResult);
        edtReviewComment = findViewById(R.id.edtReviewComment);
        edtRejectionReason = findViewById(R.id.edtRejectionReason);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
    }

    private void setupActions() {
        btnApprove.setOnClickListener(v -> handleApprove());
        btnReject.setOnClickListener(v -> handleReject());
    }

    private void loadTask() {
        if (taskId <= 0) {
            Toast.makeText(this, "Task không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        taskDbHelper.refreshExpiredTasks();
        taskItem = taskDbHelper.getLogicalTaskByRawTaskId(taskId);
        if (taskItem == null) {
            Toast.makeText(this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindTask(taskItem);
    }

    private void bindTask(LogicalTaskItem item) {
        tvTaskTitle.setText("Review Task • " + item.getDisplaySourceName() + " • Round " + item.getRoundNumber());
        tvProjectName.setText(item.getProjectName());
        tvDatasetName.setText(item.getDatasetName() + " • " + item.getDisplaySourceName());
        tvAnnotatorName.setText(item.getAnnotatorName());
        tvTaskType.setText(item.getType() + " • Assigned: " + safeText(item.getAssignedAt()));
        tvTaskStatus.setText(item.getDisplayStatus());
        tvSubmittedAt.setText(safeText(item.getSubmittedAt()));
        tvReviewedAt.setText(safeText(item.getReviewedAt()));
        tvTaskLabels.setText(TextUtils.isEmpty(item.getLabelsRaw()) ? "N/A" : item.getLabelsRaw().replace("||", ", "));
        tvVoteSummary.setText(item.getApproveCount() + " approve • " + item.getRejectCount() + " reject • " + item.getPendingVotes() + " pending");
        tvAnnotationResult.setText(TextUtils.isEmpty(item.getAnnotationResult()) ? "No annotation submitted" : item.getAnnotationResult().replace("||", ", "));

        boolean canVote = taskDbHelper.canReviewerVote(item, currentReviewerId);
        btnApprove.setEnabled(canVote);
        btnReject.setEnabled(canVote);
        edtReviewComment.setEnabled(canVote);
        edtRejectionReason.setEnabled(canVote);
        if (!canVote) {
            edtReviewComment.setHint("This round is already decided or you already voted.");
        }
    }

    private void handleApprove() {
        String reviewComment = edtReviewComment.getText().toString().trim();
        boolean success = taskDbHelper.approveTask(taskId, reviewComment);
        if (!success) {
            Toast.makeText(this, "Không thể approve task", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đã approve task", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void handleReject() {
        String reviewComment = edtReviewComment.getText().toString().trim();
        String rejectionReason = edtRejectionReason.getText().toString().trim();
        if (TextUtils.isEmpty(rejectionReason)) {
            edtRejectionReason.setError("Vui lòng nhập reason khi reject");
            edtRejectionReason.requestFocus();
            return;
        }
        boolean success = taskDbHelper.rejectTask(taskId, reviewComment, rejectionReason);
        if (!success) {
            Toast.makeText(this, "Không thể reject task", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đã reject task", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private int resolveReviewerId() {
        long sessionUserId = sessionManager.getUserId();
        if (sessionUserId > 0) {
            return (int) sessionUserId;
        }
        String email = sessionManager.getEmail();
        if (!TextUtils.isEmpty(email)) {
            AuthDbHelper authDbHelper = new AuthDbHelper(this);
            User currentUser = authDbHelper.getUserByEmail(email);
            if (currentUser != null) {
                return (int) currentUser.getId();
            }
        }
        return 0;
    }

}
