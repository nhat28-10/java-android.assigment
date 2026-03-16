package com.example.groupassigment.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.groupassigment.models.User;
import com.example.groupassigment.models.UserRole;

public class SessionManager {
    private static final String PREF_NAME = "DataLabelingSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(User user) {
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_ROLE, user.getRole().name());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;

        User user = new User();
        user.setId(prefs.getLong(KEY_USER_ID, 0));
        user.setUsername(prefs.getString(KEY_USERNAME, ""));
        user.setFullName(prefs.getString(KEY_FULL_NAME, ""));
        user.setEmail(prefs.getString(KEY_EMAIL, ""));
        user.setRole(UserRole.valueOf(prefs.getString(KEY_ROLE, "MANAGER")));
        return user;
    }

    public boolean isAdmin() {
        String role = prefs.getString(KEY_ROLE, "");
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        String role = prefs.getString(KEY_ROLE, "");
        return "MANAGER".equals(role);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0);
    }

    public String getUserRole() {
        return prefs.getString(KEY_ROLE, "");
    }
}
