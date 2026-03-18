package com.example.groupassignment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.utils.RoleNavigation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(RoleNavigation.buildHomeIntent(this, getIntent().getStringExtra("role")));
        finish();
    }
}
