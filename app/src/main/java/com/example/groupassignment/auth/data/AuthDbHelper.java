package com.example.groupassignment.auth.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.data.AppDatabaseConfig;

import java.util.ArrayList;
import java.util.List;

public class AuthDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_USERS = "users";

    public static final String COL_ID = "id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";

    public AuthDbHelper(Context context) {
        super(context, AppDatabaseConfig.DATABASE_NAME, null, AppDatabaseConfig.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createUsersTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    private void seedDefaultUsers(SQLiteDatabase db) {
        String[][] defaultUsers = {
                {"Admin User", "admin", "admin@example.com", "admin123", "admin"},
                {"Admin Gmail", "admin2", "admin@gmail.com", "admin123", "admin"},
                {"Manager User", "manager", "manager@example.com", "manager123", "manager"},
                {"Annotator User", "annotator", "annotator@example.com", "annotator123", "annotator"},
                {"Reviewer User", "reviewer", "reviewer@example.com", "reviewer123", "reviewer"}
        };

        for (String[] user : defaultUsers) {
            ContentValues values = new ContentValues();
            values.put(COL_FULL_NAME, user[0]);
            values.put(COL_USERNAME, user[1]);
            values.put(COL_EMAIL, user[2]);
            values.put(COL_PASSWORD, user[3]);
            values.put(COL_ROLE, user[4]);
            db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void createUsersTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT NOT NULL, "
                + COL_USERNAME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_ROLE + " TEXT NOT NULL"
                + ")");
    }

    public long registerUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, user.getPassword());
        values.put(COL_ROLE, user.getRole());
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_EMAIL + "=?",
                new String[]{email},
                null,
                null,
                null
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password},
                null,
                null,
                null
        );
        try {
            return cursor.moveToFirst() ? cursorToUser(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null,
                null,
                null
        );
        try {
            return cursor.moveToFirst() ? cursorToUser(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
        try {
            return cursor.moveToFirst() ? cursorToUser(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_ROLE + "=?",
                new String[]{role},
                null,
                null,
                COL_FULL_NAME + " COLLATE NOCASE ASC"
        );
        try {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );
        try {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    private User cursorToUser(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
        String role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE));
        return new User(id, fullName, username, email, password, role);
    }
}
