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
    private static final int DATABASE_VERSION = 5;

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
    public static final String COL_IMAGE_URIS = "image_uris";

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
        ensureColumnsExist(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createDatasetsTableIfNeeded(db);
        ensureColumnsExist(db);
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
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_IMAGE_URIS + " TEXT"
                + ")";
        db.execSQL(createTable);
    }

    private void ensureColumnsExist(SQLiteDatabase db) {
        ensureColumn(db, COL_PROJECT_ID, "INTEGER DEFAULT 0");
        ensureColumn(db, COL_IMAGE_URIS, "TEXT");
    }

    private void ensureColumn(SQLiteDatabase db, String columnName, String columnType) {
        boolean hasColumn = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_DATASETS + ")", null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equalsIgnoreCase(name)) {
                    hasColumn = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!hasColumn) {
            db.execSQL("ALTER TABLE " + TABLE_DATASETS + " ADD COLUMN " + columnName + " " + columnType);
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

    public void forceAddImagesToAllDatasets() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IMAGE_URIS, "https://picsum.photos/id/10/800/600|https://picsum.photos/id/11/800/600|https://picsum.photos/id/12/800/600");
        values.put(COL_TOTAL_ITEMS, 3);
        values.put(COL_PENDING_ANNOTATION_ITEMS, 3);
        db.update(TABLE_DATASETS, values, COL_IMAGE_URIS + " IS NULL OR " + COL_IMAGE_URIS + " = ''", null);
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

    public void assignDatasetsToProjectByIds(List<Integer> datasetIds, int projectId) {
        if (datasetIds == null || datasetIds.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, projectId);
        for (Integer id : datasetIds) {
            if (id != null) db.update(TABLE_DATASETS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    public void releaseDatasetsByIds(List<Integer> datasetIds, int projectId) {
        if (datasetIds == null || datasetIds.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, 0);
        for (Integer id : datasetIds) {
            if (id != null) db.update(TABLE_DATASETS, values, COL_ID + "=? AND " + COL_PROJECT_ID + "=?", new String[]{String.valueOf(id), String.valueOf(projectId)});
        }
    }

    public void seedSampleDatasetsIfEmpty() {
        if (!getAllDatasets().isEmpty()) return;
        String now = getNowText();
        
        DatasetItem d1 = new DatasetItem(0, "Human Detection", "Industrial environment detection.", "image", 2, 0, 0, 2, 0, now);
        d1.setImageUris("https://picsum.photos/id/1/800/600|https://picsum.photos/id/2/800/600");
        insertDataset(d1);

        DatasetItem d2 = new DatasetItem(0, "Vehicle OCR", "License plate recognition dataset.", "image", 2, 0, 0, 2, 0, now);
        d2.setImageUris("https://picsum.photos/id/3/800/600|https://picsum.photos/id/4/800/600");
        insertDataset(d2);
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
        values.put(COL_IMAGE_URIS, dataset.getImageUris());
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
        item.setProjectId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROJECT_ID)));
        item.setImageUris(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URIS)));
        return item;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
