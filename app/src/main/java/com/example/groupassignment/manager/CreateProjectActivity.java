package com.example.groupassignment.manager;

import android.app.DatePickerDialog;
import android.content.Intent;
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
    private Button btnSaveDraft, btnCreateProject, btnAddLabel, btnPickDeadline;

    private EditText edtProjectName, edtDescription, edtGuidelines, edtLabelInput, edtSampleRate;
    private TextView tvDeadlineValue, tvSelectedDatasetsCount, tvSelectedAnnotatorsCount, tvSelectedReviewersCount;
    private Spinner spinnerReviewMode, spinnerExportFormat;

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

    private ProjectDbHelper projectDbHelper;
    private DatasetDbHelper datasetDbHelper;
    private AuthDbHelper authDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        projectDbHelper = new ProjectDbHelper(this);
        datasetDbHelper = new DatasetDbHelper(this);
        authDbHelper = new AuthDbHelper(this);
        datasetDbHelper.seedSampleDatasetsIfEmpty();

        readIntentData();
        initViews();
        setupSpinners();
        loadDataFromSQLite();
        renderDatasets();
        renderAnnotators();
        renderReviewers();
        setupActions();
        updateCounters();
        updateReviewModeUi();
        populateEditDataIfNeeded();
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            screenMode = intent.getStringExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE);
            if (screenMode == null) {
                screenMode = ManagerProjectsActivity.MODE_CREATE;
            }

            editingProject = (ProjectItem) intent.getSerializableExtra(
                    ManagerProjectsActivity.EXTRA_PROJECT_RESULT
            );
        }
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
        ArrayAdapter<String> reviewAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Full Review", "Partial Review"}
        );
        reviewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReviewMode.setAdapter(reviewAdapter);

        ArrayAdapter<String> exportAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"JSON", "CSV", "XML"}
        );
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

        List<DatasetItem> datasetItems;
        if (ManagerProjectsActivity.MODE_EDIT.equals(screenMode) && editingProject != null) {
            datasetItems = datasetDbHelper.getDatasetsAvailableForProject(editingProject.getId());
        } else {
            datasetItems = datasetDbHelper.getUnassignedDatasets();
        }

        if (datasetItems != null) {
            for (DatasetItem item : datasetItems) {
                if (item != null && !TextUtils.isEmpty(item.getName())) {
                    availableDatasets.add(item.getName());
                    datasetNameToId.put(item.getName(), item.getId());
                    datasetIdToName.put(item.getId(), item.getName());
                }
            }
        }

        Collections.sort(availableDatasets, String.CASE_INSENSITIVE_ORDER);
    }

    private void loadAnnotatorsFromDb() {
        availableAnnotators.clear();
        annotatorNameToId.clear();
        annotatorIdToName.clear();

        List<User> users = authDbHelper.getUsersByRole("annotator");

        if (users != null) {
            for (User user : users) {
                if (user != null && !TextUtils.isEmpty(user.getFullName())) {
                    String displayName = user.getFullName();
                    availableAnnotators.add(displayName);
                    annotatorNameToId.put(displayName, (int) user.getId());
                    annotatorIdToName.put((int) user.getId(), displayName);
                }
            }
        }

        Collections.sort(availableAnnotators, String.CASE_INSENSITIVE_ORDER);
    }

    private void loadReviewersFromDb() {
        availableReviewers.clear();
        reviewerNameToId.clear();
        reviewerIdToName.clear();

        List<User> users = authDbHelper.getUsersByRole("reviewer");

        if (users != null) {
            for (User user : users) {
                if (user != null && !TextUtils.isEmpty(user.getFullName())) {
                    String displayName = user.getFullName();
                    availableReviewers.add(displayName);
                    reviewerNameToId.put(displayName, (int) user.getId());
                    reviewerIdToName.put((int) user.getId(), displayName);
                }
            }
        }

        Collections.sort(availableReviewers, String.CASE_INSENSITIVE_ORDER);
    }

    private void populateEditDataIfNeeded() {
        if (!ManagerProjectsActivity.MODE_EDIT.equals(screenMode) || editingProject == null) {
            renderLabels();
            return;
        }

        edtProjectName.setText(editingProject.getName());
        edtDescription.setText(editingProject.getDescription());
        edtGuidelines.setText(editingProject.getGuidelines());

        labelList.clear();
        if (editingProject.getLabels() != null) {
            labelList.addAll(editingProject.getLabels());
        }

        selectedDatasetIds.clear();
        if (editingProject.getDatasetIds() != null && !editingProject.getDatasetIds().isEmpty()) {
            selectedDatasetIds.addAll(editingProject.getDatasetIds());
        }

        selectedAnnotatorIds.clear();
        if (editingProject.getAnnotatorIds() != null && !editingProject.getAnnotatorIds().isEmpty()) {
            selectedAnnotatorIds.addAll(editingProject.getAnnotatorIds());
        }

        selectedReviewerIds.clear();
        if (editingProject.getReviewerIds() != null && !editingProject.getReviewerIds().isEmpty()) {
            selectedReviewerIds.addAll(editingProject.getReviewerIds());
        }

        if (!TextUtils.isEmpty(editingProject.getDeadline())
                && !"Not set".equalsIgnoreCase(editingProject.getDeadline())) {
            isDeadlineSelected = true;
            tvDeadlineValue.setText(editingProject.getDeadline());
        }

        if (!TextUtils.isEmpty(editingProject.getReviewMode())) {
            if ("Partial Review".equalsIgnoreCase(editingProject.getReviewMode())) {
                spinnerReviewMode.setSelection(1);
            } else {
                spinnerReviewMode.setSelection(0);
            }
        }

        edtSampleRate.setText(String.valueOf(editingProject.getSampleRate()));

        if (!TextUtils.isEmpty(editingProject.getExportFormat())) {
            if ("CSV".equalsIgnoreCase(editingProject.getExportFormat())) {
                spinnerExportFormat.setSelection(1);
            } else if ("XML".equalsIgnoreCase(editingProject.getExportFormat())) {
                spinnerExportFormat.setSelection(2);
            } else {
                spinnerExportFormat.setSelection(0);
            }
        }

        syncSelectedDisplayLists();

        Collections.sort(availableDatasets, String.CASE_INSENSITIVE_ORDER);
        renderLabels();
        renderDatasets();
        renderAnnotators();
        renderReviewers();
        updateCounters();
        updateReviewModeUi();
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

        btnSaveDraft.setOnClickListener(v -> saveDraft());

        btnCreateProject.setOnClickListener(v -> createProject());
    }

    private void updateReviewModeUi() {
        String selectedMode = spinnerReviewMode.getSelectedItem().toString();
        boolean isPartial = "Partial Review".equals(selectedMode);
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
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
            if (annotatorId == null) {
                continue;
            }

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
            if (reviewerId == null) {
                continue;
            }

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
        if (!selectedDatasetIds.isEmpty()) {
            for (Integer id : selectedDatasetIds) {
                String name = datasetIdToName.get(id);
                if (!TextUtils.isEmpty(name)) {
                    selectedDatasets.add(name);
                }
            }
        } else if (editingProject != null && editingProject.getDatasets() != null) {
            selectedDatasets.addAll(editingProject.getDatasets());
            for (String datasetName : editingProject.getDatasets()) {
                Integer datasetId = datasetNameToId.get(datasetName);
                if (datasetId != null && !selectedDatasetIds.contains(datasetId)) {
                    selectedDatasetIds.add(datasetId);
                }
            }
        }

        selectedAnnotators.clear();
        if (!selectedAnnotatorIds.isEmpty()) {
            for (Integer id : selectedAnnotatorIds) {
                String name = annotatorIdToName.get(id);
                if (!TextUtils.isEmpty(name)) {
                    selectedAnnotators.add(name);
                }
            }
        } else if (editingProject != null && editingProject.getAnnotators() != null) {
            selectedAnnotators.addAll(editingProject.getAnnotators());
            for (String annotatorName : editingProject.getAnnotators()) {
                Integer annotatorId = annotatorNameToId.get(annotatorName);
                if (annotatorId != null && !selectedAnnotatorIds.contains(annotatorId)) {
                    selectedAnnotatorIds.add(annotatorId);
                }
            }
        }

        selectedReviewers.clear();
        if (!selectedReviewerIds.isEmpty()) {
            for (Integer id : selectedReviewerIds) {
                String name = reviewerIdToName.get(id);
                if (!TextUtils.isEmpty(name)) {
                    selectedReviewers.add(name);
                }
            }
        } else if (editingProject != null && editingProject.getReviewers() != null) {
            selectedReviewers.addAll(editingProject.getReviewers());
            for (String reviewerName : editingProject.getReviewers()) {
                Integer reviewerId = reviewerNameToId.get(reviewerName);
                if (reviewerId != null && !selectedReviewerIds.contains(reviewerId)) {
                    selectedReviewerIds.add(reviewerId);
                }
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        cardView.setLayoutParams(params);

        cardView.addView(checkBox);
        return cardView;
    }

    private void updateCounters() {
        tvSelectedDatasetsCount.setText(selectedDatasets.size() + " selected");
        tvSelectedAnnotatorsCount.setText(selectedAnnotators.size() + " selected");
        tvSelectedReviewersCount.setText(selectedReviewers.size() + " selected");
    }

    private void showDatePicker() {
        int year = deadlineCalendar.get(Calendar.YEAR);
        int month = deadlineCalendar.get(Calendar.MONTH);
        int day = deadlineCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    deadlineCalendar.set(Calendar.YEAR, selectedYear);
                    deadlineCalendar.set(Calendar.MONTH, selectedMonth);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    isDeadlineSelected = true;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDeadlineValue.setText(sdf.format(deadlineCalendar.getTime()));
                },
                year, month, day
        );
        dialog.show();
    }

    private void saveDraft() {
        saveProjectToDatabase("draft");
    }

    private void createProject() {
        saveProjectToDatabase("active");
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
                showToast("Cập nhật project thành công");
                returnProjectResult(project, ManagerProjectsActivity.MODE_EDIT);
            } else {
                datasetDbHelper.assignDatasetsToProjectByIds(editingProject.getDatasetIds(), editingProject.getId());
                showToast("Cập nhật project thất bại");
            }
        } else {
            long insertedId = projectDbHelper.insertProject(project);
            if (insertedId > 0) {
                project.setId((int) insertedId);
                datasetDbHelper.assignDatasetsToProjectByIds(project.getDatasetIds(), project.getId());
                showToast("Tạo project thành công");
                returnProjectResult(project, ManagerProjectsActivity.MODE_CREATE);
            } else {
                showToast("Tạo project thất bại");
            }
        }
    }

    private ProjectItem buildProjectFromForm(String status) {
        String projectName = edtProjectName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String guidelines = edtGuidelines.getText().toString().trim();
        String reviewMode = spinnerReviewMode.getSelectedItem().toString();
        String exportFormat = spinnerExportFormat.getSelectedItem().toString();
        String deadline = isDeadlineSelected ? tvDeadlineValue.getText().toString().trim() : "Not set";
        String sampleRateText = edtSampleRate.getText().toString().trim();

        if (TextUtils.isEmpty(projectName)) {
            edtProjectName.setError("Project name is required");
            edtProjectName.requestFocus();
            return null;
        }

        if (TextUtils.isEmpty(guidelines)) {
            edtGuidelines.setError("Guidelines is required");
            edtGuidelines.requestFocus();
            return null;
        }

        if (labelList.isEmpty()) {
            showToast("Vui lòng thêm ít nhất 1 label");
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

        if (selectedAnnotatorIds.size() % 2 == 0) {
            showToast("Số annotator phải là số lẻ");
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
        project.setReviewStatus("pending");
        project.setReviewerCount(selectedReviewerIds.size());
        project.setAnnotatorCount(selectedAnnotatorIds.size());
        project.setLastUpdated(getTodayText());

        project.setReviewMode(reviewMode);
        project.setSampleRate(sampleRate);
        project.setDeadline(deadline);
        project.setExportFormat(exportFormat);

        project.setLabels(new ArrayList<>(labelList));
        project.setDatasets(new ArrayList<>(selectedDatasets));
        project.setAnnotators(new ArrayList<>(selectedAnnotators));
        project.setReviewers(new ArrayList<>(selectedReviewers));
        project.setDatasetIds(new ArrayList<>(selectedDatasetIds));
        project.setAnnotatorIds(new ArrayList<>(selectedAnnotatorIds));
        project.setReviewerIds(new ArrayList<>(selectedReviewerIds));

        return project;
    }

    private void returnProjectResult(ProjectItem project, String mode) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_RESULT, project);
        resultIntent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, mode);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getTodayText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        return sdf.format(Calendar.getInstance().getTime());
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
}
