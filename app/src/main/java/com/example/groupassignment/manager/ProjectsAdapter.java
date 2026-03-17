package com.example.groupassignment.manager;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment.R;
import com.example.groupassignment.manager.model.ProjectItem;

import java.util.List;
import java.util.Locale;

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
        holder.tvStatus.setText(item.getStatus().toUpperCase(Locale.ROOT));
        holder.tvReviewer.setText("Reviewers: " + item.getReviewerCount());
        holder.tvAnnotator.setText("Annotators: " + item.getAnnotatorCount());
        holder.tvUpdatedAt.setText("Last Updated: " + item.getLastUpdated());

        if (holder.tvReviewStatus != null) {
            holder.tvReviewStatus.setText("Review: " + item.getReviewStatus().toUpperCase(Locale.ROOT));
        }

        holder.tvStatus.setTextColor(getStatusColor(item.getStatus()));

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Open project: " + item.getName(), Toast.LENGTH_SHORT).show()
        );

        holder.btnEditProject.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CreateProjectActivity.class);
            intent.putExtra(ProjectsActivity.EXTRA_PROJECT_MODE, ProjectsActivity.MODE_EDIT);
            intent.putExtra("project_edit_data", item);
            v.getContext().startActivity(intent);
        });

        holder.btnDeleteProject.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Delete: " + item.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.LTGRAY;

        switch (status.toLowerCase(Locale.ROOT)) {
            case "active":
                return Color.parseColor("#22C55E");
            case "completed":
                return Color.parseColor("#3B82F6");
            case "archived":
                return Color.parseColor("#9CA3AF");
            case "draft":
                return Color.parseColor("#F59E0B");
            default:
                return Color.LTGRAY;
        }
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvProjectDescription, tvStatus, tvReviewer, tvAnnotator, tvUpdatedAt, tvReviewStatus;
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

            int reviewStatusId = itemView.getResources().getIdentifier(
                    "tvReviewStatus",
                    "id",
                    itemView.getContext().getPackageName()
            );
            tvReviewStatus = reviewStatusId != 0 ? itemView.findViewById(reviewStatusId) : null;
        }
    }
}
