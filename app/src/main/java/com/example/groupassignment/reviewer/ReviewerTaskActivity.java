package com.example.groupassignment.reviewer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;
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
    private TextView tvAnnotationResult;
    private EditText edtReviewComment;
    private EditText edtRejectionReason;
    private Button btnApprove;
    private Button btnReject;

    private TaskDbHelper taskDbHelper;
    private int taskId;
    private TaskItem taskItem;
    private SessionManager sessionManager;
    private int currentReviewerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer_task);

        taskDbHelper = new TaskDbHelper(this);
        sessionManager = new SessionManager(this);
        currentReviewerId = (int) sessionManager.getUserId();
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

        taskItem = taskDbHelper.getTaskById(taskId);
        if (taskItem == null) {
            Toast.makeText(this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentReviewerId > 0 && taskItem.getReviewerId() != currentReviewerId) {
            Toast.makeText(this, "Bạn không có quyền xem task này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindTask(taskItem);
    }

    private void bindTask(TaskItem item) {
        tvTaskTitle.setText("Review Task #" + item.getId());
        tvProjectName.setText(item.getProjectName());
        tvDatasetName.setText(item.getDatasetName());
        tvAnnotatorName.setText(item.getAnnotatorName());
        tvTaskType.setText(item.getType());
        tvTaskStatus.setText(item.getStatus());
        tvSubmittedAt.setText(safeText(item.getSubmittedAt()));
        tvReviewedAt.setText(safeText(item.getReviewedAt()));
        tvAnnotationResult.setText(safeText(item.getAnnotationResult()));

        edtReviewComment.setText(safeText(item.getReviewComments()));
        edtRejectionReason.setText(safeText(item.getRejectionReason()));

        boolean reviewed = item.isReviewed();
        if (reviewed) {
            btnApprove.setEnabled(false);
            btnReject.setEnabled(false);
            btnApprove.setAlpha(0.6f);
            btnReject.setAlpha(0.6f);
            edtReviewComment.setEnabled(false);
            edtRejectionReason.setEnabled(false);
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
        if (value == null || value.trim().isEmpty()) {
            return "N/A";
        }
        return value;
    }
}
