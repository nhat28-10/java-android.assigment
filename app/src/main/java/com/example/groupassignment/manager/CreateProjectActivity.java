package com.example.groupassignment.manager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
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
import androidx.cardview.widget.CardView;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateProjectActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSaveDraft;
    private Button btnCreateProject;
    private Button btnAddLabel;
    private Button btnPickDeadline;
    private EditText edtProjectName;
    private EditText edtDescription;
    private EditText edtGuidelines;
    private EditText edtLabelInput;
    private EditText edtSampleRate;
    private TextView tvDeadlineValue;
    private TextView tvSelectedDatasetsCount;
    private TextView tvSelectedAnnotatorsCount;
    private TextView tvSelectedReviewersCount;
    private Spinner spinnerReviewMode;
    private Spinner spinnerExportFormat;
    private LinearLayout layoutLabelsContainer;
    private LinearLayout layoutDatasetContainer;
    private LinearLayout layoutAnnotatorContainer;
    private LinearLayout layoutReviewerContainer;

    private final List<String> labelList = new ArrayList<>();
    private final List<String> selectedDatasets = new ArrayList<>();
    private final List<String> selectedAnnotators = new ArrayList<>();
    private final List<String> selectedReviewers = new ArrayList<>();
    private final List<Integer> selectedDatasetIds = new ArrayList<>();
    private final List<Integer> selectedAnnotatorIds = new ArrayList<>();
    private final List<Integer> selectedReviewerIds = new ArrayList<>();
    private final List<String> availableDatasets = new ArrayList<>();
    private final List<String> availableAnnotators = new ArrayList<>();
    private final List<String> availableReviewers = new ArrayList<>();
    private final Map<String, Integer> datasetNameToId = new HashMap<>();
    private final Map<String, Integer> annotatorNameToId = new HashMap<>();
    private final Map<String, Integer> reviewerNameToId = new HashMap<>();
    private final Map<Integer, String> datasetIdToName = new HashMap<>();
    private final Map<Integer, String> annotatorIdToName = new HashMap<>();
    private final Map<Integer, String> reviewerIdToName = new HashMap<>();
    private final Calendar deadlineCalendar = Calendar.getInstance();

    private boolean isDeadlineSelected = false;
    private String screenMode = ManagerProjectsActivity.MODE_CREATE;
    private ProjectItem editingProject;
    private int preselectedDatasetId = -1;
    private String preselectedDatasetName = "";

    private ProjectDbHelper projectDbHelper;
    private SessionManager sessionManager;
    private DatasetDbHelper datasetDbHelper;
    private AuthDbHelper authDbHelper;
    private TaskDbHelper taskDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        projectDbHelper = new ProjectDbHelper(this);
        datasetDbHelper = new DatasetDbHelper(this);
        authDbHelper = new AuthDbHelper(this);
        taskDbHelper = new TaskDbHelper(this);

        readIntentData();
        initViews();
        setupSpinners();
        loadDataFromSQLite();
        populateEditDataIfNeeded();
        applyDatasetPreselection();
        renderLabels();
        renderDatasets();
        renderAnnotators();
        renderReviewers();
        setupActions();
        updateCounters();
        updateReviewModeUi();
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String mode = intent.getStringExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE);
        if (!TextUtils.isEmpty(mode)) {
            screenMode = mode;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            editingProject = intent.getSerializableExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, ProjectItem.class);
        } else {
            editingProject = (ProjectItem) intent.getSerializableExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT);
        }
        preselectedDatasetId = intent.getIntExtra(CreateDatasetActivity.EXTRA_CREATED_DATASET_ID, -1);
        preselectedDatasetName = intent.getStringExtra(CreateDatasetActivity.EXTRA_CREATED_DATASET_NAME);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackCreateProject);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnCreateProject = findViewById(R.id.btnCreateProject);
        btnAddLabel = findViewById(R.id.btnAddLabel);
        btnPickDeadline = findViewById(R.id.btnPickDeadline);
        edtProjectName = findViewById(R.id.edtProjectName);
        edtDescription = findViewById(R.id.edtProjectDescription);
        edtGuidelines = findViewById(R.id.edtProjectGuidelines);
        edtLabelInput = findViewById(R.id.edtLabelInput);
        edtSampleRate = findViewById(R.id.edtSampleRate);
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

        if (ManagerProjectsActivity.MODE_EDIT.equals(screenMode)) {
            btnCreateProject.setText("Update Project");
            btnSaveDraft.setText("Save Changes");
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> reviewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Full Review", "Partial Review"});
        reviewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReviewMode.setAdapter(reviewAdapter);

        ArrayAdapter<String> exportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"JSON", "CSV", "XML"});
        exportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExportFormat.setAdapter(exportAdapter);
    }

    private void loadDataFromSQLite() {
        loadDatasetsFromDb();
        loadAnnotatorsFromDb();
        loadReviewersFromDb();
    }

    private void loadDatasetsFromDb() {
        availableDatasets.clear();
        datasetNameToId.clear();
        datasetIdToName.clear();
        List<DatasetItem> datasetItems = ManagerProjectsActivity.MODE_EDIT.equals(screenMode) && editingProject != null
                ? datasetDbHelper.getDatasetsAvailableForProject(editingProject.getId())
                : datasetDbHelper.getUnassignedDatasets();

        for (DatasetItem item : datasetItems) {
            if (item != null && !TextUtils.isEmpty(item.getName())) {
                availableDatasets.add(item.getName());
                datasetNameToId.put(item.getName(), item.getId());
                datasetIdToName.put(item.getId(), item.getName());
            }
        }

        if (preselectedDatasetId > 0 && !datasetIdToName.containsKey(preselectedDatasetId)) {
            DatasetItem preselected = datasetDbHelper.getDatasetById(preselectedDatasetId);
            if (preselected != null) {
                availableDatasets.add(preselected.getName());
                datasetNameToId.put(preselected.getName(), preselected.getId());
                datasetIdToName.put(preselected.getId(), preselected.getName());
            }
        }
        Collections.sort(availableDatasets, String.CASE_INSENSITIVE_ORDER);
    }

    private void loadAnnotatorsFromDb() {
        availableAnnotators.clear();
        annotatorNameToId.clear();
        annotatorIdToName.clear();
        for (User user : authDbHelper.getUsersByRole("annotator")) {
            String displayName = user.getFullName();
            availableAnnotators.add(displayName);
            annotatorNameToId.put(displayName, (int) user.getId());
            annotatorIdToName.put((int) user.getId(), displayName);
        }
        Collections.sort(availableAnnotators, String.CASE_INSENSITIVE_ORDER);
    }

    private void loadReviewersFromDb() {
        availableReviewers.clear();
        reviewerNameToId.clear();
        reviewerIdToName.clear();
        for (User user : authDbHelper.getUsersByRole("reviewer")) {
            String displayName = user.getFullName();
            availableReviewers.add(displayName);
            reviewerNameToId.put(displayName, (int) user.getId());
            reviewerIdToName.put((int) user.getId(), displayName);
        }
        Collections.sort(availableReviewers, String.CASE_INSENSITIVE_ORDER);
    }

    private void populateEditDataIfNeeded() {
        if (!ManagerProjectsActivity.MODE_EDIT.equals(screenMode) || editingProject == null) {
            return;
        }
        edtProjectName.setText(editingProject.getName());
        edtDescription.setText(editingProject.getDescription());
        edtGuidelines.setText(editingProject.getGuidelines());
        labelList.clear();
        labelList.addAll(normalizeLabels(editingProject.getLabels()));
        selectedDatasetIds.clear();
        selectedDatasetIds.addAll(editingProject.getDatasetIds());
        selectedAnnotatorIds.clear();
        selectedAnnotatorIds.addAll(editingProject.getAnnotatorIds());
        selectedReviewerIds.clear();
        selectedReviewerIds.addAll(editingProject.getReviewerIds());
        if (!TextUtils.isEmpty(editingProject.getDeadline()) && !"Not set".equalsIgnoreCase(editingProject.getDeadline())) {
            isDeadlineSelected = true;
            tvDeadlineValue.setText(editingProject.getDeadline());
        }
        if ("Partial Review".equalsIgnoreCase(editingProject.getReviewMode())) {
            spinnerReviewMode.setSelection(1);
        }
        edtSampleRate.setText(String.valueOf(editingProject.getSampleRate()));
        if ("CSV".equalsIgnoreCase(editingProject.getExportFormat())) {
            spinnerExportFormat.setSelection(1);
        } else if ("XML".equalsIgnoreCase(editingProject.getExportFormat())) {
            spinnerExportFormat.setSelection(2);
        }
        syncSelectedDisplayLists();
    }

    private void applyDatasetPreselection() {
        if (preselectedDatasetId <= 0 || ManagerProjectsActivity.MODE_EDIT.equals(screenMode)) {
            return;
        }
        if (!selectedDatasetIds.contains(preselectedDatasetId)) {
            selectedDatasetIds.add(preselectedDatasetId);
        }
        String name = !TextUtils.isEmpty(preselectedDatasetName) ? preselectedDatasetName : datasetIdToName.get(preselectedDatasetId);
        if (!TextUtils.isEmpty(name) && !selectedDatasets.contains(name)) {
            selectedDatasets.add(name);
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnAddLabel.setOnClickListener(v -> {
            String label = edtLabelInput.getText().toString().trim();
            if (TextUtils.isEmpty(label)) {
                showToast("Vui lòng nhập label");
                return;
            }
            if (labelList.contains(label)) {
                showToast("Label đã tồn tại");
                return;
            }
            labelList.add(label);
            edtLabelInput.setText("");
            renderLabels();
        });
        btnPickDeadline.setOnClickListener(v -> showDatePicker());
        spinnerReviewMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateReviewModeUi();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        btnSaveDraft.setOnClickListener(v -> saveProjectToDatabase("draft"));
        btnCreateProject.setOnClickListener(v -> saveProjectToDatabase("active"));
    }

    private void updateReviewModeUi() {
        boolean isPartial = "Partial Review".equals(spinnerReviewMode.getSelectedItem().toString());
        edtSampleRate.setEnabled(isPartial);
        if (!isPartial) {
            edtSampleRate.setText("1.0");
        } else if (TextUtils.isEmpty(edtSampleRate.getText().toString().trim())) {
            edtSampleRate.setText("0.3");
        }
    }

    private void renderLabels() {
        layoutLabelsContainer.removeAllViews();
        if (labelList.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có label nào");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layoutLabelsContainer.addView(emptyView);
            return;
        }
        for (String label : new ArrayList<>(labelList)) {
            TextView chip = new TextView(this);
            chip.setText(label + "  ✕");
            chip.setTextColor(getResources().getColor(android.R.color.white));
            chip.setTextSize(14f);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setBackgroundResource(R.drawable.bg_status_blue);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, dp(8), dp(8));
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> {
                labelList.remove(label);
                renderLabels();
            });
            layoutLabelsContainer.addView(chip);
        }
    }

    private void renderDatasets() {
        layoutDatasetContainer.removeAllViews();
        if (availableDatasets.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có dataset khả dụng");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layoutDatasetContainer.addView(emptyView);
            return;
        }
        for (String dataset : availableDatasets) {
            Integer datasetId = datasetNameToId.get(dataset);
            if (datasetId == null) {
                continue;
            }
            CheckBox checkBox = buildCheckBox(dataset);
            checkBox.setChecked(selectedDatasetIds.contains(datasetId));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedDatasetIds.contains(datasetId)) {
                        selectedDatasetIds.add(datasetId);
                    }
                    if (!selectedDatasets.contains(dataset)) {
                        selectedDatasets.add(dataset);
                    }
                } else {
                    selectedDatasetIds.remove(datasetId);
                    selectedDatasets.remove(dataset);
                }
                updateCounters();
            });
            layoutDatasetContainer.addView(wrapInCard(checkBox));
        }
    }

    private void renderAnnotators() {
        layoutAnnotatorContainer.removeAllViews();
        if (availableAnnotators.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có annotator nào");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layoutAnnotatorContainer.addView(emptyView);
            return;
        }
        for (String annotator : availableAnnotators) {
            Integer annotatorId = annotatorNameToId.get(annotator);
            CheckBox checkBox = buildCheckBox(annotator);
            checkBox.setChecked(selectedAnnotatorIds.contains(annotatorId));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedAnnotatorIds.contains(annotatorId)) {
                        selectedAnnotatorIds.add(annotatorId);
                    }
                    if (!selectedAnnotators.contains(annotator)) {
                        selectedAnnotators.add(annotator);
                    }
                } else {
                    selectedAnnotatorIds.remove(annotatorId);
                    selectedAnnotators.remove(annotator);
                }
                updateCounters();
            });
            layoutAnnotatorContainer.addView(wrapInCard(checkBox));
        }
    }

    private void renderReviewers() {
        layoutReviewerContainer.removeAllViews();
        if (availableReviewers.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có reviewer nào");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layoutReviewerContainer.addView(emptyView);
            return;
        }
        for (String reviewer : availableReviewers) {
            Integer reviewerId = reviewerNameToId.get(reviewer);
            CheckBox checkBox = buildCheckBox(reviewer);
            checkBox.setChecked(selectedReviewerIds.contains(reviewerId));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedReviewerIds.contains(reviewerId)) {
                        selectedReviewerIds.add(reviewerId);
                    }
                    if (!selectedReviewers.contains(reviewer)) {
                        selectedReviewers.add(reviewer);
                    }
                } else {
                    selectedReviewerIds.remove(reviewerId);
                    selectedReviewers.remove(reviewer);
                }
                updateCounters();
            });
            layoutReviewerContainer.addView(wrapInCard(checkBox));
        }
    }

    private void syncSelectedDisplayLists() {
        selectedDatasets.clear();
        for (Integer id : selectedDatasetIds) {
            String name = datasetIdToName.get(id);
            if (!TextUtils.isEmpty(name)) {
                selectedDatasets.add(name);
            }
        }
        selectedAnnotators.clear();
        for (Integer id : selectedAnnotatorIds) {
            String name = annotatorIdToName.get(id);
            if (!TextUtils.isEmpty(name)) {
                selectedAnnotators.add(name);
            }
        }
        selectedReviewers.clear();
        for (Integer id : selectedReviewerIds) {
            String name = reviewerIdToName.get(id);
            if (!TextUtils.isEmpty(name)) {
                selectedReviewers.add(name);
            }
        }
    }

    private CheckBox buildCheckBox(String text) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        checkBox.setTextSize(15f);
        checkBox.setTextColor(getResources().getColor(android.R.color.white));
        checkBox.setPadding(dp(8), dp(8), dp(8), dp(8));
        return checkBox;
    }

    private CardView wrapInCard(CheckBox checkBox) {
        CardView cardView = new CardView(this);
        cardView.setRadius(dp(12));
        cardView.setCardElevation(dp(2));
        cardView.setCardBackgroundColor(0xFF1F2937);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        cardView.setLayoutParams(params);
        cardView.addView(checkBox);
        return cardView;
    }

    private void updateCounters() {
        tvSelectedDatasetsCount.setText(selectedDatasetIds.size() + " selected");
        tvSelectedAnnotatorsCount.setText(selectedAnnotatorIds.size() + " selected");
        tvSelectedReviewersCount.setText(selectedReviewerIds.size() + " selected");
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            deadlineCalendar.set(Calendar.YEAR, year);
            deadlineCalendar.set(Calendar.MONTH, month);
            deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            isDeadlineSelected = true;
            tvDeadlineValue.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(deadlineCalendar.getTime()));
        }, deadlineCalendar.get(Calendar.YEAR), deadlineCalendar.get(Calendar.MONTH), deadlineCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveProjectToDatabase(String status) {
        ProjectItem project = buildProjectFromForm(status);
        if (project == null) {
            return;
        }
        if (ManagerProjectsActivity.MODE_EDIT.equals(screenMode) && editingProject != null) {
            project.setId(editingProject.getId());
            datasetDbHelper.releaseDatasetsByIds(editingProject.getDatasetIds(), editingProject.getId());
            int updatedRows = projectDbHelper.updateProject(project);
            if (updatedRows > 0) {
                datasetDbHelper.assignDatasetsToProjectByIds(project.getDatasetIds(), project.getId());
                taskDbHelper.syncTasksForProject(project);
                showToast("Cập nhật project thành công");
                returnProjectResult(project, ManagerProjectsActivity.MODE_EDIT);
            } else {
                datasetDbHelper.assignDatasetsToProjectByIds(editingProject.getDatasetIds(), editingProject.getId());
                showToast("Cập nhật project thất bại");
            }
            return;
        }

        long insertedId = projectDbHelper.insertProject(project);
        if (insertedId > 0) {
            project.setId((int) insertedId);
            datasetDbHelper.assignDatasetsToProjectByIds(project.getDatasetIds(), project.getId());
            taskDbHelper.syncTasksForProject(project);
            showToast("Tạo project thành công");
            returnProjectResult(project, ManagerProjectsActivity.MODE_CREATE);
        } else {
            showToast("Tạo project thất bại");
        }
    }

    private ProjectItem buildProjectFromForm(String status) {
        syncSelectedDisplayLists();
        String projectName = edtProjectName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String guidelines = edtGuidelines.getText().toString().trim();
        String reviewMode = spinnerReviewMode.getSelectedItem().toString();
        String exportFormat = spinnerExportFormat.getSelectedItem().toString();
        String deadline = isDeadlineSelected ? tvDeadlineValue.getText().toString().trim() : "";
        String sampleRateText = edtSampleRate.getText().toString().trim();

        if (TextUtils.isEmpty(projectName)) {
            edtProjectName.setError("Project name is required");
            return null;
        }
        if (TextUtils.isEmpty(guidelines)) {
            edtGuidelines.setError("Guidelines is required");
            return null;
        }
        if (TextUtils.isEmpty(deadline)) {
            showToast("Vui lòng chọn deadline cho project");
            return null;
        }
        if (selectedDatasetIds.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 dataset");
            return null;
        }
        if (selectedAnnotatorIds.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 annotator");
            return null;
        }
        if (selectedReviewerIds.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 reviewer");
            return null;
        }
        if (selectedReviewerIds.size() % 2 == 0) {
            showToast("Số reviewer phải là số lẻ");
            return null;
        }

        List<String> normalizedLabels = normalizeLabels(labelList);
        if (normalizedLabels.isEmpty()) {
            showToast("Vui lòng thêm ít nhất 1 label");
            return null;
        }

        double sampleRate;
        try {
            sampleRate = Double.parseDouble(sampleRateText);
        } catch (Exception e) {
            showToast("Sample rate không hợp lệ");
            return null;
        }
        if (sampleRate < 0 || sampleRate > 1) {
            showToast("Sample rate phải nằm trong khoảng 0 đến 1");
            return null;
        }
        if ("Full Review".equals(reviewMode)) {
            sampleRate = 1.0;
        }

        ProjectItem project = new ProjectItem();
        project.setName(projectName);
        project.setDescription(description);
        project.setGuidelines(guidelines);
        project.setStatus(status);
        project.setReviewStatus(statusForProjectReviewFlow(status));
        project.setReviewerCount(selectedReviewerIds.size());
        project.setAnnotatorCount(selectedAnnotatorIds.size());
        project.setLastUpdated(new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
        project.setReviewMode(reviewMode);
        project.setSampleRate(sampleRate);
        project.setDeadline(TextUtils.isEmpty(deadline) ? "" : deadline);
        project.setExportFormat(exportFormat);
        project.setLabels(new ArrayList<>(normalizedLabels));
        project.setDatasets(new ArrayList<>(selectedDatasets));
        project.setAnnotators(new ArrayList<>(selectedAnnotators));
        project.setReviewers(new ArrayList<>(selectedReviewers));
        project.setDatasetIds(new ArrayList<>(selectedDatasetIds));
        project.setAnnotatorIds(new ArrayList<>(selectedAnnotatorIds));
        project.setReviewerIds(new ArrayList<>(selectedReviewerIds));
        return project;
    }

    private List<String> normalizeLabels(List<String> labels) {
        List<String> normalized = new ArrayList<>();
        if (labels == null) {
            return normalized;
        }
        for (String label : labels) {
            if (TextUtils.isEmpty(label)) {
                continue;
            }
            String value = label.trim();
            if (!value.isEmpty() && !normalized.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private void returnProjectResult(ProjectItem project, String mode) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, project);
        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, mode);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, dp(24));
        toast.show();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
    private String statusForProjectReviewFlow(String status) {
        return "draft".equalsIgnoreCase(status) ? "draft" : TaskDbHelper.STATUS_ASSIGNED;
    }
}
