package com.example.groupassignment;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SessionManager sessionManager = new SessionManager(this);

            if (!sessionManager.isLoggedIn()) {
                startActivity(RoleNavigation.buildHomeIntent(this, null));
                finish();
                return;
            }

            startActivity(RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error in SplashActivity", e);
            startActivity(RoleNavigation.buildHomeIntent(this, null));
            finish();
        }
    }
}
