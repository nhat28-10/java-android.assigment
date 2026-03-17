package com.example.groupassignment.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "group_assignment_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLogin(String token, String role, String name, String email) {
        saveLogin(0, token, role, name, email);
    }

    public void saveLogin(long userId, String token, String role, String name, String email) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
