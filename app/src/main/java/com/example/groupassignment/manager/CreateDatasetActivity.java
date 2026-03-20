package com.example.groupassignment.manager;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.DatasetSourceItem;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CreateDatasetActivity extends AppCompatActivity {

    public static final String EXTRA_CREATED_DATASET_ID = "extra_created_dataset_id";
    public static final String EXTRA_CREATED_DATASET_NAME = "extra_created_dataset_name";

    private ImageButton btnBackCreateDataset;
    private Button btnSaveDataset;
    private Button btnPickDatasetFiles;
    private EditText etDatasetName;
    private EditText etDatasetDescription;
    private Spinner spDatasetType;
    private TextView tvFilePickerHint;
    private TextView tvSelectedFilesCount;
    private TextView tvEmptySelectedFiles;
    private LinearLayout layoutSelectedFiles;

    private DatasetDbHelper datasetDbHelper;
    private SessionManager sessionManager;
    private DatasetItem currentDataset;
    private String currentMode = DatasetsActivity.MODE_CREATE;
    private final List<DatasetSourceItem> selectedSourceItems = new ArrayList<>();

    private final ActivityResultLauncher<String[]> pickDatasetFilesLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), this::handlePickedUris);

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
        updateFilePickerHint();
        renderSelectedFiles();
    }

    private void initViews() {
        btnBackCreateDataset = findViewById(R.id.btnBackCreateDataset);
        btnSaveDataset = findViewById(R.id.btnSaveDataset);
        btnPickDatasetFiles = findViewById(R.id.btnPickDatasetFiles);
        etDatasetName = findViewById(R.id.etDatasetName);
        etDatasetDescription = findViewById(R.id.etDatasetDescription);
        spDatasetType = findViewById(R.id.spDatasetType);
        tvFilePickerHint = findViewById(R.id.tvFilePickerHint);
        tvSelectedFilesCount = findViewById(R.id.tvSelectedFilesCount);
        tvEmptySelectedFiles = findViewById(R.id.tvEmptySelectedFiles);
        layoutSelectedFiles = findViewById(R.id.layoutSelectedFiles);
    }

    private boolean isCreateMode() {
        return !DatasetsActivity.MODE_EDIT.equals(currentMode);
    }

    private void setupSpinner() {
        String[] types = {"image", "audio", "text"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spDatasetType.setAdapter(adapter);
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String mode = intent.getStringExtra(DatasetsActivity.EXTRA_DATASET_MODE);
        if (mode != null) {
            currentMode = mode;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentDataset = intent.getSerializableExtra(DatasetsActivity.EXTRA_DATASET_RESULT, DatasetItem.class);
        } else {
            currentDataset = (DatasetItem) intent.getSerializableExtra(DatasetsActivity.EXTRA_DATASET_RESULT);
        }
    }

    private void bindDataIfEditMode() {
        if (!DatasetsActivity.MODE_EDIT.equals(currentMode) || currentDataset == null) {
            return;
        }
        etDatasetName.setText(safeText(currentDataset.getName()));
        etDatasetDescription.setText(safeText(currentDataset.getDescription()));
        spDatasetType.setSelection(getTypeSelectionIndex(currentDataset.getType()));

        selectedSourceItems.clear();
        selectedSourceItems.addAll(datasetDbHelper.getDatasetItemsForDataset(currentDataset.getId()));
    }

    private void setupActions() {
        btnBackCreateDataset.setOnClickListener(v -> finish());
        btnSaveDataset.setOnClickListener(v -> saveDataset());
        btnPickDatasetFiles.setOnClickListener(v -> openFilePicker());
        spDatasetType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateFilePickerHint();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateFilePickerHint();
            }
        });
    }

    private void openFilePicker() {
        pickDatasetFilesLauncher.launch(buildMimeTypesForSelection());
    }

    private String[] buildMimeTypesForSelection() {
        String selectedType = getSelectedDatasetType();
        if ("audio".equals(selectedType)) {
            return new String[]{"audio/*", "application/zip", "application/x-zip-compressed", "application/octet-stream"};
        }
        if ("text".equals(selectedType)) {
            return new String[]{"text/*", "application/zip", "application/x-zip-compressed", "application/octet-stream"};
        }
        return new String[]{"image/*", "application/zip", "application/x-zip-compressed", "application/octet-stream"};
    }

    private void handlePickedUris(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        Set<String> existingPaths = new HashSet<>();
        for (DatasetSourceItem sourceItem : selectedSourceItems) {
            existingPaths.add(safeText(sourceItem.getItemPathOrContent()));
        }

        int addedCount = 0;
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }

            String uriValue = uri.toString();
            if (existingPaths.contains(uriValue)) {
                continue;
            }

            takePersistableReadPermission(uri);

            DatasetSourceItem sourceItem = new DatasetSourceItem();
            sourceItem.setItemName(resolveDisplayName(uri));
            sourceItem.setItemPathOrContent(uriValue);
            sourceItem.setMimeType(resolveMimeType(uri));
            sourceItem.setItemType(getSelectedDatasetType());
            sourceItem.setStatus(DatasetDbHelper.ITEM_STATUS_PENDING);
            selectedSourceItems.add(sourceItem);
            existingPaths.add(uriValue);
            addedCount++;
        }

        renderSelectedFiles();

        if (addedCount == 0) {
            Toast.makeText(this, "Các file đã chọn đã tồn tại trong dataset", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePersistableReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some providers do not support persistable permissions.
        }
    }

    private void saveDataset() {
        String name = etDatasetName.getText().toString().trim();
        if (name.isEmpty()) {
            etDatasetName.setError("Nhập tên dataset");
            return;
        }
        if (selectedSourceItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 file hoặc file zip", Toast.LENGTH_SHORT).show();
            return;
        }

        DatasetItem item = new DatasetItem();
        if (DatasetsActivity.MODE_EDIT.equals(currentMode) && currentDataset != null) {
            item.setId(currentDataset.getId());
            item.setProjectId(currentDataset.getProjectId());
            item.setCreatedAt(currentDataset.getCreatedAt());
            item.setCreatedBy(currentDataset.getCreatedBy());
        }

        item.setName(name);
        item.setDescription(etDatasetDescription.getText().toString().trim());
        item.setType(getSelectedDatasetType());
        item.setTotalItems(0);
        item.setApprovedItems(0);
        item.setSubmittedItems(0);
        item.setPendingAnnotationItems(0);
        item.setRejectedItems(0);
        String now = getNowText();
        if (TextUtils.isEmpty(item.getCreatedAt())) {
            item.setCreatedAt(now);
        }
        item.setUpdatedAt(now);
        if (item.getCreatedBy() <= 0) {
            item.setCreatedBy((int) sessionManager.getUserId());
        }

        if (DatasetsActivity.MODE_EDIT.equals(currentMode) && currentDataset != null) {
            int updateResult = datasetDbHelper.updateDataset(item);
            if (updateResult <= 0) {
                Toast.makeText(this, "Cập nhật dataset thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            long insertResult = datasetDbHelper.insertDataset(item);
            if (insertResult <= 0) {
                Toast.makeText(this, "Tạo dataset thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setId((int) insertResult);
        }

        datasetDbHelper.replaceDatasetItems(item.getId(), buildItemsForSave(item));
        DatasetItem savedDataset = datasetDbHelper.getDatasetById(item.getId());
        deliverDatasetResult(savedDataset == null ? item : savedDataset, isCreateMode());
    }

    private List<DatasetSourceItem> buildItemsForSave(DatasetItem dataset) {
        List<DatasetSourceItem> items = new ArrayList<>();
        String now = getNowText();
        for (DatasetSourceItem sourceItem : selectedSourceItems) {
            DatasetSourceItem copy = new DatasetSourceItem();
            copy.setId(sourceItem.getId());
            copy.setDatasetId(dataset.getId());
            copy.setProjectId(dataset.getProjectId());
            copy.setItemName(TextUtils.isEmpty(sourceItem.getItemName())
                    ? dataset.getName() + " Source #" + (items.size() + 1)
                    : sourceItem.getItemName());
            copy.setItemPathOrContent(sourceItem.getItemPathOrContent());
            copy.setMimeType(resolveMimeTypeFallback(sourceItem.getMimeType(), dataset.getType()));
            copy.setItemType(dataset.getType());
            copy.setStatus(TextUtils.isEmpty(sourceItem.getStatus())
                    ? DatasetDbHelper.ITEM_STATUS_PENDING
                    : sourceItem.getStatus());
            copy.setCreatedAt(TextUtils.isEmpty(sourceItem.getCreatedAt()) ? now : sourceItem.getCreatedAt());
            copy.setUpdatedAt(now);
            items.add(copy);
        }
        return items;
    }

    private void deliverDatasetResult(DatasetItem item, boolean openProjectCreation) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(DatasetsActivity.EXTRA_DATASET_RESULT, item);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this,
                DatasetsActivity.MODE_EDIT.equals(currentMode) ? "Dataset updated" : "Dataset created",
                Toast.LENGTH_SHORT).show();

        if (openProjectCreation) {
            Intent intent = new Intent(this, CreateProjectActivity.class);
            intent.putExtra(CreateDatasetActivity.EXTRA_CREATED_DATASET_ID, item.getId());
            intent.putExtra(CreateDatasetActivity.EXTRA_CREATED_DATASET_NAME, item.getName());
            intent.putExtra(ManagerProjectsActivity.EXTRA_PROJECT_MODE, ManagerProjectsActivity.MODE_CREATE);
            startActivity(intent);
        }
        finish();
    }

    private void renderSelectedFiles() {
        layoutSelectedFiles.removeAllViews();
        tvSelectedFilesCount.setText(String.format(Locale.getDefault(), "Selected Files (%d)", selectedSourceItems.size()));
        tvEmptySelectedFiles.setVisibility(selectedSourceItems.isEmpty() ? View.VISIBLE : View.GONE);

        for (int index = 0; index < selectedSourceItems.size(); index++) {
            DatasetSourceItem sourceItem = selectedSourceItems.get(index);
            layoutSelectedFiles.addView(buildSelectedFileRow(sourceItem, index));
        }
    }

    private View buildSelectedFileRow(DatasetSourceItem sourceItem, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_glass_card));

        LinearLayout infoColumn = new LinearLayout(this);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoColumn.setLayoutParams(infoParams);
        infoColumn.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        tvName.setTextSize(14f);
        tvName.setText(safeText(sourceItem.getItemName()));

        TextView tvPath = new TextView(this);
        tvPath.setTextColor(0xFF94A3B8);
        tvPath.setTextSize(12f);
        tvPath.setText(safeText(sourceItem.getItemPathOrContent()));

        infoColumn.addView(tvName);
        infoColumn.addView(tvPath);

        Button btnRemove = new Button(this);
        btnRemove.setText("Remove");
        btnRemove.setAllCaps(false);
        btnRemove.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnRemove.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
        btnRemove.setOnClickListener(v -> {
            selectedSourceItems.remove(index);
            renderSelectedFiles();
        });

        row.addView(infoColumn);
        row.addView(btnRemove);

        LinearLayout.LayoutParams rowMarginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowMarginParams.bottomMargin = dp(10);
        row.setLayoutParams(rowMarginParams);
        return row;
    }

    private int getTypeSelectionIndex(String type) {
        String normalized = safeText(type).toLowerCase(Locale.getDefault());
        if ("audio".equals(normalized)) {
            return 1;
        }
        if ("text".equals(normalized)) {
            return 2;
        }
        return 0;
    }

    private void updateFilePickerHint() {
        String datasetType = getSelectedDatasetType();
        String acceptedLabel;
        if ("audio".equals(datasetType)) {
            acceptedLabel = "Chọn audio files hoặc file zip. Hiện tại app lưu URI/path làm source item placeholder.";
        } else if ("text".equals(datasetType)) {
            acceptedLabel = "Chọn text files hoặc file zip. Hiện tại app lưu URI/path làm source item placeholder.";
        } else {
            acceptedLabel = "Chọn image files hoặc file zip. Hiện tại app lưu URI/path làm source item placeholder.";
        }
        tvFilePickerHint.setText(acceptedLabel);
    }

    private String getSelectedDatasetType() {
        Object selected = spDatasetType.getSelectedItem();
        return selected == null ? "image" : selected.toString().trim().toLowerCase(Locale.getDefault());
    }

    private String resolveDisplayName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String displayName = cursor.getString(index);
                        if (!TextUtils.isEmpty(displayName)) {
                            return displayName;
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        String lastSegment = uri.getLastPathSegment();
        return TextUtils.isEmpty(lastSegment) ? "source-item" : lastSegment;
    }

    private String resolveMimeType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return resolveMimeTypeFallback(mimeType, getSelectedDatasetType());
    }

    private String resolveMimeTypeFallback(String mimeType, String datasetType) {
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        if ("audio".equals(datasetType)) {
            return "audio/*";
        }
        if ("text".equals(datasetType)) {
            return "text/plain";
        }
        return "image/*";
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
