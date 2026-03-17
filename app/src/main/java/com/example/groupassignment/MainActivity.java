package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.annotator.AnnotatorDashboardActivity;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.manager.ManagerDashboardActivity;
import com.example.groupassignment.reviewer.ReviewerDashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = getIntent().getStringExtra("role");

        if ("manager".equals(role)) {
            startActivity(new Intent(this, ManagerDashboardActivity.class));
        } else if ("annotator".equals(role)) {
            startActivity(new Intent(this, AnnotatorDashboardActivity.class));
        } else if ("reviewer".equals(role)) {
            startActivity(new Intent(this, ReviewerDashboardActivity.class));
        } else {
            // Nếu không có role hoặc role không khớp, chuyển về màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
