package com.example.groupassignment.manager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectsActivity extends AppCompatActivity {

    private EditText edtSearchProjects;
    private Button btnNewProject;
    private RecyclerView rvProjects;
    private TextView tvProjectsFooter;

    private final List<ProjectItem> allProjects = new ArrayList<>();
    private final List<ProjectItem> filteredProjects = new ArrayList<>();
    private ProjectsAdapter projectsAdapter;

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

        allProjects.add(new ProjectItem(
                "Medical Image Labeling",
                "Segmentation workflow for radiology dataset",
                "active",
                "Anna",
                "John",
                "13 Mar 2026"
        ));

        allProjects.add(new ProjectItem(
                "Retail Product Detection",
                "Bounding box labeling for shelf products",
                "completed",
                "David",
                "Lina",
                "11 Mar 2026"
        ));

        allProjects.add(new ProjectItem(
                "Autonomous Driving Dataset",
                "Lane and object annotation for street scenes",
                "archived",
                "Sophia",
                "Michael",
                "08 Mar 2026"
        ));

        allProjects.add(new ProjectItem(
                "Customer Support Intent Tagging",
                "NLP intent classification dataset",
                "pending",
                "Emma",
                "Chris",
                "10 Mar 2026"
        ));

        filteredProjects.clear();
        filteredProjects.addAll(allProjects);
        projectsAdapter.notifyDataSetChanged();
    }

    private void setupActions() {
        btnNewProject.setOnClickListener(v ->
                Toast.makeText(this, "Open Create Project screen", Toast.LENGTH_SHORT).show()
        );

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
                if (item.getName().toLowerCase(Locale.ROOT).contains(lower)
                        || item.getDescription().toLowerCase(Locale.ROOT).contains(lower)) {
                    filteredProjects.add(item);
                }
            }
        }

        projectsAdapter.notifyDataSetChanged();
        updateFooter();
    }

    private void updateFooter() {
        tvProjectsFooter.setText("Showing " + filteredProjects.size() + " of " + allProjects.size() + " projects");
    }
}