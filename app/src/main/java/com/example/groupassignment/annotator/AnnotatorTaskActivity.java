package com.example.groupassignment.annotator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

public class AnnotatorTaskActivity extends AppCompatActivity {

    private TextView tvProjectName, tvDatasetName, tvStatus, tvInstructions;
    private EditText etAnnotation;
    private Button btnSave, btnSubmit;
    private TaskDbHelper taskDbHelper;
    private TaskItem currentTask;
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
        tvInstructions = findViewById(R.id.tvInstructions);
        etAnnotation = findViewById(R.id.etAnnotation);
        btnSave = findViewById(R.id.btnSave);
        btnSubmit = findViewById(R.id.btnSubmit);

        taskDbHelper = new TaskDbHelper(this);
    }

    private void loadTask() {
        currentTask = taskDbHelper.getTaskById(taskId);
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

        tvProjectName.setText("Project: " + currentTask.getProjectName());
        tvDatasetName.setText("Dataset: " + currentTask.getDatasetName());
        tvStatus.setText("Status: " + currentTask.getStatus());
        tvInstructions.setText("Instructions:\n1. Review the data carefully\n2. Add your annotations\n3. Click Submit when done");

        if (currentTask.getAnnotationResult() != null && !currentTask.getAnnotationResult().isEmpty()) {
            etAnnotation.setText(currentTask.getAnnotationResult());
        }

        if ("submitted".equalsIgnoreCase(currentTask.getStatus())
                || "approved".equalsIgnoreCase(currentTask.getStatus())) {
            btnSubmit.setEnabled(false);
            btnSave.setEnabled(false);
            etAnnotation.setEnabled(false);
        }
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

        boolean success = taskDbHelper.updateAnnotation(taskId, annotation, "in_progress");
        if (success) {
            tvStatus.setText("Status: in_progress");
            Toast.makeText(this, "Draft saved", Toast.LENGTH_SHORT).show();
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

        boolean success = taskDbHelper.updateAnnotation(taskId, annotation, "submitted");
        if (success) {
            tvStatus.setText("Status: submitted");
            Toast.makeText(this, "Task submitted successfully", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(false);
            btnSave.setEnabled(false);
            etAnnotation.setEnabled(false);
        } else {
            Toast.makeText(this, "Failed to submit", Toast.LENGTH_SHORT).show();
        }
    }
}
