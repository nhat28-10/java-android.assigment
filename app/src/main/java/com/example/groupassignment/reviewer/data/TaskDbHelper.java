package com.example.groupassignment.reviewer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.model.TaskItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_TASKS = "reviewer_tasks";

    public static final String COL_ID = "id";
    public static final String COL_PROJECT_ID = "project_id";
    public static final String COL_PROJECT_NAME = "project_name";
    public static final String COL_DATASET_ID = "dataset_id";
    public static final String COL_DATASET_NAME = "dataset_name";
    public static final String COL_ANNOTATOR_ID = "annotator_id";
    public static final String COL_ANNOTATOR_NAME = "annotator_name";
    public static final String COL_REVIEWER_ID = "reviewer_id";
    public static final String COL_REVIEWER_NAME = "reviewer_name";
    public static final String COL_TYPE = "type";
    public static final String COL_STATUS = "status";
    public static final String COL_SUBMITTED_AT = "submitted_at";
    public static final String COL_REVIEWED_AT = "reviewed_at";
    public static final String COL_ANNOTATION_RESULT = "annotation_result";
    public static final String COL_REVIEW_COMMENTS = "review_comments";
    public static final String COL_REJECTION_REASON = "rejection_reason";
    public static final String COL_IMAGE_URI = "image_uri";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTaskTableIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTaskTableIfNeeded(db);
        ensureColumns(db);
    }

    private void createTaskTableIfNeeded(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROJECT_ID + " INTEGER, "
                + COL_PROJECT_NAME + " TEXT, "
                + COL_DATASET_ID + " INTEGER, "
                + COL_DATASET_NAME + " TEXT, "
                + COL_ANNOTATOR_ID + " INTEGER, "
                + COL_ANNOTATOR_NAME + " TEXT, "
                + COL_REVIEWER_ID + " INTEGER, "
                + COL_REVIEWER_NAME + " TEXT, "
                + COL_TYPE + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_SUBMITTED_AT + " TEXT, "
                + COL_REVIEWED_AT + " TEXT, "
                + COL_ANNOTATION_RESULT + " TEXT, "
                + COL_REVIEW_COMMENTS + " TEXT, "
                + COL_REJECTION_REASON + " TEXT, "
                + COL_IMAGE_URI + " TEXT"
                + ")";
        db.execSQL(createTable);
    }

    private void ensureColumns(SQLiteDatabase db) {
        ensureColumn(db, COL_IMAGE_URI, "TEXT");
        ensureColumn(db, COL_REVIEW_COMMENTS, "TEXT");
        ensureColumn(db, COL_REJECTION_REASON, "TEXT");
    }

    private void ensureColumn(SQLiteDatabase db, String colName, String type) {
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_TASKS + ")", null);
            boolean exists = false;
            while (cursor.moveToNext()) {
                if (colName.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    exists = true;
                    break;
                }
            }
            cursor.close();
            if (!exists) {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + colName + " " + type);
            }
        } catch (Exception ignored) {}
    }

    public void createTasksFromAssignment(Context context, ProjectItem project) {
        SQLiteDatabase db = getWritableDatabase();
        createTaskTableIfNeeded(db);
        
        DatasetDbHelper datasetHelper = new DatasetDbHelper(context);
        AuthDbHelper authHelper = new AuthDbHelper(context);

        List<Integer> datasetIds = project.getDatasetIds();
        List<Integer> annotatorIds = project.getAnnotatorIds();
        List<Integer> reviewerIds = project.getReviewerIds();

        if (datasetIds == null || datasetIds.isEmpty() || annotatorIds == null || annotatorIds.isEmpty() || reviewerIds == null || reviewerIds.isEmpty()) {
            Log.e("TaskDbHelper", "Missing IDs for task creation");
            return;
        }

        int annoIdx = 0;
        int revIdx = 0;

        for (int datasetId : datasetIds) {
            DatasetItem dataset = datasetHelper.getDatasetById(datasetId);
            if (dataset == null) continue;

            List<String> images = dataset.getImageUriList();
            Log.d("TaskDbHelper", "Creating tasks for dataset: " + dataset.getName() + " with " + images.size() + " images");

            for (String uri : images) {
                if (uri == null || uri.isEmpty()) continue;

                User annotator = authHelper.getUserById(annotatorIds.get(annoIdx % annotatorIds.size()));
                User reviewer = authHelper.getUserById(reviewerIds.get(revIdx % reviewerIds.size()));

                if (annotator == null || reviewer == null) continue;

                ContentValues values = new ContentValues();
                values.put(COL_PROJECT_ID, project.getId());
                values.put(COL_PROJECT_NAME, project.getName());
                values.put(COL_DATASET_ID, dataset.getId());
                values.put(COL_DATASET_NAME, dataset.getName());
                values.put(COL_ANNOTATOR_ID, (int) annotator.getId());
                values.put(COL_ANNOTATOR_NAME, annotator.getFullName());
                values.put(COL_REVIEWER_ID, (int) reviewer.getId());
                values.put(COL_REVIEWER_NAME, reviewer.getFullName());
                values.put(COL_TYPE, "image");
                values.put(COL_STATUS, "new");
                values.put(COL_IMAGE_URI, uri);

                db.insert(TABLE_TASKS, null, values);

                annoIdx++;
                revIdx++;
            }
        }
    }

    public void deleteTasksByProjectId(int projectId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    public List<TaskItem> getTasksForAnnotator(int annotatorId) {
        createTaskTableIfNeeded(getWritableDatabase());
        return queryTasks(COL_ANNOTATOR_ID + "=?", new String[]{String.valueOf(annotatorId)});
    }

    public List<TaskItem> getPendingTasksForAnnotator(int annotatorId) {
        createTaskTableIfNeeded(getWritableDatabase());
        return queryTasks(COL_ANNOTATOR_ID + "=? AND (" + COL_STATUS + "='new' OR " + COL_STATUS + "='in_progress' OR " + COL_STATUS + "='rejected')", new String[]{String.valueOf(annotatorId)});
    }

    public List<TaskItem> getTasksForReviewer(int reviewerId) {
        createTaskTableIfNeeded(getWritableDatabase());
        return queryTasks(COL_REVIEWER_ID + "=?", new String[]{String.valueOf(reviewerId)});
    }

    public List<TaskItem> getPendingReviewTasksForReviewer(int reviewerId) {
        createTaskTableIfNeeded(getWritableDatabase());
        return queryTasks(COL_REVIEWER_ID + "=? AND " + COL_STATUS + "='submitted'", new String[]{String.valueOf(reviewerId)});
    }

    public List<TaskItem> getReviewedTasksForReviewer(int reviewerId) {
        createTaskTableIfNeeded(getWritableDatabase());
        return queryTasks(COL_REVIEWER_ID + "=? AND (" + COL_STATUS + "='approved' OR " + COL_STATUS + "='rejected')", new String[]{String.valueOf(reviewerId)});
    }

    private List<TaskItem> queryTasks(String selection, String[] args) {
        List<TaskItem> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_TASKS, null, selection, args, null, null, COL_ID + " DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToTask(cursor));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateAnnotation(int taskId, String result, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ANNOTATION_RESULT, result);
        values.put(COL_STATUS, status);
        values.put(COL_SUBMITTED_AT, getNowText());
        return db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(taskId)}) > 0;
    }

    public boolean approveTask(int taskId, String comment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "approved");
        values.put(COL_REVIEW_COMMENTS, comment);
        values.put(COL_REVIEWED_AT, getNowText());
        return db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(taskId)}) > 0;
    }

    public boolean rejectTask(int taskId, String comment, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "rejected");
        values.put(COL_REVIEW_COMMENTS, comment);
        values.put(COL_REJECTION_REASON, reason);
        values.put(COL_REVIEWED_AT, getNowText());
        return db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(taskId)}) > 0;
    }

    public void ensureReviewerTasksSeeded(int reviewerId) {
        seedDemoTask(COL_REVIEWER_ID, reviewerId, "Reviewer");
    }

    public void seedDemoTasksForAnnotator(int annotatorId) {
        seedDemoTask(COL_ANNOTATOR_ID, annotatorId, "Annotator");
    }

    private void seedDemoTask(String col, int id, String role) {
        SQLiteDatabase db = getWritableDatabase();
        createTaskTableIfNeeded(db);
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_TASKS, new String[]{COL_ID}, col + "=?", new String[]{String.valueOf(id)}, null, null, null);
            int count = cursor != null ? cursor.getCount() : 0;
            if (cursor != null) cursor.close();

            if (count == 0) {
                ContentValues v = new ContentValues();
                v.put(COL_PROJECT_NAME, "Demo Project");
                v.put(COL_DATASET_NAME, "Demo Dataset");
                v.put(COL_ANNOTATOR_ID, role.equals("Annotator") ? id : 3);
                v.put(COL_ANNOTATOR_NAME, "Annotator Demo");
                v.put(COL_REVIEWER_ID, role.equals("Reviewer") ? id : 6);
                v.put(COL_REVIEWER_NAME, "Reviewer Demo");
                v.put(COL_STATUS, role.equals("Reviewer") ? "submitted" : "new");
                v.put(COL_TYPE, "image");
                v.put(COL_IMAGE_URI, "https://picsum.photos/id/237/800/600");
                db.insert(TABLE_TASKS, null, v);
            }
        } catch (Exception e) {
            if (cursor != null) cursor.close();
            e.printStackTrace();
        }
    }

    private TaskItem cursorToTask(Cursor cursor) {
        TaskItem item = new TaskItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setProjectId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROJECT_ID)));
        item.setProjectName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROJECT_NAME)));
        item.setDatasetName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATASET_NAME)));
        item.setAnnotatorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ANNOTATOR_NAME)));
        item.setReviewerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REVIEWER_ID)));
        item.setReviewerName(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEWER_NAME)));
        item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        item.setAnnotationResult(cursor.getString(cursor.getColumnIndexOrThrow(COL_ANNOTATION_RESULT)));
        item.setReviewComments(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEW_COMMENTS)));
        item.setRejectionReason(cursor.getString(cursor.getColumnIndexOrThrow(COL_REJECTION_REASON)));
        item.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI)));
        return item;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
    
    public TaskItem getTaskById(int taskId) {
        createTaskTableIfNeeded(getWritableDatabase());
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_ID + "=?", new String[]{String.valueOf(taskId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            TaskItem item = cursorToTask(cursor);
            cursor.close();
            return item;
        }
        return null;
    }
}
