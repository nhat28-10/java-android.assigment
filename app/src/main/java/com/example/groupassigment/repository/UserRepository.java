package com.example.groupassigment.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.groupassigment.database.DatabaseHelper;
import com.example.groupassigment.models.User;
import com.example.groupassigment.models.UserRole;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public User login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1",
            new String[]{username, password});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        db.close();
        return user;
    }

    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("email", user.getEmail());
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole().name());
        values.put("is_active", user.isActive() ? 1 : 0);
        long id = db.insert("users", null, values);
        db.close();
        return id;
    }

    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("email", user.getEmail());
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole().name());
        values.put("is_active", user.isActive() ? 1 : 0);
        int result = db.update("users", values, "id = ?", new String[]{String.valueOf(user.getId())});
        db.close();
        return result;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(id)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        db.close();
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users ORDER BY created_at DESC", null);
        if (cursor.moveToFirst()) {
            do { users.add(cursorToUser(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return users;
    }

    public List<User> getUsersByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE role = ? ORDER BY full_name", new String[]{role.name()});
        if (cursor.moveToFirst()) {
            do { users.add(cursorToUser(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return users;
    }

    public List<User> getAnnotatorsAndReviewers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM users WHERE role IN ('ANNOTATOR', 'REVIEWER') AND is_active = 1 ORDER BY full_name", null);
        if (cursor.moveToFirst()) {
            do { users.add(cursorToUser(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return users;
    }

    public int getUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        int count = 0;
        if (cursor.moveToFirst()) { count = cursor.getInt(0); }
        cursor.close();
        db.close();
        return count;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setRole(UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("role"))));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
        return user;
    }
}
