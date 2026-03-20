package com.example.groupassignment.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
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
    private TextView tvStatTotal, tvStatManager, tvStatAnnotator, tvStatReviewer;
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

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatManager = findViewById(R.id.tvStatManager);
        tvStatAnnotator = findViewById(R.id.tvStatAnnotator);
        tvStatReviewer = findViewById(R.id.tvStatReviewer);
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
        updateStatistics();

        ArrayAdapter<User> adapter = new ArrayAdapter<User>(this, R.layout.item_user_card, userList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_card, parent, false);
                }

                User u = userList.get(position);
                TextView tvName = convertView.findViewById(R.id.tvUserFullName);
                TextView tvEmail = convertView.findViewById(R.id.tvUserEmail);
                TextView tvRole = convertView.findViewById(R.id.tvUserRole);
                TextView tvStatus = convertView.findViewById(R.id.tvUserStatus);
                View indicator = convertView.findViewById(R.id.viewRoleIndicator);

                boolean isEnabled = authDbHelper.isUserEnabled(u.getId());

                tvName.setText(u.getFullName());
                tvEmail.setText(u.getEmail());
                tvRole.setText(u.getRole());

                if (isEnabled) {
                    tvStatus.setText("ACTIVE");
                    tvStatus.setBackgroundResource(R.drawable.bg_button_cyan);
                    tvStatus.setTextColor(Color.parseColor("#004D40"));
                } else {
                    tvStatus.setText("DISABLED");
                    tvStatus.setBackgroundResource(R.drawable.bg_button_primary);
                    tvStatus.setTextColor(Color.WHITE);
                }

                // Set indicator color based on role
                switch (u.getRole().toLowerCase()) {
                    case "admin": indicator.setBackgroundColor(Color.parseColor("#FF5252")); break;
                    case "manager": indicator.setBackgroundColor(Color.parseColor("#448AFF")); break;
                    case "annotator": indicator.setBackgroundColor(Color.parseColor("#4CAF50")); break;
                    case "reviewer": indicator.setBackgroundColor(Color.parseColor("#FFC107")); break;
                }

                return convertView;
            }
        };
        lvAllUsers.setAdapter(adapter);
    }

    private void updateStatistics() {
        int total = userList.size();
        int managers = 0, annotators = 0, reviewers = 0;
        for (User u : userList) {
            switch (u.getRole().toLowerCase()) {
                case "manager": managers++; break;
                case "annotator": annotators++; break;
                case "reviewer": reviewers++; break;
            }
        }
        tvStatTotal.setText(String.valueOf(total));
        tvStatManager.setText(String.valueOf(managers));
        tvStatAnnotator.setText(String.valueOf(annotators));
        tvStatReviewer.setText(String.valueOf(reviewers));
    }

    private void showUserActionDialog(User user) {
        if (user.getEmail().equals("admin@gmail.com")) {
            Toast.makeText(this, "Cannot modify Super Admin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isEnabled = authDbHelper.isUserEnabled(user.getId());
        String actionText = isEnabled ? "Vô hiệu hóa tài khoản" : "Kích hoạt tài khoản";

        new AlertDialog.Builder(this)
                .setTitle("Quản lý: " + user.getFullName())
                .setMessage("Bạn có chắc chắn muốn " + (isEnabled ? "vô hiệu hóa" : "kích hoạt") + " tài khoản này?")
                .setPositiveButton(actionText, (dialog, which) -> {
                    authDbHelper.updateUserStatus(user.getId(), !isEnabled);
                    refreshUserList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
