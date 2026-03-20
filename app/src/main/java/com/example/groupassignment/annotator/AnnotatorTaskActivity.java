package com.example.groupassignment.annotator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.SourceItemPreviewHelper;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotatorTaskActivity extends AppCompatActivity {

    private TextView tvProjectName;
    private TextView tvStatus;
    private TextView tvReviewerFeedback;
    private TextView tvSourceTextPreview;
    private AnnotationDrawingView drawingView;
    private LinearLayout layoutLabelChips;
    private ImageButton btnUndo, btnClear;
    private Button btnSave, btnSubmit;
    
    private TaskDbHelper taskDbHelper;
    private LogicalTaskItem currentTask;
    private int taskId;
    private SessionManager sessionManager;
    private int currentAnnotatorId;

    private final String[] labelColors = {
            "#FF5252", "#448AFF", "#4CAF50", "#FFC107", 
            "#9C27B0", "#00BCD4", "#E91E63", "#FF9800",
            "#795548", "#607D8B"
    };

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
        tvStatus = findViewById(R.id.tvStatus);
        tvReviewerFeedback = findViewById(R.id.tvReviewerFeedback);
        tvSourceTextPreview = findViewById(R.id.tvSourceTextPreview);
        drawingView = findViewById(R.id.drawingView);
        layoutLabelChips = findViewById(R.id.layoutLabelChips);
        btnUndo = findViewById(R.id.btnUndo);
        btnClear = findViewById(R.id.btnClear);
        btnSave = findViewById(R.id.btnSave);
        btnSubmit = findViewById(R.id.btnSubmit);
        taskDbHelper = new TaskDbHelper(this);
    }

    private void loadTask() {
        taskDbHelper.refreshExpiredTasks();
        currentTask = taskDbHelper.getLogicalTaskByRawTaskId(taskId);
        if (currentTask == null) {
            finish();
            return;
        }

        tvProjectName.setText(currentTask.getProjectName() + " • Round " + currentTask.getRoundNumber());
        tvStatus.setText("STATUS: " + currentTask.getDisplayStatus().toUpperCase());
        tvReviewerFeedback.setText(buildFeedbackText());
        
        setupLabelChips(currentTask.getLabelsRaw());

        String existingResult = currentTask.getAnnotationResult();
        if (!TextUtils.isEmpty(existingResult) && existingResult.startsWith("[")) {
            drawingView.loadBoxesFromJson(existingResult);
        }
        
        bindSourcePreview(currentTask);

        boolean editable = taskDbHelper.isTaskEditable(currentTask);
        setEditable(editable);
    }

    private void setupLabelChips(String labelsRaw) {
        layoutLabelChips.removeAllViews();
        List<String> labels = new ArrayList<>();
        if (!TextUtils.isEmpty(labelsRaw)) {
            labels.addAll(Arrays.asList(labelsRaw.split("\\|\\|")));
        }
        if (labels.isEmpty()) labels.add("Default");

        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            String colorStr = labelColors[i % labelColors.length];
            
            TextView chip = new TextView(this);
            chip.setText(label);
            chip.setTextColor(Color.WHITE);
            chip.setPadding(32, 16, 32, 16);
            chip.setTextSize(14);
            chip.setTypeface(null, Typeface.BOLD);
            chip.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            // Background chip
            updateChipUI(chip, colorStr, i == 0);
            if (i == 0) drawingView.setSelectedLabel(label);

            final int index = i;
            chip.setOnClickListener(v -> {
                drawingView.setSelectedLabel(label);
                // Reset UI of all chips
                for (int j = 0; j < layoutLabelChips.getChildCount(); j++) {
                    updateChipUI((TextView) layoutLabelChips.getChildAt(j), labelColors[j % labelColors.length], j == index);
                }
            });

            layoutLabelChips.addView(chip);
        }
    }

    private void updateChipUI(TextView chip, String colorHex, boolean isSelected) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(Color.parseColor(colorHex));
        gd.setCornerRadius(50);
        if (isSelected) {
            gd.setStroke(6, Color.WHITE);
        } else {
            gd.setAlpha(150); // Mờ đi nếu không chọn
        }
        chip.setBackground(gd);
    }

    private void setEditable(boolean editable) {
        drawingView.setEnabled(editable);
        btnUndo.setEnabled(editable);
        btnClear.setEnabled(editable);
        btnSave.setEnabled(editable);
        btnSubmit.setEnabled(editable);
        layoutLabelChips.setEnabled(editable);
        for (int i = 0; i < layoutLabelChips.getChildCount(); i++) {
            layoutLabelChips.getChildAt(i).setEnabled(editable);
        }
    }

    private void bindSourcePreview(LogicalTaskItem item) {
        drawingView.setVisibility(View.GONE);
        tvSourceTextPreview.setVisibility(View.GONE);

        String sourceUri = item.getSourceUri();
        String displayName = item.getDisplaySourceName();
        String mimeType = item.getSourceMimeType();

        if (SourceItemPreviewHelper.isImage(mimeType, displayName, sourceUri)) {
            drawingView.setVisibility(View.VISIBLE);
            Bitmap bitmap = SourceItemPreviewHelper.loadImageThumbnail(this, sourceUri, 1200);
            if (bitmap != null) {
                drawingView.setImageBitmap(bitmap);
            }
        } else {
            tvSourceTextPreview.setVisibility(View.VISIBLE);
            String preview = (sourceUri != null && sourceUri.contains("://"))
                    ? SourceItemPreviewHelper.readTextPreview(this, sourceUri, 1000)
                    : sourceUri;
            tvSourceTextPreview.setText(preview);
        }
    }

    private String buildFeedbackText() {
        if (!TaskDbHelper.STATUS_REWORK_REQUIRED.equals(currentTask.getDisplayStatus())) {
            return "No feedback yet.";
        }
        StringBuilder builder = new StringBuilder("Rework feedback:\n");
        for (ReviewerVoteItem vote : currentTask.getReviewerVotes()) {
            if (vote.hasVoted()) {
                builder.append("- ").append(vote.getReviewerName()).append(": ").append(vote.getComments()).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private void setupListeners() {
        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnClear.setOnClickListener(v -> drawingView.clear());
        btnSave.setOnClickListener(v -> saveTask(TaskDbHelper.STATUS_IN_PROGRESS));
        btnSubmit.setOnClickListener(v -> saveTask(TaskDbHelper.STATUS_SUBMITTED));
    }

    private void saveTask(String status) {
        String annotationJson = drawingView.getBoxesAsJson();
        if (annotationJson.equals("[]")) {
            Toast.makeText(this, "Please draw at least one box", Toast.LENGTH_SHORT).show();
            return;
        }

        int result = taskDbHelper.saveAnnotationForLogicalTask(currentTask.getReferenceTaskId(), annotationJson, status);
        if (result > 0) {
            Toast.makeText(this, status.equals(TaskDbHelper.STATUS_SUBMITTED) ? "Submitted!" : "Saved draft", Toast.LENGTH_SHORT).show();
            if (status.equals(TaskDbHelper.STATUS_SUBMITTED)) finish();
        } else {
            Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show();
        }
    }
}
