package com.example.groupassignment.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.model.ProjectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectsActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_RESULT = "extra_project_result";
    public static final String EXTRA_PROJECT_MODE = "extra_project_mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    private EditText edtSearchProjects;
    private Button btnNewProject;
    private RecyclerView rvProjects;
    private TextView tvProjectsFooter;

    private final List<ProjectItem> allProjects = new ArrayList<>();
    private final List<ProjectItem> filteredProjects = new ArrayList<>();
    private ProjectsAdapter projectsAdapter;

    private final ActivityResultLauncher<Intent> projectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ProjectItem project = (ProjectItem) data.getSerializableExtra(EXTRA_PROJECT_RESULT);
                    String mode = data.getStringExtra(EXTRA_PROJECT_MODE);

                    if (project != null) {
                        if (MODE_EDIT.equals(mode)) {
                            updateExistingProject(project);
                            Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
                        } else {
                            allProjects.add(0, project);
                            Toast.makeText(this, "Project created", Toast.LENGTH_SHORT).show();
                        }
                        filterProjects(edtSearchProjects.getText().toString().trim());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        initViews();
        setupRecyclerView();
        loadMockProjects();
        setupActions();
        updateFooter();
    }

    private void initViews() {
        edtSearchProjects = findViewById(R.id.edtSearchProjects);
        btnNewProject = findViewById(R.id.btnNewProject);
        rvProjects = findViewById(R.id.rvProjects);
        tvProjectsFooter = findViewById(R.id.tvProjectsFooter);
    }

    private void setupRecyclerView() {
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        projectsAdapter = new ProjectsAdapter(filteredProjects);
        rvProjects.setAdapter(projectsAdapter);
    }

    private void loadMockProjects() {
        allProjects.clear();

        ProjectItem p1 = new ProjectItem(
                "Medical Image Labeling",
                "Segmentation workflow for radiology dataset",
                "active",
                "pending",
                3,
                5,
                "13 Mar 2026"
        );

        ProjectItem p2 = new ProjectItem(
                "Retail Product Detection",
                "Bounding box labeling for shelf products",
                "completed",
                "approved",
                5,
                7,
                "11 Mar 2026"
        );

        ProjectItem p3 = new ProjectItem(
                "Autonomous Driving Dataset",
                "Lane and object annotation for street scenes",
                "archived",
                "rejected",
                1,
                3,
                "08 Mar 2026"
        );

        ProjectItem p4 = new ProjectItem(
                "Customer Support Intent Tagging",
                "NLP intent classification dataset",
                "draft",
                "pending",
                3,
                5,
                "10 Mar 2026"
        );

        allProjects.add(p1);
        allProjects.add(p2);
        allProjects.add(p3);
        allProjects.add(p4);

        filteredProjects.clear();
        filteredProjects.addAll(allProjects);
        projectsAdapter.notifyDataSetChanged();
    }

    private void setupActions() {
        btnNewProject.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectsActivity.this, CreateProjectActivity.class);
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

    private void filterProjects(String keyword) {
        filteredProjects.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredProjects.addAll(allProjects);
        } else {
            String lower = keyword.toLowerCase(Locale.ROOT);
            for (ProjectItem item : allProjects) {
                String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
                String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase(Locale.ROOT);

                if (name.contains(lower) || description.contains(lower)) {
                    filteredProjects.add(item);
                }
            }
        }

        projectsAdapter.notifyDataSetChanged();
        updateFooter();
    }

    private void updateExistingProject(ProjectItem updatedProject) {
        for (int i = 0; i < allProjects.size(); i++) {
            ProjectItem current = allProjects.get(i);
            if (current.getName() != null &&
                    current.getName().equalsIgnoreCase(updatedProject.getName())) {
                allProjects.set(i, updatedProject);
                return;
            }
        }
        allProjects.add(0, updatedProject);
    }

    private void updateFooter() {
        tvProjectsFooter.setText("Showing " + filteredProjects.size() + " of " + allProjects.size() + " projects");
    }
}
