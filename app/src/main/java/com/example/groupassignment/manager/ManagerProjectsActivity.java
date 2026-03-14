package com.example.groupassignment.manager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.ProjectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagerProjectsActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_RESULT = "extra_project_result";
    public static final String EXTRA_PROJECT_MODE = "extra_project_mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    private ImageButton btnBack;
    private EditText edtSearchProjects;
    private Button btnNewProject;
    private LinearLayout layoutProjectsContainer;
    private TextView tvProjectsSummary;
    private TextView tvProjectsHint;

    private final List<ProjectItem> allProjects = new ArrayList<>();
    private final List<ProjectItem> filteredProjects = new ArrayList<>();

    private ProjectDbHelper projectDbHelper;

    private final ActivityResultLauncher<Intent> projectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    ProjectItem project;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        project = data.getSerializableExtra(EXTRA_PROJECT_RESULT, ProjectItem.class);
                    } else {
                        project = (ProjectItem) data.getSerializableExtra(EXTRA_PROJECT_RESULT);
                    }

                    String mode = data.getStringExtra(EXTRA_PROJECT_MODE);
                    String detailAction = data.getStringExtra(ProjectDetailActivity.EXTRA_DETAIL_ACTION);

                    if (ProjectDetailActivity.ACTION_DELETED.equals(detailAction) && project != null) {
                        removeProjectById(project.getId());
                        showToast("Project deleted");
                        filterProjects(edtSearchProjects.getText().toString().trim());
                        return;
                    }

                    if (ProjectDetailActivity.ACTION_UPDATED.equals(detailAction) && project != null) {
                        updateExistingProject(project);
                        showToast("Project updated");
                        filterProjects(edtSearchProjects.getText().toString().trim());
                        return;
                    }

                    if (project != null) {
                        if (MODE_EDIT.equals(mode)) {
                            updateExistingProject(project);
                            showToast("Project updated");
                        } else if (MODE_CREATE.equals(mode)) {
                            addOrReplaceProject(project);
                            showToast("Project created");
                        }
                        filterProjects(edtSearchProjects.getText().toString().trim());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_projects);

        projectDbHelper = new ProjectDbHelper(this);

        initViews();
        loadProjectsFromDatabase();
        setupActions();
        filterProjects("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjectsFromDatabase();
        filterProjects(edtSearchProjects.getText().toString().trim());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackProjects);
        edtSearchProjects = findViewById(R.id.edtSearchProjects);
        btnNewProject = findViewById(R.id.btnNewProject);
        layoutProjectsContainer = findViewById(R.id.layoutProjectsContainer);
        tvProjectsSummary = findViewById(R.id.tvProjectsSummary);
        tvProjectsHint = findViewById(R.id.tvProjectsHint);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnNewProject.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProjectsActivity.this, CreateProjectActivity.class);
            intent.putExtra(EXTRA_PROJECT_MODE, MODE_CREATE);
            projectLauncher.launch(intent);
        });

        edtSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void loadProjectsFromDatabase() {
        allProjects.clear();
        allProjects.addAll(projectDbHelper.getAllProjects());
    }

    private void addOrReplaceProject(ProjectItem project) {
        for (int i = 0; i < allProjects.size(); i++) {
            if (allProjects.get(i).getId() == project.getId()) {
                allProjects.set(i, project);
                return;
            }
        }
        allProjects.add(0, project);
    }

    private void updateExistingProject(ProjectItem updatedProject) {
        for (int i = 0; i < allProjects.size(); i++) {
            ProjectItem current = allProjects.get(i);
            if (current.getId() == updatedProject.getId()) {
                allProjects.set(i, updatedProject);
                return;
            }
        }
        allProjects.add(0, updatedProject);
    }

    private void removeProjectById(int projectId) {
        for (int i = 0; i < allProjects.size(); i++) {
            if (allProjects.get(i).getId() == projectId) {
                allProjects.remove(i);
                return;
            }
        }
    }

    private void filterProjects(String keyword) {
        filteredProjects.clear();

        String lower = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());

        if (lower.isEmpty()) {
            filteredProjects.addAll(allProjects);
        } else {
            for (ProjectItem item : allProjects) {
                String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.getDefault());
                String desc = item.getDescription() == null ? "" : item.getDescription().toLowerCase(Locale.getDefault());

                if (name.contains(lower) || desc.contains(lower)) {
                    filteredProjects.add(item);
                }
            }
        }

        renderProjects();
        tvProjectsSummary.setText("Showing " + filteredProjects.size() + " of " + allProjects.size() + " projects");
        tvProjectsHint.setText("Quản lý team & tiến độ labeling hiệu quả hơn.");
    }

    private void renderProjects() {
        layoutProjectsContainer.removeAllViews();

        if (filteredProjects.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Không có project nào phù hợp.");
            emptyView.setTextColor(0xFF9CA3AF);
            emptyView.setTextSize(15f);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(dp(12), dp(40), dp(12), dp(40));
            layoutProjectsContainer.addView(emptyView);
            return;
        }

        for (ProjectItem item : filteredProjects) {
            layoutProjectsContainer.addView(createProjectCard(item));
        }
    }

    private CardView createProjectCard(ProjectItem item) {
        CardView card = new CardView(this);
        card.setRadius(dp(18));
        card.setCardElevation(dp(3));
        card.setCardBackgroundColor(0xFF111827);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleWrap = new LinearLayout(this);
        titleWrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleWrapParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        titleWrap.setLayoutParams(titleWrapParams);

        TextView tvName = new TextView(this);
        tvName.setText(item.getName());
        tvName.setTextColor(0xFFE5E7EB);
        tvName.setTextSize(17f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvDescription = new TextView(this);
        tvDescription.setText(item.getDescription());
        tvDescription.setTextColor(0xFF9CA3AF);
        tvDescription.setTextSize(13f);
        tvDescription.setMaxLines(1);

        titleWrap.addView(tvName);
        titleWrap.addView(tvDescription);

        LinearLayout chipWrap = new LinearLayout(this);
        chipWrap.setOrientation(LinearLayout.VERTICAL);
        chipWrap.setGravity(Gravity.END);

        String statusText = item.getStatus() == null ? "UNKNOWN" : item.getStatus().toUpperCase(Locale.getDefault());
        String reviewText = item.getReviewStatus() == null
                ? "Review: UNKNOWN"
                : "Review: " + item.getReviewStatus().toUpperCase(Locale.getDefault());

        TextView tvStatusChip = buildChip(
                statusText,
                getStatusBg(item.getStatus()),
                getStatusText(item.getStatus())
        );
        TextView tvReviewChip = buildChip(
                reviewText,
                getReviewBg(item.getReviewStatus()),
                getReviewText(item.getReviewStatus())
        );

        chipWrap.addView(tvStatusChip);

        LinearLayout.LayoutParams reviewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        reviewParams.topMargin = dp(6);
        tvReviewChip.setLayoutParams(reviewParams);
        chipWrap.addView(tvReviewChip);

        topRow.addView(titleWrap);
        topRow.addView(chipWrap);

        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaParams.topMargin = dp(16);
        metaRow.setLayoutParams(metaParams);

        TextView tvReviewer = buildMetaText("Reviewer: " + item.getReviewerCount());
        TextView tvAnnotator = buildMetaText("Annotator: " + item.getAnnotatorCount());
        TextView tvUpdated = buildMetaText("Last updated: " + safeText(item.getLastUpdated()));

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tvReviewer.setLayoutParams(itemParams);
        tvAnnotator.setLayoutParams(itemParams);
        tvUpdated.setLayoutParams(itemParams);

        metaRow.addView(tvReviewer);
        metaRow.addView(tvAnnotator);
        metaRow.addView(tvUpdated);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.END);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        actionParams.topMargin = dp(14);
        actionRow.setLayoutParams(actionParams);

        Button btnView = new Button(this);
        btnView.setText("View");
        btnView.setTextColor(0xFFFFFFFF);
        btnView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF374151));

        Button btnEdit = new Button(this);
        btnEdit.setText("Edit");
        btnEdit.setTextColor(0xFFFFFFFF);
        btnEdit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));

        LinearLayout.LayoutParams btnParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams1.rightMargin = dp(8);
        btnView.setLayoutParams(btnParams1);

        actionRow.addView(btnView);
        actionRow.addView(btnEdit);

        root.addView(topRow);
        root.addView(metaRow);
        root.addView(actionRow);

        card.addView(root);

        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProjectsActivity.this, ProjectDetailActivity.class);
            intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_DETAIL, item);
            projectLauncher.launch(intent);
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProjectsActivity.this, CreateProjectActivity.class);
            intent.putExtra(EXTRA_PROJECT_MODE, MODE_EDIT);
            intent.putExtra(EXTRA_PROJECT_RESULT, item);
            projectLauncher.launch(intent);
        });

        return card;
    }

    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "Not set" : text;
    }

    private TextView buildChip(String text, int bgColor, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(textColor);
        tv.setPadding(dp(10), dp(6), dp(10), dp(6));
        tv.setBackground(makeRoundedDrawable(bgColor));
        return tv;
    }

    private TextView buildMetaText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFCBD5E1);
        tv.setTextSize(13f);
        return tv;
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
            default:
                return 0xFFFBBF24;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}