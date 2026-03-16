package com.example.groupassigment.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassigment.R;
import com.example.groupassigment.models.User;
import com.example.groupassigment.models.UserRole;
import com.example.groupassigment.repository.UserRepository;
import com.example.groupassigment.utils.SessionManager;
import java.util.Arrays;

public class UserManagementActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etFullName, etPassword;
    private Spinner spRole;
    private Button btnSave;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private User editUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, " Access denied\, Toast.LENGTH_SHORT).show();
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

 btnSave.setOnClickListener(new View.OnClickListener() {
 @Override
 public void onClick(View v) { saveUser(); }
 });
 }

 private void setupRoleSpinner() {
 String[] roles = {\ADMIN\, \MANAGER\, \ANNOTATOR\, \REVIEWER\};
 ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
 spRole.setAdapter(adapter);
 }

 private void saveUser() {
 String username = etUsername.getText().toString().trim();
 String email = etEmail.getText().toString().trim();
 String fullName = etFullName.getText().toString().trim();
 String password = etPassword.getText().toString().trim();
 String role = spRole.getSelectedItem().toString();

 if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
 Toast.makeText(this, \Please fill all required fields\, Toast.LENGTH_SHORT).show();
 return;
 }

 User user = new User();
 user.setUsername(username);
 user.setEmail(email);
 user.setFullName(fullName);
 user.setPassword(password);
 user.setRole(UserRole.valueOf(role));
 user.setActive(true);

 long id = userRepository.insertUser(user);
 if (id > 0) {
 Toast.makeText(this, \User created successfully\, Toast.LENGTH_SHORT).show();
 finish();
 } else {
 Toast.makeText(this, \Failed to create user\, Toast.LENGTH_SHORT).show();
 }
 }
}
