package com.example.groupassignment.annotator;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.data.ProjectDbHelper;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.TaskItem;

import java.util.ArrayList;
import java.util.List;

public class AnnotatorTaskActivity extends AppCompatActivity {

    private TextView tvProjectName, tvDatasetName, tvStatus;
    private AnnotationView annotationView;
    private Spinner spLabels;
    private ImageButton btnUndo, btnClear;
    private Button btnSave, btnSubmit;

    private TaskDbHelper taskDbHelper;
    private ProjectDbHelper projectDbHelper;
    private TaskItem currentTask;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_task);

        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taskDbHelper = new TaskDbHelper(this);
        projectDbHelper = new ProjectDbHelper(this);

        initViews();
        loadTask();
        setupListeners();
    }

    private void initViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDatasetName = findViewById(R.id.tvDatasetName);
        tvStatus = findViewById(R.id.tvStatus);

        annotationView = findViewById(R.id.annotationView);
        spLabels = findViewById(R.id.spLabels);
        btnUndo = findViewById(R.id.btnUndo);
        btnClear = findViewById(R.id.btnClear);

        btnSave = findViewById(R.id.btnSave);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void loadTask() {
        currentTask = taskDbHelper.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvProjectName.setText("Project: " + currentTask.getProjectName());
        tvDatasetName.setText("Dataset: " + currentTask.getDatasetName());
        tvStatus.setText("Status: " + currentTask.getStatus().toUpperCase());

        // Load image
        if (currentTask.getImageUri() != null && !currentTask.getImageUri().isEmpty()) {
            try {
                annotationView.setImageURI(Uri.parse(currentTask.getImageUri()));
            } catch (Exception e) {
                Toast.makeText(this, "Image load error", Toast.LENGTH_SHORT).show();
            }
        }

        // Load labels from Project
        ProjectItem project = projectDbHelper.getProjectById(currentTask.getProjectId());
        List<String> labels = new ArrayList<>();
        if (project != null && project.getLabels() != null && !project.getLabels().isEmpty()) {
            labels.addAll(project.getLabels());
        } else {
            labels.add("Object");
            labels.add("Person");
            labels.add("Vehicle");
        }

        // SỬ DỤNG CUSTOM LAYOUT ĐỂ CHỮ NỔI BẬT TRÊN NỀN TỐI
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner_label, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_label);
        spLabels.setAdapter(adapter);
        
        if (!labels.isEmpty()) {
            annotationView.setSelectedLabel(labels.get(0));
        }

        if (currentTask.getAnnotationResult() != null && !currentTask.getAnnotationResult().isEmpty()) {
            annotationView.setBoxesFromJson(currentTask.getAnnotationResult());
        }

        boolean isEditable = !"submitted".equalsIgnoreCase(currentTask.getStatus()) 
                && !"approved".equalsIgnoreCase(currentTask.getStatus());
        
        if (!isEditable) {
            disableEditing();
        }
    }

    private void disableEditing() {
        btnSubmit.setEnabled(false);
        btnSave.setEnabled(false);
        btnUndo.setEnabled(false);
        btnClear.setEnabled(false);
        annotationView.setEnabled(false);
        spLabels.setEnabled(false);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveDraft());
        btnSubmit.setOnClickListener(v -> submitTask());
        btnUndo.setOnClickListener(v -> annotationView.clearLast());
        btnClear.setOnClickListener(v -> annotationView.clearAll());

        spLabels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String label = parent.getItemAtPosition(position).toString();
                annotationView.setSelectedLabel(label);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveDraft() {
        String jsonResult = annotationView.getBoxesAsJson();
        if (taskDbHelper.updateAnnotation(taskId, jsonResult, "in_progress")) {
            Toast.makeText(this, "Draft saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitTask() {
        if (annotationView.getBoxes().isEmpty()) {
            Toast.makeText(this, "Please add at least one label", Toast.LENGTH_SHORT).show();
            return;
        }
        String jsonResult = annotationView.getBoxesAsJson();
        if (taskDbHelper.updateAnnotation(taskId, jsonResult, "submitted")) {
            Toast.makeText(this, "Task submitted for review!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
