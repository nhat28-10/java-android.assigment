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
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.reviewer.model.ReviewerVoteItem;
import com.example.groupassignment.reviewer.model.TaskItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 3;

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
    public static final String COL_LOGICAL_TASK_KEY = "logical_task_key";
    public static final String COL_ROUND_NUMBER = "round_number";
    public static final String COL_PROJECT_DEADLINE = "project_deadline";
    public static final String COL_PROJECT_GUIDELINES = "project_guidelines";
    public static final String COL_PROJECT_LABELS = "project_labels";
    public static final String COL_VOTE_DECISION = "vote_decision";
    public static final String COL_FINAL_STATUS = "final_status";
    public static final String COL_UPDATED_AT = "updated_at";

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
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTaskTableIfNeeded(db);
        ensureTaskColumns(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTaskTableIfNeeded(db);
        ensureTaskColumns(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTaskTableIfNeeded(db);
        ensureTaskColumns(db);
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

    private void ensureTaskColumns(SQLiteDatabase db) {
        ensureColumn(db, COL_LOGICAL_TASK_KEY, "TEXT");
        ensureColumn(db, COL_ROUND_NUMBER, "INTEGER DEFAULT 1");
        ensureColumn(db, COL_PROJECT_DEADLINE, "TEXT");
        ensureColumn(db, COL_PROJECT_GUIDELINES, "TEXT");
        ensureColumn(db, COL_PROJECT_LABELS, "TEXT");
        ensureColumn(db, COL_VOTE_DECISION, "TEXT");
        ensureColumn(db, COL_FINAL_STATUS, "TEXT");
        ensureColumn(db, COL_UPDATED_AT, "TEXT");
    }

    private void ensureColumn(SQLiteDatabase db, String columnName, String columnType) {
        boolean hasColumn = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_TASKS + ")", null);
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
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + columnName + " " + columnType);
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

        for (int index = 0; index < datasetIds.size(); index++) {
            int datasetId = datasetIds.get(index);
            DatasetItem dataset = datasetById.get(datasetId);
            if (dataset == null || reviewerIds.isEmpty()) {
                continue;
            }

            int annotatorId = pickAnnotatorId(index, annotatorIds);
            User annotator = annotatorById.get(annotatorId);
            String logicalTaskKey = buildLogicalTaskKey(project.getId(), datasetId, annotatorId);
            desiredKeys.add(logicalTaskKey);

            int latestRound = getLatestRoundForLogicalTask(logicalTaskKey);
            List<TaskItem> currentRoundRows = getRawTasksForLogicalTask(logicalTaskKey, latestRound <= 0 ? 1 : latestRound);
            boolean keepExistingState = !currentRoundRows.isEmpty();
            int roundNumber = latestRound <= 0 ? 1 : latestRound;
            String sharedStatus = keepExistingState ? normalizeStatus(currentRoundRows.get(0).getStatus()) : STATUS_ASSIGNED;
            String finalStatus = keepExistingState ? normalizeStatus(currentRoundRows.get(0).getFinalStatus()) : STATUS_ASSIGNED;
            String annotation = keepExistingState ? safeText(currentRoundRows.get(0).getAnnotationResult()) : "";
            String submittedAt = keepExistingState ? safeText(currentRoundRows.get(0).getSubmittedAt()) : "";

            for (int reviewerId : reviewerIds) {
                User reviewer = reviewerById.get(reviewerId);
                if (reviewer == null) {
                    continue;
                }

                TaskItem existingTask = getTaskByProjectDatasetReviewer(project.getId(), datasetId, reviewerId, logicalTaskKey, roundNumber);
                if (existingTask == null) {
                    insertTask(
                            db,
                            project.getId(),
                            safeText(project.getName()),
                            datasetId,
                            safeText(dataset.getName()),
                            annotatorId,
                            annotator == null ? "Unassigned" : safeText(annotator.getFullName()),
                            reviewerId,
                            safeText(reviewer.getFullName()),
                            normalizeType(dataset.getType()),
                            sharedStatus,
                            annotation,
                            submittedAt,
                            "",
                            "",
                            "",
                            logicalTaskKey,
                            roundNumber,
                            normalizeDeadline(project.getDeadline()),
                            safeText(project.getGuidelines()),
                            joinLabels(project.getLabels()),
                            "",
                            finalStatus
                    );
                } else {
                    ContentValues values = buildProjectSyncValues(project, dataset, annotator, reviewer, logicalTaskKey, roundNumber);
                    if (!keepExistingState) {
                        values.put(COL_STATUS, STATUS_ASSIGNED);
                        values.put(COL_FINAL_STATUS, STATUS_ASSIGNED);
                    }
                    db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(existingTask.getId())});
                }
            }

            removeDeprecatedReviewerRows(db, logicalTaskKey, roundNumber, reviewerIds);
            recomputeMajorityDecision(logicalTaskKey, roundNumber);
        }

        removeDeprecatedProjectTasks(db, project.getId(), desiredKeys);
    }

    private ContentValues buildProjectSyncValues(ProjectItem project,
                                                 DatasetItem dataset,
                                                 User annotator,
                                                 User reviewer,
                                                 String logicalTaskKey,
                                                 int roundNumber) {
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_NAME, safeText(project.getName()));
        values.put(COL_DATASET_NAME, safeText(dataset.getName()));
        values.put(COL_ANNOTATOR_ID, annotator == null ? 0 : (int) annotator.getId());
        values.put(COL_ANNOTATOR_NAME, annotator == null ? "Unassigned" : safeText(annotator.getFullName()));
        values.put(COL_REVIEWER_NAME, reviewer == null ? "" : safeText(reviewer.getFullName()));
        values.put(COL_TYPE, normalizeType(dataset.getType()));
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
        db.delete(TABLE_TASKS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    public void refreshExpiredTasks() {
        for (LogicalTaskItem logicalTask : getAllLatestLogicalTasks()) {
            if (logicalTask == null || logicalTask.isFinalDecisionReached()) {
                continue;
            }
            if (!logicalTask.isDeadlinePassed()) {
                continue;
            }

            String currentStatus = normalizeStatus(logicalTask.getStatus());
            if (STATUS_REWORK_REQUIRED.equals(currentStatus)) {
                updateLogicalTaskStatus(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), STATUS_REJECTED_FINAL, STATUS_REJECTED_FINAL);
            } else if (STATUS_ASSIGNED.equals(currentStatus) || STATUS_IN_PROGRESS.equals(currentStatus)) {
                updateLogicalTaskStatus(logicalTask.getLogicalTaskKey(), logicalTask.getRoundNumber(), STATUS_OVERDUE, STATUS_OVERDUE);
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
            if (reviewerRow == null) {
                continue;
            }
            if (hasReviewerAlreadyVoted(task.getLogicalTaskKey(), task.getRoundNumber(), reviewerId)) {
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
        String logicalTaskKey = safeText(task.getLogicalTaskKey());
        if (TextUtils.isEmpty(logicalTaskKey)) {
            logicalTaskKey = buildLogicalTaskKey(task.getProjectId(), task.getDatasetId(), task.getAnnotatorId());
        }
        int latestRound = getLatestRoundForLogicalTask(logicalTaskKey);
        List<TaskItem> rows = getRawTasksForLogicalTask(logicalTaskKey, latestRound);
        if (rows.isEmpty()) {
            return null;
        }
        return buildLogicalTask(rows);
    }

    public List<ReviewerVoteItem> getVotesForLogicalTask(String logicalTaskKey, int roundNumber) {
        LogicalTaskItem task = buildLogicalTask(getRawTasksForLogicalTask(logicalTaskKey, roundNumber));
        if (task == null) {
            return new ArrayList<>();
        }
        return task.getReviewerVotes();
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
        ContentValues values = new ContentValues();
        values.put(COL_VOTE_DECISION, decision);
        values.put(COL_REVIEW_COMMENTS, safeText(reviewComments));
        values.put(COL_REJECTION_REASON, safeText(rejectionReason));
        values.put(COL_REVIEWED_AT, getNowText());
        values.put(COL_STATUS, STATUS_UNDER_REVIEW);
        values.put(COL_UPDATED_AT, getNowText());

        int updatedRows = db.update(TABLE_TASKS, values, COL_ID + "=?", new String[]{String.valueOf(taskId)});
        if (updatedRows <= 0) {
            return false;
        }
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
            finalStatus = nextStatus;
        } else if (approveCount > 0 || rejectCount > 0) {
            nextStatus = STATUS_UNDER_REVIEW;
            if (TextUtils.isEmpty(finalStatus) || STATUS_ASSIGNED.equals(finalStatus) || STATUS_IN_PROGRESS.equals(finalStatus)) {
                finalStatus = STATUS_UNDER_REVIEW;
            }
        }

        updateLogicalTaskStatus(logicalTaskKey, roundNumber, nextStatus, finalStatus);
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
        TaskItem item = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                item = cursorToTask(cursor);
            }
            cursor.close();
        }
        return item;
    }

    public boolean updateAnnotation(int taskId, String annotation, String status) {
        return saveAnnotationForLogicalTask(taskId, annotation, status) > 0;
    }

    public int saveAnnotationForLogicalTask(int taskId, String annotation, String targetStatus) {
        TaskItem row = getTaskById(taskId);
        if (row == null) {
            return -1;
        }

        String logicalTaskKey = safeText(row.getLogicalTaskKey());
        int roundNumber = row.getRoundNumber() <= 0 ? 1 : row.getRoundNumber();
        String normalizedAnnotation = normalizeAnnotation(annotation);
        String now = getNowText();
        String normalizedTarget = normalizeStatus(targetStatus);

        if (STATUS_REWORK_REQUIRED.equals(normalizeStatus(row.getStatus()))) {
            if (isPastDeadline(row.getProjectDeadline())) {
                updateLogicalTaskStatus(logicalTaskKey, roundNumber, STATUS_REJECTED_FINAL, STATUS_REJECTED_FINAL);
                return -1;
            }
            return createNewRoundFromRework(row, normalizedAnnotation, normalizedTarget, now);
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ANNOTATION_RESULT, normalizedAnnotation);
        values.put(COL_STATUS, STATUS_SUBMITTED.equals(normalizedTarget) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS);
        values.put(COL_FINAL_STATUS, STATUS_SUBMITTED.equals(normalizedTarget) ? STATUS_SUBMITTED : STATUS_IN_PROGRESS);
        values.put(COL_UPDATED_AT, now);
        if (STATUS_SUBMITTED.equals(normalizedTarget)) {
            values.put(COL_SUBMITTED_AT, now);
        }
        db.update(TABLE_TASKS,
                values,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)});

        if (STATUS_SUBMITTED.equals(normalizedTarget)) {
            ContentValues submittedValues = new ContentValues();
            submittedValues.put(COL_STATUS, STATUS_UNDER_REVIEW);
            submittedValues.put(COL_FINAL_STATUS, STATUS_UNDER_REVIEW);
            submittedValues.put(COL_UPDATED_AT, now);
            db.update(TABLE_TASKS,
                    submittedValues,
                    COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                    new String[]{logicalTaskKey, String.valueOf(roundNumber)});
        }
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
                    row.getAnnotatorId(),
                    safeText(row.getAnnotatorName()),
                    row.getReviewerId(),
                    safeText(row.getReviewerName()),
                    normalizeType(row.getType()),
                    STATUS_SUBMITTED.equals(targetStatus) ? STATUS_UNDER_REVIEW : STATUS_IN_PROGRESS,
                    annotation,
                    STATUS_SUBMITTED.equals(targetStatus) ? now : "",
                    "",
                    "",
                    "",
                    safeText(row.getLogicalTaskKey()),
                    newRound,
                    safeText(row.getProjectDeadline()),
                    safeText(row.getProjectGuidelines()),
                    safeText(row.getProjectLabels()),
                    "",
                    STATUS_SUBMITTED.equals(targetStatus) ? STATUS_UNDER_REVIEW : STATUS_IN_PROGRESS);
            if (firstId <= 0) {
                firstId = (int) insertedId;
            }
        }
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

    private List<LogicalTaskItem> getAllLatestLogicalTasks() {
        return aggregateLatestTasks(queryTasks(null, null));
    }

    private void updateLogicalTaskStatus(String logicalTaskKey, int roundNumber, String status, String finalStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        values.put(COL_FINAL_STATUS, finalStatus);
        values.put(COL_UPDATED_AT, getNowText());
        db.update(TABLE_TASKS,
                values,
                COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{logicalTaskKey, String.valueOf(roundNumber)});
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
        logicalTask.setAnnotatorId(base.getAnnotatorId());
        logicalTask.setAnnotatorName(base.getAnnotatorName());
        logicalTask.setType(base.getType());
        logicalTask.setStatus(normalizeStatus(base.getStatus()));
        logicalTask.setFinalStatus(normalizeStatus(base.getFinalStatus()));
        logicalTask.setSubmittedAt(base.getSubmittedAt());
        logicalTask.setReviewedAt(base.getReviewedAt());
        logicalTask.setAnnotationResult(base.getAnnotationResult());
        logicalTask.setGuidelines(base.getProjectGuidelines());
        logicalTask.setLabelsRaw(base.getProjectLabels());
        logicalTask.setDeadline(base.getProjectDeadline());
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

    private List<TaskItem> queryTasks(String selection, String[] selectionArgs) {
        List<TaskItem> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, COL_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
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
        TaskItem task = null;
        if (cursor.moveToFirst()) {
            task = cursorToTask(cursor);
        }
        cursor.close();
        return task;
    }

    private TaskItem getTaskByProjectDatasetReviewer(int projectId, int datasetId, int reviewerId, String logicalTaskKey, int roundNumber) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COL_PROJECT_ID + "=? AND " + COL_DATASET_ID + "=? AND " + COL_REVIEWER_ID + "=? AND "
                        + COL_LOGICAL_TASK_KEY + "=? AND " + COL_ROUND_NUMBER + "=?",
                new String[]{String.valueOf(projectId), String.valueOf(datasetId), String.valueOf(reviewerId), logicalTaskKey, String.valueOf(roundNumber)},
                null,
                null,
                null,
                "1");
        TaskItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToTask(cursor);
        }
        cursor.close();
        return item;
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
            }
        }
    }

    private int getLatestRoundForLogicalTask(String logicalTaskKey) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_ROUND_NUMBER + ") FROM " + TABLE_TASKS + " WHERE " + COL_LOGICAL_TASK_KEY + "=?",
                new String[]{logicalTaskKey});
        int round = 0;
        if (cursor.moveToFirst()) {
            round = cursor.isNull(0) ? 0 : cursor.getInt(0);
        }
        cursor.close();
        return round;
    }

    private int pickAnnotatorId(int index, List<Integer> annotatorIds) {
        if (annotatorIds == null || annotatorIds.isEmpty()) {
            return 0;
        }
        return annotatorIds.get(index % annotatorIds.size());
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
        item.setSubmittedAt(getOptional(cursor, COL_SUBMITTED_AT));
        item.setReviewedAt(getOptional(cursor, COL_REVIEWED_AT));
        item.setAnnotationResult(getOptional(cursor, COL_ANNOTATION_RESULT));
        item.setReviewComments(getOptional(cursor, COL_REVIEW_COMMENTS));
        item.setRejectionReason(getOptional(cursor, COL_REJECTION_REASON));
        item.setLogicalTaskKey(getOptional(cursor, COL_LOGICAL_TASK_KEY));
        item.setRoundNumber(getOptionalInt(cursor, COL_ROUND_NUMBER, 1));
        item.setProjectDeadline(getOptional(cursor, COL_PROJECT_DEADLINE));
        item.setProjectGuidelines(getOptional(cursor, COL_PROJECT_GUIDELINES));
        item.setProjectLabels(getOptional(cursor, COL_PROJECT_LABELS));
        item.setVoteDecision(getOptional(cursor, COL_VOTE_DECISION));
        item.setFinalStatus(getOptional(cursor, COL_FINAL_STATUS));
        item.setUpdatedAt(getOptional(cursor, COL_UPDATED_AT));
        return item;
    }

    private String getOptional(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index < 0 || cursor.isNull(index)) {
            return "";
        }
        return cursor.getString(index);
    }

    private int getOptionalInt(Cursor cursor, String columnName, int fallback) {
        int index = cursor.getColumnIndex(columnName);
        if (index < 0 || cursor.isNull(index)) {
            return fallback;
        }
        return cursor.getInt(index);
    }

    private long insertTask(SQLiteDatabase db,
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
                            String rejectionReason,
                            String logicalTaskKey,
                            int roundNumber,
                            String deadline,
                            String guidelines,
                            String labels,
                            String voteDecision,
                            String finalStatus) {
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
        values.put(COL_SUBMITTED_AT, submittedAt);
        values.put(COL_REVIEWED_AT, reviewedAt);
        values.put(COL_REVIEW_COMMENTS, reviewComments);
        values.put(COL_REJECTION_REASON, rejectionReason);
        values.put(COL_LOGICAL_TASK_KEY, logicalTaskKey);
        values.put(COL_ROUND_NUMBER, roundNumber);
        values.put(COL_PROJECT_DEADLINE, deadline);
        values.put(COL_PROJECT_GUIDELINES, guidelines);
        values.put(COL_PROJECT_LABELS, labels);
        values.put(COL_VOTE_DECISION, voteDecision);
        values.put(COL_FINAL_STATUS, finalStatus);
        values.put(COL_UPDATED_AT, getNowText());
        return db.insert(TABLE_TASKS, null, values);
    }

    private String getLogicalKey(TaskItem item) {
        String current = safeText(item.getLogicalTaskKey());
        if (!TextUtils.isEmpty(current)) {
            return current;
        }
        return buildLogicalTaskKey(item.getProjectId(), item.getDatasetId(), item.getAnnotatorId());
    }

    private String buildLogicalTaskKey(int projectId, int datasetId, int annotatorId) {
        return projectId + "_" + datasetId + "_" + annotatorId;
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
        List<String> labels = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) {
            return labels;
        }
        String normalized = raw.replace("\n", "||").replace(",", "||").replace(";", "||");
        for (String token : normalized.split("\\|\\|")) {
            String label = token.trim();
            if (!label.isEmpty() && !labels.contains(label)) {
                labels.add(label);
            }
        }
        return labels;
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
        return parsed != null && parsed.before(new Date());
    }

    private Date parseDate(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        List<String> patterns = Arrays.asList("yyyy-MM-dd", "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd MMM yyyy");
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public String getNowText() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
