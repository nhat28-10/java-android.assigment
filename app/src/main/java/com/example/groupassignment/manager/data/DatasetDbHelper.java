package com.example.groupassignment.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.groupassignment.manager.model.DatasetItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatasetDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_DATASETS = "datasets";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TYPE = "type";
    public static final String COL_TOTAL_ITEMS = "total_items";
    public static final String COL_APPROVED_ITEMS = "approved_items";
    public static final String COL_SUBMITTED_ITEMS = "submitted_items";
    public static final String COL_PENDING_ANNOTATION_ITEMS = "pending_annotation_items";
    public static final String COL_REJECTED_ITEMS = "rejected_items";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_PROJECT_ID = "project_id";

    public DatasetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatasetsTableIfNeeded(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createDatasetsTableIfNeeded(db);
        ensureProjectIdColumn(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createDatasetsTableIfNeeded(db);
        ensureProjectIdColumn(db);
    }

    private void createDatasetsTableIfNeeded(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DATASETS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_TYPE + " TEXT DEFAULT 'image', "
                + COL_TOTAL_ITEMS + " INTEGER DEFAULT 0, "
                + COL_APPROVED_ITEMS + " INTEGER DEFAULT 0, "
                + COL_SUBMITTED_ITEMS + " INTEGER DEFAULT 0, "
                + COL_PENDING_ANNOTATION_ITEMS + " INTEGER DEFAULT 0, "
                + COL_REJECTED_ITEMS + " INTEGER DEFAULT 0, "
                + COL_CREATED_AT + " TEXT, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(createTable);
    }

    private void ensureProjectIdColumn(SQLiteDatabase db) {
        boolean hasProjectId = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_DATASETS + ")", null);
        if (cursor.moveToFirst()) {
            do {
                String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (COL_PROJECT_ID.equalsIgnoreCase(columnName)) {
                    hasProjectId = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (!hasProjectId) {
            db.execSQL("ALTER TABLE " + TABLE_DATASETS + " ADD COLUMN " + COL_PROJECT_ID + " INTEGER DEFAULT 0");
        }
    }

    public long insertDataset(DatasetItem dataset) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_DATASETS, null, toContentValues(dataset));
    }

    public int updateDataset(DatasetItem dataset) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(
                TABLE_DATASETS,
                toContentValues(dataset),
                COL_ID + "=?",
                new String[]{String.valueOf(dataset.getId())}
        );
    }

    public int deleteDatasetById(int datasetId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_DATASETS,
                COL_ID + "=?",
                new String[]{String.valueOf(datasetId)}
        );
    }

    public DatasetItem getDatasetById(int datasetId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DATASETS,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(datasetId)},
                null,
                null,
                null
        );

        DatasetItem item = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                item = cursorToDataset(cursor);
            }
            cursor.close();
        }
        return item;
    }

    public List<DatasetItem> getAllDatasets() {
        List<DatasetItem> datasets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DATASETS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                datasets.add(cursorToDataset(cursor));
            }
            cursor.close();
        }

        return datasets;
    }

    public List<DatasetItem> getUnassignedDatasets() {
        return queryDatasetsByProjectSelection(COL_PROJECT_ID + " IS NULL OR " + COL_PROJECT_ID + " = 0", null);
    }

    public List<DatasetItem> getDatasetsAvailableForProject(int projectId) {
        String selection = COL_PROJECT_ID + " IS NULL OR " + COL_PROJECT_ID + " = 0 OR " + COL_PROJECT_ID + " = ?";
        return queryDatasetsByProjectSelection(selection, new String[]{String.valueOf(projectId)});
    }

    private List<DatasetItem> queryDatasetsByProjectSelection(String selection, String[] selectionArgs) {
        List<DatasetItem> datasets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DATASETS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                datasets.add(cursorToDataset(cursor));
            }
            cursor.close();
        }
        return datasets;
    }

    public void assignDatasetsToProject(List<String> datasetNames, int projectId) {
        if (datasetNames == null || datasetNames.isEmpty()) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, projectId);

        for (String datasetName : datasetNames) {
            db.update(
                    TABLE_DATASETS,
                    values,
                    COL_NAME + "=?",
                    new String[]{datasetName}
            );
        }
    }

    public void releaseDatasetsFromProject(int projectId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, 0);

        db.update(
                TABLE_DATASETS,
                values,
                COL_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)}
        );
    }

    public void releaseDatasetsByNames(List<String> datasetNames, int projectId) {
        if (datasetNames == null || datasetNames.isEmpty()) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, 0);

        for (String datasetName : datasetNames) {
            db.update(
                    TABLE_DATASETS,
                    values,
                    COL_NAME + "=? AND " + COL_PROJECT_ID + "=?",
                    new String[]{datasetName, String.valueOf(projectId)}
            );
        }
    }

    public void seedSampleDatasetsIfEmpty() {
        if (!getAllDatasets().isEmpty()) {
            return;
        }

        String now = getNowText();

        insertDataset(new DatasetItem(
                0,
                "Human Detection",
                "Dataset ảnh phục vụ phát hiện người trong môi trường công nghiệp.",
                "image",
                240,
                240,
                0,
                0,
                0,
                now
        ));

        insertDataset(new DatasetItem(
                0,
                "Warehouse Audio",
                "Tập âm thanh tiếng máy móc và môi trường kho.",
                "audio",
                120,
                68,
                12,
                40,
                0,
                now
        ));

        insertDataset(new DatasetItem(
                0,
                "Support Tickets",
                "Dữ liệu text để phân loại nội dung ticket hỗ trợ.",
                "text",
                180,
                0,
                0,
                180,
                0,
                now
        ));

        insertDataset(new DatasetItem(
                0,
                "Vehicle Images",
                "Ảnh phương tiện giao thông để annotate bounding boxes.",
                "image",
                320,
                210,
                30,
                80,
                0,
                now
        ));
    }

    private ContentValues toContentValues(DatasetItem dataset) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, dataset.getName());
        values.put(COL_DESCRIPTION, dataset.getDescription());
        values.put(COL_TYPE, dataset.getType());
        values.put(COL_TOTAL_ITEMS, dataset.getTotalItems());
        values.put(COL_APPROVED_ITEMS, dataset.getApprovedItems());
        values.put(COL_SUBMITTED_ITEMS, dataset.getSubmittedItems());
        values.put(COL_PENDING_ANNOTATION_ITEMS, dataset.getPendingAnnotationItems());
        values.put(COL_REJECTED_ITEMS, dataset.getRejectedItems());
        values.put(COL_CREATED_AT, dataset.getCreatedAt());
        values.put(COL_PROJECT_ID, dataset.getProjectId());
        return values;
    }

    private DatasetItem cursorToDataset(Cursor cursor) {
        DatasetItem item = new DatasetItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
        item.setTotalItems(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_ITEMS)));
        item.setApprovedItems(cursor.getInt(cursor.getColumnIndexOrThrow(COL_APPROVED_ITEMS)));
        item.setSubmittedItems(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUBMITTED_ITEMS)));
        item.setPendingAnnotationItems(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PENDING_ANNOTATION_ITEMS)));
        item.setRejectedItems(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REJECTED_ITEMS)));
        item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));

        int projectIdIndex = cursor.getColumnIndex(COL_PROJECT_ID);
        if (projectIdIndex >= 0) {
            item.setProjectId(cursor.getInt(projectIdIndex));
        }
        return item;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
