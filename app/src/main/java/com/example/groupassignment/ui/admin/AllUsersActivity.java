package com.example.groupassignment.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class AllUsersActivity extends AppCompatActivity {
    private ListView lvAllUsers;
    private Button btnBack;
    private AuthDbHelper authDbHelper;
    private SessionManager sessionManager;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        sessionManager = new SessionManager(this);
        authDbHelper = new AuthDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        lvAllUsers = findViewById(R.id.lvAllUsers);
        btnBack = findViewById(R.id.btnBack);

        refreshUserList();

        lvAllUsers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = userList.get(position);
            showUserActionDialog(selectedUser);
        });

        btnBack.setOnClickListener(v -> finish());
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
                text1.setTextSize(18);

                text2.setText("Role: " + u.getRole() + " | Email: " + u.getEmail());
                text2.setTextColor(Color.LTGRAY);
                
                // Set background for items to look like cards
                view.setPadding(32, 32, 32, 32);
                return view;
            }
        };
        lvAllUsers.setAdapter(adapter);
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
                .setMessage("Do you want to " + (isEnabled ? "disable" : "enable") + " this account?")
                .setPositiveButton(actionText, (dialog, which) -> {
                    authDbHelper.updateUserStatus(user.getId(), !isEnabled);
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                    refreshUserList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
