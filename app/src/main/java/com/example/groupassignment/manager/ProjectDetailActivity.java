package com.example.groupassignment.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.reviewer.model.TaskAnnotationItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_DETAIL = "extra_project_detail";
    public static final String EXTRA_DETAIL_ACTION = "extra_detail_action";
    public static final String ACTION_UPDATED = "updated";
    public static final String ACTION_DELETED = "deleted";

    private ImageButton btnBackDetail;
    private Button btnEditProjectDetail;
    private Button btnDeleteProjectDetail;
    private TextView tvProjectNameDetail;
    private TextView tvProjectDescriptionDetail;
    private TextView tvStatusDetail;
    private TextView tvReviewStatusDetail;
    private TextView tvLastUpdatedDetail;
    private TextView tvGuidelinesDetail;
    private TextView tvReviewModeDetail;
    private TextView tvSampleRateDetail;
    private TextView tvDeadlineDetail;
    private TextView tvExportFormatDetail;
    private TextView tvReviewerCountDetail;
    private TextView tvAnnotatorCountDetail;
    private TextView tvTaskSummaryDetail;
    private TextView tvProgressDetail;
    private TextView tvLabelAnalyticsDetail;
    private LinearLayout layoutLabelsDetail;
    private LinearLayout layoutDatasetsDetail;
    private LinearLayout layoutAnnotatorsDetail;
    private LinearLayout layoutReviewersDetail;

    private ProjectItem currentProject;
    private ProjectDbHelper projectDbHelper;
    private SessionManager sessionManager;
    private DatasetDbHelper datasetDbHelper;
    private TaskDbHelper taskDbHelper;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ProjectItem updatedProject;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        updatedProject = data.getSerializableExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, ProjectItem.class);
                    } else {
                        updatedProject = (ProjectItem) data.getSerializableExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT);
                    }
                    if (updatedProject != null) {
                        currentProject = updatedProject;
                        bindProjectData(updatedProject);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, updatedProject);
                        resultIntent.putExtra(EXTRA_DETAIL_ACTION, ACTION_UPDATED);
                        setResult(RESULT_OK, resultIntent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        projectDbHelper = new ProjectDbHelper(this);
        datasetDbHelper = new DatasetDbHelper(this);
        taskDbHelper = new TaskDbHelper(this);
        initViews();
        readIntentData();
        btnBackDetail.setOnClickListener(v -> finish());
        btnEditProjectDetail.setOnClickListener(v -> openEdit());
        btnDeleteProjectDetail.setOnClickListener(v -> showDeleteConfirmDialog());
        bindProjectData(currentProject);
    }

    private void initViews() {
        btnBackDetail = findViewById(R.id.btnBackDetail);
        btnEditProjectDetail = findViewById(R.id.btnEditProjectDetail);
        btnDeleteProjectDetail = findViewById(R.id.btnDeleteProjectDetail);
        tvProjectNameDetail = findViewById(R.id.tvProjectNameDetail);
        tvProjectDescriptionDetail = findViewById(R.id.tvProjectDescriptionDetail);
        tvStatusDetail = findViewById(R.id.tvStatusDetail);
        tvReviewStatusDetail = findViewById(R.id.tvReviewStatusDetail);
        tvLastUpdatedDetail = findViewById(R.id.tvLastUpdatedDetail);
        tvGuidelinesDetail = findViewById(R.id.tvGuidelinesDetail);
        tvReviewModeDetail = findViewById(R.id.tvReviewModeDetail);
        tvSampleRateDetail = findViewById(R.id.tvSampleRateDetail);
        tvDeadlineDetail = findViewById(R.id.tvDeadlineDetail);
        tvExportFormatDetail = findViewById(R.id.tvExportFormatDetail);
        tvReviewerCountDetail = findViewById(R.id.tvReviewerCountDetail);
        tvAnnotatorCountDetail = findViewById(R.id.tvAnnotatorCountDetail);
        tvTaskSummaryDetail = findViewById(R.id.tvTaskSummaryDetail);
        tvProgressDetail = findViewById(R.id.tvProgressDetail);
        tvLabelAnalyticsDetail = findViewById(R.id.tvLabelAnalyticsDetail);
        layoutLabelsDetail = findViewById(R.id.layoutLabelsDetail);
        layoutDatasetsDetail = findViewById(R.id.layoutDatasetsDetail);
        layoutAnnotatorsDetail = findViewById(R.id.layoutAnnotatorsDetail);
        layoutReviewersDetail = findViewById(R.id.layoutReviewersDetail);
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentProject = intent.getSerializableExtra(EXTRA_PROJECT_DETAIL, ProjectItem.class);
        } else {
            currentProject = (ProjectItem) intent.getSerializableExtra(EXTRA_PROJECT_DETAIL);
        }
        if (currentProject == null) {
            Toast.makeText(this, "Project data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openEdit() {
        if (currentProject == null) {
            return;
        }
        Intent intent = new Intent(this, CreateProjectActivity.class);
        intent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, ManagerProjectsActivity.MODE_EDIT);
        intent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, currentProject);
        editLauncher.launch(intent);
    }

    private void showDeleteConfirmDialog() {
        if (currentProject == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete project")
                .setMessage("Are you sure you want to delete \"" + safeText(currentProject.getName()) + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentProject() {
        if (currentProject == null) return;
        int deletedRows = projectDbHelper.deleteProjectById(currentProject.getId());
        if (deletedRows > 0) {
            datasetDbHelper.releaseDatasetsByIds(currentProject.getDatasetIds(), currentProject.getId());
            taskDbHelper.deleteTasksByProjectId(currentProject.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, currentProject);
            resultIntent.putExtra(EXTRA_DETAIL_ACTION, ACTION_DELETED);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void bindProjectData(ProjectItem project) {
        if (project == null) {
            return;
        }
        taskDbHelper.refreshExpiredTasks();
        List<LogicalTaskItem> tasks = taskDbHelper.getLogicalTasksForProject(project.getId());
        List<TaskAnnotationItem> annotations = taskDbHelper.getProjectAnnotations(project.getId());

        tvProjectNameDetail.setText(safeText(project.getName()));
        tvProjectDescriptionDetail.setText(safeText(project.getDescription()));
        tvStatusDetail.setText((project.getStatus() == null ? "draft" : project.getStatus()).toUpperCase(Locale.getDefault()));
        tvReviewStatusDetail.setText("FLOW: " + buildProjectFlowStatus(tasks).toUpperCase(Locale.getDefault()));
        tvLastUpdatedDetail.setText("Last updated: " + safeText(project.getLastUpdated()));
        tvGuidelinesDetail.setText("Guidelines: " + safeText(project.getGuidelines()));
        tvReviewModeDetail.setText("Review mode: " + safeText(project.getReviewMode()));
        tvSampleRateDetail.setText("Sample rate: " + project.getSampleRate());
        tvDeadlineDetail.setText("Deadline: " + safeText(project.getDeadline()));
        tvExportFormatDetail.setText("Export format: " + safeText(project.getExportFormat()));
        tvReviewerCountDetail.setText("Reviewer count: " + project.getReviewerCount() + " (odd-majority required)");
        tvAnnotatorCountDetail.setText("Annotator count: " + project.getAnnotatorCount());

        Map<String, Integer> taskSummary = buildTaskSummary(tasks);
        int total = tasks.size();
        int completed = taskSummary.get("approved_final") + taskSummary.get("rejected_final") + taskSummary.get("overdue");
        int progress = total == 0 ? 0 : Math.round((completed * 100f) / total);
        tvTaskSummaryDetail.setText(buildTaskSummaryText(taskSummary, total));
        tvProgressDetail.setText("Project completion: " + progress + "%\n" + buildReviewerActivitySummary(tasks));
        tvLabelAnalyticsDetail.setText(buildLabelAnalytics(project, tasks, annotations));

        renderList(layoutLabelsDetail, project.getLabels(), false);
        renderList(layoutDatasetsDetail, project.getDatasets(), false);
        renderList(layoutAnnotatorsDetail, project.getAnnotators(), true);
        renderList(layoutReviewersDetail, project.getReviewers(), false);
    }

    private String buildProjectFlowStatus(List<LogicalTaskItem> tasks) {
        if (tasks.isEmpty()) {
            return TaskDbHelper.STATUS_ASSIGNED;
        }
        boolean hasRework = false;
        boolean hasUnderReview = false;
        boolean hasInProgress = false;
        for (LogicalTaskItem task : tasks) {
            String status = task.getDisplayStatus();
            if (TaskDbHelper.STATUS_REWORK_REQUIRED.equals(status)) {
                hasRework = true;
            } else if (TaskDbHelper.STATUS_SUBMITTED.equals(status) || TaskDbHelper.STATUS_UNDER_REVIEW.equals(status)) {
                hasUnderReview = true;
            } else if (TaskDbHelper.STATUS_ASSIGNED.equals(status) || TaskDbHelper.STATUS_IN_PROGRESS.equals(status)) {
                hasInProgress = true;
            }
        }
        if (hasRework) return TaskDbHelper.STATUS_REWORK_REQUIRED;
        if (hasUnderReview) return TaskDbHelper.STATUS_UNDER_REVIEW;
        if (hasInProgress) return TaskDbHelper.STATUS_IN_PROGRESS;
        return tasks.get(0).getDisplayStatus();
    }

    private Map<String, Integer> buildTaskSummary(List<LogicalTaskItem> tasks) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("in_progress", 0);
        summary.put("under_review", 0);
        summary.put("rework_required", 0);
        summary.put("approved_final", 0);
        summary.put("rejected_final", 0);
        summary.put("overdue", 0);
        for (LogicalTaskItem task : tasks) {
            String status = task.getDisplayStatus();
            if (TaskDbHelper.STATUS_ASSIGNED.equals(status) || TaskDbHelper.STATUS_IN_PROGRESS.equals(status)) {
                summary.put("in_progress", summary.get("in_progress") + 1);
            } else if (TaskDbHelper.STATUS_SUBMITTED.equals(status) || TaskDbHelper.STATUS_UNDER_REVIEW.equals(status)) {
                summary.put("under_review", summary.get("under_review") + 1);
            } else if (TaskDbHelper.STATUS_REWORK_REQUIRED.equals(status)) {
                summary.put("rework_required", summary.get("rework_required") + 1);
            } else if (TaskDbHelper.STATUS_APPROVED_FINAL.equals(status)) {
                summary.put("approved_final", summary.get("approved_final") + 1);
            } else if (TaskDbHelper.STATUS_REJECTED_FINAL.equals(status)) {
                summary.put("rejected_final", summary.get("rejected_final") + 1);
            } else if (TaskDbHelper.STATUS_OVERDUE.equals(status)) {
                summary.put("overdue", summary.get("overdue") + 1);
            }
        }
        return summary;
    }

    private String buildTaskSummaryText(Map<String, Integer> summary, int total) {
        return "Total tasks: " + total
                + "\nIn progress: " + summary.get("in_progress")
                + "\nUnder review: " + summary.get("under_review")
                + "\nRework required: " + summary.get("rework_required")
                + "\nApproved final: " + summary.get("approved_final")
                + "\nRejected final: " + summary.get("rejected_final")
                + "\nOverdue / auto rejected: " + summary.get("overdue");
    }

    private String buildReviewerActivitySummary(List<LogicalTaskItem> tasks) {
        Map<String, int[]> reviewerStats = new LinkedHashMap<>();
        for (LogicalTaskItem task : tasks) {
            for (ReviewerVoteItem vote : task.getReviewerVotes()) {
                String reviewerName = safeText(vote.getReviewerName());
                if (!reviewerStats.containsKey(reviewerName)) {
                    reviewerStats.put(reviewerName, new int[]{0, 0, 0});
                }
                int[] stats = reviewerStats.get(reviewerName);
                if (TaskDbHelper.VOTE_APPROVE.equalsIgnoreCase(vote.getDecision())) {
                    stats[0]++;
                } else if (TaskDbHelper.VOTE_REJECT.equalsIgnoreCase(vote.getDecision())) {
                    stats[1]++;
                } else {
                    stats[2]++;
                }
            }
        }
        if (reviewerStats.isEmpty()) {
            return "No reviewer activity yet.";
        }
        StringBuilder builder = new StringBuilder("Reviewer activity:");
        for (Map.Entry<String, int[]> entry : reviewerStats.entrySet()) {
            int[] stats = entry.getValue();
            builder.append("\n• ").append(entry.getKey())
                    .append(" → approve ").append(stats[0])
                    .append(", reject ").append(stats[1])
                    .append(", pending ").append(stats[2]);
        }
        return builder.toString();
    }

    private String buildLabelAnalytics(ProjectItem project, List<LogicalTaskItem> tasks, List<TaskAnnotationItem> annotations) {
        Map<String, int[]> analytics = new LinkedHashMap<>();
        Map<String, List<String>> detailLines = new LinkedHashMap<>();
        for (String label : project.getLabels()) {
            analytics.put(label, new int[]{0, 0, 0});
            detailLines.put(label, new ArrayList<>());
        }
        for (TaskAnnotationItem annotation : annotations) {
            String labelName = annotation.getLabelName();
            if (!analytics.containsKey(labelName)) {
                analytics.put(labelName, new int[]{0, 0, 0});
                detailLines.put(labelName, new ArrayList<>());
            }
            int[] counts = analytics.get(labelName);
            String finalStatus = annotation.getFinalStatus();
            if (TaskDbHelper.STATUS_APPROVED_FINAL.equals(finalStatus)) {
                counts[0]++;
            } else if (TaskDbHelper.STATUS_REJECTED_FINAL.equals(finalStatus) || TaskDbHelper.STATUS_OVERDUE.equals(finalStatus)) {
                counts[1]++;
            } else {
                counts[2]++;
            }
            detailLines.get(labelName).add("task#" + annotation.getTaskId()
                    + " / " + safeText(annotation.getDatasetItemName())
                    + " / annotator=" + safeText(annotation.getAnnotatorName())
                    + " / reviews=" + safeText(annotation.getReviewerSummary()));
        }
        if (analytics.isEmpty()) {
            return "No labels captured yet from task annotations.";
        }
        StringBuilder builder = new StringBuilder("Structured label audit:");
        for (Map.Entry<String, int[]> entry : analytics.entrySet()) {
            int[] counts = entry.getValue();
            builder.append("\n• ").append(entry.getKey())
                    .append(" → approved ").append(counts[0])
                    .append(", rejected ").append(counts[1])
                    .append(", pending ").append(counts[2]);
            List<String> samples = detailLines.get(entry.getKey());
            if (samples != null) {
                for (int i = 0; i < Math.min(3, samples.size()); i++) {
                    builder.append("\n   - ").append(samples.get(i));
                }
            }
        }
        if (tasks.isEmpty()) {
            builder.append("\nNo logical tasks have been created yet. Add dataset items to generate work units.");
        }
        return builder.toString();
    }

    private void renderList(LinearLayout container, List<String> items, boolean auditClickable) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No data");
            empty.setTextColor(0xFF94A3B8);
            container.addView(empty);
            return;
        }
        for (String item : items) {
            TextView textView = new TextView(this);
            textView.setText(item + (auditClickable ? " • Audit" : ""));
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(dp(12), dp(8), dp(12), dp(8));
            textView.setBackgroundColor(0xFF1F2937);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(8);
            textView.setLayoutParams(params);
            if (auditClickable) {
                textView.setOnClickListener(v -> openAnnotatorAudit(item));
            }
            container.addView(textView);
        }
    }

    private void openAnnotatorAudit(String annotatorName) {
        Intent intent = new Intent(this, AnnotatorAuditDetailActivity.class);
        intent.putExtra(AnnotatorAuditDetailActivity.EXTRA_PROJECT_ID, currentProject.getId());
        intent.putExtra(AnnotatorAuditDetailActivity.EXTRA_PROJECT_NAME, currentProject.getName());
        intent.putExtra(AnnotatorAuditDetailActivity.EXTRA_ANNOTATOR_NAME, annotatorName);
        startActivity(intent);
    }

    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "Not set" : text;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
