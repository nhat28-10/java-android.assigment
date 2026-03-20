package com.example.groupassignment.auth.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.groupassignment.auth.model.User;

import java.util.ArrayList;
import java.util.List;

public class AuthDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "group_assignment.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_USERS = "users";

    public static final String COL_ID = "id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";

    public AuthDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    public void ensureDefaultUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count == 0) {
                seedDefaultUsers(db);
            }
        }
    }

    private void seedDefaultUsers(SQLiteDatabase db) {
        String[][] defaultUsers = {
            {"Admin", "admin", "admin@example.com", "123", "admin"},
            {"Manager", "manager", "manager@example.com", "123", "manager"},
            {"Annotator 1", "anno1", "anno1@example.com", "123", "annotator"},
            {"Annotator 2", "anno2", "anno2@example.com", "123", "annotator"},
            {"Annotator 3", "anno3", "anno3@example.com", "123", "annotator"},
            {"Reviewer 1", "rev1", "rev1@example.com", "123", "reviewer"},
            {"Reviewer 2", "rev2", "rev2@example.com", "123", "reviewer"},
            {"Reviewer 3", "rev3", "rev3@example.com", "123", "reviewer"}
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTableIfNeeded(db);
        seedDefaultUsers(db);
    }

    private void createUsersTableIfNeeded(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT NOT NULL, "
                + COL_USERNAME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_ROLE + " TEXT NOT NULL"
                + ")";
        db.execSQL(createUsersTable);
    }

    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, user.getPassword());
        values.put(COL_ROLE, user.getRole());
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    public User loginUser(String email, String password) {
        ensureDefaultUsers(); // Đảm bảo có user để login
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_EMAIL + "=? AND " + COL_PASSWORD + "=?", new String[]{email, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = cursorToUser(cursor);
            cursor.close();
            return user;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = cursorToUser(cursor);
            cursor.close();
            return user;
        }
        return null;
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = cursorToUser(cursor);
            cursor.close();
            return user;
        }
        return null;
    }

    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_ROLE + "=?", new String[]{role}, null, null, COL_FULL_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, COL_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return users;
    }

    private User cursorToUser(Cursor cursor) {
        return new User(
            cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
        );
    }
}
