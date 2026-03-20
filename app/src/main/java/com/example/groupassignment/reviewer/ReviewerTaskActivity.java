package com.example.groupassignment.reviewer;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.annotator.AnnotationView;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;
import com.example.groupassignment.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;

public class ReviewerTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvTaskTitle, tvProjectName, tvDatasetName, tvAnnotatorName, tvTaskStatus, tvBoxCount;
    private AnnotationView annotationView;
    private Switch swShowLabels;
    private EditText edtReviewComment, edtRejectionReason;
    private Button btnApprove, btnReject;

    private TaskDbHelper taskDbHelper;
    private int taskId;
    private TaskItem taskItem;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer_task);

        taskDbHelper = new TaskDbHelper(this);
        sessionManager = new SessionManager(this);

        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        
        initViews();
        loadTask();
        setupActions();
    }

    private void initViews() {
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDatasetName = findViewById(R.id.tvDatasetName);
        tvAnnotatorName = findViewById(R.id.tvAnnotatorName);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvBoxCount = findViewById(R.id.tvBoxCount);
        
        annotationView = findViewById(R.id.annotationViewReview);
        swShowLabels = findViewById(R.id.swShowLabels);
        
        edtReviewComment = findViewById(R.id.edtReviewComment);
        edtRejectionReason = findViewById(R.id.edtRejectionReason);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        annotationView.setEnabled(false); // Only for viewing
    }

    private void loadTask() {
        if (taskId <= 0) {
            finish();
            return;
        }

        taskItem = taskDbHelper.getTaskById(taskId);
        if (taskItem == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTaskTitle.setText("Review Task #" + taskItem.getId());
        tvProjectName.setText(taskItem.getProjectName());
        tvDatasetName.setText(taskItem.getDatasetName());
        tvAnnotatorName.setText(taskItem.getAnnotatorName());
        tvTaskStatus.setText(taskItem.getStatus().toUpperCase());

        // LOAD ACTUAL IMAGE
        if (taskItem.getImageUri() != null && !taskItem.getImageUri().isEmpty()) {
            try {
                annotationView.setImageURI(Uri.parse(taskItem.getImageUri()));
            } catch (Exception e) {
                Toast.makeText(this, "Image load error", Toast.LENGTH_SHORT).show();
            }
        }

        // LOAD ANNOTATION DATA
        if (taskItem.getAnnotationResult() != null && !taskItem.getAnnotationResult().isEmpty()) {
            annotationView.setBoxesFromJson(taskItem.getAnnotationResult());
            updateBoxCount(taskItem.getAnnotationResult());
        }

        if (taskItem.isReviewed()) {
            btnApprove.setEnabled(false);
            btnReject.setEnabled(false);
            edtReviewComment.setEnabled(false);
            edtRejectionReason.setEnabled(false);
        }
    }

    private void updateBoxCount(String json) {
        try {
            JSONArray array = new JSONArray(json);
            tvBoxCount.setText("Boxes: " + array.length());
        } catch (Exception e) {
            tvBoxCount.setText("Boxes: 0");
        }
    }

    private void setupActions() {
        btnApprove.setOnClickListener(v -> handleDecision(true));
        btnReject.setOnClickListener(v -> handleDecision(false));
        swShowLabels.setOnCheckedChangeListener((b, checked) -> annotationView.setShowLabels(checked));
    }

    private void handleDecision(boolean approved) {
        String comment = edtReviewComment.getText().toString().trim();
        String reason = edtRejectionReason.getText().toString().trim();

        if (!approved && reason.isEmpty()) {
            edtRejectionReason.setError("Required for rejection");
            return;
        }

        boolean success;
        if (approved) {
            success = taskDbHelper.approveTask(taskId, comment);
        } else {
            success = taskDbHelper.rejectTask(taskId, comment, reason);
        }

        if (success) {
            Toast.makeText(this, approved ? "Approved!" : "Rejected!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }
}
