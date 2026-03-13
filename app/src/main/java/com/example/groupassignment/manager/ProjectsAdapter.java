package com.example.groupassignment.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment.R;

import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<ProjectItem> projectList;

    public ProjectsAdapter(List<ProjectItem> projectList) {
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem item = projectList.get(position);

        holder.tvProjectName.setText(item.getName());
        holder.tvProjectDescription.setText(item.getDescription());
        holder.tvStatus.setText(item.getStatus().toUpperCase());
        holder.tvReviewer.setText("Reviewer: " + item.getReviewer());
        holder.tvAnnotator.setText("Annotator: " + item.getAnnotator());
        holder.tvUpdatedAt.setText("Last Updated: " + item.getUpdatedAt());

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Open project: " + item.getName(), Toast.LENGTH_SHORT).show()
        );

        holder.btnEditProject.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Edit: " + item.getName(), Toast.LENGTH_SHORT).show()
        );

        holder.btnDeleteProject.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Delete: " + item.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvProjectDescription, tvStatus, tvReviewer, tvAnnotator, tvUpdatedAt;
        Button btnEditProject, btnDeleteProject;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReviewer = itemView.findViewById(R.id.tvReviewer);
            tvAnnotator = itemView.findViewById(R.id.tvAnnotator);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
            btnEditProject = itemView.findViewById(R.id.btnEditProject);
            btnDeleteProject = itemView.findViewById(R.id.btnDeleteProject);
        }
    }
}