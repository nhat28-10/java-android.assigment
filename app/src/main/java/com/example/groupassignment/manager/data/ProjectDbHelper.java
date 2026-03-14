package com.example.groupassignment.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.groupassignment.manager.model.ProjectItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PROJECTS = "projects";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_STATUS = "status";
    public static final String COL_REVIEW_STATUS = "review_status";
    public static final String COL_REVIEWER_COUNT = "reviewer_count";
    public static final String COL_ANNOTATOR_COUNT = "annotator_count";
    public static final String COL_LAST_UPDATED = "last_updated";
    public static final String COL_GUIDELINES = "guidelines";
    public static final String COL_REVIEW_MODE = "review_mode";
    public static final String COL_SAMPLE_RATE = "sample_rate";
    public static final String COL_DEADLINE = "deadline";
    public static final String COL_EXPORT_FORMAT = "export_format";
    public static final String COL_LABELS = "labels";
    public static final String COL_DATASETS = "datasets";
    public static final String COL_ANNOTATORS = "annotators";
    public static final String COL_REVIEWERS = "reviewers";

    public ProjectDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProjectsTable = "CREATE TABLE " + TABLE_PROJECTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_REVIEW_STATUS + " TEXT, "
                + COL_REVIEWER_COUNT + " INTEGER DEFAULT 0, "
                + COL_ANNOTATOR_COUNT + " INTEGER DEFAULT 0, "
                + COL_LAST_UPDATED + " TEXT, "
                + COL_GUIDELINES + " TEXT, "
                + COL_REVIEW_MODE + " TEXT, "
                + COL_SAMPLE_RATE + " REAL DEFAULT 0, "
                + COL_DEADLINE + " TEXT, "
                + COL_EXPORT_FORMAT + " TEXT, "
                + COL_LABELS + " TEXT, "
                + COL_DATASETS + " TEXT, "
                + COL_ANNOTATORS + " TEXT, "
                + COL_REVIEWERS + " TEXT"
                + ")";
        db.execSQL(createProjectsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
    }

    public long insertProject(ProjectItem project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = toContentValues(project);
        return db.insert(TABLE_PROJECTS, null, values);
    }

    public int updateProject(ProjectItem project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = toContentValues(project);

        return db.update(
                TABLE_PROJECTS,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(project.getId())}
        );
    }

    public int deleteProjectById(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                TABLE_PROJECTS,
                COL_ID + "=?",
                new String[]{String.valueOf(projectId)}
        );
    }

    public ProjectItem getProjectById(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(projectId)},
                null,
                null,
                null
        );

        ProjectItem project = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                project = cursorToProject(cursor);
            }
            cursor.close();
        }
        return project;
    }

    public List<ProjectItem> getAllProjects() {
        List<ProjectItem> projectList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                projectList.add(cursorToProject(cursor));
            }
            cursor.close();
        }

        return projectList;
    }

    private ContentValues toContentValues(ProjectItem project) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, project.getName());
        values.put(COL_DESCRIPTION, project.getDescription());
        values.put(COL_STATUS, project.getStatus());
        values.put(COL_REVIEW_STATUS, project.getReviewStatus());
        values.put(COL_REVIEWER_COUNT, project.getReviewerCount());
        values.put(COL_ANNOTATOR_COUNT, project.getAnnotatorCount());
        values.put(COL_LAST_UPDATED, project.getLastUpdated());
        values.put(COL_GUIDELINES, project.getGuidelines());
        values.put(COL_REVIEW_MODE, project.getReviewMode());
        values.put(COL_SAMPLE_RATE, project.getSampleRate());
        values.put(COL_DEADLINE, project.getDeadline());
        values.put(COL_EXPORT_FORMAT, project.getExportFormat());
        values.put(COL_LABELS, joinList(project.getLabels()));
        values.put(COL_DATASETS, joinList(project.getDatasets()));
        values.put(COL_ANNOTATORS, joinList(project.getAnnotators()));
        values.put(COL_REVIEWERS, joinList(project.getReviewers()));
        return values;
    }

    private ProjectItem cursorToProject(Cursor cursor) {
        ProjectItem project = new ProjectItem();

        project.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        project.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        project.setReviewStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEW_STATUS)));
        project.setReviewerCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REVIEWER_COUNT)));
        project.setAnnotatorCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANNOTATOR_COUNT)));
        project.setLastUpdated(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_UPDATED)));
        project.setGuidelines(cursor.getString(cursor.getColumnIndexOrThrow(COL_GUIDELINES)));
        project.setReviewMode(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEW_MODE)));
        project.setSampleRate(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SAMPLE_RATE)));
        project.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEADLINE)));
        project.setExportFormat(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPORT_FORMAT)));
        project.setLabels(splitToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_LABELS))));
        project.setDatasets(splitToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATASETS))));
        project.setAnnotators(splitToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_ANNOTATORS))));
        project.setReviewers(splitToList(cursor.getString(cursor.getColumnIndexOrThrow(COL_REVIEWERS))));

        return project;
    }

    private String joinList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i < items.size() - 1) {
                builder.append("||");
            }
        }
        return builder.toString();
    }

    private List<String> splitToList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split("\\|\\|")));
    }
}