package com.example.groupassignment.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.utils.SessionManager;

public class UserManagementActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etFullName, etPassword;
    private Spinner spRole;
    private Button btnSave;
    private AuthDbHelper authDbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        sessionManager = new SessionManager(this);
        authDbHelper = new AuthDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnSave = findViewById(R.id.btnSave);

        setupRoleSpinner();
        btnSave.setOnClickListener(v -> saveUser());
    }

    private void setupRoleSpinner() {
        String[] roles = {"admin", "manager", "annotator", "reviewer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spRole.setAdapter(adapter);
    }

    private void saveUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
            return;
        }

        if (authDbHelper.isEmailExists(email)) {
            etEmail.setError("Email already exists");
            etEmail.requestFocus();
            return;
        }

        User user = new User(0, fullName, username, email, password, role);
        long id = authDbHelper.registerUser(user);
        if (id > 0) {
            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show();
        }
    }
}
