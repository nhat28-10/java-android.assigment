package com.example.groupassignment.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.DatasetSourceItem;

import java.util.Locale;

public class DatasetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DATASET_DETAIL = "extra_dataset_detail";
    public static final String EXTRA_DETAIL_ACTION = "extra_dataset_detail_action";

    public static final String ACTION_UPDATED = "updated";
    public static final String ACTION_DELETED = "deleted";

    private ImageButton btnBackDatasetDetail;
    private Button btnEditDatasetDetail;
    private Button btnDeleteDatasetDetail;

    private TextView tvDatasetNameDetail;
    private TextView tvDatasetDescriptionDetail;
    private TextView tvDatasetTypeDetail;
    private TextView tvDatasetStatusDetail;
    private TextView tvDatasetCreatedAtDetail;

    private TextView tvTotalItemsDetail;
    private TextView tvApprovedItemsDetail;
    private TextView tvSubmittedItemsDetail;
    private TextView tvPendingItemsDetail;
    private TextView tvRejectedItemsDetail;
    private TextView tvProgressTextDetail;
    private TextView tvSourceItemsSummary;
    private TextView tvEmptySourceItems;

    private LinearLayout layoutSourceItems;
    private ProgressBar progressDatasetDetail;

    private DatasetItem currentDataset;
    private DatasetDbHelper datasetDbHelper;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    DatasetItem updatedDataset;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        updatedDataset = data.getSerializableExtra(
                                DatasetsActivity.EXTRA_DATASET_RESULT,
                                DatasetItem.class
                        );
                    } else {
                        updatedDataset = (DatasetItem) data.getSerializableExtra(
                                DatasetsActivity.EXTRA_DATASET_RESULT
                        );
                    }

                    if (updatedDataset != null) {
                        currentDataset = updatedDataset;
                        bindDatasetData(updatedDataset);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(DatasetsActivity.EXTRA_DATASET_RESULT, updatedDataset);
                        resultIntent.putExtra(EXTRA_DETAIL_ACTION, ACTION_UPDATED);
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(this, "Dataset updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_detail);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        datasetDbHelper = new DatasetDbHelper(this);

        initViews();
        readIntentData();
        setupActions();
        bindDatasetData(currentDataset);
    }

    private void initViews() {
        btnBackDatasetDetail = findViewById(R.id.btnBackDatasetDetail);
        btnEditDatasetDetail = findViewById(R.id.btnEditDatasetDetail);
        btnDeleteDatasetDetail = findViewById(R.id.btnDeleteDatasetDetail);

        tvDatasetNameDetail = findViewById(R.id.tvDatasetNameDetail);
        tvDatasetDescriptionDetail = findViewById(R.id.tvDatasetDescriptionDetail);
        tvDatasetTypeDetail = findViewById(R.id.tvDatasetTypeDetail);
        tvDatasetStatusDetail = findViewById(R.id.tvDatasetStatusDetail);
        tvDatasetCreatedAtDetail = findViewById(R.id.tvDatasetCreatedAtDetail);

        tvTotalItemsDetail = findViewById(R.id.tvTotalItemsDetail);
        tvApprovedItemsDetail = findViewById(R.id.tvApprovedItemsDetail);
        tvSubmittedItemsDetail = findViewById(R.id.tvSubmittedItemsDetail);
        tvPendingItemsDetail = findViewById(R.id.tvPendingItemsDetail);
        tvRejectedItemsDetail = findViewById(R.id.tvRejectedItemsDetail);
        tvProgressTextDetail = findViewById(R.id.tvProgressTextDetail);
        tvSourceItemsSummary = findViewById(R.id.tvSourceItemsSummary);
        tvEmptySourceItems = findViewById(R.id.tvEmptySourceItems);

        layoutSourceItems = findViewById(R.id.layoutSourceItems);
        progressDatasetDetail = findViewById(R.id.progressDatasetDetail);
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentDataset = intent.getSerializableExtra(EXTRA_DATASET_DETAIL, DatasetItem.class);
        } else {
            currentDataset = (DatasetItem) intent.getSerializableExtra(EXTRA_DATASET_DETAIL);
        }

        if (currentDataset == null) {
            Toast.makeText(this, "Dataset data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupActions() {
        btnBackDatasetDetail.setOnClickListener(v -> finish());

        btnEditDatasetDetail.setOnClickListener(v -> {
            if (currentDataset == null) return;

            Intent intent = new Intent(DatasetDetailActivity.this, CreateDatasetActivity.class);
            intent.putExtra(DatasetsActivity.EXTRA_DATASET_MODE, DatasetsActivity.MODE_EDIT);
            intent.putExtra(DatasetsActivity.EXTRA_DATASET_RESULT, currentDataset);
            editLauncher.launch(intent);
        });

        btnDeleteDatasetDetail.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDeleteConfirmDialog() {
        if (currentDataset == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete dataset")
                .setMessage("Are you sure you want to delete \"" + safeText(currentDataset.getName()) + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentDataset())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentDataset() {
        if (currentDataset == null) return;

        int deletedRows = datasetDbHelper.deleteDatasetById(currentDataset.getId());
        if (deletedRows > 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(DatasetsActivity.EXTRA_DATASET_RESULT, currentDataset);
            resultIntent.putExtra(EXTRA_DETAIL_ACTION, ACTION_DELETED);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Dataset deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindDatasetData(DatasetItem item) {
        if (item == null) return;

        tvDatasetNameDetail.setText(safeText(item.getName()));
        tvDatasetDescriptionDetail.setText(safeText(item.getDescription()));
        tvDatasetTypeDetail.setText(safeText(item.getType()).toUpperCase(Locale.getDefault()));
        tvDatasetCreatedAtDetail.setText("Created at: " + safeText(item.getCreatedAt()));

        tvTotalItemsDetail.setText(String.valueOf(item.getTotalItems()));
        tvApprovedItemsDetail.setText(String.valueOf(item.getApprovedItems()));
        tvSubmittedItemsDetail.setText(String.valueOf(item.getSubmittedItems()));
        tvPendingItemsDetail.setText(String.valueOf(item.getPendingAnnotationItems()));
        tvRejectedItemsDetail.setText(String.valueOf(item.getRejectedItems()));
        tvProgressTextDetail.setText(item.getProgressPercent() + "%");

        tvDatasetStatusDetail.setText(item.getStatusLabel());
        tvDatasetStatusDetail.setTextColor(getStatusTextColor(item.getStatusCode()));
        tvDatasetStatusDetail.setBackground(makeRoundedDrawable(getStatusBgColor(item.getStatusCode())));

        progressDatasetDetail.setProgress(item.getProgressPercent());
        progressDatasetDetail.setProgressTintList(
                ColorStateList.valueOf(getStatusSolidColor(item.getStatusCode()))
        );

        renderSourceItems(item.getId());
    }

    private void renderSourceItems(int datasetId) {
        layoutSourceItems.removeAllViews();
        java.util.List<DatasetSourceItem> sourceItems = datasetDbHelper.getDatasetItemsForDataset(datasetId);
        tvSourceItemsSummary.setText(String.format(Locale.getDefault(), "Source Items (%d)", sourceItems.size()));
        tvEmptySourceItems.setVisibility(sourceItems.isEmpty() ? View.VISIBLE : View.GONE);

        for (DatasetSourceItem sourceItem : sourceItems) {
            layoutSourceItems.addView(buildSourceItemView(sourceItem));
        }
    }

    private View buildSourceItemView(DatasetSourceItem sourceItem) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(12), dp(10), dp(12), dp(10));
        container.setBackground(makeRoundedDrawable(0xFF0F172A));

        TextView tvName = new TextView(this);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(14f);
        tvName.setText(safeText(sourceItem.getItemName()));

        TextView tvPath = new TextView(this);
        tvPath.setTextColor(0xFF94A3B8);
        tvPath.setTextSize(12f);
        tvPath.setText(safeText(sourceItem.getItemPathOrContent()));

        container.addView(tvName);
        container.addView(tvPath);
        return container;
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
}
