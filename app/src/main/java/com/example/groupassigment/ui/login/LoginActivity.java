package com.example.groupassigment.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassigment.R;
import com.example.groupassigment.models.User;
import com.example.groupassigment.repository.UserRepository;
import com.example.groupassigment.ui.admin.AdminDashboardActivity;
import com.example.groupassigment.ui.manager.ManagerDashboardActivity;
import com.example.groupassigment.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirectToDashboard();
            return;
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { login(); }
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError(" Username is required\);
 return;
 }
 if (password.isEmpty()) {
 etPassword.setError(\Password is required\);
 return;
 }

 User user = userRepository.login(username, password);
 if (user != null) {
 sessionManager.createSession(user);
 Toast.makeText(this, \Welcome \ + user.getFullName() + \!\, Toast.LENGTH_SHORT).show();
 redirectToDashboard();
 } else {
 Toast.makeText(this, \Invalid username or password\, Toast.LENGTH_SHORT).show();
 }
 }

 private void redirectToDashboard() {
 Intent intent;
 if (sessionManager.isAdmin()) {
 intent = new Intent(this, AdminDashboardActivity.class);
 } else if (sessionManager.isManager()) {
 intent = new Intent(this, ManagerDashboardActivity.class);
 } else {
 Toast.makeText(this, \Unauthorized role\, Toast.LENGTH_SHORT).show();
 sessionManager.logout();
 return;
 }
 startActivity(intent);
 finish();
 }
}
