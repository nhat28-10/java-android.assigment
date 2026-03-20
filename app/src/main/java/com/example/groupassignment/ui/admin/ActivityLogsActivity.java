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
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.List;

public class ActivityLogsActivity extends AppCompatActivity {
    private ListView lvActivityLogs;
    private Button btnBackLogs;
    private AuthDbHelper authDbHelper;
    private SessionManager sessionManager;
    private List<AuthDbHelper.ActivityLog> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_logs);

        sessionManager = new SessionManager(this);
        authDbHelper = new AuthDbHelper(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        lvActivityLogs = findViewById(R.id.lvActivityLogs);
        btnBackLogs = findViewById(R.id.btnBackLogs);

        refreshLogs();

        btnBackLogs.setOnClickListener(v -> finish());
    }

    private void refreshLogs() {
        logList = authDbHelper.getAllActivityLogs();
        
        ArrayAdapter<AuthDbHelper.ActivityLog> adapter = new ArrayAdapter<AuthDbHelper.ActivityLog>(this, android.R.layout.simple_list_item_2, logList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                }

                AuthDbHelper.ActivityLog log = logList.get(position);
                TextView text1 = convertView.findViewById(android.R.id.text1);
                TextView text2 = convertView.findViewById(android.R.id.text2);

                text1.setText(log.action + " - " + log.userName);
                text1.setTextColor(getActionColor(log.action));
                text1.setShadowLayer(2, 1, 1, Color.BLACK);
                text1.setTextSize(16);

                text2.setText(log.details + "\n" + log.timestamp);
                text2.setTextColor(Color.LTGRAY);
                
                convertView.setPadding(16, 16, 16, 16);
                return convertView;
            }
        };
        lvActivityLogs.setAdapter(adapter);
    }

    private int getActionColor(String action) {
        if (action == null) return Color.WHITE;
        switch (action) {
            case "LOGIN": return Color.CYAN;
            case "STATUS_CHANGE": return Color.YELLOW;
            case "CREATE_USER": return Color.GREEN;
            default: return Color.WHITE;
        }
    }
}
