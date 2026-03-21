package com.example.groupassignment.reviewer;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.annotator.AnnotationDrawingView;
import com.example.groupassignment.auth.data.AuthDbHelper;
import com.example.groupassignment.auth.model.User;
import com.example.groupassignment.manager.SourceItemPreviewHelper;
import com.example.groupassignment.reviewer.data.TaskDbHelper;
import com.example.groupassignment.reviewer.model.LogicalTaskItem;
import com.example.groupassignment.utils.SessionManager;

public class ReviewerTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView tvTaskTitle;
    private TextView tvProjectName;
    private TextView tvDatasetName;
    private TextView tvAnnotatorName;
    private TextView tvTaskType;
    private TextView tvTaskStatus;
    private TextView tvSubmittedAt;
    private TextView tvReviewedAt;
    private TextView tvSourceMeta;
    private TextView tvSourceTextPreview;
    private TextView tvTaskLabels;
    private TextView tvVoteSummary;
    private TextView tvAnnotationResult;
    private TextView tvAnnotationPayload;
    private EditText edtReviewComment;
    private EditText edtRejectionReason;
    private AnnotationDrawingView ivSourcePreview;
    private View layoutImagePreview;
    private View layoutAudioPreview;
    private View layoutTextPreview;
    private Button btnPlayAudio;
    private Button btnPauseAudio;
    private Button btnApprove;
    private Button btnReject;

    private TaskDbHelper taskDbHelper;
    private int taskId;
    private LogicalTaskItem taskItem;
    private SessionManager sessionManager;
    private int currentReviewerId;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer_task);

        taskDbHelper = new TaskDbHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isReviewer()) {
            finish();
            startActivity(com.example.groupassignment.utils.RoleNavigation.buildHomeIntent(this, sessionManager.getRole()));
            return;
        }

        currentReviewerId = resolveReviewerId();
        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        initViews();
        setupActions();
        loadTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
    }

    private void initViews() {
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDatasetName = findViewById(R.id.tvDatasetName);
        tvAnnotatorName = findViewById(R.id.tvAnnotatorName);
        tvTaskType = findViewById(R.id.tvTaskType);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvSubmittedAt = findViewById(R.id.tvSubmittedAt);
        tvReviewedAt = findViewById(R.id.tvReviewedAt);
        tvSourceMeta = findViewById(R.id.tvSourceMeta);
        tvSourceTextPreview = findViewById(R.id.tvSourceTextPreview);
        tvTaskLabels = findViewById(R.id.tvTaskLabels);
        tvVoteSummary = findViewById(R.id.tvVoteSummary);
        tvAnnotationResult = findViewById(R.id.tvAnnotationResult);
        tvAnnotationPayload = findViewById(R.id.tvAnnotationPayload);
        edtReviewComment = findViewById(R.id.edtReviewComment);
        edtRejectionReason = findViewById(R.id.edtRejectionReason);
        ivSourcePreview = findViewById(R.id.ivSourcePreview);
        ivSourcePreview.setReadOnly(true);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        layoutAudioPreview = findViewById(R.id.layoutAudioPreview);
        layoutTextPreview = findViewById(R.id.layoutTextPreview);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnPauseAudio = findViewById(R.id.btnPauseAudio);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
    }

    private void setupActions() {
        btnApprove.setOnClickListener(v -> handleApprove());
        btnReject.setOnClickListener(v -> handleReject());
        btnPlayAudio.setOnClickListener(v -> playAudioPreview());
        btnPauseAudio.setOnClickListener(v -> pauseAudioPreview());
    }

    private void loadTask() {
        if (taskId <= 0) {
            Toast.makeText(this, "Task không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        taskDbHelper.refreshExpiredTasks();
        taskItem = taskDbHelper.getLogicalTaskByRawTaskId(taskId);
        if (taskItem == null) {
            Toast.makeText(this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindTask(taskItem);
    }

    private void bindTask(LogicalTaskItem item) {
        tvTaskTitle.setText("Review Task • " + item.getDisplaySourceName() + " • Round " + item.getRoundNumber());
        tvProjectName.setText(item.getProjectName());
        tvDatasetName.setText(item.getDatasetName() + " • " + item.getDisplaySourceName());
        tvAnnotatorName.setText(item.getAnnotatorName());
        tvTaskType.setText(item.getType() + " • Assigned: " + safeText(item.getAssignedAt()));
        tvTaskStatus.setText(item.getDisplayStatus());
        tvSubmittedAt.setText(safeText(item.getSubmittedAt()));
        tvReviewedAt.setText(safeText(item.getReviewedAt()));
        tvTaskLabels.setText(formatPipeSeparated(item.getAnnotationLabelsRaw(), "No labels submitted"));
        tvVoteSummary.setText(item.getApproveCount() + " approve • " + item.getRejectCount() + " reject • " + item.getPendingVotes() + " pending");
        tvAnnotationResult.setText(TextUtils.isEmpty(item.getAnnotationResult()) ? "No annotation submitted" : item.getAnnotationResult().replace("||", ", "));
        tvAnnotationPayload.setText(TextUtils.isEmpty(item.getAnnotationPayload()) ? "No payload available" : item.getAnnotationPayload());
        tvSourceMeta.setText(buildSourceMeta(item));
        bindSourcePreview(item);

        boolean canVote = taskDbHelper.canReviewerVote(item, currentReviewerId);
        btnApprove.setEnabled(canVote);
        btnReject.setEnabled(canVote);
        edtReviewComment.setEnabled(canVote);
        edtRejectionReason.setEnabled(canVote);
        if (!canVote) {
            edtReviewComment.setHint("This round is already decided or you already voted.");
        }
    }

    private void bindSourcePreview(LogicalTaskItem item) {
        releaseMediaPlayer();
        layoutImagePreview.setVisibility(View.GONE);
        layoutAudioPreview.setVisibility(View.GONE);
        layoutTextPreview.setVisibility(View.GONE);
        ivSourcePreview.setImageDrawable(null);
        ivSourcePreview.loadBoxesFromJson(null);
        tvSourceTextPreview.setText("");

        String sourceUri = item.getSourceUri();
        String displayName = item.getDisplaySourceName();
        String mimeType = item.getSourceMimeType();
        if (SourceItemPreviewHelper.isImage(mimeType, displayName, sourceUri)) {
            layoutImagePreview.setVisibility(View.VISIBLE);
            Bitmap bitmap = SourceItemPreviewHelper.loadImageThumbnail(this, sourceUri, 1600);
            if (bitmap != null) {
                ivSourcePreview.setImageBitmap(bitmap);
                String annotationJson = item.getAnnotationResult();
                if (!TextUtils.isEmpty(annotationJson) && annotationJson.trim().startsWith("[")) {
 codex/fix-annotation-overlay-for-reviewer
                    ivSourcePreview.post(() -> ivSourcePreview.loadBoxesFromJson(annotationJson));

                    ivSourcePreview.loadBoxesFromJson(annotationJson);

                }
            } else {
                tvSourceMeta.append("\nPreview unavailable. Check SAF permission or source URI.");
            }
            return;
        }

        if (SourceItemPreviewHelper.isAudio(mimeType, displayName, sourceUri)) {
            layoutAudioPreview.setVisibility(View.VISIBLE);
            tvSourceTextPreview.setText(TextUtils.isEmpty(sourceUri)
                    ? "Audio source URI is missing."
                    : "Audio source ready for playback. URI: " + sourceUri);
            return;
        }

        layoutTextPreview.setVisibility(View.VISIBLE);
        String preview = looksLikeUri(sourceUri)
                ? SourceItemPreviewHelper.readTextPreview(this, sourceUri, 1200)
                : sourceUri;
        if (TextUtils.isEmpty(preview)) {
            preview = TextUtils.isEmpty(sourceUri)
                    ? "No text preview available."
                    : "Text source stored at: " + sourceUri;
        }
        tvSourceTextPreview.setText(preview);
    }

    private String buildSourceMeta(LogicalTaskItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(safeText(item.getDisplaySourceName()));
        builder.append("\nMime type: ").append(safeText(item.getSourceMimeType()));
        builder.append("\nSource URI: ").append(safeText(item.getSourceUri()));
        return builder.toString();
    }

    private void playAudioPreview() {
        if (taskItem == null || !looksLikeUri(taskItem.getSourceUri())) {
            Toast.makeText(this, "Audio source not available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, Uri.parse(taskItem.getSourceUri()));
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.setOnCompletionListener(mp -> pauseAudioPreview());
                mediaPlayer.prepareAsync();
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception exception) {
            releaseMediaPlayer();
            Toast.makeText(this, "Không thể phát audio preview", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudioPreview() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException ignored) {
            // MediaPlayer may already be idle.
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private boolean looksLikeUri(String value) {
        return !TextUtils.isEmpty(value) && value.contains("://");
    }

    private void handleApprove() {
        String reviewComment = edtReviewComment.getText().toString().trim();
        boolean success = taskDbHelper.approveTask(taskId, reviewComment);
        if (!success) {
            Toast.makeText(this, "Không thể approve task", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đã approve task", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void handleReject() {
        String reviewComment = edtReviewComment.getText().toString().trim();
        String rejectionReason = edtRejectionReason.getText().toString().trim();
        if (TextUtils.isEmpty(rejectionReason)) {
            edtRejectionReason.setError("Vui lòng nhập reason khi reject");
            edtRejectionReason.requestFocus();
            return;
        }
        boolean success = taskDbHelper.rejectTask(taskId, reviewComment, rejectionReason);
        if (!success) {
            Toast.makeText(this, "Không thể reject task", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đã reject task", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private String formatPipeSeparated(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value.replace("||", ", ");
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private int resolveReviewerId() {
        long sessionUserId = sessionManager.getUserId();
        if (sessionUserId > 0) {
            return (int) sessionUserId;
        }
        String email = sessionManager.getEmail();
        if (!TextUtils.isEmpty(email)) {
            AuthDbHelper authDbHelper = new AuthDbHelper(this);
            User currentUser = authDbHelper.getUserByEmail(email);
            if (currentUser != null) {
                return (int) currentUser.getId();
            }
        }
        return 0;
    }
}
