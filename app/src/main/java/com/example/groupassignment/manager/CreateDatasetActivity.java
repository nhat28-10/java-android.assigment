package com.example.groupassignment.manager;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateDatasetActivity extends AppCompatActivity {

    private ImageButton btnBackCreateDataset;
    private Button btnSaveDataset, btnSelectImages;

    private EditText etDatasetName, etDatasetDescription;
    private Spinner spDatasetType;
    private TextView tvUploadStatus, tvTotalItems, tvPendingItems;

    private DatasetDbHelper datasetDbHelper;
    private DatasetItem currentDataset;
    private String currentMode = DatasetsActivity.MODE_CREATE;
    private List<String> selectedUris = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleImagePickResult(result.getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dataset);

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
        btnSelectImages = findViewById(R.id.btnSelectImages);

        etDatasetName = findViewById(R.id.etDatasetName);
        etDatasetDescription = findViewById(R.id.etDatasetDescription);
        spDatasetType = findViewById(R.id.spDatasetType);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);

        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvPendingItems = findViewById(R.id.tvPendingItems);
    }

    private void setupSpinner() {
        String[] types = {"image", "text", "audio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spDatasetType.setAdapter(adapter);
        spDatasetType.setEnabled(false);
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;
        currentMode = intent.getStringExtra(DatasetsActivity.EXTRA_DATASET_MODE);
        if (currentMode == null) currentMode = DatasetsActivity.MODE_CREATE;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentDataset = intent.getSerializableExtra(DatasetsActivity.EXTRA_DATASET_RESULT, DatasetItem.class);
            } else {
                currentDataset = (DatasetItem) intent.getSerializableExtra(DatasetsActivity.EXTRA_DATASET_RESULT);
            }
        } catch (Exception e) {
            Log.e("CreateDataset", "Error reading intent data", e);
        }
    }

    private void bindDataIfEditMode() {
        if (!DatasetsActivity.MODE_EDIT.equals(currentMode) || currentDataset == null) return;

        etDatasetName.setText(currentDataset.getName());
        etDatasetDescription.setText(currentDataset.getDescription());
        selectedUris = new ArrayList<>(currentDataset.getImageUriList());
        updateUploadUi();
    }

    private void setupActions() {
        btnBackCreateDataset.setOnClickListener(v -> finish());
        btnSelectImages.setOnClickListener(v -> openImagePicker());
        btnSaveDataset.setOnClickListener(v -> saveDataset());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(intent);
    }

    private void handleImagePickResult(Intent data) {
        selectedUris.clear();
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        
        if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                persistUriPermission(uri, takeFlags);
                selectedUris.add(uri.toString());
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            persistUriPermission(uri, takeFlags);
            selectedUris.add(uri.toString());
        }
        updateUploadUi();
    }

    private void persistUriPermission(Uri uri, int takeFlags) {
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (Exception e) {
            Log.e("CreateDataset", "Failed to take persistable permission for: " + uri, e);
        }
    }

    private void updateUploadUi() {
        int count = selectedUris.size();
        tvUploadStatus.setText(count + (count == 1 ? " image" : " images") + " selected");
        tvTotalItems.setText(String.valueOf(count));
        tvPendingItems.setText(String.valueOf(count));
    }

    private void saveDataset() {
        String name = etDatasetName.getText().toString().trim();
        if (name.isEmpty()) {
            etDatasetName.setError("Name required");
            return;
        }
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "Please upload at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        DatasetItem item = (currentDataset != null) ? currentDataset : new DatasetItem();
        item.setName(name);
        item.setDescription(etDatasetDescription.getText().toString().trim());
        item.setType("image");
        item.setTotalItems(selectedUris.size());
        item.setPendingAnnotationItems(selectedUris.size());
        // Đảm bảo cập nhật lại danh sách URI mới nhất
        item.setImageUriList(selectedUris);
        
        if (item.getCreatedAt() == null || item.getCreatedAt().isEmpty()) {
            item.setCreatedAt(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        }

        long resultId;
        if (DatasetsActivity.MODE_EDIT.equals(currentMode)) {
            resultId = datasetDbHelper.updateDataset(item);
        } else {
            resultId = datasetDbHelper.insertDataset(item);
        }

        if (resultId != -1) {
            Toast.makeText(this, "Dataset saved successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to save dataset", Toast.LENGTH_SHORT).show();
        }
    }
}
