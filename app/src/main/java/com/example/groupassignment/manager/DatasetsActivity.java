package com.example.groupassignment.manager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Intent;

public class DatasetsActivity extends AppCompatActivity {

    private ImageButton btnBackDatasets;
    private Button btnCreateDataset;
    private EditText etSearchDataset;

    private TextView tvTotalDatasets;
    private TextView tvTotalItems;
    private TextView tvAnnotatedItems;
    private TextView tvReadyForAi;

    private LinearLayout layoutDatasetList;
    private TextView tvEmptyDatasets;

    private DatasetDbHelper datasetDbHelper;
    private SessionManager sessionManager;
    private final List<DatasetItem> allDatasets = new ArrayList<>();
    public static final String EXTRA_DATASET_RESULT = "extra_dataset_result";
    public static final String EXTRA_DATASET_MODE = "extra_dataset_mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_datasets);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        datasetDbHelper = new DatasetDbHelper(this);
        datasetDbHelper.seedSampleDatasetsIfEmpty();

        initViews();
        setupActions();
        refreshScreen();
    }

    private void initViews() {
        btnBackDatasets = findViewById(R.id.btnBackDatasets);
        btnCreateDataset = findViewById(R.id.btnCreateDataset);
        etSearchDataset = findViewById(R.id.etSearchDataset);

        tvTotalDatasets = findViewById(R.id.tvTotalDatasets);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvAnnotatedItems = findViewById(R.id.tvAnnotatedItems);
        tvReadyForAi = findViewById(R.id.tvReadyForAi);

        layoutDatasetList = findViewById(R.id.layoutDatasetList);
        tvEmptyDatasets = findViewById(R.id.tvEmptyDatasets);
    }

    private void setupActions() {
        btnBackDatasets.setOnClickListener(v -> finish());

        btnCreateDataset.setOnClickListener(v -> {
            Intent intent = new Intent(DatasetsActivity.this, CreateDatasetActivity.class);
            intent.putExtra(EXTRA_DATASET_MODE, MODE_CREATE);
            datasetLauncher.launch(intent);
        });

        etSearchDataset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderDatasetList(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void refreshScreen() {
        loadDatasetsFromDb();
        bindStats();
        renderDatasetList(etSearchDataset.getText().toString().trim());
    }

    private void loadDatasetsFromDb() {
        allDatasets.clear();
        allDatasets.addAll(datasetDbHelper.getAllDatasets());
    }

    private void bindStats() {
        int totalDatasets = allDatasets.size();
        int totalItems = 0;
        int totalAnnotated = 0;
        int readyCount = 0;

        for (DatasetItem item : allDatasets) {
            totalItems += item.getTotalItems();
            totalAnnotated += item.getApprovedItems();

            if (item.isReadyForAi()) {
                readyCount++;
            }
        }

        tvTotalDatasets.setText(String.valueOf(totalDatasets));
        tvTotalItems.setText(String.valueOf(totalItems));
        tvAnnotatedItems.setText(String.valueOf(totalAnnotated));
        tvReadyForAi.setText(String.valueOf(readyCount));
    }

    private void renderDatasetList(String keyword) {
        layoutDatasetList.removeAllViews();

        List<DatasetItem> filtered = new ArrayList<>();
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());

        for (DatasetItem item : allDatasets) {
            String name = safeText(item.getName()).toLowerCase(Locale.getDefault());
            String type = safeText(item.getType()).toLowerCase(Locale.getDefault());
            String status = safeText(item.getStatusLabel()).toLowerCase(Locale.getDefault());

            if (query.isEmpty()
                    || name.contains(query)
                    || type.contains(query)
                    || status.contains(query)) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            tvEmptyDatasets.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyDatasets.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DatasetItem item : filtered) {
            View card = inflater.inflate(R.layout.item_manager_dataset, layoutDatasetList, false);

            TextView tvDatasetName = card.findViewById(R.id.tvDatasetName);
            TextView tvDatasetDescription = card.findViewById(R.id.tvDatasetDescription);
            TextView tvDatasetType = card.findViewById(R.id.tvDatasetType);
            TextView tvDatasetCount = card.findViewById(R.id.tvDatasetCount);
            TextView tvDatasetProgress = card.findViewById(R.id.tvDatasetProgress);
            TextView tvDatasetStatus = card.findViewById(R.id.tvDatasetStatus);

            ProgressBar progressDataset = card.findViewById(R.id.progressDataset);

            Button btnViewDataset = card.findViewById(R.id.btnViewDataset);
            Button btnExportDataset = card.findViewById(R.id.btnExportDataset);
            Button btnDeleteDataset = card.findViewById(R.id.btnDeleteDataset);
            ImageButton btnDeleteDatasetItem = card.findViewById(R.id.btnDeleteDatasetItem);

            tvDatasetName.setText(safeText(item.getName()));
            tvDatasetDescription.setText(safeText(item.getDescription()));
            tvDatasetType.setText(safeText(item.getType()).toUpperCase(Locale.getDefault()));
            tvDatasetCount.setText(item.getTotalItems() + " items");
            tvDatasetProgress.setText(item.getProgressPercent() + "%");
            progressDataset.setProgress(item.getProgressPercent());

            int statusTextColor = getStatusTextColor(item.getStatusCode());
            int statusBgColor = getStatusBgColor(item.getStatusCode());
            int progressColor = getStatusSolidColor(item.getStatusCode());

            tvDatasetStatus.setText(item.getStatusLabel());
            tvDatasetStatus.setTextColor(statusTextColor);
            tvDatasetStatus.setBackground(makeRoundedDrawable(statusBgColor));

            progressDataset.setProgressTintList(ColorStateList.valueOf(progressColor));

            btnExportDataset.setEnabled(item.isReadyForAi());
            btnExportDataset.setAlpha(item.isReadyForAi() ? 1f : 0.45f);

            btnViewDataset.setOnClickListener(v -> {
                Intent intent = new Intent(DatasetsActivity.this, DatasetDetailActivity.class);
                intent.putExtra(DatasetDetailActivity.EXTRA_DATASET_DETAIL, item);
                datasetLauncher.launch(intent);
            });

            btnExportDataset.setOnClickListener(v -> {
                if (!item.isReadyForAi()) {
                    Toast.makeText(this, "Dataset chưa sẵn sàng để export", Toast.LENGTH_SHORT).show();
                    return;
                }
                showExportDialog(item);
            });

            View.OnClickListener deleteListener = v -> showDeleteConfirmDialog(item);
            btnDeleteDataset.setOnClickListener(deleteListener);
            btnDeleteDatasetItem.setOnClickListener(deleteListener);

            layoutDatasetList.addView(card);
        }
    }

    private void showExportDialog(DatasetItem item) {
        String exportPreview =
                "{\n" +
                        "  \"dataset\": {\n" +
                        "    \"id\": " + item.getId() + ",\n" +
                        "    \"name\": \"" + safeText(item.getName()) + "\",\n" +
                        "    \"type\": \"" + safeText(item.getType()) + "\",\n" +
                        "    \"totalRawItems\": " + item.getTotalItems() + ",\n" +
                        "    \"totalFinalItems\": " + item.getApprovedItems() + "\n" +
                        "  },\n" +
                        "  \"exportedAt\": \"" + getNowText() + "\"\n" +
                        "}";

        new AlertDialog.Builder(this)
                .setTitle("Export Final Dataset")
                .setMessage(exportPreview)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmDialog(DatasetItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Dataset")
                .setMessage("Bạn có chắc muốn xóa dataset \"" + safeText(item.getName()) + "\" không?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedRows = datasetDbHelper.deleteDatasetById(item.getId());
                    if (deletedRows > 0) {
                        refreshScreen();
                        Toast.makeText(this, "Đã xóa dataset", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Xóa dataset thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "Not set" : text;
    }

    private GradientDrawable makeRoundedDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(999));
        return drawable;
    }

    private int getStatusBgColor(String status) {
        switch (status) {
            case "ready":
                return 0x2622C55E;
            case "under_review":
                return 0x26F59E0B;
            case "annotating":
                return 0x263B82F6;
            case "not_started":
            default:
                return 0x2664748B;
        }
    }

    private int getStatusTextColor(String status) {
        switch (status) {
            case "ready":
                return 0xFF22C55E;
            case "under_review":
                return 0xFFF59E0B;
            case "annotating":
                return 0xFF3B82F6;
            case "not_started":
            default:
                return 0xFF94A3B8;
        }
    }
    private final androidx.activity.result.ActivityResultLauncher<Intent> datasetLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshScreen();
                }
            });
    private int getStatusSolidColor(String status) {
        switch (status) {
            case "ready":
                return Color.parseColor("#22C55E");
            case "under_review":
                return Color.parseColor("#F59E0B");
            case "annotating":
                return Color.parseColor("#3B82F6");
            case "not_started":
            default:
                return Color.parseColor("#64748B");
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
