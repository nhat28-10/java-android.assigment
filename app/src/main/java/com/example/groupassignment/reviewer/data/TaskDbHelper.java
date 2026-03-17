package com.example.groupassignment.reviewer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.model.TaskItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 2;

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

    private final Context context;

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTaskTableIfNeeded(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTaskTableIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTaskTableIfNeeded(db);
    }

    private void createTaskTableIfNeeded(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_PROJECT_NAME + " TEXT, "
                + COL_DATASET_ID + " INTEGER DEFAULT 0, "
                + COL_DATASET_NAME + " TEXT, "
                + COL_ANNOTATOR_ID + " INTEGER DEFAULT 0, "
                + COL_ANNOTATOR_NAME + " TEXT, "
                + COL_REVIEWER_ID + " INTEGER DEFAULT 0, "
                + COL_REVIEWER_NAME + " TEXT, "
                + COL_TYPE + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_SUBMITTED_AT + " TEXT, "
                + COL_REVIEWED_AT + " TEXT, "
                + COL_ANNOTATION_RESULT + " TEXT, "
                + COL_REVIEW_COMMENTS + " TEXT, "
                + COL_REJECTION_REASON + " TEXT"
                + ")";
        db.execSQL(createTable);
    }

    public List<TaskItem> getTasksForReviewer(int reviewerId) {
        return queryTasks(COL_REVIEWER_ID + "=?", new String[]{String.valueOf(reviewerId)});
    }

    public List<TaskItem> getPendingReviewTasksForReviewer(int reviewerId) {
        return queryTasks(
                COL_REVIEWER_ID + "=? AND " + COL_STATUS + "=?",
                new String[]{String.valueOf(reviewerId), "submitted"}
        );
    }

    public List<TaskItem> getReviewedTasksForReviewer(int reviewerId) {
        return queryTasks(
                COL_REVIEWER_ID + "=? AND (" + COL_STATUS + "=? OR " + COL_STATUS + "=?)",
                new String[]{String.valueOf(reviewerId), "approved", "rejected"}
        );
    }

    public TaskItem getTaskById(int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                null
        );

        TaskItem item = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                item = cursorToTask(cursor);
            }
            cursor.close();
        }
        return item;
    }

    public boolean approveTask(int taskId, String reviewComments) {
        return updateReviewDecision(taskId, "approved", reviewComments, "");
    }

    public boolean rejectTask(int taskId, String reviewComments, String rejectionReason) {
        return updateReviewDecision(taskId, "rejected", reviewComments, rejectionReason);
    }

    public void syncTasksForProject(ProjectItem project) {
        if (project == null || project.getId() <= 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        AuthDbHelper authDbHelper = new AuthDbHelper(context);
        DatasetDbHelper datasetDbHelper = new DatasetDbHelper(context);

        Map<Integer, DatasetItem> datasetById = new HashMap<>();
        for (DatasetItem item : datasetDbHelper.getAllDatasets()) {
            datasetById.put(item.getId(), item);
        }

        Map<Integer, User> reviewerById = new HashMap<>();
        for (User reviewer : authDbHelper.getUsersByRole("reviewer")) {
            reviewerById.put((int) reviewer.getId(), reviewer);
        }

        Map<Integer, User> annotatorById = new HashMap<>();
        for (User annotator : authDbHelper.getUsersByRole("annotator")) {
            annotatorById.put((int) annotator.getId(), annotator);
        }

        List<Integer> datasetIds = safeIntList(project.getDatasetIds());
        List<Integer> reviewerIds = safeIntList(project.getReviewerIds());
        List<Integer> annotatorIds = safeIntList(project.getAnnotatorIds());

        Set<String> desiredKeys = new HashSet<>();

        for (int datasetId : datasetIds) {
            DatasetItem dataset = datasetById.get(datasetId);
            if (dataset == null) {
                continue;
            }

            for (int reviewerId : reviewerIds) {
                User reviewer = reviewerById.get(reviewerId);
                if (reviewer == null) {
                    continue;
                }

                int annotatorId = pickAnnotatorId(datasetId, reviewerId, annotatorIds);
                User annotator = annotatorById.get(annotatorId);

                String datasetType = normalizeType(dataset.getType());
                String key = buildUniqueKey(project.getId(), datasetId, reviewerId);
                desiredKeys.add(key);

                TaskItem existingTask = getTaskByProjectDatasetReviewer(project.getId(), datasetId, reviewerId);
                if (existingTask == null) {
                    insertTask(
                            db,
                            project.getId(),
                            safeText(project.getName()),
                            datasetId,
                            safeText(dataset.getName()),
                            annotator == null ? 0 : (int) annotator.getId(),
                            annotator == null ? "Unassigned" : safeText(annotator.getFullName()),
                            reviewerId,
                            safeText(reviewer.getFullName()),
                            datasetType,
                            "submitted",
                            buildDefaultAnnotationResult(project, dataset),
                            getNowText(),
                            "",
                            "",
                            ""
                    );
                } else {
                    ContentValues values = new ContentValues();
                    values.put(COL_PROJECT_NAME, safeText(project.getName()));
                    values.put(COL_DATASET_NAME, safeText(dataset.getName()));
                    values.put(COL_REVIEWER_NAME, safeText(reviewer.getFullName()));
                    values.put(COL_TYPE, datasetType);
                    values.put(COL_ANNOTATOR_ID, annotator == null ? 0 : (int) annotator.getId());
                    values.put(COL_ANNOTATOR_NAME, annotator == null ? "Unassigned" : safeText(annotator.getFullName()));

                    db.update(
                            TABLE_TASKS,
                            values,
                            COL_ID + "=?",
                            new String[]{String.valueOf(existingTask.getId())}
                    );
                }
            }
        }

        removeDeprecatedProjectTasks(db, project.getId(), desiredKeys);
    }

    public void deleteTasksByProjectId(int projectId) {
        if (projectId <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    public void ensureReviewerTasksSeeded(int reviewerId) {
        if (reviewerId <= 0) {
            return;
        }

        if (hasAnyTaskForReviewer(reviewerId)) {
            return;
        }

        if (hasAnyRealProjectTask()) {
            return;
        }

        seedDemoTasksIfEmpty();
    }

    private boolean updateReviewDecision(int taskId, String status, String reviewComments, String rejectionReason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        values.put(COL_REVIEW_COMMENTS, safeText(reviewComments));
        values.put(COL_REJECTION_REASON, safeText(rejectionReason));
        values.put(COL_REVIEWED_AT, getNowText());

        int updatedRows = db.update(
                TABLE_TASKS,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(taskId)}
        );
        return updatedRows > 0;
    }

    private List<TaskItem> queryTasks(String selection, String[] selectionArgs) {
        List<TaskItem> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        }

        return tasks;
    }

    private TaskItem getTaskByProjectDatasetReviewer(int projectId, int datasetId, int reviewerId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COL_PROJECT_ID + "=? AND " + COL_DATASET_ID + "=? AND " + COL_REVIEWER_ID + "=?",
                new String[]{String.valueOf(projectId), String.valueOf(datasetId), String.valueOf(reviewerId)},
                null,
                null,
                null,
                "1"
        );

        TaskItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToTask(cursor);
        }
        cursor.close();
        return item;
    }

    private void removeDeprecatedProjectTasks(SQLiteDatabase db, int projectId, Set<String> desiredKeys) {
        List<TaskItem> existing = queryTasks(COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
        for (TaskItem task : existing) {
            String key = buildUniqueKey(task.getProjectId(), task.getDatasetId(), task.getReviewerId());
            if (!desiredKeys.contains(key)) {
                db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(task.getId())});
            }
        }
    }

    private int pickAnnotatorId(int datasetId, int reviewerId, List<Integer> annotatorIds) {
        if (annotatorIds.isEmpty()) {
            return 0;
        }

        int index = Math.abs(datasetId + reviewerId) % annotatorIds.size();
        return annotatorIds.get(index);
    }

    private List<Integer> safeIntList(List<Integer> value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return value;
    }

    private String buildDefaultAnnotationResult(ProjectItem project, DatasetItem dataset) {
        return "Preview from project '" + safeText(project.getName()) + "' dataset '" + safeText(dataset.getName()) + "'";
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

    private String buildUniqueKey(int projectId, int datasetId, int reviewerId) {
        return projectId + "_" + datasetId + "_" + reviewerId;
    }

    private TaskItem cursorToTask(Cursor cursor) {
        TaskItem item = new TaskItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setProjectId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROJECT_ID)));
        item.setProjectName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROJECT_NAME)));
        item.setDatasetId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DATASET_ID)));
        item.setDatasetName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATASET_NAME)));
        item.setAnnotatorId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANNOTATOR_ID)));
        item.setAnnotatorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ANNOTATOR_NAME)));
        item.setReviewerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REVIEWER_ID)));
        item.setReviewerName(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEWER_NAME)));
        item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
        item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        item.setSubmittedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBMITTED_AT)));
        item.setReviewedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEWED_AT)));
        item.setAnnotationResult(cursor.getString(cursor.getColumnIndexOrThrow(COL_ANNOTATION_RESULT)));
        item.setReviewComments(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEW_COMMENTS)));
        item.setRejectionReason(cursor.getString(cursor.getColumnIndexOrThrow(COL_REJECTION_REASON)));
        return item;
    }

    public void seedDemoTasksIfEmpty() {
        if (hasAnyTask()) {
            return;
        }

        AuthDbHelper authDbHelper = new AuthDbHelper(context);
        List<User> reviewers = authDbHelper.getUsersByRole("reviewer");
        List<User> annotators = authDbHelper.getUsersByRole("annotator");

        if (reviewers.isEmpty()) {
            return;
        }

        if (annotators.isEmpty()) {
            annotators.add(new User(0, "Annotator Demo", "anno_demo", "anno@example.com", "", "annotator"));
        }

        SQLiteDatabase db = getWritableDatabase();
        int projectBase = 100;
        int datasetBase = 200;
        String[] types = {"image", "text", "audio"};

        for (int i = 0; i < reviewers.size(); i++) {
            User reviewer = reviewers.get(i);
            User annotator = annotators.get(i % annotators.size());

            insertTask(db, projectBase + i, "Safety Monitoring", datasetBase + i,
                    "Forklift Camera Batch " + (i + 1),
                    (int) annotator.getId(), annotator.getFullName(),
                    (int) reviewer.getId(), reviewer.getFullName(),
                    types[i % types.length], "submitted",
                    "Đã gán nhãn ban đầu theo guideline v1.2", getNowText(), "", "", "");

            insertTask(db, projectBase + i, "Support Classification", datasetBase + i + 10,
                    "Customer Tickets " + (i + 1),
                    (int) annotator.getId(), annotator.getFullName(),
                    (int) reviewer.getId(), reviewer.getFullName(),
                    "text", "approved",
                    "Đã gán nhãn 4 span cảm xúc và urgency", getNowText(), getNowText(),
                    "Good quality annotation", "");

            insertTask(db, projectBase + i, "Factory Audio QA", datasetBase + i + 20,
                    "Noise Segments " + (i + 1),
                    (int) annotator.getId(), annotator.getFullName(),
                    (int) reviewer.getId(), reviewer.getFullName(),
                    "audio", "rejected",
                    "Segment alarm chưa ổn định ở 00:02-00:04", getNowText(), getNowText(),
                    "Need to re-check engine overlap", "Segment overlap issue");

            insertTask(db, projectBase + i, "Warehouse OCR", datasetBase + i + 30,
                    "Label Text OCR " + (i + 1),
                    (int) annotator.getId(), annotator.getFullName(),
                    (int) reviewer.getId(), reviewer.getFullName(),
                    "image", "not_submitted",
                    "Chưa có dữ liệu submit", "", "", "", "");
        }
    }

    private void insertTask(
            SQLiteDatabase db,
            int projectId,
            String projectName,
            int datasetId,
            String datasetName,
            int annotatorId,
            String annotatorName,
            int reviewerId,
            String reviewerName,
            String type,
            String status,
            String annotationResult,
            String submittedAt,
            String reviewedAt,
            String reviewComments,
            String rejectionReason
    ) {
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, projectId);
        values.put(COL_PROJECT_NAME, projectName);
        values.put(COL_DATASET_ID, datasetId);
        values.put(COL_DATASET_NAME, datasetName);
        values.put(COL_ANNOTATOR_ID, annotatorId);
        values.put(COL_ANNOTATOR_NAME, annotatorName);
        values.put(COL_REVIEWER_ID, reviewerId);
        values.put(COL_REVIEWER_NAME, reviewerName);
        values.put(COL_TYPE, type);
        values.put(COL_STATUS, status);
        values.put(COL_ANNOTATION_RESULT, annotationResult);
        values.put(COL_SUBMITTED_AT, safeText(submittedAt));
        values.put(COL_REVIEWED_AT, safeText(reviewedAt));
        values.put(COL_REVIEW_COMMENTS, safeText(reviewComments));
        values.put(COL_REJECTION_REASON, safeText(rejectionReason));
        db.insert(TABLE_TASKS, null, values);
    }

    private boolean hasAnyTask() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
        boolean hasData = false;
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasData;
    }

    private boolean hasAnyTaskForReviewer(int reviewerId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COL_ID},
                COL_REVIEWER_ID + "=?",
                new String[]{String.valueOf(reviewerId)},
                null,
                null,
                null,
                "1"
        );

        boolean hasData = cursor.moveToFirst();
        cursor.close();
        return hasData;
    }

    private boolean hasAnyRealProjectTask() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COL_ID},
                COL_PROJECT_ID + " > 0",
                null,
                null,
                null,
                null,
                "1"
        );

        boolean hasData = cursor.moveToFirst();
        cursor.close();
        return hasData;
    }

    private String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
