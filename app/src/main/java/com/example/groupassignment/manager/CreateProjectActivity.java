package com.example.groupassignment.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.data.TaskDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateProjectActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSaveDraft, btnCreateProject, btnAddLabel, btnPickDeadline;
    private EditText edtProjectName, edtDescription, edtGuidelines, edtLabelInput;
    private TextView tvHeadline, tvDeadlineValue, tvSelectedDatasetsCount, tvSelectedAnnotatorsCount, tvSelectedReviewersCount;
    private Spinner spinnerReviewMode, spinnerExportFormat;
    private LinearLayout layoutLabelsContainer, layoutDatasetContainer, layoutAnnotatorContainer, layoutReviewerContainer;

    private final List<String> labelList = new ArrayList<>();
    private final List<Integer> selectedDatasetIds = new ArrayList<>();
    private final List<Integer> selectedAnnotatorIds = new ArrayList<>();
    private final List<Integer> selectedReviewerIds = new ArrayList<>();

    private final List<String> availableDatasets = new ArrayList<>();
    private final Map<String, Integer> datasetNameToId = new HashMap<>();
    private final Map<String, Integer> annotatorNameToId = new HashMap<>();
    private final Map<String, Integer> reviewerNameToId = new HashMap<>();

    private ProjectDbHelper projectDbHelper;
    private DatasetDbHelper datasetDbHelper;
    private AuthDbHelper authDbHelper;
    private TaskDbHelper taskDbHelper;

    private int editingProjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        projectDbHelper = new ProjectDbHelper(this);
        datasetDbHelper = new DatasetDbHelper(this);
        authDbHelper = new AuthDbHelper(this);
        taskDbHelper = new TaskDbHelper(this);

        initViews();
        setupSpinners();
        loadResources();
        
        editingProjectId = getIntent().getIntExtra("project_id", -1);
        if (editingProjectId != -1) {
            setupEditMode();
        }

        setupActions();
        refreshAllUIs();
    }

    private void initViews() {
        tvHeadline = findViewById(R.id.tvHeadline);
        btnBack = findViewById(R.id.btnBackCreateProject);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnCreateProject = findViewById(R.id.btnCreateProject);
        btnAddLabel = findViewById(R.id.btnAddLabel);
        btnPickDeadline = findViewById(R.id.btnPickDeadline);
        edtProjectName = findViewById(R.id.edtProjectName);
        edtDescription = findViewById(R.id.edtProjectDescription);
        edtGuidelines = findViewById(R.id.edtProjectGuidelines);
        edtLabelInput = findViewById(R.id.edtLabelInput);
        tvDeadlineValue = findViewById(R.id.tvDeadlineValue);
        tvSelectedDatasetsCount = findViewById(R.id.tvSelectedDatasetsCount);
        tvSelectedAnnotatorsCount = findViewById(R.id.tvSelectedAnnotatorsCount);
        tvSelectedReviewersCount = findViewById(R.id.tvSelectedReviewersCount);
        spinnerReviewMode = findViewById(R.id.spinnerReviewMode);
        spinnerExportFormat = findViewById(R.id.spinnerExportFormat);
        layoutLabelsContainer = findViewById(R.id.layoutLabelsContainer);
        layoutDatasetContainer = findViewById(R.id.layoutDatasetContainer);
        layoutAnnotatorContainer = findViewById(R.id.layoutAnnotatorContainer);
        layoutReviewerContainer = findViewById(R.id.layoutReviewerContainer);
    }

    private void setupEditMode() {
        ProjectItem project = projectDbHelper.getProjectById(editingProjectId);
        if (project != null) {
            if (tvHeadline != null) tvHeadline.setText("Edit Project");
            btnCreateProject.setText("Update Project");
            edtProjectName.setText(project.getName());
            edtDescription.setText(project.getDescription());
            edtGuidelines.setText(project.getGuidelines());
            tvDeadlineValue.setText(project.getDeadline());
            
            if (project.getLabels() != null) {
                labelList.clear();
                labelList.addAll(project.getLabels());
            }
            if (project.getDatasetIds() != null) {
                selectedDatasetIds.clear();
                selectedDatasetIds.addAll(project.getDatasetIds());
            }
            if (project.getAnnotatorIds() != null) {
                selectedAnnotatorIds.clear();
                selectedAnnotatorIds.addAll(project.getAnnotatorIds());
            }
            if (project.getReviewerIds() != null) {
                selectedReviewerIds.clear();
                selectedReviewerIds.addAll(project.getReviewerIds());
            }
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> rAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Full Review", "Partial Review"});
        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReviewMode.setAdapter(rAdapter);

        ArrayAdapter<String> eAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"JSON", "CSV", "XML"});
        eAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExportFormat.setAdapter(eAdapter);
    }

    private void loadResources() {
        datasetDbHelper.seedSampleDatasetsIfEmpty();
        datasetDbHelper.forceAddImagesToAllDatasets();

        List<DatasetItem> datasets = datasetDbHelper.getAllDatasets();
        availableDatasets.clear();
        for (DatasetItem d : datasets) {
            availableDatasets.add(d.getName());
            datasetNameToId.put(d.getName(), d.getId());
        }

        List<User> annos = authDbHelper.getUsersByRole("annotator");
        annotatorNameToId.clear();
        for (User u : annos) annotatorNameToId.put(u.getFullName() + " (" + u.getEmail() + ")", (int) u.getId());

        List<User> revs = authDbHelper.getUsersByRole("reviewer");
        reviewerNameToId.clear();
        for (User u : revs) reviewerNameToId.put(u.getFullName() + " (" + u.getEmail() + ")", (int) u.getId());
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnAddLabel.setOnClickListener(v -> {
            String label = edtLabelInput.getText().toString().trim();
            if (!label.isEmpty()) {
                labelList.add(label);
                edtLabelInput.setText("");
                renderLabels();
            }
        });
        btnPickDeadline.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> tvDeadlineValue.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        btnCreateProject.setOnClickListener(v -> saveProject("active"));
        btnSaveDraft.setOnClickListener(v -> saveProject("draft"));
    }

    private void refreshAllUIs() {
        renderLabels();
        renderDatasets();
        renderAnnotators();
        renderReviewers();
        updateCounters();
    }

    private void renderLabels() {
        layoutLabelsContainer.removeAllViews();
        for (String label : labelList) {
            TextView tv = new TextView(this);
            tv.setText(label + " ✕");
            tv.setTextColor(0xFFFFFFFF);
            tv.setBackgroundResource(R.drawable.bg_status_blue);
            tv.setPadding(24, 12, 24, 12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(0, 0, 16, 16);
            tv.setLayoutParams(lp);
            tv.setOnClickListener(v -> { labelList.remove(label); renderLabels(); });
            layoutLabelsContainer.addView(tv);
        }
    }

    private void renderDatasets() {
        layoutDatasetContainer.removeAllViews();
        for (String name : availableDatasets) {
            CheckBox cb = new CheckBox(this);
            cb.setText(name);
            cb.setTextColor(0xFFFFFFFF);
            int id = datasetNameToId.get(name);
            cb.setChecked(selectedDatasetIds.contains(id));
            cb.setOnCheckedChangeListener((v, c) -> {
                if (c) { if(!selectedDatasetIds.contains(id)) selectedDatasetIds.add(id); } 
                else { selectedDatasetIds.remove((Integer)id); }
                updateCounters();
            });
            layoutDatasetContainer.addView(cb);
        }
    }

    private void renderAnnotators() {
        layoutAnnotatorContainer.removeAllViews();
        for (String key : annotatorNameToId.keySet()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(key);
            cb.setTextColor(0xFFFFFFFF);
            int id = annotatorNameToId.get(key);
            cb.setChecked(selectedAnnotatorIds.contains(id));
            cb.setOnCheckedChangeListener((v, c) -> {
                if (c) { if(!selectedAnnotatorIds.contains(id)) selectedAnnotatorIds.add(id); } 
                else { selectedAnnotatorIds.remove((Integer)id); }
                updateCounters();
            });
            layoutAnnotatorContainer.addView(cb);
        }
    }

    private void renderReviewers() {
        layoutReviewerContainer.removeAllViews();
        for (String key : reviewerNameToId.keySet()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(key);
            cb.setTextColor(0xFFFFFFFF);
            int id = reviewerNameToId.get(key);
            cb.setChecked(selectedReviewerIds.contains(id));
            cb.setOnCheckedChangeListener((v, c) -> {
                if (c) { if(!selectedReviewerIds.contains(id)) selectedReviewerIds.add(id); } 
                else { selectedReviewerIds.remove((Integer)id); }
                updateCounters();
            });
            layoutReviewerContainer.addView(cb);
        }
    }

    private void updateCounters() {
        tvSelectedDatasetsCount.setText(selectedDatasetIds.size() + " selected");
        tvSelectedAnnotatorsCount.setText(selectedAnnotatorIds.size() + " selected");
        tvSelectedReviewersCount.setText(selectedReviewerIds.size() + " selected");
    }

    private void saveProject(String status) {
        String name = edtProjectName.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "Project name required", Toast.LENGTH_SHORT).show(); return; }
        if (selectedDatasetIds.isEmpty()) { Toast.makeText(this, "Select a Dataset", Toast.LENGTH_SHORT).show(); return; }
        if (selectedAnnotatorIds.isEmpty() || selectedReviewerIds.isEmpty()) { 
            Toast.makeText(this, "Select Annotators & Reviewers", Toast.LENGTH_SHORT).show(); return; 
        }

        ProjectItem project = new ProjectItem();
        if (editingProjectId != -1) project.setId(editingProjectId);
        project.setName(name);
        project.setDescription(edtDescription.getText().toString());
        project.setGuidelines(edtGuidelines.getText().toString());
        project.setStatus(status);
        project.setReviewStatus("pending");
        project.setLastUpdated(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        
        // Cập nhật các thông tin cài đặt
        project.setReviewMode(spinnerReviewMode.getSelectedItem().toString());
        project.setExportFormat(spinnerExportFormat.getSelectedItem().toString());
        project.setDeadline(tvDeadlineValue.getText().toString());
        project.setSampleRate(0.0); // Mặc định

        // Cập nhật danh sách Labels
        project.setLabels(new ArrayList<>(labelList));
        
        // Cập nhật IDs và Tên hiển thị (Quan trọng để hiển thị ở màn hình Detail)
        project.setDatasetIds(new ArrayList<>(selectedDatasetIds));
        project.setDatasets(getNamesFromIds(datasetNameToId, selectedDatasetIds));
        
        project.setAnnotatorIds(new ArrayList<>(selectedAnnotatorIds));
        project.setAnnotators(getNamesFromIds(annotatorNameToId, selectedAnnotatorIds));
        project.setAnnotatorCount(selectedAnnotatorIds.size());

        project.setReviewerIds(new ArrayList<>(selectedReviewerIds));
        project.setReviewers(getNamesFromIds(reviewerNameToId, selectedReviewerIds));
        project.setReviewerCount(selectedReviewerIds.size());

        boolean success;
        if (editingProjectId != -1) {
            success = projectDbHelper.updateProject(project) > 0;
            taskDbHelper.deleteTasksByProjectId(editingProjectId);
            taskDbHelper.createTasksFromAssignment(this, project);
        } else {
            long id = projectDbHelper.insertProject(project);
            success = id > 0;
            if (success) {
                project.setId((int) id);
                taskDbHelper.createTasksFromAssignment(this, project);
            }
        }

        if (success) {
            Toast.makeText(this, editingProjectId != -1 ? "Project Updated!" : "Project Created!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving project", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getNamesFromIds(Map<String, Integer> nameToIdMap, List<Integer> ids) {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nameToIdMap.entrySet()) {
            if (ids.contains(entry.getValue())) {
                names.add(entry.getKey());
            }
        }
        return names;
    }
}
