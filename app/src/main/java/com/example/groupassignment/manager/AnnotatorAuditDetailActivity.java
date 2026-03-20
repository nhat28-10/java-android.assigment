package com.example.groupassignment.manager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnnotatorAuditDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "extra_project_id";
    public static final String EXTRA_PROJECT_NAME = "extra_project_name";
    public static final String EXTRA_ANNOTATOR_NAME = "extra_annotator_name";

    private ImageButton btnBackAuditDetail;
    private TextView tvAuditBreadcrumbProject;
    private TextView tvAuditTitle;
    private TextView tvAnnotatorAvatar;
    private TextView tvAnnotatorName;
    private TextView tvAnnotatorTaskCount;
    private EditText etSearchAuditTask;
    private Spinner spAuditStatusFilter;
    private Spinner spAuditReviewFilter;
    private LinearLayout layoutAuditTaskList;
    private TextView tvEmptyAuditTasks;

    private int projectId;
    private String projectName = "Project";
    private String annotatorName = "Annotator";
    private final List<LogicalTaskItem> allTasks = new ArrayList<>();
    private SessionManager sessionManager;
    private TaskDbHelper taskDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_audit_detail);
        sessionManager = new SessionManager(this);
        taskDbHelper = new TaskDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        initViews();
        readIntentData();
        setupSpinners();
        bindHeader();
        loadTasks();
        setupActions();
        renderTaskList();
    }

    private void initViews() {
        btnBackAuditDetail = findViewById(R.id.btnBackAuditDetail);
        tvAuditBreadcrumbProject = findViewById(R.id.tvAuditBreadcrumbProject);
        tvAuditTitle = findViewById(R.id.tvAuditTitle);
        tvAnnotatorAvatar = findViewById(R.id.tvAnnotatorAvatar);
        tvAnnotatorName = findViewById(R.id.tvAnnotatorName);
        tvAnnotatorTaskCount = findViewById(R.id.tvAnnotatorTaskCount);
        etSearchAuditTask = findViewById(R.id.etSearchAuditTask);
        spAuditStatusFilter = findViewById(R.id.spAuditStatusFilter);
        spAuditReviewFilter = findViewById(R.id.spAuditReviewFilter);
        layoutAuditTaskList = findViewById(R.id.layoutAuditTaskList);
        tvEmptyAuditTasks = findViewById(R.id.tvEmptyAuditTasks);
    }

    private void readIntentData() {
        if (getIntent() == null) {
            return;
        }
        projectId = getIntent().getIntExtra(EXTRA_PROJECT_ID, 0);
        String projectExtra = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
        String annotatorExtra = getIntent().getStringExtra(EXTRA_ANNOTATOR_NAME);
        if (!TextUtils.isEmpty(projectExtra)) {
            projectName = projectExtra;
        }
        if (!TextUtils.isEmpty(annotatorExtra)) {
            annotatorName = annotatorExtra;
        }
    }

    private void setupSpinners() {
        spAuditStatusFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All Status", "Assigned", "In Progress", "Under Review", "Rework Required", "Approved Final", "Rejected Final", "Overdue"}));
        spAuditReviewFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All Results", "Approved", "Rejected", "Pending Review"}));
    }

    private void bindHeader() {
        tvAuditBreadcrumbProject.setText(projectName);
        tvAuditTitle.setText("Annotator Audit: " + annotatorName);
        tvAnnotatorAvatar.setText(annotatorName.substring(0, 1).toUpperCase(Locale.getDefault()));
        tvAnnotatorName.setText(annotatorName);
    }

    private void loadTasks() {
        taskDbHelper.refreshExpiredTasks();
        allTasks.clear();
        List<LogicalTaskItem> projectTasks = taskDbHelper.getLogicalTasksForProject(projectId);
        for (LogicalTaskItem item : projectTasks) {
            if (annotatorName.equalsIgnoreCase(item.getAnnotatorName())) {
                allTasks.add(item);
            }
        }
        tvAnnotatorTaskCount.setText("Task count: " + allTasks.size());
    }

    private void setupActions() {
        btnBackAuditDetail.setOnClickListener(v -> finish());
        etSearchAuditTask.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderTaskList(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        spAuditStatusFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(this::renderTaskList));
        spAuditReviewFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(this::renderTaskList));
    }

    private void renderTaskList() {
        layoutAuditTaskList.removeAllViews();
        List<LogicalTaskItem> filtered = getFilteredTasks();
        if (filtered.isEmpty()) {
            tvEmptyAuditTasks.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyAuditTasks.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (LogicalTaskItem item : filtered) {
            View card = inflater.inflate(R.layout.item_annotator_audit_task, layoutAuditTaskList, false);
            TextView tvTaskId = card.findViewById(R.id.tvAuditTaskId);
            TextView tvTaskPreview = card.findViewById(R.id.tvAuditTaskPreview);
            TextView tvLabelType = card.findViewById(R.id.tvAuditLabelType);
            TextView tvAnnotatorStatus = card.findViewById(R.id.tvAuditAnnotatorStatus);
            TextView tvReviewResult = card.findViewById(R.id.tvAuditReviewResult);
            Button btnViewReviewDetail = card.findViewById(R.id.btnViewReviewDetail);
            tvTaskId.setText(item.getDatasetName() + " • Round " + item.getRoundNumber());
            tvTaskPreview.setText("Project: " + item.getProjectName());
            tvLabelType.setText(TextUtils.isEmpty(item.getLabelsRaw()) ? "No labels" : item.getLabelsRaw().replace("||", ", "));
            tvAnnotatorStatus.setText(item.getDisplayStatus().toUpperCase(Locale.getDefault()));
            tvReviewResult.setText(item.getApproveCount() + " approve / " + item.getRejectCount() + " reject / " + item.getPendingVotes() + " pending");
            btnViewReviewDetail.setOnClickListener(v -> showTaskDetailDialog(item));
            layoutAuditTaskList.addView(card);
        }
    }

    private List<LogicalTaskItem> getFilteredTasks() {
        List<LogicalTaskItem> filtered = new ArrayList<>();
        String query = etSearchAuditTask.getText().toString().trim().toLowerCase(Locale.getDefault());
        String statusFilter = getSelectedStatusFilter();
        String reviewFilter = getSelectedReviewFilter();
        for (LogicalTaskItem item : allTasks) {
            String displayStatus = item.getDisplayStatus();
            if (!"all".equals(statusFilter) && !statusFilter.equals(displayStatus)) {
                continue;
            }
            if (!"all".equals(reviewFilter)) {
                if ("approved".equals(reviewFilter) && !TaskDbHelper.STATUS_APPROVED_FINAL.equals(displayStatus)) {
                    continue;
                }
                if ("rejected".equals(reviewFilter) && !(TaskDbHelper.STATUS_REJECTED_FINAL.equals(displayStatus) || TaskDbHelper.STATUS_OVERDUE.equals(displayStatus))) {
                    continue;
                }
                if ("pending".equals(reviewFilter) && (TaskDbHelper.STATUS_APPROVED_FINAL.equals(displayStatus) || TaskDbHelper.STATUS_REJECTED_FINAL.equals(displayStatus) || TaskDbHelper.STATUS_OVERDUE.equals(displayStatus))) {
                    continue;
                }
            }
            if (!query.isEmpty()) {
                String dataset = item.getDatasetName().toLowerCase(Locale.getDefault());
                String annotation = item.getAnnotationResult().toLowerCase(Locale.getDefault());
                if (!dataset.contains(query) && !annotation.contains(query)) {
                    continue;
                }
            }
            filtered.add(item);
        }
        return filtered;
    }

    private String getSelectedStatusFilter() {
        switch (spAuditStatusFilter.getSelectedItemPosition()) {
            case 1: return TaskDbHelper.STATUS_ASSIGNED;
            case 2: return TaskDbHelper.STATUS_IN_PROGRESS;
            case 3: return TaskDbHelper.STATUS_UNDER_REVIEW;
            case 4: return TaskDbHelper.STATUS_REWORK_REQUIRED;
            case 5: return TaskDbHelper.STATUS_APPROVED_FINAL;
            case 6: return TaskDbHelper.STATUS_REJECTED_FINAL;
            case 7: return TaskDbHelper.STATUS_OVERDUE;
            default: return "all";
        }
    }

    private String getSelectedReviewFilter() {
        switch (spAuditReviewFilter.getSelectedItemPosition()) {
            case 1: return "approved";
            case 2: return "rejected";
            case 3: return "pending";
            default: return "all";
        }
    }

    private void showTaskDetailDialog(LogicalTaskItem item) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        root.setPadding(padding, padding, padding, padding);

        addDialogText(root, "Project: " + item.getProjectName(), true);
        addDialogText(root, "Dataset: " + item.getDatasetName(), false);
        addDialogText(root, "Annotator: " + item.getAnnotatorName(), false);
        addDialogText(root, "Round: " + item.getRoundNumber(), false);
        addDialogText(root, "Status: " + item.getDisplayStatus(), false);
        addDialogText(root, "Deadline: " + (TextUtils.isEmpty(item.getDeadline()) ? "Not set" : item.getDeadline()), false);
        addDialogText(root, "Guideline: " + (TextUtils.isEmpty(item.getGuidelines()) ? "N/A" : item.getGuidelines()), false);
        addDialogText(root, "Selected labels / annotation: " + (TextUtils.isEmpty(item.getAnnotationResult()) ? "N/A" : item.getAnnotationResult().replace("||", ", ")), false);

        StringBuilder voteBuilder = new StringBuilder("Reviewer votes:");
        for (ReviewerVoteItem vote : item.getReviewerVotes()) {
            voteBuilder.append("\n• ").append(vote.getReviewerName())
                    .append(" → ").append(TextUtils.isEmpty(vote.getDecision()) ? "pending" : vote.getDecision());
            if (!TextUtils.isEmpty(vote.getComments())) {
                voteBuilder.append(" | comment: ").append(vote.getComments());
            }
            if (!TextUtils.isEmpty(vote.getRejectionReason())) {
                voteBuilder.append(" | reason: ").append(vote.getRejectionReason());
            }
        }
        addDialogText(root, voteBuilder.toString(), false);

        scrollView.addView(root);
        new AlertDialog.Builder(this)
                .setTitle("Audit Detail")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void addDialogText(LinearLayout root, String text, boolean title) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(0xFF0F172A);
        textView.setTextSize(title ? 18f : 14f);
        if (!title) {
            textView.setPadding(0, dp(8), 0, 0);
        }
        root.addView(textView);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable runnable;

        SimpleItemSelectedListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            runnable.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }

}
