package com.example.groupassignment.manager;

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
import com.example.groupassignment.manager.model.ProjectItem;

import java.util.List;
import java.util.Locale;

public class ProjectDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_DETAIL = "extra_project_detail";

    private ImageButton btnBackDetail;
    private Button btnEditProjectDetail;

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

    private LinearLayout layoutLabelsDetail;
    private LinearLayout layoutDatasetsDetail;
    private LinearLayout layoutAnnotatorsDetail;
    private LinearLayout layoutReviewersDetail;

    private ProjectItem currentProject;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    ProjectItem updatedProject;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        updatedProject = data.getSerializableExtra(
                                ManagerProjectsActivity.EXTRA_PROJECT_RESULT,
                                ProjectItem.class
                        );
                    } else {
                        updatedProject = (ProjectItem) data.getSerializableExtra(
                                ManagerProjectsActivity.EXTRA_PROJECT_RESULT
                        );
                    }

                    String mode = data.getStringExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE);

                    if (updatedProject != null) {
                        currentProject = updatedProject;
                        bindProjectData(updatedProject);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, updatedProject);
                        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, mode);
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        initViews();
        readIntentData();
        setupActions();
        bindProjectData(currentProject);
    }

    private void initViews() {
        btnBackDetail = findViewById(R.id.btnBackDetail);
        btnEditProjectDetail = findViewById(R.id.btnEditProjectDetail);

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

    private void setupActions() {
        btnBackDetail.setOnClickListener(v -> finish());

        btnEditProjectDetail.setOnClickListener(v -> {
            if (currentProject == null) return;

            Intent intent = new Intent(ProjectDetailActivity.this, CreateProjectActivity.class);
            intent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, ManagerProjectsActivity.MODE_EDIT);
            intent.putExtra("project_edit_data", currentProject);
            editLauncher.launch(intent);
        });
    }

    private void bindProjectData(ProjectItem project) {
        if (project == null) return;

        tvProjectNameDetail.setText(safeText(project.getName()));
        tvProjectDescriptionDetail.setText(safeText(project.getDescription()));
        tvLastUpdatedDetail.setText("Last updated: " + safeText(project.getLastUpdated()));

        tvStatusDetail.setText(project.getStatus() == null
                ? "UNKNOWN"
                : project.getStatus().toUpperCase(Locale.getDefault()));
        tvReviewStatusDetail.setText(project.getReviewStatus() == null
                ? "REVIEW: UNKNOWN"
                : "REVIEW: " + project.getReviewStatus().toUpperCase(Locale.getDefault()));

        tvStatusDetail.setTextColor(getStatusText(project.getStatus()));
        tvStatusDetail.setBackground(makeRoundedDrawable(getStatusBg(project.getStatus())));

        tvReviewStatusDetail.setTextColor(getReviewText(project.getReviewStatus()));
        tvReviewStatusDetail.setBackground(makeRoundedDrawable(getReviewBg(project.getReviewStatus())));

        tvGuidelinesDetail.setText(safeText(project.getGuidelines()));
        tvReviewModeDetail.setText(safeText(project.getReviewMode()));
        tvSampleRateDetail.setText(String.valueOf(project.getSampleRate()));
        tvDeadlineDetail.setText(safeText(project.getDeadline()));
        tvExportFormatDetail.setText(safeText(project.getExportFormat()));
        tvReviewerCountDetail.setText(String.valueOf(project.getReviewerCount()));
        tvAnnotatorCountDetail.setText(String.valueOf(project.getAnnotatorCount()));

        renderChips(layoutLabelsDetail, project.getLabels(), true);
        renderChips(layoutDatasetsDetail, project.getDatasets(), false);
        renderChips(layoutAnnotatorsDetail, project.getAnnotators(), false);
        renderChips(layoutReviewersDetail, project.getReviewers(), false);
    }

    private void renderChips(LinearLayout container, List<String> items, boolean compact) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No data");
            tvEmpty.setTextColor(0xFF94A3B8);
            tvEmpty.setTextSize(13f);
            container.addView(tvEmpty);
            return;
        }

        for (String item : items) {
            TextView chip = new TextView(this);
            chip.setText(item);
            chip.setTextColor(0xFFFFFFFF);
            chip.setTextSize(compact ? 12f : 13f);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setBackground(makeRoundedDrawable(compact ? 0xFF2563EB : 0xFF334155));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dp(8), dp(8));
            chip.setLayoutParams(params);

            container.addView(chip);
        }
    }

    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "Not set" : text;
    }

    private android.graphics.drawable.GradientDrawable makeRoundedDrawable(int color) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(999));
        return drawable;
    }

    private int getStatusBg(String status) {
        if (status == null) return 0x26F59E0B;

        switch (status) {
            case "active":
                return 0x2622C55E;
            case "completed":
                return 0x263B82F6;
            case "archived":
                return 0x269CA3AF;
            case "draft":
            default:
                return 0x26F59E0B;
        }
    }

    private int getStatusText(String status) {
        if (status == null) return 0xFFF59E0B;

        switch (status) {
            case "active":
                return 0xFF4ADE80;
            case "completed":
                return 0xFF60A5FA;
            case "archived":
                return 0xFF9CA3AF;
            case "draft":
            default:
                return 0xFFF59E0B;
        }
    }

    private int getReviewBg(String status) {
        if (status == null) return 0x26F59E0B;

        switch (status) {
            case "approved":
                return 0x2610B981;
            case "rejected":
                return 0x26EF4444;
            case "pending":
            default:
                return 0x26F59E0B;
        }
    }

    private int getReviewText(String status) {
        if (status == null) return 0xFFFBBF24;

        switch (status) {
            case "approved":
                return 0xFF34D399;
            case "rejected":
                return 0xFFF87171;
            case "pending":
            default:
                return 0xFFFBBF24;
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}