package com.example.groupassignment.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;

public class UserManagementActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etFullName, etPassword;
    private Spinner spRole;
    private ListView lvUsers;
    private Button btnSave;
    private AuthDbHelper authDbHelper;
    private SessionManager sessionManager;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        sessionManager = new SessionManager(this);
        authDbHelper = new AuthDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        lvUsers = findViewById(R.id.lvUsers);
        btnSave = findViewById(R.id.btnSave);

        setupRoleSpinner();
        refreshUserList();

        btnSave.setOnClickListener(v -> saveUser());

        lvUsers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = userList.get(position);
            showUserActionDialog(selectedUser);
        });
    }

    private void setupRoleSpinner() {
        // Chỉ cho phép tạo Manager, Annotator, Reviewer. Chặn tạo Admin.
        String[] roles = {"manager", "annotator", "reviewer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roles) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.WHITE); // Chữ sáng
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                v.setBackgroundColor(Color.parseColor("#1A1A1A")); // Nền tối
                ((TextView) v).setTextColor(Color.WHITE); // Chữ sáng
                return v;
            }
        };
        spRole.setAdapter(adapter);
    }

    private void refreshUserList() {
        userList = authDbHelper.getAllUsers();
        ArrayAdapter<User> adapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_2, android.R.id.text1, userList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                User u = userList.get(position);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                boolean isEnabled = authDbHelper.isUserEnabled(u.getId());
                String status = isEnabled ? " [Enabled]" : " [DISABLED]";

                text1.setText(u.getFullName() + status);
                text1.setTextColor(isEnabled ? Color.CYAN : Color.RED);

                text2.setText("Role: " + u.getRole() + " | Email: " + u.getEmail());
                text2.setTextColor(Color.LTGRAY);
                return view;
            }
        };
        lvUsers.setAdapter(adapter);
    }

    private void showUserActionDialog(User user) {
        if (user.getEmail().equals("admin@gmail.com")) {
            Toast.makeText(this, "Cannot modify Super Admin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isEnabled = authDbHelper.isUserEnabled(user.getId());
        String actionText = isEnabled ? "Disable Account" : "Enable Account";

        new AlertDialog.Builder(this)
                .setTitle("Manage User: " + user.getUsername())
                .setItems(new String[]{actionText, "Cancel"}, (dialog, which) -> {
                    if (which == 0) {
                        authDbHelper.updateUserStatus(user.getId(), !isEnabled);
                        Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                        refreshUserList();
                    }
                })
                .show();
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

        if (authDbHelper.isEmailExists(email)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(0, fullName, username, email, password, role);
        long id = authDbHelper.registerUser(user);
        if (id > 0) {
            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
            refreshUserList();
            clearForm();
        }
    }

    private void clearForm() {
        etUsername.setText("");
        etEmail.setText("");
        etFullName.setText("");
        etPassword.setText("");
    }
}
