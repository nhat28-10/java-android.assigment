package com.example.groupassignment.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtUsername;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Spinner spRole;
    private Button btnCreateAccount;
    private TextView tvGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupRoleSpinner();
        setupActions();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        spRole = findViewById(R.id.spRole);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoLogin = findViewById(R.id.tvGoLogin);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.register_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);
    }

    private void setupActions() {
        btnCreateAccount.setOnClickListener(v -> handleRegister());

        tvGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String roleLabel = spRole.getSelectedItem().toString();
        String roleValue = mapRoleToApiValue(roleLabel);

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Please enter your full name");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Please enter username");
            edtUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Please enter email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email address");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Please enter password");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Please confirm password");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return;
        }

        AuthDbHelper authDbHelper = new AuthDbHelper(this);

        if (authDbHelper.isEmailExists(email)) {
            edtEmail.setError("Email already exists");
            edtEmail.requestFocus();
            return;
        }

        User user = new User(fullName, username, email, password, roleValue);
        long result = authDbHelper.registerUser(user);

        if (result == -1) {
            Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Register success. Please login.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    private String mapRoleToApiValue(String roleLabel) {
        if ("Annotator".equalsIgnoreCase(roleLabel)) return "annotator";
        if ("Reviewer".equalsIgnoreCase(roleLabel)) return "reviewer";
        if ("Manager".equalsIgnoreCase(roleLabel)) return "manager";
        return "annotator";
    }
}
