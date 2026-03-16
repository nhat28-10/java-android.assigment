package com.example.groupassigment.ui.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groupassigment.R;
import com.example.groupassigment.models.Project;
import com.example.groupassigment.repository.ProjectRepository;
import com.example.groupassigment.utils.SessionManager;

public class ProjectManagementActivity extends AppCompatActivity {
    private EditText etProjectName, etProjectDescription;
    private Button btnCreateProject;
    private ProjectRepository projectRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_management);

        sessionManager = new SessionManager(this);
        projectRepository = new ProjectRepository(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, " Access denied\, Toast.LENGTH_SHORT).show();
 finish();
 return;
 }

 etProjectName = findViewById(R.id.etProjectName);
 etProjectDescription = findViewById(R.id.etProjectDescription);
 btnCreateProject = findViewById(R.id.btnCreateProject);

 btnCreateProject.setOnClickListener(new View.OnClickListener() {
 @Override
 public void onClick(View v) { createProject(); }
 });
 }

 private void createProject() {
 String name = etProjectName.getText().toString().trim();
 String description = etProjectDescription.getText().toString().trim();

 if (name.isEmpty()) {
 etProjectName.setError(\Project name is required\);
 return;
 }

 Project project = new Project();
 project.setName(name);
 project.setDescription(description);
 project.setManagerId(sessionManager.getUserId());
 project.setStatus(\ACTIVE\);

 long id = projectRepository.insertProject(project);
 if (id > 0) {
 Toast.makeText(this, \Project created successfully\, Toast.LENGTH_SHORT).show();
 finish();
 } else {
 Toast.makeText(this, \Failed to create project\, Toast.LENGTH_SHORT).show();
 }
 }
}
