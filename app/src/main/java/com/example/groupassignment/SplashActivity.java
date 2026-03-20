package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Có thể thêm layout splash nếu muốn: setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(this);

            if (!sessionManager.isLoggedIn()) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                startActivity(RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            }
            finish();
        }, 500); // Delay 500ms để đảm bảo hệ thống ổn định
    }
}
