package com.example.groupassignment.reviewer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.data.AppDatabaseConfig;
import com.example.groupassignment.manager.data.DatasetDbHelper;
import com.example.groupassignment.manager.model.DatasetItem;
import com.example.groupassignment.manager.model.DatasetSourceItem;
import com.example.groupassignment.manager.model.ProjectItem;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.reviewer.model.TaskAnnotationItem;
import com.example.groupassignment.reviewer.model.TaskItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_TASKS = "reviewer_tasks";
    public static final String TABLE_TASK_ANNOTATIONS = "task_annotations";
    public static final String TABLE_ANNOTATION_REVIEWS = "annotation_reviews";

    public static final String COL_ID = "id";
    public static final String COL_PROJECT_ID = "project_id";
    public static final String COL_PROJECT_NAME = "project_name";
    public static final String COL_DATASET_ID = "dataset_id";
    public static final String COL_DATASET_NAME = "dataset_name";
    public static final String COL_DATASET_ITEM_ID = "dataset_item_id";
    public static final String COL_DATASET_ITEM_NAME = "dataset_item_name";
    public static final String COL_SOURCE_URI = "source_uri";
    public static final String COL_SOURCE_DISPLAY_NAME = "source_display_name";
    public static final String COL_SOURCE_MIME_TYPE = "source_mime_type";
    public static final String COL_ANNOTATOR_ID = "annotator_id";
    public static final String COL_ANNOTATOR_NAME = "annotator_name";
    public static final String COL_REVIEWER_ID = "reviewer_id";
    public static final String COL_REVIEWER_NAME = "reviewer_name";
    public static final String COL_TYPE = "type";
    public static final String COL_STATUS = "status";
    public static final String COL_ASSIGNED_AT = "assigned_at";
    public static final String COL_STARTED_AT = "started_at";
    public static final String COL_SUBMITTED_AT = "submitted_at";
    public static final String COL_REVIEWED_AT = "reviewed_at";
    public static final String COL_ANNOTATION_RESULT = "annotation_result";
    public static final String COL_REVIEW_COMMENTS = "review_comments";
    public static final String COL_REJECTION_REASON = "rejection_reason";
    public static final String COL_LOGICAL_TASK_KEY = "logical_task_key";
    public static final String COL_ROUND_NUMBER = "round_number";
    public static final String COL_PREVIOUS_ROUND_TASK_ID = "previous_round_task_id";
    public static final String COL_PROJECT_DEADLINE = "project_deadline";
    public static final String COL_PROJECT_GUIDELINES = "project_guidelines";
    public static final String COL_PROJECT_LABELS = "project_labels";
    public static final String COL_VOTE_DECISION = "vote_decision";
    public static final String COL_FINAL_STATUS = "final_status";
    public static final String COL_AUTO_REJECTED_AT = "auto_rejected_at";
    public static final String COL_UPDATED_AT = "updated_at";

    public static final String COL_TASK_ID = "task_id";
    public static final String COL_LABEL_NAME = "label_name";
    public static final String COL_LABEL_VALUE_OR_PAYLOAD = "label_value_or_payload";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_ANNOTATION_ID = "annotation_id";
    public static final String COL_DECISION = "decision";
    public static final String COL_COMMENT = "comment";

    public static final String STATUS_ASSIGNED = "assigned";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_UNDER_REVIEW = "under_review";
    public static final String STATUS_REWORK_REQUIRED = "rework_required";
    public static final String STATUS_APPROVED_FINAL = "approved_final";
    public static final String STATUS_REJECTED_FINAL = "rejected_final";
    public static final String STATUS_OVERDUE = "overdue";

    public static final String VOTE_APPROVE = "approve";
    public static final String VOTE_REJECT = "reject";

    private static final List<String> EDITABLE_STATUSES = Arrays.asList(
            STATUS_ASSIGNED,
            STATUS_IN_PROGRESS,
            STATUS_REWORK_REQUIRED
    );

    private final Context context;

    public TaskDbHelper(Context context) {
        super(context, AppDatabaseConfig.DATABASE_NAME, null, AppDatabaseConfig.DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTaskTableIfNeeded(db);
        createTaskAnnotationsTableIfNeeded(db);
        createAnnotationReviewsTableIfNeeded(db);
        ensureTaskColumns(db);
        backfillTaskSourceColumns(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTaskTableIfNeeded(db);
        createTaskAnnotationsTableIfNeeded(db);
        createAnnotationReviewsTableIfNeeded(db);
        ensureTaskColumns(db);
        backfillTaskSourceColumns(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTaskTableIfNeeded(db);
        createTaskAnnotationsTableIfNeeded(db);
        createAnnotationReviewsTableIfNeeded(db);
        ensureTaskColumns(db);
        backfillTaskSourceColumns(db);
    }

    private void createTaskTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_PROJECT_NAME + " TEXT, "
                + COL_DATASET_ID + " INTEGER DEFAULT 0, "
                + COL_DATASET_NAME + " TEXT, "
                + COL_DATASET_ITEM_ID + " INTEGER DEFAULT 0, "
                + COL_DATASET_ITEM_NAME + " TEXT, "
                + COL_SOURCE_URI + " TEXT, "
                + COL_SOURCE_DISPLAY_NAME + " TEXT, "
                + COL_SOURCE_MIME_TYPE + " TEXT, "
                + COL_ANNOTATOR_ID + " INTEGER DEFAULT 0, "
                + COL_ANNOTATOR_NAME + " TEXT, "
                + COL_REVIEWER_ID + " INTEGER DEFAULT 0, "
                + COL_REVIEWER_NAME + " TEXT, "
                + COL_TYPE + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_ASSIGNED_AT + " TEXT, "
                + COL_STARTED_AT + " TEXT, "
                + COL_SUBMITTED_AT + " TEXT, "
                + COL_REVIEWED_AT + " TEXT, "
                + COL_ANNOTATION_RESULT + " TEXT, "
                + COL_REVIEW_COMMENTS + " TEXT, "
                + COL_REJECTION_REASON + " TEXT, "
                + COL_LOGICAL_TASK_KEY + " TEXT, "
                + COL_ROUND_NUMBER + " INTEGER DEFAULT 1, "
                + COL_PREVIOUS_ROUND_TASK_ID + " INTEGER DEFAULT 0, "
                + COL_PROJECT_DEADLINE + " TEXT, "
                + COL_PROJECT_GUIDELINES + " TEXT, "
                + COL_PROJECT_LABELS + " TEXT, "
                + COL_VOTE_DECISION + " TEXT, "
                + COL_FINAL_STATUS + " TEXT, "
                + COL_AUTO_REJECTED_AT + " TEXT, "
                + COL_UPDATED_AT + " TEXT"
                + ")");
    }

    private void createTaskAnnotationsTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASK_ANNOTATIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_ID + " INTEGER DEFAULT 0, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_DATASET_ID + " INTEGER DEFAULT 0, "
                + COL_DATASET_ITEM_ID + " INTEGER DEFAULT 0, "
                + COL_ANNOTATOR_ID + " INTEGER DEFAULT 0, "
                + COL_LABEL_NAME + " TEXT NOT NULL, "
                + COL_LABEL_VALUE_OR_PAYLOAD + " TEXT, "
                + COL_ROUND_NUMBER + " INTEGER DEFAULT 1, "
                + COL_CREATED_AT + " TEXT"
                + ")");
    }

    private void createAnnotationReviewsTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ANNOTATION_REVIEWS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ANNOTATION_ID + " INTEGER DEFAULT 0, "
                + COL_TASK_ID + " INTEGER DEFAULT 0, "
                + COL_PROJECT_ID + " INTEGER DEFAULT 0, "
                + COL_REVIEWER_ID + " INTEGER DEFAULT 0, "
                + COL_REVIEWER_NAME + " TEXT, "
                + COL_DECISION + " TEXT, "
                + COL_COMMENT + " TEXT, "
                + COL_REJECTION_REASON + " TEXT, "
                + COL_ROUND_NUMBER + " INTEGER DEFAULT 1, "
                + COL_CREATED_AT + " TEXT"
                + ")");
    }

    private void ensureTaskColumns(SQLiteDatabase db) {
        ensureColumn(db, TABLE_TASKS, COL_DATASET_ITEM_ID, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_TASKS, COL_DATASET_ITEM_NAME, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_SOURCE_URI, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_SOURCE_DISPLAY_NAME, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_SOURCE_MIME_TYPE, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_ASSIGNED_AT, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_STARTED_AT, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_LOGICAL_TASK_KEY, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_ROUND_NUMBER, "INTEGER DEFAULT 1");
        ensureColumn(db, TABLE_TASKS, COL_PREVIOUS_ROUND_TASK_ID, "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_TASKS, COL_PROJECT_DEADLINE, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_PROJECT_GUIDELINES, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_PROJECT_LABELS, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_VOTE_DECISION, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_FINAL_STATUS, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_AUTO_REJECTED_AT, "TEXT");
        ensureColumn(db, TABLE_TASKS, COL_UPDATED_AT, "TEXT");
    }

    private void backfillTaskSourceColumns(SQLiteDatabase db) {
        db.execSQL("UPDATE " + TABLE_TASKS + " SET "
                + COL_SOURCE_URI + " = COALESCE(NULLIF(" + COL_SOURCE_URI + ", ''), (SELECT di." + DatasetDbHelper.COL_ITEM_PATH_OR_CONTENT
                + " FROM " + DatasetDbHelper.TABLE_DATASET_ITEMS + " di WHERE di." + DatasetDbHelper.COL_ID + " = " + TABLE_TASKS + "." + COL_DATASET_ITEM_ID + " LIMIT 1)), "
                + COL_SOURCE_DISPLAY_NAME + " = COALESCE(NULLIF(" + COL_SOURCE_DISPLAY_NAME + ", ''), NULLIF(" + COL_DATASET_ITEM_NAME + ", ''), (SELECT di." + DatasetDbHelper.COL_ITEM_NAME
                + " FROM " + DatasetDbHelper.TABLE_DATASET_ITEMS + " di WHERE di." + DatasetDbHelper.COL_ID + " = " + TABLE_TASKS + "." + COL_DATASET_ITEM_ID + " LIMIT 1)), "
                + COL_SOURCE_MIME_TYPE + " = COALESCE(NULLIF(" + COL_SOURCE_MIME_TYPE + ", ''), (SELECT di." + DatasetDbHelper.COL_MIME_TYPE
                + " FROM " + DatasetDbHelper.TABLE_DATASET_ITEMS + " di WHERE di." + DatasetDbHelper.COL_ID + " = " + TABLE_TASKS + "." + COL_DATASET_ITEM_ID + " LIMIT 1))");
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

    public void syncTasksForProject(ProjectItem project) {
        if (project == null || project.getId() <= 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        AuthDbHelper authDbHelper = new AuthDbHelper(context);
        DatasetDbHelper datasetDbHelper = new DatasetDbHelper(context);

        Map<Integer, DatasetItem> datasetById = new HashMap<>();
        for (DatasetItem dataset : datasetDbHelper.getAllDatasets()) {
            datasetById.put(dataset.getId(), dataset);
        }

        Map<Integer, List<DatasetSourceItem>> datasetItemsByDatasetId = new LinkedHashMap<>();
        for (int datasetId : safeIntList(project.getDatasetIds())) {
            datasetItemsByDatasetId.put(datasetId, datasetDbHelper.getDatasetItemsForDataset(datasetId));
        }

        Map<Integer, User> reviewerById = new HashMap<>();
        for (User reviewer : authDbHelper.getUsersByRole("reviewer")) {
            reviewerById.put((int) reviewer.getId(), reviewer);
        }

        Map<Integer, User> annotatorById = new HashMap<>();
        for (User annotator : authDbHelper.getUsersByRole("annotator")) {
            annotatorById.put((int) annotator.getId(), annotator);
        }

        List<Integer> reviewerIds = safeIntList(project.getReviewerIds());
        List<Integer> annotatorIds = safeIntList(project.getAnnotatorIds());
        List<Integer> effectiveAnnotatorIds = annotatorIds.isEmpty()
                ? Collections.singletonList(0)
                : annotatorIds;
        List<Integer> effectiveReviewerIds = reviewerIds.isEmpty()
                ? Collections.singletonList(0)
                : reviewerIds;
        Set<String> desiredKeys = new HashSet<>();

        for (Map.Entry<Integer, List<DatasetSourceItem>> entry : datasetItemsByDatasetId.entrySet()) {
            int datasetId = entry.getKey();
            DatasetItem dataset = datasetById.get(datasetId);
            if (dataset == null) {
                continue;
            }
            for (DatasetSourceItem sourceItem : entry.getValue()) {
                if (sourceItem == null) {
                    continue;
                }
                for (int annotatorId : effectiveAnnotatorIds) {
                    User annotator = annotatorById.get(annotatorId);
                    String logicalTaskKey = buildLogicalTaskKey(project.getId(), sourceItem.getId(), annotatorId);
                    desiredKeys.add(logicalTaskKey);

                    int latestRound = Math.max(1, getLatestRoundForLogicalTask(logicalTaskKey));
                    List<TaskItem> latestRows = getRawTasksForLogicalTask(logicalTaskKey, latestRound);
                    boolean keepExistingState = !latestRows.isEmpty();
                    String sharedStatus = keepExistingState ? normalizeStatus(latestRows.get(0).getStatus()) : STATUS_ASSIGNED;
                    String finalStatus = keepExistingState ? normalizeStatus(latestRows.get(0).getFinalStatus()) : STATUS_ASSIGNED;
                    String annotation = keepExistingState ? safeText(latestRows.get(0).getAnnotationResult()) : "";
                    String assignedAt = keepExistingState ? safeText(latestRows.get(0).getAssignedAt()) : getNowText();
                    String startedAt = keepExistingState ? safeText(latestRows.get(0).getStartedAt()) : "";
                    String submittedAt = keepExistingState ? safeText(latestRows.get(0).getSubmittedAt()) : "";
                    String autoRejectedAt = keepExistingState ? safeText(latestRows.get(0).getAutoRejectedAt()) : "";

                    for (int reviewerId : effectiveReviewerIds) {
                        User reviewer = reviewerById.get(reviewerId);
                        String reviewerName = reviewer == null
                                ? (reviewerId == 0 ? "Waiting reviewer assignment" : "Unknown reviewer")
                                : safeText(reviewer.getFullName());
                        TaskItem existing = getTaskByProjectItemReviewer(project.getId(), sourceItem.getId(), reviewerId, logicalTaskKey, latestRound);
                        if (existing == null) {
                            insertTask(db,
                                    project.getId(),
                                    safeText(project.getName()),
                                    datasetId,
                                    safeText(dataset.getName()),
                                    sourceItem.getId(),
                                    safeText(sourceItem.getItemName()),
                                    safeText(sourceItem.getItemPathOrContent()),
                                    safeText(sourceItem.getItemName()),
                                    safeText(sourceItem.getMimeType()),
                                    annotatorId,
                                    annotator == null ? "Unassigned" : safeText(annotator.getFullName()),
                                    reviewerId,
                                    reviewerName,
                                    normalizeType(sourceItem.getItemType()),
                                    sharedStatus,
                                    assignedAt,
                                    startedAt,
                                    submittedAt,
                                    "",
                                    annotation,
                                    "",
                                    "",
                                    logicalTaskKey,
                                    latestRound,
                                    0,
                                    normalizeDeadline(project.getDeadline()),
                                    safeText(project.getGuidelines()),
                                    joinLabels(project.getLabels()),
                                    "",
                                    finalStatus,
                                    autoRejectedAt);
                        } else {
                            ContentValues values = buildProjectSyncValues(project, dataset, sourceItem, annotator, reviewer, logicalTaskKey, latestRound);
                            db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(existing.getId())});
                        }
                    }

                    removeDeprecatedReviewerRows(db, logicalTaskKey, latestRound, effectiveReviewerIds);
                    updateDatasetItemStatusFromLogicalTask(logicalTaskKey, latestRound);
                    recomputeMajorityDecision(logicalTaskKey, latestRound);
                }
            }
        }

        removeDeprecatedProjectTasks(db, project.getId(), desiredKeys);
    }

    private ContentValues buildProjectSyncValues(ProjectItem project,
                                                 DatasetItem dataset,
                                                 DatasetSourceItem sourceItem,
                                                 User annotator,
                                                 User reviewer,
                                                 String logicalTaskKey,
                                                 int roundNumber) {
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_NAME, safeText(project.getName()));
        values.put(COL_DATASET_NAME, safeText(dataset.getName()));
        values.put(COL_DATASET_ITEM_NAME, safeText(sourceItem.getItemName()));
        values.put(COL_SOURCE_URI, safeText(sourceItem.getItemPathOrContent()));
        values.put(COL_SOURCE_DISPLAY_NAME, safeText(sourceItem.getItemName()));
        values.put(COL_SOURCE_MIME_TYPE, safeText(sourceItem.getMimeType()));
        values.put(COL_ANNOTATOR_ID, annotator == null ? 0 : (int) annotator.getId());
        values.put(COL_ANNOTATOR_NAME, annotator == null ? "Unassigned" : safeText(annotator.getFullName()));
        values.put(COL_REVIEWER_NAME, reviewer == null ? "Waiting reviewer assignment" : safeText(reviewer.getFullName()));
        values.put(COL_TYPE, normalizeType(sourceItem.getItemType()));
        values.put(COL_LOGICAL_TASK_KEY, logicalTaskKey);
        values.put(COL_ROUND_NUMBER, roundNumber);
        values.put(COL_PROJECT_DEADLINE, normalizeDeadline(project.getDeadline()));
        values.put(COL_PROJECT_GUIDELINES, safeText(project.getGuidelines()));
        values.put(COL_PROJECT_LABELS, joinLabels(project.getLabels()));
        values.put(COL_UPDATED_AT, getNowText());
        return values;
    }

    public void deleteTasksByProjectId(int projectId) {
        if (projectId <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ANNOTATION_REVIEWS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
        db.delete(TABLE_TASK_ANNOTATIONS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
        db.delete(TABLE_TASKS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    public void refreshExpiredTasks() {
        for (LogicalTaskItem logicalTask : getAllLatestLogicalTasks()) {
            if (logicalTask == null || logicalTask.isFinalDecisionReached() || !logicalTask.isDeadlinePassed()) {
                continue;
            }
            String status = normalizeStatus(logicalTask.getStatus());
            if (STATUS_REWORK_REQUIRED.equals(status)) {
                updateLogicalTaskStatus(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), STATUS_REJECTED_FINAL, STATUS_REJECTED_FINAL, true);
            } else if (STATUS_ASSIGNED.equals(status) || STATUS_IN_PROGRESS.equals(status) || STATUS_SUBMITTED.equals(status) || STATUS_UNDER_REVIEW.equals(status)) {
                updateLogicalTaskStatus(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), STATUS_OVERDUE, STATUS_OVERDUE, false);
            }
        }
    }

    public List<LogicalTaskItem> getLogicalTasksForProject(int projectId) {
        refreshExpiredTasks();
        return aggregateLatestTasks(queryTasks(COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)}));
    }

    public List<LogicalTaskItem> getLogicalTasksForAnnotator(int annotatorId) {
        refreshExpiredTasks();
        return aggregateLatestTasks(queryTasks(COL_ANNOTATOR_ID + "=?", new String[]{String.valueOf(annotatorId)}));
    }

    public List<LogicalTaskItem> getLogicalTasksForReviewer(int reviewerId) {
        refreshExpiredTasks();
        return aggregateLatestTasks(queryTasks(COL_REVIEWER_ID + "=?", new String[]{String.valueOf(reviewerId)}));
    }

    public List<LogicalTaskItem> getPendingLogicalTasksForReviewer(int reviewerId) {
        refreshExpiredTasks();
        List<LogicalTaskItem> aggregated = aggregateLatestTasks(queryTasks(COL_REVIEWER_ID + "=?", new String[]{String.valueOf(reviewerId)}));
        List<LogicalTaskItem> pending = new ArrayList<>();
        for (LogicalTaskItem task : aggregated) {
            if (task == null || task.isFinalDecisionReached()) {
                continue;
            }
            TaskItem reviewerRow = getReviewerRowForRound(task.getLogicalTaskKey(), task.getRoundNumber(), reviewerId);
            if (reviewerRow == null || hasReviewerAlreadyVoted(task.getLogicalTaskKey(), task.getRoundNumber(), reviewerId)) {
                continue;
            }
            String status = normalizeStatus(task.getStatus());
            if (STATUS_SUBMITTED.equals(status) || STATUS_UNDER_REVIEW.equals(status)) {
                task.setReferenceTaskId(reviewerRow.getId());
                pending.add(task);
            }
        }
        return pending;
    }

    public LogicalTaskItem getLogicalTaskByRawTaskId(int taskId) {
        TaskItem task = getTaskById(taskId);
        if (task == null) {
            return null;
        }
        String logicalTaskKey = getLogicalKey(task);
        int latestRound = getLatestRoundForLogicalTask(logicalTaskKey);
        List<TaskItem> rows = getRawTasksForLogicalTask(logicalTaskKey, latestRound);
        return rows.isEmpty() ? null : buildLogicalTask(rows);
    }

    public List<ReviewerVoteItem> getVotesForLogicalTask(String logicalTaskKey, int roundNumber) {
        LogicalTaskItem task = buildLogicalTask(getRawTasksForLogicalTask(logicalTaskKey, roundNumber));
        return task == null ? new ArrayList<>() : task.getReviewerVotes();
    }

    public boolean hasReviewerAlreadyVoted(String logicalTaskKey, int roundNumber, int reviewerId) {
        TaskItem row = getReviewerRowForRound(logicalTaskKey, roundNumber, reviewerId);
        return row != null && !TextUtils.isEmpty(row.getVoteDecision());
    }

    public boolean approveTask(int taskId, String reviewComments) {
        return saveReviewerVote(taskId, VOTE_APPROVE, reviewComments, "");
    }

    public boolean rejectTask(int taskId, String reviewComments, String rejectionReason) {
        return saveReviewerVote(taskId, VOTE_REJECT, reviewComments, rejectionReason);
    }

    public boolean saveReviewerVote(int taskId, String decision, String reviewComments, String rejectionReason) {
        TaskItem row = getTaskById(taskId);
        if (row == null) {
            return false;
        }

        LogicalTaskItem logicalTask = getLogicalTaskByRawTaskId(taskId);
        if (logicalTask == null || logicalTask.isFinalDecisionReached()) {
            return false;
        }
        if (hasReviewerAlreadyVoted(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), row.getReviewerId())) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        String now = getNowText();
        ContentValues values = new ContentValues();
        values.put(COL_VOTE_DECISION, decision);
        values.put(COL_REVIEW_COMMENTS, safeText(reviewComments));
        values.put(COL_REJECTION_REASON, safeText(rejectionReason));
        values.put(COL_REVIEWED_AT, now);
        values.put(COL_STATUS, STATUS_UNDER_REVIEW);
        values.put(COL_UPDATED_AT, now);

        int updatedRows = db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(taskId)});
        if (updatedRows <= 0) {
            return false;
        }

        persistAnnotationReviews(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), row.getReviewerId(), row.getReviewerName(), decision, reviewComments, rejectionReason, now);
        recomputeMajorityDecision(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber());
        return true;
    }

    public String recomputeMajorityDecision(String logicalTaskKey, int roundNumber) {
        List<TaskItem> rows = getRawTasksForLogicalTask(logicalTaskKey, roundNumber);
        if (rows.isEmpty()) {
            return STATUS_ASSIGNED;
        }

        int approveCount = 0;
        int rejectCount = 0;
        int threshold = (rows.size() / 2) + 1;
        for (TaskItem row : rows) {
            if (VOTE_APPROVE.equalsIgnoreCase(row.getVoteDecision())) {
                approveCount++;
            } else if (VOTE_REJECT.equalsIgnoreCase(row.getVoteDecision())) {
                rejectCount++;
            }
        }

        String nextStatus = normalizeStatus(rows.get(0).getStatus());
        String finalStatus = normalizeStatus(rows.get(0).getFinalStatus());
        if (approveCount >= threshold) {
            nextStatus = STATUS_APPROVED_FINAL;
            finalStatus = STATUS_APPROVED_FINAL;
        } else if (rejectCount >= threshold) {
            boolean deadlinePassed = isPastDeadline(rows.get(0).getProjectDeadline());
            nextStatus = deadlinePassed ? STATUS_REJECTED_FINAL : STATUS_REWORK_REQUIRED;
            finalStatus = deadlinePassed ? STATUS_REJECTED_FINAL : STATUS_REWORK_REQUIRED;
        } else if (approveCount > 0 || rejectCount > 0) {
            nextStatus = STATUS_UNDER_REVIEW;
            finalStatus = STATUS_UNDER_REVIEW;
        }

        updateLogicalTaskStatus(logicalTaskKey, roundNumber, nextStatus, finalStatus, false);
        return finalStatus;
    }

    public List<TaskItem> getTasksForReviewer(int reviewerId) {
        return queryTasks(COL_REVIEWER_ID + "=?", new String[]{String.valueOf(reviewerId)});
    }

    public List<TaskItem> getPendingReviewTasksForReviewer(int reviewerId) {
        return queryTasks(COL_REVIEWER_ID + "=? AND " + COL_STATUS + " IN (?,?)",
                new String[]{String.valueOf(reviewerId), STATUS_SUBMITTED, STATUS_UNDER_REVIEW});
    }

    public List<TaskItem> getReviewedTasksForReviewer(int reviewerId) {
        return queryTasks(COL_REVIEWER_ID + "=? AND " + COL_FINAL_STATUS + " IN (?,?)",
                new String[]{String.valueOf(reviewerId), STATUS_APPROVED_FINAL, STATUS_REJECTED_FINAL});
    }

    public List<TaskItem> getTasksForAnnotator(int annotatorId) {
        return queryTasks(COL_ANNOTATOR_ID + "=?", new String[]{String.valueOf(annotatorId)});
    }

    public List<TaskItem> getPendingTasksForAnnotator(int annotatorId) {
        return queryTasks(COL_ANNOTATOR_ID + "=? AND " + COL_STATUS + " IN (?,?,?)",
                new String[]{String.valueOf(annotatorId), STATUS_ASSIGNED, STATUS_IN_PROGRESS, STATUS_REWORK_REQUIRED});
    }

    public TaskItem getTaskById(int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_ID + "=?", new String[]{String.valueOf(taskId)}, null, null, null);
        try {
            return cursor.moveToFirst() ? cursorToTask(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public boolean updateAnnotation(int taskId, String annotation, String status) {
        return saveAnnotationForLogicalTask(taskId, annotation, status) > 0;
    }

    public int saveAnnotationForLogicalTask(int taskId, String annotation, String targetStatus) {
        TaskItem row = getTaskById(taskId);
        if (row == null) {
            return -1;
        }

        String logicalTaskKey = getLogicalKey(row);
        int roundNumber = row.getRoundNumber() <= 0 ? 1 : row.getRoundNumber();
        String normalizedAnnotation = normalizeAnnotation(annotation);
        String normalizedTarget = normalizeStatus(targetStatus);
        String now = getNowText();

        if (STATUS_REWORK_REQUIRED.equals(normalizeStatus(row.getStatus()))) {
            if (isPastDeadline(row.getProjectDeadline())) {
                updateLogicalTaskStatus(logicalTaskKey, roundNumber, STATUS_REJECTED_FINAL, STATUS_REJECTED_FINAL, true);
                return -1;
            }
            return createNewRoundFromRework(row, normalizedAnnotation, normalizedTarget, now);
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ANNOTATION_RESULT, normalizedAnnotation);
        values.put(COL_STATUS, STATUS_SUBMITTED.equals(normalizedTarget) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS);
        values.put(COL_FINAL_STATUS, STATUS_SUBMITTED.equals(normalizedTarget) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS);
        values.put(COL_STARTED_AT, TextUtils.isEmpty(row.getStartedAt()) ? now : row.getStartedAt());
        values.put(COL_UPDATED_AT, now);
        if (STATUS_SUBMITTED.equals(normalizedTarget)) {
            values.put(COL_SUBMITTED_AT, now);
        }
        db.update(TABLE_TASKS,
                values,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)});

        TaskItem latestRow = getFirstTaskForRound(logicalTaskKey, roundNumber);
        if (latestRow != null) {
            persistTaskAnnotations(latestRow, normalizedAnnotation, now);
        }
        updateDatasetItemStatusFromLogicalTask(logicalTaskKey, roundNumber);

        LogicalTaskItem latest = getLogicalTaskByRawTaskId(taskId);
        return latest == null ? taskId : latest.getReferenceTaskId();
    }

    private int createNewRoundFromRework(TaskItem currentRow, String annotation, String targetStatus, String now) {
        List<TaskItem> currentRoundRows = getRawTasksForLogicalTask(currentRow.getLogicalTaskKey(), currentRow.getRoundNumber());
        if (currentRoundRows.isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        int newRound = currentRow.getRoundNumber() + 1;
        int firstId = -1;
        for (TaskItem row : currentRoundRows) {
            long insertedId = insertTask(db,
                    row.getProjectId(),
                    safeText(row.getProjectName()),
                    row.getDatasetId(),
                    safeText(row.getDatasetName()),
                    row.getDatasetItemId(),
                    safeText(row.getDatasetItemName()),
                    safeText(row.getSourceUri()),
                    safeText(row.getSourceDisplayName()),
                    safeText(row.getSourceMimeType()),
                    row.getAnnotatorId(),
                    safeText(row.getAnnotatorName()),
                    row.getReviewerId(),
                    safeText(row.getReviewerName()),
                    normalizeType(row.getType()),
                    STATUS_SUBMITTED.equals(targetStatus) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS,
                    now,
                    now,
                    STATUS_SUBMITTED.equals(targetStatus) ? now : "",
                    "",
                    annotation,
                    "",
                    "",
                    safeText(row.getLogicalTaskKey()),
                    newRound,
                    row.getId(),
                    safeText(row.getProjectDeadline()),
                    safeText(row.getProjectGuidelines()),
                    safeText(row.getProjectLabels()),
                    "",
                    STATUS_SUBMITTED.equals(targetStatus) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS,
                    "");
            if (firstId <= 0) {
                firstId = (int) insertedId;
            }
        }
        TaskItem firstTask = getTaskById(firstId);
        if (firstTask != null) {
            persistTaskAnnotations(firstTask, annotation, now);
        }
        updateDatasetItemStatusFromLogicalTask(currentRow.getLogicalTaskKey(), newRound);
        return firstId;
    }

    public void ensureReviewerTasksSeeded(int reviewerId) {
        // mock data intentionally removed; real SQLite-backed project tasks only
    }

    public void seedDemoTasksIfEmpty() {
        // mock data intentionally removed
    }

    public void seedDemoTasksForAnnotator(int annotatorId) {
        // mock data intentionally removed
    }

    public List<TaskAnnotationItem> getProjectAnnotations(int projectId) {
        refreshExpiredTasks();
        List<TaskAnnotationItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASK_ANNOTATIONS,
                null,
                COL_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)},
                null,
                null,
                COL_CREATED_AT + " DESC");
        try {
            while (cursor.moveToNext()) {
                TaskAnnotationItem item = cursorToTaskAnnotation(cursor);
                TaskItem task = getBaseTaskForAnnotation(item.getTaskId(), item.getRoundNumber());
                if (task != null) {
                    item.setAnnotatorName(task.getAnnotatorName());
                    item.setDatasetName(task.getDatasetName());
                    item.setDatasetItemName(task.getDatasetItemName());
                    item.setTaskStatus(task.getStatus());
                    item.setFinalStatus(task.getFinalStatus());
                    item.setReviewerSummary(buildReviewerSummary(task.getLogicalTaskKey(), task.getRoundNumber()));
                }
                items.add(item);
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    private TaskItem getBaseTaskForAnnotation(int taskId, int roundNumber) {
        TaskItem task = getTaskById(taskId);
        if (task == null) {
            return null;
        }
        return getFirstTaskForRound(getLogicalKey(task), roundNumber);
    }

    private String buildReviewerSummary(String logicalTaskKey, int roundNumber) {
        List<ReviewerVoteItem> votes = getVotesForLogicalTask(logicalTaskKey, roundNumber);
        if (votes.isEmpty()) {
            return "No review yet";
        }
        StringBuilder builder = new StringBuilder();
        for (ReviewerVoteItem vote : votes) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(vote.getReviewerName()).append(": ")
                    .append(TextUtils.isEmpty(vote.getDecision()) ? "pending" : vote.getDecision());
        }
        return builder.toString();
    }

    private List<LogicalTaskItem> getAllLatestLogicalTasks() {
        return aggregateLatestTasks(queryTasks(null, null));
    }

    private void updateLogicalTaskStatus(String logicalTaskKey, int roundNumber, String status, String finalStatus, boolean autoRejected) {
        SQLiteDatabase db = getWritableDatabase();
        String now = getNowText();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        values.put(COL_FINAL_STATUS, finalStatus);
        values.put(COL_UPDATED_AT, now);
        if (autoRejected) {
            values.put(COL_AUTO_REJECTED_AT, now);
        }
        if (STATUS_APPROVED_FINAL.equals(finalStatus) || STATUS_REJECTED_FINAL.equals(finalStatus)) {
            values.put(COL_REVIEWED_AT, now);
        }
        db.update(TABLE_TASKS,
                values,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)});
        updateDatasetItemStatusFromLogicalTask(logicalTaskKey, roundNumber);
    }

    private void updateDatasetItemStatusFromLogicalTask(String logicalTaskKey, int roundNumber) {
        List<TaskItem> rows = getRawTasksForLogicalTask(logicalTaskKey, roundNumber);
        if (rows.isEmpty()) {
            return;
        }
        String datasetStatus = normalizeDatasetItemStatus(rows.get(0).getFinalStatus(), rows.get(0).getStatus());
        new DatasetDbHelper(context).updateDatasetItemStatus(rows.get(0).getDatasetItemId(), datasetStatus);
    }

    private String normalizeDatasetItemStatus(String finalStatus, String status) {
        String normalizedFinal = normalizeStatus(finalStatus);
        if (STATUS_APPROVED_FINAL.equals(normalizedFinal)) {
            return DatasetDbHelper.ITEM_STATUS_APPROVED_FINAL;
        }
        if (STATUS_REJECTED_FINAL.equals(normalizedFinal)) {
            return DatasetDbHelper.ITEM_STATUS_REJECTED_FINAL;
        }
        if (STATUS_OVERDUE.equals(normalizedFinal)) {
            return DatasetDbHelper.ITEM_STATUS_OVERDUE;
        }
        String normalizedStatus = normalizeStatus(status);
        if (STATUS_REWORK_REQUIRED.equals(normalizedStatus)) {
            return DatasetDbHelper.ITEM_STATUS_REWORK_REQUIRED;
        }
        if (STATUS_SUBMITTED.equals(normalizedStatus) || STATUS_UNDER_REVIEW.equals(normalizedStatus)) {
            return DatasetDbHelper.ITEM_STATUS_SUBMITTED;
        }
        if (STATUS_IN_PROGRESS.equals(normalizedStatus)) {
            return DatasetDbHelper.ITEM_STATUS_IN_PROGRESS;
        }
        return DatasetDbHelper.ITEM_STATUS_PENDING;
    }

    private List<LogicalTaskItem> aggregateLatestTasks(List<TaskItem> rawTasks) {
        Map<String, Integer> latestRoundByKey = new HashMap<>();
        for (TaskItem row : rawTasks) {
            String key = getLogicalKey(row);
            int currentRound = latestRoundByKey.containsKey(key) ? latestRoundByKey.get(key) : 0;
            if (row.getRoundNumber() > currentRound) {
                latestRoundByKey.put(key, row.getRoundNumber());
            }
        }

        Map<String, List<TaskItem>> grouped = new LinkedHashMap<>();
        for (TaskItem row : rawTasks) {
            String key = getLogicalKey(row);
            int latestRound = latestRoundByKey.containsKey(key) ? latestRoundByKey.get(key) : row.getRoundNumber();
            if (row.getRoundNumber() != latestRound) {
                continue;
            }
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<>());
            }
            grouped.get(key).add(row);
        }

        List<LogicalTaskItem> aggregated = new ArrayList<>();
        for (List<TaskItem> rows : grouped.values()) {
            LogicalTaskItem logicalTask = buildLogicalTask(rows);
            if (logicalTask != null) {
                aggregated.add(logicalTask);
            }
        }
        Collections.sort(aggregated, (left, right) -> Integer.compare(right.getReferenceTaskId(), left.getReferenceTaskId()));
        return aggregated;
    }

    private LogicalTaskItem buildLogicalTask(List<TaskItem> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Collections.sort(rows, (left, right) -> Integer.compare(left.getId(), right.getId()));
        TaskItem base = rows.get(0);
        LogicalTaskItem logicalTask = new LogicalTaskItem();
        logicalTask.setReferenceTaskId(base.getId());
        logicalTask.setLogicalTaskKey(getLogicalKey(base));
        logicalTask.setRoundNumber(base.getRoundNumber());
        logicalTask.setProjectId(base.getProjectId());
        logicalTask.setProjectName(base.getProjectName());
        logicalTask.setDatasetId(base.getDatasetId());
        logicalTask.setDatasetName(base.getDatasetName());
        logicalTask.setDatasetItemId(base.getDatasetItemId());
        logicalTask.setDatasetItemName(base.getDatasetItemName());
        logicalTask.setSourceUri(base.getSourceUri());
        logicalTask.setSourceDisplayName(base.getSourceDisplayName());
        logicalTask.setSourceMimeType(base.getSourceMimeType());
        logicalTask.setAnnotatorId(base.getAnnotatorId());
        logicalTask.setAnnotatorName(base.getAnnotatorName());
        logicalTask.setType(base.getType());
        logicalTask.setStatus(normalizeStatus(base.getStatus()));
        logicalTask.setFinalStatus(normalizeStatus(base.getFinalStatus()));
        logicalTask.setAssignedAt(base.getAssignedAt());
        logicalTask.setStartedAt(base.getStartedAt());
        logicalTask.setSubmittedAt(base.getSubmittedAt());
        logicalTask.setReviewedAt(base.getReviewedAt());
        logicalTask.setAnnotationResult(buildAnnotationDisplay(base));
        logicalTask.setAnnotationLabelsRaw(buildAnnotationLabels(base));
        logicalTask.setAnnotationPayload(buildAnnotationPayloadDisplay(base));
        logicalTask.setGuidelines(base.getProjectGuidelines());
        logicalTask.setProjectLabelsRaw(base.getProjectLabels());
        hydrateSourceInfo(logicalTask, base);
        logicalTask.setDeadline(base.getProjectDeadline());
        logicalTask.setAutoRejectedAt(base.getAutoRejectedAt());
        logicalTask.setUpdatedAt(base.getUpdatedAt());
        logicalTask.setDeadlinePassed(isPastDeadline(base.getProjectDeadline()));
        logicalTask.setReviewerCount(rows.size());

        int approveCount = 0;
        int rejectCount = 0;
        int pendingCount = 0;
        boolean hasFinal = STATUS_APPROVED_FINAL.equals(logicalTask.getFinalStatus())
                || STATUS_REJECTED_FINAL.equals(logicalTask.getFinalStatus())
                || STATUS_OVERDUE.equals(logicalTask.getFinalStatus());
        for (TaskItem row : rows) {
            ReviewerVoteItem vote = new ReviewerVoteItem();
            vote.setReviewerId(row.getReviewerId());
            vote.setReviewerName(row.getReviewerName());
            vote.setDecision(row.getVoteDecision());
            vote.setComments(row.getReviewComments());
            vote.setRejectionReason(row.getRejectionReason());
            vote.setReviewedAt(row.getReviewedAt());
            logicalTask.getReviewerVotes().add(vote);

            if (VOTE_APPROVE.equalsIgnoreCase(row.getVoteDecision())) {
                approveCount++;
            } else if (VOTE_REJECT.equalsIgnoreCase(row.getVoteDecision())) {
                rejectCount++;
            } else {
                pendingCount++;
            }
        }
        logicalTask.setApproveCount(approveCount);
        logicalTask.setRejectCount(rejectCount);
        logicalTask.setPendingVotes(pendingCount);
        logicalTask.setFinalDecisionReached(hasFinal);
        return logicalTask;
    }

    private String buildAnnotationDisplay(TaskItem base) {
        if (!TextUtils.isEmpty(base.getAnnotationResult())) {
            return base.getAnnotationResult();
        }
        return buildAnnotationLabels(base);
    }

    private String buildAnnotationLabels(TaskItem base) {
        List<TaskAnnotationItem> annotations = getAnnotationsForLogicalTask(base.getLogicalTaskKey(), base.getRoundNumber());
        List<String> labels = new ArrayList<>();
        for (TaskAnnotationItem item : annotations) {
            if (!TextUtils.isEmpty(item.getLabelName()) && !labels.contains(item.getLabelName())) {
                labels.add(item.getLabelName());
            }
        }
        return joinLabels(labels);
    }

    private String buildAnnotationPayloadDisplay(TaskItem base) {
        List<TaskAnnotationItem> annotations = getAnnotationsForLogicalTask(base.getLogicalTaskKey(), base.getRoundNumber());
        if (annotations.isEmpty()) {
            return safeText(base.getAnnotationResult());
        }
        List<String> payloads = new ArrayList<>();
        for (TaskAnnotationItem item : annotations) {
            String payload = item.getLabelValueOrPayload();
            if (TextUtils.isEmpty(payload)) {
                continue;
            }
            payloads.add(item.getLabelName() + ": " + payload);
        }
        return payloads.isEmpty() ? safeText(base.getAnnotationResult()) : TextUtils.join("\n\n", payloads);
    }

    private void hydrateSourceInfo(LogicalTaskItem logicalTask, TaskItem base) {
        if (logicalTask == null || base == null) {
            return;
        }
        if (!TextUtils.isEmpty(logicalTask.getSourceUri())
                && !TextUtils.isEmpty(logicalTask.getSourceDisplayName())
                && !TextUtils.isEmpty(logicalTask.getSourceMimeType())) {
            return;
        }
        DatasetSourceItem sourceItem = new DatasetDbHelper(context).getDatasetItemById(base.getDatasetItemId());
        if (sourceItem == null) {
            return;
        }
        if (TextUtils.isEmpty(logicalTask.getSourceUri())) {
            logicalTask.setSourceUri(sourceItem.getItemPathOrContent());
        }
        if (TextUtils.isEmpty(logicalTask.getSourceDisplayName())) {
            logicalTask.setSourceDisplayName(sourceItem.getItemName());
        }
        if (TextUtils.isEmpty(logicalTask.getSourceMimeType())) {
            logicalTask.setSourceMimeType(sourceItem.getMimeType());
        }
    }

    private List<TaskItem> queryTasks(String selection, String[] selectionArgs) {
        List<TaskItem> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_ID + " DESC");
        try {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
        } finally {
            cursor.close();
        }
        return tasks;
    }

    private List<TaskItem> getRawTasksForLogicalTask(String logicalTaskKey, int roundNumber) {
        return queryTasks(COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)});
    }

    private TaskItem getReviewerRowForRound(String logicalTaskKey, int roundNumber, int reviewerId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=? AND " + COL_REVIEWER_ID + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber), String.valueOf(reviewerId)},
                null,
                null,
                COL_ID + " DESC",
                "1");
        try {
            return cursor.moveToFirst() ? cursorToTask(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private TaskItem getTaskByProjectItemReviewer(int projectId, int datasetItemId, int reviewerId, String logicalTaskKey, int roundNumber) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COL_PROJECT_ID + "=? AND " + COL_DATASET_ITEM_ID + "=? AND " + COL_REVIEWER_ID + "=? AND "
                        + COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{String.valueOf(projectId), String.valueOf(datasetItemId), String.valueOf(reviewerId), logicalTaskKey, String.valueOf(roundNumber)},
                null,
                null,
                null,
                "1");
        try {
            return cursor.moveToFirst() ? cursorToTask(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private TaskItem getFirstTaskForRound(String logicalTaskKey, int roundNumber) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)},
                null,
                null,
                COL_ID + " ASC",
                "1");
        try {
            return cursor.moveToFirst() ? cursorToTask(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private void removeDeprecatedReviewerRows(SQLiteDatabase db, String logicalTaskKey, int roundNumber, List<Integer> reviewerIds) {
        List<TaskItem> existing = getRawTasksForLogicalTask(logicalTaskKey, roundNumber);
        Set<Integer> desired = new HashSet<>(reviewerIds);
        for (TaskItem task : existing) {
            if (!desired.contains(task.getReviewerId())) {
                db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(task.getId())});
            }
        }
    }

    private void removeDeprecatedProjectTasks(SQLiteDatabase db, int projectId, Set<String> desiredKeys) {
        List<TaskItem> existing = queryTasks(COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
        for (TaskItem task : existing) {
            if (!desiredKeys.contains(getLogicalKey(task))) {
                db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(task.getId())});
                db.delete(TABLE_TASK_ANNOTATIONS, COL_TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
                db.delete(TABLE_ANNOTATION_REVIEWS, COL_TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
            }
        }
    }

    private int getLatestRoundForLogicalTask(String logicalTaskKey) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_ROUND_NUMBER + ") FROM " + TABLE_TASKS + " WHERE " + COL_LOGICAL_TASK_KEY + "=?",
                new String[]{logicalTaskKey});
        try {
            return cursor.moveToFirst() && !cursor.isNull(0) ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private TaskItem cursorToTask(Cursor cursor) {
        TaskItem item = new TaskItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setProjectId(getOptionalInt(cursor, COL_PROJECT_ID, 0));
        item.setProjectName(getOptional(cursor, COL_PROJECT_NAME));
        item.setDatasetId(getOptionalInt(cursor, COL_DATASET_ID, 0));
        item.setDatasetName(getOptional(cursor, COL_DATASET_NAME));
        item.setDatasetItemId(getOptionalInt(cursor, COL_DATASET_ITEM_ID, 0));
        item.setDatasetItemName(getOptional(cursor, COL_DATASET_ITEM_NAME));
        item.setSourceUri(getOptional(cursor, COL_SOURCE_URI));
        item.setSourceDisplayName(getOptional(cursor, COL_SOURCE_DISPLAY_NAME));
        item.setSourceMimeType(getOptional(cursor, COL_SOURCE_MIME_TYPE));
        item.setAnnotatorId(getOptionalInt(cursor, COL_ANNOTATOR_ID, 0));
        item.setAnnotatorName(getOptional(cursor, COL_ANNOTATOR_NAME));
        item.setReviewerId(getOptionalInt(cursor, COL_REVIEWER_ID, 0));
        item.setReviewerName(getOptional(cursor, COL_REVIEWER_NAME));
        item.setType(getOptional(cursor, COL_TYPE));
        item.setStatus(getOptional(cursor, COL_STATUS));
        item.setAssignedAt(getOptional(cursor, COL_ASSIGNED_AT));
        item.setStartedAt(getOptional(cursor, COL_STARTED_AT));
        item.setSubmittedAt(getOptional(cursor, COL_SUBMITTED_AT));
        item.setReviewedAt(getOptional(cursor, COL_REVIEWED_AT));
        item.setAnnotationResult(getOptional(cursor, COL_ANNOTATION_RESULT));
        item.setReviewComments(getOptional(cursor, COL_REVIEW_COMMENTS));
        item.setRejectionReason(getOptional(cursor, COL_REJECTION_REASON));
        item.setLogicalTaskKey(getOptional(cursor, COL_LOGICAL_TASK_KEY));
        item.setRoundNumber(getOptionalInt(cursor, COL_ROUND_NUMBER, 1));
        item.setPreviousRoundTaskId(getOptionalInt(cursor, COL_PREVIOUS_ROUND_TASK_ID, 0));
        item.setProjectDeadline(getOptional(cursor, COL_PROJECT_DEADLINE));
        item.setProjectGuidelines(getOptional(cursor, COL_PROJECT_GUIDELINES));
        item.setProjectLabels(getOptional(cursor, COL_PROJECT_LABELS));
        item.setVoteDecision(getOptional(cursor, COL_VOTE_DECISION));
        item.setFinalStatus(getOptional(cursor, COL_FINAL_STATUS));
        item.setAutoRejectedAt(getOptional(cursor, COL_AUTO_REJECTED_AT));
        item.setUpdatedAt(getOptional(cursor, COL_UPDATED_AT));
        return item;
    }

    private TaskAnnotationItem cursorToTaskAnnotation(Cursor cursor) {
        TaskAnnotationItem item = new TaskAnnotationItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setTaskId(getOptionalInt(cursor, COL_TASK_ID, 0));
        item.setProjectId(getOptionalInt(cursor, COL_PROJECT_ID, 0));
        item.setDatasetId(getOptionalInt(cursor, COL_DATASET_ID, 0));
        item.setDatasetItemId(getOptionalInt(cursor, COL_DATASET_ITEM_ID, 0));
        item.setAnnotatorId(getOptionalInt(cursor, COL_ANNOTATOR_ID, 0));
        item.setLabelName(getOptional(cursor, COL_LABEL_NAME));
        item.setLabelValueOrPayload(getOptional(cursor, COL_LABEL_VALUE_OR_PAYLOAD));
        item.setRoundNumber(getOptionalInt(cursor, COL_ROUND_NUMBER, 1));
        item.setCreatedAt(getOptional(cursor, COL_CREATED_AT));
        return item;
    }

    private long insertTask(SQLiteDatabase db,
                            int projectId,
                            String projectName,
                            int datasetId,
                            String datasetName,
                            int datasetItemId,
                            String datasetItemName,
                            String sourceUri,
                            String sourceDisplayName,
                            String sourceMimeType,
                            int annotatorId,
                            String annotatorName,
                            int reviewerId,
                            String reviewerName,
                            String type,
                            String status,
                            String assignedAt,
                            String startedAt,
                            String submittedAt,
                            String reviewedAt,
                            String annotationResult,
                            String reviewComments,
                            String rejectionReason,
                            String logicalTaskKey,
                            int roundNumber,
                            int previousRoundTaskId,
                            String deadline,
                            String guidelines,
                            String labels,
                            String voteDecision,
                            String finalStatus,
                            String autoRejectedAt) {
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_ID, projectId);
        values.put(COL_PROJECT_NAME, projectName);
        values.put(COL_DATASET_ID, datasetId);
        values.put(COL_DATASET_NAME, datasetName);
        values.put(COL_DATASET_ITEM_ID, datasetItemId);
        values.put(COL_DATASET_ITEM_NAME, datasetItemName);
        values.put(COL_SOURCE_URI, sourceUri);
        values.put(COL_SOURCE_DISPLAY_NAME, sourceDisplayName);
        values.put(COL_SOURCE_MIME_TYPE, sourceMimeType);
        values.put(COL_ANNOTATOR_ID, annotatorId);
        values.put(COL_ANNOTATOR_NAME, annotatorName);
        values.put(COL_REVIEWER_ID, reviewerId);
        values.put(COL_REVIEWER_NAME, reviewerName);
        values.put(COL_TYPE, type);
        values.put(COL_STATUS, status);
        values.put(COL_ASSIGNED_AT, assignedAt);
        values.put(COL_STARTED_AT, startedAt);
        values.put(COL_SUBMITTED_AT, submittedAt);
        values.put(COL_REVIEWED_AT, reviewedAt);
        values.put(COL_ANNOTATION_RESULT, annotationResult);
        values.put(COL_REVIEW_COMMENTS, reviewComments);
        values.put(COL_REJECTION_REASON, rejectionReason);
        values.put(COL_LOGICAL_TASK_KEY, logicalTaskKey);
        values.put(COL_ROUND_NUMBER, roundNumber);
        values.put(COL_PREVIOUS_ROUND_TASK_ID, previousRoundTaskId);
        values.put(COL_PROJECT_DEADLINE, deadline);
        values.put(COL_PROJECT_GUIDELINES, guidelines);
        values.put(COL_PROJECT_LABELS, labels);
        values.put(COL_VOTE_DECISION, voteDecision);
        values.put(COL_FINAL_STATUS, finalStatus);
        values.put(COL_AUTO_REJECTED_AT, autoRejectedAt);
        values.put(COL_UPDATED_AT, getNowText());
        return db.insert(TABLE_TASKS, null, values);
    }

    private void persistTaskAnnotations(TaskItem task, String normalizedAnnotation, String createdAt) {
        if (task == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASK_ANNOTATIONS,
                COL_TASK_ID + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{String.valueOf(task.getId()), String.valueOf(task.getRoundNumber())});

        List<String> labels = parseLabels(normalizedAnnotation);
        for (String label : labels) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", label);
                payload.put("raw_annotation", normalizedAnnotation);
                payload.put("dataset_item_name", task.getDatasetItemName());
                payload.put("task_status", task.getStatus());

                ContentValues values = new ContentValues();
                values.put(COL_TASK_ID, task.getId());
                values.put(COL_PROJECT_ID, task.getProjectId());
                values.put(COL_DATASET_ID, task.getDatasetId());
                values.put(COL_DATASET_ITEM_ID, task.getDatasetItemId());
                values.put(COL_ANNOTATOR_ID, task.getAnnotatorId());
                values.put(COL_LABEL_NAME, label);
                values.put(COL_LABEL_VALUE_OR_PAYLOAD, payload.toString());
                values.put(COL_ROUND_NUMBER, task.getRoundNumber());
                values.put(COL_CREATED_AT, createdAt);
                db.insert(TABLE_TASK_ANNOTATIONS, null, values);
            } catch (JSONException ignored) {
            }
        }
    }

    private List<TaskAnnotationItem> getAnnotationsForLogicalTask(String logicalTaskKey, int roundNumber) {
        TaskItem task = getFirstTaskForRound(logicalTaskKey, roundNumber);
        if (task == null) {
            return new ArrayList<>();
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASK_ANNOTATIONS,
                null,
                COL_TASK_ID + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{String.valueOf(task.getId()), String.valueOf(roundNumber)},
                null,
                null,
                COL_ID + " ASC");
        List<TaskAnnotationItem> items = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                items.add(cursorToTaskAnnotation(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    private void persistAnnotationReviews(String logicalTaskKey,
                                          int roundNumber,
                                          int reviewerId,
                                          String reviewerName,
                                          String decision,
                                          String reviewComments,
                                          String rejectionReason,
                                          String createdAt) {
        TaskItem task = getFirstTaskForRound(logicalTaskKey, roundNumber);
        if (task == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ANNOTATION_REVIEWS,
                COL_TASK_ID + "=? AND " + COL_REVIEWER_ID + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{String.valueOf(task.getId()), String.valueOf(reviewerId), String.valueOf(roundNumber)});

        List<TaskAnnotationItem> annotations = getAnnotationsForLogicalTask(logicalTaskKey, roundNumber);
        if (annotations.isEmpty()) {
            insertAnnotationReview(db, 0, task, reviewerId, reviewerName, decision, reviewComments, rejectionReason, roundNumber, createdAt);
            return;
        }
        for (TaskAnnotationItem annotation : annotations) {
            insertAnnotationReview(db, annotation.getId(), task, reviewerId, reviewerName, decision, reviewComments, rejectionReason, roundNumber, createdAt);
        }
    }

    private void insertAnnotationReview(SQLiteDatabase db,
                                        int annotationId,
                                        TaskItem task,
                                        int reviewerId,
                                        String reviewerName,
                                        String decision,
                                        String reviewComments,
                                        String rejectionReason,
                                        int roundNumber,
                                        String createdAt) {
        ContentValues values = new ContentValues();
        values.put(COL_ANNOTATION_ID, annotationId);
        values.put(COL_TASK_ID, task.getId());
        values.put(COL_PROJECT_ID, task.getProjectId());
        values.put(COL_REVIEWER_ID, reviewerId);
        values.put(COL_REVIEWER_NAME, reviewerName);
        values.put(COL_DECISION, decision);
        values.put(COL_COMMENT, safeText(reviewComments));
        values.put(COL_REJECTION_REASON, safeText(rejectionReason));
        values.put(COL_ROUND_NUMBER, roundNumber);
        values.put(COL_CREATED_AT, createdAt);
        db.insert(TABLE_ANNOTATION_REVIEWS, null, values);
    }

    private String getLogicalKey(TaskItem item) {
        String current = safeText(item.getLogicalTaskKey());
        if (!TextUtils.isEmpty(current)) {
            return current;
        }
        return buildLogicalTaskKey(item.getProjectId(), item.getDatasetItemId(), item.getAnnotatorId());
    }

    private String buildLogicalTaskKey(int projectId, int datasetItemId, int annotatorId) {
        return projectId + "_" + datasetItemId + "_" + annotatorId;
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

    private String normalizeStatus(String status) {
        if (TextUtils.isEmpty(status)) {
            return STATUS_ASSIGNED;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        if ("approved".equals(value)) {
            return STATUS_APPROVED_FINAL;
        }
        if ("rejected".equals(value)) {
            return STATUS_REJECTED_FINAL;
        }
        if ("new".equals(value) || "not_submitted".equals(value)) {
            return STATUS_ASSIGNED;
        }
        return value;
    }

    public boolean isTaskEditable(LogicalTaskItem task) {
        return task != null
                && EDITABLE_STATUSES.contains(normalizeStatus(task.getStatus()))
                && !STATUS_APPROVED_FINAL.equals(normalizeStatus(task.getFinalStatus()))
                && !STATUS_REJECTED_FINAL.equals(normalizeStatus(task.getFinalStatus()))
                && !STATUS_OVERDUE.equals(normalizeStatus(task.getFinalStatus()))
                && !(STATUS_REWORK_REQUIRED.equals(normalizeStatus(task.getStatus())) && task.isDeadlinePassed());
    }

    public boolean canReviewerVote(LogicalTaskItem task, int reviewerId) {
        return task != null
                && !task.isFinalDecisionReached()
                && !hasReviewerAlreadyVoted(task.getLogicalTaskKey(), task.getRoundNumber(), reviewerId)
                && (STATUS_SUBMITTED.equals(normalizeStatus(task.getStatus()))
                || STATUS_UNDER_REVIEW.equals(normalizeStatus(task.getStatus())));
    }

    public List<String> parseLabels(String raw) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        if (TextUtils.isEmpty(raw)) {
            return new ArrayList<>();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                JSONArray array = new JSONArray(trimmed);
                for (int i = 0; i < array.length(); i++) {
                    Object value = array.get(i);
                    if (value instanceof JSONObject) {
                        JSONObject object = (JSONObject) value;
                        String label = object.optString("label");
                        if (!TextUtils.isEmpty(label)) {
                            labels.add(label.trim());
                        }
                    } else {
                        String label = String.valueOf(value).trim();
                        if (!label.isEmpty()) {
                            labels.add(label);
                        }
                    }
                }
            } catch (JSONException ignored) {
            }
        }
        if (labels.isEmpty()) {
            String normalized = raw.replace("\n", "||").replace(",", "||").replace(";", "||");
            for (String token : normalized.split("\\|\\|")) {
                String label = token.trim();
                if (!label.isEmpty()) {
                    labels.add(label);
                }
            }
        }
        return new ArrayList<>(labels);
    }

    private String normalizeAnnotation(String annotation) {
        return joinLabels(parseLabels(annotation));
    }

    private String joinLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            builder.append(labels.get(i).trim());
            if (i < labels.size() - 1) {
                builder.append("||");
            }
        }
        return builder.toString();
    }

    private String normalizeDeadline(String deadline) {
        if (TextUtils.isEmpty(deadline) || "Not set".equalsIgnoreCase(deadline)) {
            return "";
        }
        Date parsed = parseDate(deadline);
        if (parsed == null) {
            return deadline.trim();
        }
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed);
    }

    private boolean isPastDeadline(String deadline) {
        if (TextUtils.isEmpty(deadline)) {
            return false;
        }
        Date parsed = parseDate(deadline);
        if (parsed == null) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date today = sdf.parse(sdf.format(new Date()));
            return parsed.before(today);
        } catch (ParseException ignored) {
            return parsed.before(new Date());
        }
    }

    private Date parseDate(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        List<String> patterns = Arrays.asList("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd MMM yyyy");
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(value.trim());
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private List<Integer> safeIntList(List<Integer> value) {
        return value == null ? Collections.emptyList() : value;
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public String getNowText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }
}
