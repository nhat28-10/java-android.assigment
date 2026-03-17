package com.example.groupassignment.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "data_labeling.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "email TEXT, " +
                "full_name TEXT, " +
                "role TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS projects (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "manager_id INTEGER, " +
                "status TEXT DEFAULT 'ACTIVE', " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(manager_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE IF NOT EXISTS datasets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "data_path TEXT, " +
                "total_items INTEGER DEFAULT 0, " +
                "labeled_items INTEGER DEFAULT 0, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES projects(id))");

        db.execSQL("CREATE TABLE IF NOT EXISTS labels (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "color TEXT, " +
                "description TEXT, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES projects(id))");

        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "dataset_id INTEGER NOT NULL, " +
                "project_id INTEGER NOT NULL, " +
                "assigned_to INTEGER, " +
                "status TEXT DEFAULT 'PENDING', " +
                "labeling_data TEXT, " +
                "labels TEXT, " +
                "reviewer_comment TEXT, " +
                "assigned_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "completed_at TEXT, " +
                "reviewed_at TEXT, " +
                "FOREIGN KEY(dataset_id) REFERENCES datasets(id), " +
                "FOREIGN KEY(project_id) REFERENCES projects(id), " +
                "FOREIGN KEY(assigned_to) REFERENCES users(id))");

        db.execSQL("INSERT INTO users (username, password, email, full_name, role) " +
                "VALUES ('admin', 'admin123', 'admin@example.com', 'System Admin', 'ADMIN')");
        db.execSQL("INSERT INTO users (username, password, email, full_name, role) " +
                "VALUES ('manager', 'manager123', 'manager@example.com', 'Project Manager', 'MANAGER')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS labels");
        db.execSQL("DROP TABLE IF EXISTS datasets");
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
