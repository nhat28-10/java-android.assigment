package com.example.groupassignment.annotator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.LoginActivity;
import com.example.groupassignment.utils.SessionManager;

public class AnnotatorOverviewActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_overview);

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}