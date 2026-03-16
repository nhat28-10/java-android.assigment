package com.example.groupassignment.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.MainActivity;
import com.example.groupassignment.R;
import com.example.groupassignment.utils.SessionManager;
import com.example.groupassignment.manager.ManagerDashboardActivity;
import com.example.groupassignment.annotator.AnnotatorOverviewActivity;
import com.example.groupassignment.reviewer.ReviewerDashboardActivity;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng quên mật khẩu sẽ làm sau", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        AuthDbHelper authDbHelper = new AuthDbHelper(this);
        User user = authDbHelper.loginUser(email, password);

        if (user == null) {
            Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(LoginActivity.this);
        sessionManager.saveLogin(
                "demo_token",
                user.getRole(),
                user.getFullName(),
                user.getEmail()
        );

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        navigateByRole(user.getRole());
        finish();
    }
    private void navigateByRole(String roleValue) {
        if ("manager".equalsIgnoreCase(roleValue) || "admin".equalsIgnoreCase(roleValue)) {
            startActivity(new Intent(LoginActivity.this, ManagerDashboardActivity.class));
        } else if ("annotator".equalsIgnoreCase(roleValue)) {
            startActivity(new Intent(LoginActivity.this, AnnotatorOverviewActivity.class));
        } else if ("reviewer".equalsIgnoreCase(roleValue)) {
            startActivity(new Intent(LoginActivity.this, ReviewerDashboardActivity.class));
        }
    }
}