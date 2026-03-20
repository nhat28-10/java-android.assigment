package com.example.groupassignment.auth.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.data.AppDatabaseConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuthDbHelper extends SQLiteOpenHelper {

    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACTIVITY_LOGS = "activity_logs";

    public static final String COL_ID = "id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_IS_ENABLED = "is_enabled";

    // Activity Logs Columns
    public static final String COL_LOG_USER_ID = "log_user_id";
    public static final String COL_LOG_USER_NAME = "log_user_name";
    public static final String COL_LOG_ACTION = "log_action";
    public static final String COL_LOG_DETAILS = "log_details";
    public static final String COL_LOG_TIMESTAMP = "log_timestamp";

    public AuthDbHelper(Context context) {
        super(context, AppDatabaseConfig.DATABASE_NAME, null, AppDatabaseConfig.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTableIfNeeded(db);
        createActivityLogsTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createUsersTableIfNeeded(db);
        createActivityLogsTableIfNeeded(db);
        ensureIsEnabledColumn(db);
        seedDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTableIfNeeded(db);
        createActivityLogsTableIfNeeded(db);
        ensureIsEnabledColumn(db);
        seedDefaultUsers(db);
    }

    private void ensureIsEnabledColumn(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_USERS + ")", null);
        boolean exists = false;
        while (cursor.moveToNext()) {
            if (COL_IS_ENABLED.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                exists = true;
                break;
            }
        }
        cursor.close();
        if (!exists) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_IS_ENABLED + " INTEGER DEFAULT 1");
        }
    }

    private void seedDefaultUsers(SQLiteDatabase db) {
        String[][] defaultUsers = {
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
            values.put(COL_IS_ENABLED, 1);
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
                + COL_ROLE + " TEXT NOT NULL, "
                + COL_IS_ENABLED + " INTEGER DEFAULT 1"
                + ")");
    }

    private void createActivityLogsTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVITY_LOGS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LOG_USER_ID + " INTEGER, "
                + COL_LOG_USER_NAME + " TEXT, "
                + COL_LOG_ACTION + " TEXT, "
                + COL_LOG_DETAILS + " TEXT, "
                + COL_LOG_TIMESTAMP + " TEXT"
                + ")");
    }

    public void logActivity(long userId, String userName, String action, String details) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_USER_ID, userId);
            values.put(COL_LOG_USER_NAME, userName);
            values.put(COL_LOG_ACTION, action);
            values.put(COL_LOG_DETAILS, details);
            values.put(COL_LOG_TIMESTAMP, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_ACTIVITY_LOGS, null, values);
        } catch (Exception ignored) {}
    }

    public long registerUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, user.getPassword());
        values.put(COL_ROLE, user.getRole());
        values.put(COL_IS_ENABLED, 1);
        long id = db.insert(TABLE_USERS, null, values);
        if (id > 0) {
            logActivity(0, "SYSTEM/ADMIN", "CREATE_USER", "Created user: " + user.getEmail() + " as " + user.getRole());
        }
        return id;
    }

    public boolean updateUserStatus(long userId, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_ENABLED, enabled ? 1 : 0);
        boolean success = db.update(TABLE_USERS, values, COL_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
        if (success) {
            User user = getUserById(userId);
            String status = enabled ? "ENABLED" : "DISABLED";
            logActivity(0, "ADMIN", "STATUS_CHANGE", status + " account: " + (user != null ? user.getEmail() : userId));
        }
        return success;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=? AND " + COL_IS_ENABLED + "=1",
                new String[]{email, password},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                User user = cursorToUser(cursor);
                logActivity(user.getId(), user.getFullName(), "LOGIN", "User logged in");
                return user;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
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
                COL_ROLE + " ASC, " + COL_FULL_NAME + " ASC"
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

    public List<ActivityLog> getAllActivityLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACTIVITY_LOGS, null, null, null, null, null, COL_ID + " DESC");
        try {
            while (cursor.moveToNext()) {
                logs.add(new ActivityLog(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_USER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_ACTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_TIMESTAMP))
                ));
            }
        } finally {
            cursor.close();
        }
        return logs;
    }

    public static class ActivityLog {
        public long id;
        public long userId;
        public String userName;
        public String action;
        public String details;
        public String timestamp;

        public ActivityLog(long id, long userId, String userName, String action, String details, String timestamp) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.action = action;
            this.details = details;
            this.timestamp = timestamp;
        }
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

    public boolean isUserEnabled(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_IS_ENABLED}, COL_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1;
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
