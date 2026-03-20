package com.example.groupassignment.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.groupassignment.data.AppDatabaseConfig;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.DatasetSourceItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatasetDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_DATASETS = "datasets";
    public static final String TABLE_DATASET_ITEMS = "dataset_items";

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
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_PROJECT_ID = "project_id";
    public static final String COL_CREATED_BY = "created_by";

    public static final String COL_DATASET_ID = "dataset_id";
    public static final String COL_ITEM_NAME = "item_name";
    public static final String COL_ITEM_PATH_OR_CONTENT = "item_path_or_content";
    public static final String COL_MIME_TYPE = "mime_type";
    public static final String COL_ITEM_TYPE = "item_type";
    public static final String COL_STATUS = "status";

    public static final String ITEM_STATUS_PENDING = "pending_annotation";
    public static final String ITEM_STATUS_IN_PROGRESS = "in_progress";
    public static final String ITEM_STATUS_SUBMITTED = "submitted";
    public static final String ITEM_STATUS_REWORK_REQUIRED = "rework_required";
    public static final String ITEM_STATUS_APPROVED_FINAL = "approved_final";
    public static final String ITEM_STATUS_REJECTED_FINAL = "rejected_final";
    public static final String ITEM_STATUS_OVERDUE = "overdue";

    public DatasetDbHelper(Context context) {
        super(context, AppDatabaseConfig.DATABASE_NAME, null, AppDatabaseConfig.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatasetsTableIfNeeded(db);
        createDatasetItemsTableIfNeeded(db);
        ensureDatasetColumns(db);
        ensureDatasetItemColumns(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createDatasetsTableIfNeeded(db);
        createDatasetItemsTableIfNeeded(db);
        ensureDatasetColumns(db);
        ensureDatasetItemColumns(db);
        migrateLegacyDatasetItems(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createDatasetsTableIfNeeded(db);
        createDatasetItemsTableIfNeeded(db);
        ensureDatasetColumns(db);
        ensureDatasetItemColumns(db);
        migrateLegacyDatasetItems(db);
    }

    private void createDatasetsTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DATASETS + " ("
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
                + COL_UPDATED_AT + " TEXT, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_CREATED_BY + " INTEGER DEFAULT 0"
                + ")");
    }

    private void createDatasetItemsTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DATASET_ITEMS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DATASET_ID + " INTEGER NOT NULL, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_ITEM_NAME + " TEXT NOT NULL, "
                + COL_ITEM_PATH_OR_CONTENT + " TEXT, "
                + COL_MIME_TYPE + " TEXT, "
                + COL_ITEM_TYPE + " TEXT, "
                + COL_STATUS + " TEXT DEFAULT '" + ITEM_STATUS_PENDING + "', "
                + COL_CREATED_AT + " TEXT, "
                + COL_UPDATED_AT + " TEXT"
                + ")");
    }

    private void ensureDatasetColumns(SQLiteDatabase db) {
        ensureColumn(db, TABLE_DATASETS, COL_PROJECT_ID, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_DATASETS, COL_UPDATED_AT, "TEXT");
        ensureColumn(db, TABLE_DATASETS, COL_CREATED_BY, "INTEGER DEFAULT 0");
    }

    private void ensureDatasetItemColumns(SQLiteDatabase db) {
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_PROJECT_ID, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_ITEM_PATH_OR_CONTENT, "TEXT");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_MIME_TYPE, "TEXT");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_ITEM_TYPE, "TEXT");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_STATUS, "TEXT DEFAULT '" + ITEM_STATUS_PENDING + "'");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_CREATED_AT, "TEXT");
        ensureColumn(db, TABLE_DATASET_ITEMS, COL_UPDATED_AT, "TEXT");
    }

    private void ensureColumn(SQLiteDatabase db, String tableName, String columnName, String columnType) {
        boolean hasColumn = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        if (cursor.moveToFirst()) {
            do {
                String existing = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equalsIgnoreCase(existing)) {
                    hasColumn = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!hasColumn) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        }
    }

    private void migrateLegacyDatasetItems(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_DATASETS,
                null,
                null,
                null,
                null,
                null,
                null);
        try {
            while (cursor.moveToNext()) {
                int datasetId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                int totalItems = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_ITEMS));
                int currentCount = countDatasetItems(db, datasetId);
                if (totalItems > 0 && currentCount == 0) {
                    String datasetName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                    String datasetType = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
                    int projectId = getOptionalInt(cursor, COL_PROJECT_ID, 0);
                    String now = getNowText();
                    for (int index = 1; index <= totalItems; index++) {
                        ContentValues values = new ContentValues();
                        values.put(COL_DATASET_ID, datasetId);
                        values.put(COL_PROJECT_ID, projectId);
                        values.put(COL_ITEM_NAME, datasetName + " Item #" + index);
                        values.put(COL_ITEM_PATH_OR_CONTENT, "legacy://dataset/" + datasetId + "/item/" + index);
                        values.put(COL_MIME_TYPE, defaultMimeType(datasetType));
                        values.put(COL_ITEM_TYPE, normalizeType(datasetType));
                        values.put(COL_STATUS, ITEM_STATUS_PENDING);
                        values.put(COL_CREATED_AT, now);
                        values.put(COL_UPDATED_AT, now);
                        db.insert(TABLE_DATASET_ITEMS, null, values);
                    }
                }
                refreshDatasetSummary(db, datasetId);
            }
        } finally {
            cursor.close();
        }
    }

    public long insertDataset(DatasetItem dataset) {
        SQLiteDatabase db = getWritableDatabase();
        if (TextUtils.isEmpty(dataset.getCreatedAt())) {
            dataset.setCreatedAt(getNowText());
        }
        dataset.setUpdatedAt(TextUtils.isEmpty(dataset.getUpdatedAt()) ? dataset.getCreatedAt() : dataset.getUpdatedAt());
        long insertedId = db.insert(TABLE_DATASETS, null, toContentValues(dataset));
        if (insertedId > 0) {
            dataset.setId((int) insertedId);
            ensureDefaultDatasetItems(dataset, dataset.getTotalItems());
            refreshDatasetSummary(db, dataset.getId());
        }
        return insertedId;
    }

    public int updateDataset(DatasetItem dataset) {
        SQLiteDatabase db = getWritableDatabase();
        dataset.setUpdatedAt(getNowText());
        int rows = db.update(TABLE_DATASETS, toContentValues(dataset), COL_ID + "=?", new String[]{String.valueOf(dataset.getId())});
        if (rows > 0) {
            syncPlaceholderDatasetItems(dataset);
            refreshDatasetSummary(db, dataset.getId());
        }
        return rows;
    }

    public int deleteDatasetById(int datasetId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DATASET_ITEMS, COL_DATASET_ID + "=?", new String[]{String.valueOf(datasetId)});
        return db.delete(TABLE_DATASETS, COL_ID + "=?", new String[]{String.valueOf(datasetId)});
    }

    public DatasetItem getDatasetById(int datasetId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATASETS, null, COL_ID + "=?", new String[]{String.valueOf(datasetId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                DatasetItem item = cursorToDataset(cursor);
                refreshDatasetSummary(db, item.getId());
                return getDatasetByIdInternal(db, item.getId());
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private DatasetItem getDatasetByIdInternal(SQLiteDatabase db, int datasetId) {
        Cursor cursor = db.query(TABLE_DATASETS, null, COL_ID + "=?", new String[]{String.valueOf(datasetId)}, null, null, null);
        try {
            return cursor.moveToFirst() ? cursorToDataset(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public List<DatasetItem> getAllDatasets() {
        List<DatasetItem> datasets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATASETS, null, null, null, null, null, COL_ID + " DESC");
        try {
            while (cursor.moveToNext()) {
                DatasetItem item = cursorToDataset(cursor);
                refreshDatasetSummary(db, item.getId());
                DatasetItem refreshed = getDatasetByIdInternal(db, item.getId());
                datasets.add(refreshed == null ? item : refreshed);
            }
        } finally {
            cursor.close();
        }
        return datasets;
    }

    public List<DatasetItem> getUnassignedDatasets() {
        return queryDatasetsByProjectSelection(COL_PROJECT_ID + " IS NULL OR " + COL_PROJECT_ID + " = 0", null);
    }

    public List<DatasetItem> getDatasetsAvailableForProject(int projectId) {
        return queryDatasetsByProjectSelection(COL_PROJECT_ID + " IS NULL OR " + COL_PROJECT_ID + " = 0 OR " + COL_PROJECT_ID + " = ?",
                new String[]{String.valueOf(projectId)});
    }

    private List<DatasetItem> queryDatasetsByProjectSelection(String selection, String[] selectionArgs) {
        List<DatasetItem> datasets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATASETS, null, selection, selectionArgs, null, null, COL_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                DatasetItem item = cursorToDataset(cursor);
                refreshDatasetSummary(db, item.getId());
                DatasetItem refreshed = getDatasetByIdInternal(db, item.getId());
                datasets.add(refreshed == null ? item : refreshed);
            }
        } finally {
            cursor.close();
        }
        return datasets;
    }

    public List<DatasetSourceItem> getDatasetItemsForDataset(int datasetId) {
        List<DatasetSourceItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATASET_ITEMS,
                null,
                COL_DATASET_ID + "=?",
                new String[]{String.valueOf(datasetId)},
                null,
                null,
                COL_ID + " ASC");
        try {
            while (cursor.moveToNext()) {
                items.add(cursorToDatasetSource(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public long insertDatasetItem(DatasetSourceItem item) {
        SQLiteDatabase db = getWritableDatabase();
        String now = getNowText();
        if (TextUtils.isEmpty(item.getCreatedAt())) {
            item.setCreatedAt(now);
        }
        item.setUpdatedAt(TextUtils.isEmpty(item.getUpdatedAt()) ? now : item.getUpdatedAt());
        long insertedId = db.insert(TABLE_DATASET_ITEMS, null, toContentValues(item));
        if (insertedId > 0) {
            refreshDatasetSummary(db, item.getDatasetId());
        }
        return insertedId;
    }

    public void replaceDatasetItems(int datasetId, List<DatasetSourceItem> items) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DATASET_ITEMS, COL_DATASET_ID + "=?", new String[]{String.valueOf(datasetId)});
            if (items != null) {
                String now = getNowText();
                for (DatasetSourceItem item : items) {
                    if (item == null) {
                        continue;
                    }
                    item.setDatasetId(datasetId);
                    if (TextUtils.isEmpty(item.getCreatedAt())) {
                        item.setCreatedAt(now);
                    }
                    item.setUpdatedAt(TextUtils.isEmpty(item.getUpdatedAt()) ? now : item.getUpdatedAt());
                    db.insert(TABLE_DATASET_ITEMS, null, toContentValues(item));
                }
            }
            refreshDatasetSummary(db, datasetId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void assignDatasetsToProject(List<String> datasetNames, int projectId) {
        if (datasetNames == null || datasetNames.isEmpty()) {
            return;
        }
        List<Integer> datasetIds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        for (String datasetName : datasetNames) {
            Cursor cursor = db.query(TABLE_DATASETS, new String[]{COL_ID}, COL_NAME + "=?", new String[]{datasetName}, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    datasetIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                }
            } finally {
                cursor.close();
            }
        }
        assignDatasetsToProjectByIds(datasetIds, projectId);
    }

    public void assignDatasetsToProjectByIds(List<Integer> datasetIds, int projectId) {
        if (datasetIds == null || datasetIds.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues datasetValues = new ContentValues();
        datasetValues.put(COL_PROJECT_ID, projectId);
        datasetValues.put(COL_UPDATED_AT, getNowText());

        ContentValues itemValues = new ContentValues();
        itemValues.put(COL_PROJECT_ID, projectId);
        itemValues.put(COL_UPDATED_AT, getNowText());

        for (Integer datasetId : datasetIds) {
            if (datasetId == null) {
                continue;
            }
            db.update(TABLE_DATASETS, datasetValues, COL_ID + "=?", new String[]{String.valueOf(datasetId)});
            db.update(TABLE_DATASET_ITEMS, itemValues, COL_DATASET_ID + "=?", new String[]{String.valueOf(datasetId)});
        }
    }

    public void releaseDatasetsFromProject(int projectId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, 0);
        values.put(COL_UPDATED_AT, getNowText());
        db.update(TABLE_DATASETS, values, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
        db.update(TABLE_DATASET_ITEMS, values, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    public void releaseDatasetsByNames(List<String> datasetNames, int projectId) {
        if (datasetNames == null || datasetNames.isEmpty()) {
            return;
        }
        List<Integer> datasetIds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        for (String datasetName : datasetNames) {
            Cursor cursor = db.query(TABLE_DATASETS,
                    new String[]{COL_ID},
                    COL_NAME + "=? AND " + COL_PROJECT_ID + "=?",
                    new String[]{datasetName, String.valueOf(projectId)},
                    null,
                    null,
                    null);
            try {
                if (cursor.moveToFirst()) {
                    datasetIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                }
            } finally {
                cursor.close();
            }
        }
        releaseDatasetsByIds(datasetIds, projectId);
    }

    public void releaseDatasetsByIds(List<Integer> datasetIds, int projectId) {
        if (datasetIds == null || datasetIds.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, 0);
        values.put(COL_UPDATED_AT, getNowText());
        for (Integer datasetId : datasetIds) {
            if (datasetId == null) {
                continue;
            }
            db.update(TABLE_DATASETS, values, COL_ID + "=? AND " + COL_PROJECT_ID + "=?",
                    new String[]{String.valueOf(datasetId), String.valueOf(projectId)});
            db.update(TABLE_DATASET_ITEMS, values, COL_DATASET_ID + "=? AND " + COL_PROJECT_ID + "=?",
                    new String[]{String.valueOf(datasetId), String.valueOf(projectId)});
        }
    }

    public void updateDatasetItemStatus(int datasetItemId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        int datasetId = 0;
        Cursor cursor = db.query(TABLE_DATASET_ITEMS, new String[]{COL_DATASET_ID}, COL_ID + "=?", new String[]{String.valueOf(datasetItemId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                datasetId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DATASET_ID));
            }
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        values.put(COL_UPDATED_AT, getNowText());
        db.update(TABLE_DATASET_ITEMS, values, COL_ID + "=?", new String[]{String.valueOf(datasetItemId)});
        if (datasetId > 0) {
            refreshDatasetSummary(db, datasetId);
        }
    }

    public void seedSampleDatasetsIfEmpty() {
        if (!getAllDatasets().isEmpty()) {
            return;
        }
        String now = getNowText();
        insertDataset(new DatasetItem(0, "Human Detection", "Dataset ảnh phục vụ phát hiện người trong môi trường công nghiệp.", "image", 12, 0, 0, 12, 0, now));
        insertDataset(new DatasetItem(0, "Warehouse Audio", "Tập âm thanh tiếng máy móc và môi trường kho.", "audio", 10, 0, 0, 10, 0, now));
        insertDataset(new DatasetItem(0, "Support Tickets", "Dữ liệu text để phân loại nội dung ticket hỗ trợ.", "text", 8, 0, 0, 8, 0, now));
    }

    private void ensureDefaultDatasetItems(DatasetItem dataset, int desiredCount) {
        if (dataset == null || dataset.getId() <= 0 || desiredCount <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        int current = countDatasetItems(db, dataset.getId());
        String now = getNowText();
        for (int index = current + 1; index <= desiredCount; index++) {
            DatasetSourceItem item = new DatasetSourceItem();
            item.setDatasetId(dataset.getId());
            item.setProjectId(dataset.getProjectId());
            item.setItemName(dataset.getName() + " Item #" + index);
            item.setItemPathOrContent("local://dataset/" + dataset.getId() + "/item/" + index);
            item.setMimeType(defaultMimeType(dataset.getType()));
            item.setItemType(normalizeType(dataset.getType()));
            item.setStatus(ITEM_STATUS_PENDING);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            db.insert(TABLE_DATASET_ITEMS, null, toContentValues(item));
        }
    }

    private void syncPlaceholderDatasetItems(DatasetItem dataset) {
        if (dataset == null || dataset.getId() <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        int desiredCount = Math.max(0, dataset.getTotalItems());
        int currentCount = countDatasetItems(db, dataset.getId());
        if (desiredCount > currentCount) {
            ensureDefaultDatasetItems(dataset, desiredCount);
            return;
        }
        if (desiredCount < currentCount) {
            int removable = currentCount - desiredCount;
            Cursor cursor = db.query(TABLE_DATASET_ITEMS,
                    new String[]{COL_ID},
                    COL_DATASET_ID + "=? AND " + COL_STATUS + "=?",
                    new String[]{String.valueOf(dataset.getId()), ITEM_STATUS_PENDING},
                    null,
                    null,
                    COL_ID + " DESC",
                    String.valueOf(removable));
            try {
                while (cursor.moveToNext()) {
                    db.delete(TABLE_DATASET_ITEMS, COL_ID + "=?", new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)))});
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void refreshDatasetSummary(SQLiteDatabase db, int datasetId) {
        Cursor cursor = db.rawQuery("SELECT "
                        + "COUNT(*) AS total_count, "
                        + "SUM(CASE WHEN " + COL_STATUS + "='" + ITEM_STATUS_APPROVED_FINAL + "' THEN 1 ELSE 0 END) AS approved_count, "
                        + "SUM(CASE WHEN " + COL_STATUS + " IN ('" + ITEM_STATUS_SUBMITTED + "','under_review') THEN 1 ELSE 0 END) AS submitted_count, "
                        + "SUM(CASE WHEN " + COL_STATUS + " IN ('" + ITEM_STATUS_PENDING + "','" + ITEM_STATUS_IN_PROGRESS + "','" + ITEM_STATUS_REWORK_REQUIRED + "') THEN 1 ELSE 0 END) AS pending_count, "
                        + "SUM(CASE WHEN " + COL_STATUS + " IN ('" + ITEM_STATUS_REJECTED_FINAL + "','" + ITEM_STATUS_OVERDUE + "') THEN 1 ELSE 0 END) AS rejected_count "
                        + "FROM " + TABLE_DATASET_ITEMS + " WHERE " + COL_DATASET_ID + "=?",
                new String[]{String.valueOf(datasetId)});
        try {
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(COL_TOTAL_ITEMS, cursor.getInt(cursor.getColumnIndexOrThrow("total_count")));
                values.put(COL_APPROVED_ITEMS, cursor.getInt(cursor.getColumnIndexOrThrow("approved_count")));
                values.put(COL_SUBMITTED_ITEMS, cursor.getInt(cursor.getColumnIndexOrThrow("submitted_count")));
                values.put(COL_PENDING_ANNOTATION_ITEMS, cursor.getInt(cursor.getColumnIndexOrThrow("pending_count")));
                values.put(COL_REJECTED_ITEMS, cursor.getInt(cursor.getColumnIndexOrThrow("rejected_count")));
                values.put(COL_UPDATED_AT, getNowText());
                db.update(TABLE_DATASETS, values, COL_ID + "=?", new String[]{String.valueOf(datasetId)});
            }
        } finally {
            cursor.close();
        }
    }

    private int countDatasetItems(SQLiteDatabase db, int datasetId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATASET_ITEMS + " WHERE " + COL_DATASET_ID + "=?",
                new String[]{String.valueOf(datasetId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private ContentValues toContentValues(DatasetItem dataset) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, dataset.getName());
        values.put(COL_DESCRIPTION, dataset.getDescription());
        values.put(COL_TYPE, normalizeType(dataset.getType()));
        values.put(COL_TOTAL_ITEMS, Math.max(0, dataset.getTotalItems()));
        values.put(COL_APPROVED_ITEMS, Math.max(0, dataset.getApprovedItems()));
        values.put(COL_SUBMITTED_ITEMS, Math.max(0, dataset.getSubmittedItems()));
        values.put(COL_PENDING_ANNOTATION_ITEMS, Math.max(0, dataset.getPendingAnnotationItems()));
        values.put(COL_REJECTED_ITEMS, Math.max(0, dataset.getRejectedItems()));
        values.put(COL_CREATED_AT, dataset.getCreatedAt());
        values.put(COL_UPDATED_AT, dataset.getUpdatedAt());
        values.put(COL_PROJECT_ID, dataset.getProjectId());
        values.put(COL_CREATED_BY, dataset.getCreatedBy());
        return values;
    }

    private ContentValues toContentValues(DatasetSourceItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_DATASET_ID, item.getDatasetId());
        values.put(COL_PROJECT_ID, item.getProjectId());
        values.put(COL_ITEM_NAME, item.getItemName());
        values.put(COL_ITEM_PATH_OR_CONTENT, item.getItemPathOrContent());
        values.put(COL_MIME_TYPE, item.getMimeType());
        values.put(COL_ITEM_TYPE, normalizeType(item.getItemType()));
        values.put(COL_STATUS, TextUtils.isEmpty(item.getStatus()) ? ITEM_STATUS_PENDING : item.getStatus());
        values.put(COL_CREATED_AT, item.getCreatedAt());
        values.put(COL_UPDATED_AT, item.getUpdatedAt());
        return values;
    }

    private DatasetItem cursorToDataset(Cursor cursor) {
        DatasetItem item = new DatasetItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        item.setDescription(getOptional(cursor, COL_DESCRIPTION));
        item.setType(getOptional(cursor, COL_TYPE));
        item.setTotalItems(getOptionalInt(cursor, COL_TOTAL_ITEMS, 0));
        item.setApprovedItems(getOptionalInt(cursor, COL_APPROVED_ITEMS, 0));
        item.setSubmittedItems(getOptionalInt(cursor, COL_SUBMITTED_ITEMS, 0));
        item.setPendingAnnotationItems(getOptionalInt(cursor, COL_PENDING_ANNOTATION_ITEMS, 0));
        item.setRejectedItems(getOptionalInt(cursor, COL_REJECTED_ITEMS, 0));
        item.setCreatedAt(getOptional(cursor, COL_CREATED_AT));
        item.setUpdatedAt(getOptional(cursor, COL_UPDATED_AT));
        item.setProjectId(getOptionalInt(cursor, COL_PROJECT_ID, 0));
        item.setCreatedBy(getOptionalInt(cursor, COL_CREATED_BY, 0));
        return item;
    }

    private DatasetSourceItem cursorToDatasetSource(Cursor cursor) {
        DatasetSourceItem item = new DatasetSourceItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setDatasetId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DATASET_ID)));
        item.setProjectId(getOptionalInt(cursor, COL_PROJECT_ID, 0));
        item.setItemName(getOptional(cursor, COL_ITEM_NAME));
        item.setItemPathOrContent(getOptional(cursor, COL_ITEM_PATH_OR_CONTENT));
        item.setMimeType(getOptional(cursor, COL_MIME_TYPE));
        item.setItemType(getOptional(cursor, COL_ITEM_TYPE));
        item.setStatus(getOptional(cursor, COL_STATUS));
        item.setCreatedAt(getOptional(cursor, COL_CREATED_AT));
        item.setUpdatedAt(getOptional(cursor, COL_UPDATED_AT));
        return item;
    }

    private int getOptionalInt(Cursor cursor, String columnName, int fallback) {
        int index = cursor.getColumnIndex(columnName);
        if (index < 0 || cursor.isNull(index)) {
            return fallback;
        }
        return cursor.getInt(index);
    }

    private String getOptional(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index < 0 || cursor.isNull(index)) {
            return "";
        }
        return cursor.getString(index);
    }

    private String normalizeType(String datasetType) {
        if (TextUtils.isEmpty(datasetType)) {
            return "image";
        }
        String lower = datasetType.trim().toLowerCase(Locale.ROOT);
        if ("audio".equals(lower) || "text".equals(lower)) {
            return lower;
        }
        return "image";
    }

    private String defaultMimeType(String datasetType) {
        switch (normalizeType(datasetType)) {
            case "audio":
                return "audio/*";
            case "text":
                return "text/plain";
            case "image":
            default:
                return "image/*";
        }
    }

    private String getNowText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }
}
