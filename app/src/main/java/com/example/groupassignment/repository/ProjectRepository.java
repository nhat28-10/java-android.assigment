package com.example.groupassignment.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.groupassignment.database.DatabaseHelper;
import com.example.groupassignment.models.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    private DatabaseHelper dbHelper;

    public ProjectRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertProject(Project project) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", project.getName());
        values.put("description", project.getDescription());
        values.put("manager_id", project.getManagerId());
        values.put("status", project.getStatus());
        long id = db.insert("projects", null, values);
        db.close();
        return id;
    }

    public int updateProject(Project project) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", project.getName());
        values.put("description", project.getDescription());
        values.put("manager_id", project.getManagerId());
        values.put("status", project.getStatus());
        int result = db.update("projects", values, "id = ?", new String[]{String.valueOf(project.getId())});
        db.close();
        return result;
    }

    public int deleteProject(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("projects", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    public Project getProjectById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM projects WHERE id = ?", new String[]{String.valueOf(id)});
        Project project = null;
        if (cursor.moveToFirst()) {
            project = cursorToProject(cursor);
        }
        cursor.close();
        db.close();
        return project;
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM projects ORDER BY created_at DESC", null);
        if (cursor.moveToFirst()) {
            do { projects.add(cursorToProject(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return projects;
    }

    public List<Project> getProjectsByManager(long managerId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM projects WHERE manager_id = ? ORDER BY created_at DESC",
            new String[]{String.valueOf(managerId)});
        if (cursor.moveToFirst()) {
            do { projects.add(cursorToProject(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return projects;
    }

    public int getProjectCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM projects", null);
        int count = 0;
        if (cursor.moveToFirst()) { count = cursor.getInt(0); }
        cursor.close();
        db.close();
        return count;
    }

    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        project.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        project.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        project.setManagerId(cursor.getLong(cursor.getColumnIndexOrThrow("manager_id")));
        project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        project.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return project;
    }
}
