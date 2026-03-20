package com.example.groupassignment.annotator;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

public class AnnotatorTaskActivity extends AppCompatActivity {

    private TextView tvProjectName;
    private TextView tvDatasetName;
    private TextView tvStatus;
    private TextView tvDeadline;
    private TextView tvLabels;
    private TextView tvInstructions;
    private TextView tvReviewerFeedback;
    private EditText etAnnotation;
    private Button btnSave;
    private Button btnSubmit;
    private TaskDbHelper taskDbHelper;
    private LogicalTaskItem currentTask;
    private int taskId;
    private SessionManager sessionManager;
    private int currentAnnotatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_task);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !sessionManager.isAnnotator()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        currentAnnotatorId = (int) sessionManager.getUserId();
        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadTask();
        setupListeners();
    }

    private void initViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDatasetName = findViewById(R.id.tvDatasetName);
        tvStatus = findViewById(R.id.tvStatus);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvLabels = findViewById(R.id.tvLabels);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvReviewerFeedback = findViewById(R.id.tvReviewerFeedback);
        etAnnotation = findViewById(R.id.etAnnotation);
        btnSave = findViewById(R.id.btnSave);
        btnSubmit = findViewById(R.id.btnSubmit);
        taskDbHelper = new TaskDbHelper(this);
    }

    private void loadTask() {
        taskDbHelper.refreshExpiredTasks();
        currentTask = taskDbHelper.getLogicalTaskByRawTaskId(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (currentAnnotatorId > 0 && currentTask.getAnnotatorId() != currentAnnotatorId) {
            Toast.makeText(this, "You do not have access to this task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taskId = currentTask.getReferenceTaskId();
        tvProjectName.setText("Project: " + currentTask.getProjectName());
        tvDatasetName.setText("Dataset: " + currentTask.getDatasetName() + " • Item: " + currentTask.getDisplaySourceName() + " • Round " + currentTask.getRoundNumber());
        tvStatus.setText("Status: " + currentTask.getDisplayStatus() + " • Assigned: " + (TextUtils.isEmpty(currentTask.getAssignedAt()) ? "N/A" : currentTask.getAssignedAt()));
        tvDeadline.setText("Deadline: " + (TextUtils.isEmpty(currentTask.getDeadline()) ? "Not set" : currentTask.getDeadline()));
        tvLabels.setText("Project labels: " + (TextUtils.isEmpty(currentTask.getLabelsRaw()) ? "N/A" : currentTask.getLabelsRaw().replace("||", ", ")));
        tvInstructions.setText("Guideline:\n" + (TextUtils.isEmpty(currentTask.getGuidelines()) ? "No guideline provided" : currentTask.getGuidelines()));
        etAnnotation.setText(TextUtils.isEmpty(currentTask.getAnnotationResult()) ? "" : currentTask.getAnnotationResult().replace("||", ", "));
        tvReviewerFeedback.setText(buildFeedbackText());

        boolean editable = taskDbHelper.isTaskEditable(currentTask);
        btnSave.setEnabled(editable);
        btnSubmit.setEnabled(editable);
        etAnnotation.setEnabled(editable);
        if (!editable) {
            Toast.makeText(this, "This task is read-only in its current state.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildFeedbackText() {
        if (!TaskDbHelper.STATUS_REWORK_REQUIRED.equals(currentTask.getDisplayStatus())) {
            return "Reviewer feedback will appear here if the task requires rework.";
        }
        StringBuilder builder = new StringBuilder("Rework feedback:\n");
        for (ReviewerVoteItem vote : currentTask.getReviewerVotes()) {
            if (!vote.hasVoted()) {
                continue;
            }
            builder.append("- ").append(vote.getReviewerName())
                    .append(": ")
                    .append(TextUtils.isEmpty(vote.getComments()) ? "No comment" : vote.getComments());
            if (!TextUtils.isEmpty(vote.getRejectionReason())) {
                builder.append(" (Reason: ").append(vote.getRejectionReason()).append(")");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveDraft());
        btnSubmit.setOnClickListener(v -> submitTask());
    }

    private void saveDraft() {
        String annotation = etAnnotation.getText().toString().trim();
        if (annotation.isEmpty()) {
            Toast.makeText(this, "Please enter annotation", Toast.LENGTH_SHORT).show();
            return;
        }
        int latestTaskId = taskDbHelper.saveAnnotationForLogicalTask(taskId, annotation, TaskDbHelper.STATUS_IN_PROGRESS);
        if (latestTaskId > 0) {
            taskId = latestTaskId;
            Toast.makeText(this, "Draft saved", Toast.LENGTH_SHORT).show();
            loadTask();
        } else {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitTask() {
        String annotation = etAnnotation.getText().toString().trim();
        if (annotation.isEmpty()) {
            Toast.makeText(this, "Please enter annotation", Toast.LENGTH_SHORT).show();
            return;
        }
        int latestTaskId = taskDbHelper.saveAnnotationForLogicalTask(taskId, annotation, TaskDbHelper.STATUS_SUBMITTED);
        if (latestTaskId > 0) {
            taskId = latestTaskId;
            Toast.makeText(this, "Task submitted successfully", Toast.LENGTH_SHORT).show();
            loadTask();
        } else {
            Toast.makeText(this, "Failed to submit", Toast.LENGTH_SHORT).show();
        }
    }
}
