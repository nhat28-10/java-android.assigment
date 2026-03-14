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
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.ProjectItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    private final List<String> mockDatasets = new ArrayList<>();
    private final List<String> mockAnnotators = new ArrayList<>();
    private final List<String> mockReviewers = new ArrayList<>();

    private final Calendar deadlineCalendar = Calendar.getInstance();
    private boolean isDeadlineSelected = false;

    private String screenMode = ManagerProjectsActivity.MODE_CREATE;
    private ProjectItem editingProject;

    private ProjectDbHelper projectDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        projectDbHelper = new ProjectDbHelper(this);

        readIntentData();
        initViews();
        setupSpinners();
        bindMockData();
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

    private void bindMockData() {
        mockDatasets.clear();
        mockAnnotators.clear();
        mockReviewers.clear();

        mockDatasets.add("Product Images Batch 01");
        mockDatasets.add("Street Objects Dataset");
        mockDatasets.add("Vietnamese Audio Clips");
        mockDatasets.add("Customer Reviews Text");

        mockAnnotators.add("Nguyen Van A");
        mockAnnotators.add("Tran Thi B");
        mockAnnotators.add("Le Minh C");
        mockAnnotators.add("Pham Quoc D");
        mockAnnotators.add("Hoang Gia E");

        mockReviewers.add("Reviewer Linh");
        mockReviewers.add("Reviewer Khoa");
        mockReviewers.add("Reviewer Trang");
        mockReviewers.add("Reviewer Nam");
        mockReviewers.add("Reviewer Phuc");
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

        selectedDatasets.clear();
        if (editingProject.getDatasets() != null) {
            selectedDatasets.addAll(editingProject.getDatasets());
        }

        selectedAnnotators.clear();
        if (editingProject.getAnnotators() != null) {
            selectedAnnotators.addAll(editingProject.getAnnotators());
        }

        selectedReviewers.clear();
        if (editingProject.getReviewers() != null) {
            selectedReviewers.addAll(editingProject.getReviewers());
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

        for (String dataset : mockDatasets) {
            CheckBox checkBox = buildCheckBox(dataset);
            checkBox.setChecked(selectedDatasets.contains(dataset));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedDatasets.contains(dataset)) {
                        selectedDatasets.add(dataset);
                    }
                } else {
                    selectedDatasets.remove(dataset);
                }
                updateCounters();
            });
            layoutDatasetContainer.addView(wrapInCard(checkBox));
        }
    }

    private void renderAnnotators() {
        layoutAnnotatorContainer.removeAllViews();

        for (String annotator : mockAnnotators) {
            CheckBox checkBox = buildCheckBox(annotator);
            checkBox.setChecked(selectedAnnotators.contains(annotator));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedAnnotators.contains(annotator)) {
                        selectedAnnotators.add(annotator);
                    }
                } else {
                    selectedAnnotators.remove(annotator);
                }
                updateCounters();
            });
            layoutAnnotatorContainer.addView(wrapInCard(checkBox));
        }
    }

    private void renderReviewers() {
        layoutReviewerContainer.removeAllViews();

        for (String reviewer : mockReviewers) {
            CheckBox checkBox = buildCheckBox(reviewer);
            checkBox.setChecked(selectedReviewers.contains(reviewer));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedReviewers.contains(reviewer)) {
                        selectedReviewers.add(reviewer);
                    }
                } else {
                    selectedReviewers.remove(reviewer);
                }
                updateCounters();
            });
            layoutReviewerContainer.addView(wrapInCard(checkBox));
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

            int updatedRows = projectDbHelper.updateProject(project);
            if (updatedRows > 0) {
                showToast("Cập nhật project thành công");
                returnProjectResult(project, ManagerProjectsActivity.MODE_EDIT);
            } else {
                showToast("Cập nhật project thất bại");
            }
        } else {
            long insertedId = projectDbHelper.insertProject(project);
            if (insertedId > 0) {
                project.setId((int) insertedId);
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

        if (selectedDatasets.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 dataset");
            return null;
        }

        if (selectedAnnotators.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 annotator");
            return null;
        }

        if (selectedAnnotators.size() % 2 == 0) {
            showToast("Số annotator phải là số lẻ");
            return null;
        }

        if (selectedReviewers.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 reviewer");
            return null;
        }

        if (selectedReviewers.size() % 2 == 0) {
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
        project.setReviewerCount(selectedReviewers.size());
        project.setAnnotatorCount(selectedAnnotators.size());
        project.setLastUpdated(getTodayText());

        project.setReviewMode(reviewMode);
        project.setSampleRate(sampleRate);
        project.setDeadline(deadline);
        project.setExportFormat(exportFormat);

        project.setLabels(new ArrayList<>(labelList));
        project.setDatasets(new ArrayList<>(selectedDatasets));
        project.setAnnotators(new ArrayList<>(selectedAnnotators));
        project.setReviewers(new ArrayList<>(selectedReviewers));

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