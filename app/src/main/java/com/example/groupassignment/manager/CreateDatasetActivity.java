package com.example.groupassignment.manager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateDatasetActivity extends AppCompatActivity {

    private ImageButton btnBackCreateDataset;
    private Button btnSaveDataset;

    private EditText etDatasetName;
    private EditText etDatasetDescription;
    private Spinner spDatasetType;

    private EditText etTotalItems;
    private EditText etApprovedItems;
    private EditText etSubmittedItems;
    private EditText etPendingItems;
    private EditText etRejectedItems;

    private DatasetDbHelper datasetDbHelper;
    private SessionManager sessionManager;
    private DatasetItem currentDataset;
    private String currentMode = DatasetsActivity.MODE_CREATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dataset);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        datasetDbHelper = new DatasetDbHelper(this);

        initViews();
        setupSpinner();
        readIntentData();
        bindDataIfEditMode();
        setupActions();
    }

    private void initViews() {
        btnBackCreateDataset = findViewById(R.id.btnBackCreateDataset);
        btnSaveDataset = findViewById(R.id.btnSaveDataset);

        etDatasetName = findViewById(R.id.etDatasetName);
        etDatasetDescription = findViewById(R.id.etDatasetDescription);
        spDatasetType = findViewById(R.id.spDatasetType);

        etTotalItems = findViewById(R.id.etTotalItems);
        etApprovedItems = findViewById(R.id.etApprovedItems);
        etSubmittedItems = findViewById(R.id.etSubmittedItems);
        etPendingItems = findViewById(R.id.etPendingItems);
        etRejectedItems = findViewById(R.id.etRejectedItems);

        etTotalItems.setInputType(InputType.TYPE_CLASS_NUMBER);
        etApprovedItems.setInputType(InputType.TYPE_CLASS_NUMBER);
        etSubmittedItems.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPendingItems.setInputType(InputType.TYPE_CLASS_NUMBER);
        etRejectedItems.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupSpinner() {
        String[] types = {"image", "text", "audio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                types
        );
        spDatasetType.setAdapter(adapter);
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String mode = intent.getStringExtra(DatasetsActivity.EXTRA_DATASET_MODE);
        if (mode != null) {
            currentMode = mode;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentDataset = intent.getSerializableExtra(
                    DatasetsActivity.EXTRA_DATASET_RESULT,
                    DatasetItem.class
            );
        } else {
            currentDataset = (DatasetItem) intent.getSerializableExtra(
                    DatasetsActivity.EXTRA_DATASET_RESULT
            );
        }
    }

    private void bindDataIfEditMode() {
        if (!DatasetsActivity.MODE_EDIT.equals(currentMode) || currentDataset == null) {
            return;
        }

        etDatasetName.setText(safeText(currentDataset.getName()));
        etDatasetDescription.setText(safeText(currentDataset.getDescription()));

        String type = safeText(currentDataset.getType()).toLowerCase(Locale.getDefault());
        if ("text".equals(type)) {
            spDatasetType.setSelection(1);
        } else if ("audio".equals(type)) {
            spDatasetType.setSelection(2);
        } else {
            spDatasetType.setSelection(0);
        }

        etTotalItems.setText(String.valueOf(currentDataset.getTotalItems()));
        etApprovedItems.setText(String.valueOf(currentDataset.getApprovedItems()));
        etSubmittedItems.setText(String.valueOf(currentDataset.getSubmittedItems()));
        etPendingItems.setText(String.valueOf(currentDataset.getPendingAnnotationItems()));
        etRejectedItems.setText(String.valueOf(currentDataset.getRejectedItems()));
    }

    private void setupActions() {
        btnBackCreateDataset.setOnClickListener(v -> finish());
        btnSaveDataset.setOnClickListener(v -> saveDataset());
    }

    private void saveDataset() {
        String name = etDatasetName.getText().toString().trim();
        String description = etDatasetDescription.getText().toString().trim();
        String type = spDatasetType.getSelectedItem().toString();

        if (name.isEmpty()) {
            etDatasetName.setError("Nhập tên dataset");
            return;
        }

        int totalItems = parseNumber(etTotalItems, "Nhập total items");
        int approvedItems = parseNumber(etApprovedItems, "Nhập approved items");
        int submittedItems = parseNumber(etSubmittedItems, "Nhập under review items");
        int pendingItems = parseNumber(etPendingItems, "Nhập pending items");
        int rejectedItems = parseNumber(etRejectedItems, "Nhập rejected items");

        if (totalItems < 0 || approvedItems < 0 || submittedItems < 0 || pendingItems < 0 || rejectedItems < 0) {
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (approvedItems > totalItems) {
            etApprovedItems.setError("Approved không được lớn hơn total");
            return;
        }

        DatasetItem item = new DatasetItem();
        if (DatasetsActivity.MODE_EDIT.equals(currentMode) && currentDataset != null) {
            item.setId(currentDataset.getId());
        }

        item.setName(name);
        item.setDescription(description);
        item.setType(type);
        item.setTotalItems(totalItems);
        item.setApprovedItems(approvedItems);
        item.setSubmittedItems(submittedItems);
        item.setPendingAnnotationItems(pendingItems);
        item.setRejectedItems(rejectedItems);
        item.setCreatedAt(getNowText());

        long insertResult = -1;
        int updateResult = 0;

        if (DatasetsActivity.MODE_EDIT.equals(currentMode) && currentDataset != null) {
            updateResult = datasetDbHelper.updateDataset(item);
            if (updateResult <= 0) {
                Toast.makeText(this, "Cập nhật dataset thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            insertResult = datasetDbHelper.insertDataset(item);
            if (insertResult <= 0) {
                Toast.makeText(this, "Tạo dataset thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setId((int) insertResult);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(DatasetsActivity.EXTRA_DATASET_RESULT, item);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(
                this,
                DatasetsActivity.MODE_EDIT.equals(currentMode) ? "Dataset updated" : "Dataset created",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private int parseNumber(EditText editText, String errorText) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setText("0");
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            editText.setError(errorText);
            return -1;
        }
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
