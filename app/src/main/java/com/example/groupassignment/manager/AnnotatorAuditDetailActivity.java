package com.example.groupassignment.manager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment.R;
import com.example.groupassignment.utils.RoleNavigation;
import com.example.groupassignment.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AnnotatorAuditDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_NAME = "extra_project_name";
    public static final String EXTRA_ANNOTATOR_NAME = "extra_annotator_name";

    private ImageButton btnBackAuditDetail;

    private TextView tvAuditBreadcrumbProject;
    private TextView tvAuditTitle;
    private TextView tvAnnotatorAvatar;
    private TextView tvAnnotatorName;
    private TextView tvAnnotatorTaskCount;

    private EditText etSearchAuditTask;
    private Spinner spAuditStatusFilter;
    private Spinner spAuditReviewFilter;

    private LinearLayout layoutAuditTaskList;
    private TextView tvEmptyAuditTasks;

    private String projectName = "Project";
    private String annotatorName = "Annotator";

    private final List<AuditTaskItem> allTasks = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotator_audit_detail);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isManager()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            RoleNavigation.redirectToHome(this, sessionManager.getRole());
            return;
        }

        initViews();
        readIntentData();
        setupSpinners();
        seedMockTasks();
        bindHeader();
        setupActions();
        renderTaskList();
    }

    private void initViews() {
        btnBackAuditDetail = findViewById(R.id.btnBackAuditDetail);

        tvAuditBreadcrumbProject = findViewById(R.id.tvAuditBreadcrumbProject);
        tvAuditTitle = findViewById(R.id.tvAuditTitle);
        tvAnnotatorAvatar = findViewById(R.id.tvAnnotatorAvatar);
        tvAnnotatorName = findViewById(R.id.tvAnnotatorName);
        tvAnnotatorTaskCount = findViewById(R.id.tvAnnotatorTaskCount);

        etSearchAuditTask = findViewById(R.id.etSearchAuditTask);
        spAuditStatusFilter = findViewById(R.id.spAuditStatusFilter);
        spAuditReviewFilter = findViewById(R.id.spAuditReviewFilter);

        layoutAuditTaskList = findViewById(R.id.layoutAuditTaskList);
        tvEmptyAuditTasks = findViewById(R.id.tvEmptyAuditTasks);
    }

    private void readIntentData() {
        if (getIntent() == null) return;

        String projectExtra = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
        String annotatorExtra = getIntent().getStringExtra(EXTRA_ANNOTATOR_NAME);

        if (projectExtra != null && !projectExtra.trim().isEmpty()) {
            projectName = projectExtra;
        }

        if (annotatorExtra != null && !annotatorExtra.trim().isEmpty()) {
            annotatorName = annotatorExtra;
        }
    }

    private void setupSpinners() {
        String[] statusItems = {"All Status", "Assigned", "In Progress", "Submitted", "Approved", "Rejected"};
        String[] reviewItems = {"All Results", "Approved", "Rejected", "Pending Review"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statusItems
        );
        ArrayAdapter<String> reviewAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reviewItems
        );

        spAuditStatusFilter.setAdapter(statusAdapter);
        spAuditReviewFilter.setAdapter(reviewAdapter);
    }

    private void bindHeader() {
        tvAuditBreadcrumbProject.setText(projectName);
        tvAuditTitle.setText("Annotator Audit: " + annotatorName);

        String initial = annotatorName.trim().isEmpty() ? "A" : annotatorName.substring(0, 1).toUpperCase(Locale.getDefault());
        tvAnnotatorAvatar.setText(initial);
        tvAnnotatorName.setText(annotatorName);
        tvAnnotatorTaskCount.setText("Task count: " + allTasks.size());
    }

    private void setupActions() {
        btnBackAuditDetail.setOnClickListener(v -> finish());

        etSearchAuditTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderTaskList();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spAuditStatusFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> renderTaskList()));
        spAuditReviewFilter.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> renderTaskList()));
    }

    private void seedMockTasks() {
        allTasks.clear();

        allTasks.add(new AuditTaskItem(
                "TASK-IMG-102431",
                "forklift_01.jpg",
                "image",
                "approved",
                "3 bounding boxes for: Forklift, Worker, Helmet",
                "Ảnh kho hàng. Annotator đã khoanh đúng đủ 3 object chính.",
                "Tốt, annotation chính xác và đúng guideline.",
                "No issue",
                "16/03/2026 08:15",
                Arrays.asList(
                        new ReviewerVote("Reviewer Linh", "approved", "BBox chính xác", "16/03/2026 08:05"),
                        new ReviewerVote("Reviewer An", "approved", "Đúng label set", "16/03/2026 08:12")
                ),
                Arrays.asList(
                        new ReviewNote("Worker", "[22, 18, 31, 66]", "BBox hợp lệ"),
                        new ReviewNote("Helmet", "[27, 14, 30, 20]", "Nhận diện tốt")
                )
        ));

        allTasks.add(new AuditTaskItem(
                "TASK-TXT-884120",
                "support_ticket_17.txt",
                "text",
                "rejected",
                "2 text spans: Billing Issue, Escalation",
                "Khách hàng phản ánh bị trừ tiền hai lần và yêu cầu escalated support.",
                "Thiếu span cho thông tin urgency, cần gán nhãn lại.",
                "Missing span label",
                "16/03/2026 09:10",
                Arrays.asList(
                        new ReviewerVote("Reviewer Huy", "rejected", "Thiếu span quan trọng", "16/03/2026 09:02")
                ),
                Arrays.asList(
                        new ReviewNote("Urgency", "-", "Chưa đánh dấu đoạn thể hiện mức độ khẩn cấp")
                )
        ));

        allTasks.add(new AuditTaskItem(
                "TASK-AUD-445902",
                "machine_noise_03.wav",
                "audio",
                "submitted",
                "3 audio segments labeled: Alarm, Engine, Ambient",
                "Audio 18 giây. Có tiếng còi cảnh báo ở đầu file và tiếng động cơ nền.",
                "Đang chờ reviewer xác nhận kết quả segment.",
                "Pending review",
                "16/03/2026 10:20",
                Arrays.asList(
                        new ReviewerVote("Reviewer Nam", "pending", "Chưa review", "Pending")
                ),
                Arrays.asList(
                        new ReviewNote("Alarm", "0.00s - 2.10s", "Segment đã submit, chờ duyệt")
                )
        ));

        allTasks.add(new AuditTaskItem(
                "TASK-IMG-992001",
                "warehouse_cam_09.png",
                "image",
                "in_progress",
                "Annotator is drawing bounding boxes",
                "Ảnh camera kho đang được gán nhãn dở.",
                "Chưa có kết quả review vì task còn đang làm.",
                "Pending review",
                "Not reviewed",
                Arrays.asList(
                        new ReviewerVote("Reviewer Mai", "pending", "Chưa nhận submit", "Pending")
                ),
                new ArrayList<>()
        ));

        allTasks.add(new AuditTaskItem(
                "TASK-TXT-550321",
                "policy_note.md",
                "text",
                "assigned",
                "Task assigned, no labels yet",
                "Văn bản hướng dẫn nội bộ đang chờ annotator xử lý.",
                "Chưa có comment review.",
                "Pending review",
                "Not reviewed",
                Arrays.asList(
                        new ReviewerVote("Reviewer Khoa", "pending", "Task chưa submit", "Pending")
                ),
                new ArrayList<>()
        ));
    }

    private void renderTaskList() {
        layoutAuditTaskList.removeAllViews();

        List<AuditTaskItem> filtered = getFilteredTasks();

        if (filtered.isEmpty()) {
            tvEmptyAuditTasks.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyAuditTasks.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (AuditTaskItem item : filtered) {
            View card = inflater.inflate(R.layout.item_annotator_audit_task, layoutAuditTaskList, false);

            TextView tvTaskId = card.findViewById(R.id.tvAuditTaskId);
            TextView tvTaskPreview = card.findViewById(R.id.tvAuditTaskPreview);
            TextView tvLabelType = card.findViewById(R.id.tvAuditLabelType);
            TextView tvAnnotatorStatus = card.findViewById(R.id.tvAuditAnnotatorStatus);
            TextView tvReviewResult = card.findViewById(R.id.tvAuditReviewResult);
            Button btnViewReviewDetail = card.findViewById(R.id.btnViewReviewDetail);

            tvTaskId.setText("#" + getShortTaskId(item.taskId));
            tvTaskPreview.setText(item.getPreviewIcon() + "  " + item.fileName);
            tvLabelType.setText(item.getLabelTypeText());

            tvAnnotatorStatus.setText(item.getStatusDisplayText());
            tvAnnotatorStatus.setTextColor(getStatusTextColor(item.status));
            tvAnnotatorStatus.setBackground(makeRoundedBackground(getStatusBgColor(item.status)));

            tvReviewResult.setText(item.getReviewResultText());
            tvReviewResult.setTextColor(getReviewTextColor(item.status));
            tvReviewResult.setBackground(makeRoundedBackground(getReviewBgColor(item.status)));

            btnViewReviewDetail.setOnClickListener(v -> showTaskDetailDialog(item));

            layoutAuditTaskList.addView(card);
        }
    }

    private List<AuditTaskItem> getFilteredTasks() {
        List<AuditTaskItem> filtered = new ArrayList<>();

        String query = etSearchAuditTask.getText().toString().trim().toLowerCase(Locale.getDefault());
        String statusFilter = getSelectedStatusFilter();
        String reviewFilter = getSelectedReviewFilter();

        for (AuditTaskItem item : allTasks) {
            if (!statusFilter.equals("all") && !item.status.equals(statusFilter)) {
                continue;
            }

            if (!reviewFilter.equals("all")) {
                if ("approved".equals(reviewFilter) && !"approved".equals(item.status)) continue;
                if ("rejected".equals(reviewFilter) && !"rejected".equals(item.status)) continue;
                if ("pending".equals(reviewFilter) && ("approved".equals(item.status) || "rejected".equals(item.status))) continue;
            }

            if (!query.isEmpty()) {
                String taskId = item.taskId.toLowerCase(Locale.getDefault());
                String fileName = item.fileName.toLowerCase(Locale.getDefault());
                if (!taskId.contains(query) && !fileName.contains(query)) {
                    continue;
                }
            }

            filtered.add(item);
        }

        return filtered;
    }

    private String getSelectedStatusFilter() {
        int pos = spAuditStatusFilter.getSelectedItemPosition();
        switch (pos) {
            case 1:
                return "assigned";
            case 2:
                return "in_progress";
            case 3:
                return "submitted";
            case 4:
                return "approved";
            case 5:
                return "rejected";
            default:
                return "all";
        }
    }

    private String getSelectedReviewFilter() {
        int pos = spAuditReviewFilter.getSelectedItemPosition();
        switch (pos) {
            case 1:
                return "approved";
            case 2:
                return "rejected";
            case 3:
                return "pending";
            default:
                return "all";
        }
    }

    private void showTaskDetailDialog(AuditTaskItem item) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(18));
        scrollView.addView(root);

        root.addView(makeSectionTitle("Task #" + getShortTaskId(item.taskId) + " (" + item.kind.toUpperCase(Locale.getDefault()) + ")"));
        root.addView(makeBodyText("File: " + item.fileName));
        root.addView(makeBodyText("Annotator status: " + item.getStatusDisplayText()));
        root.addView(makeBodyText("Reviewer result: " + item.getReviewResultText()));
        root.addView(makeBodyText("Reviewed at: " + item.reviewedAt));

        root.addView(space());

        root.addView(makeSectionTitle("Preview"));
        root.addView(makePreviewBox(item));

        root.addView(space());

        root.addView(makeSectionTitle("Annotator Results"));
        root.addView(makeBodyText(item.annotatorResult));

        root.addView(space());

        root.addView(makeSectionTitle("Reviewer Results"));
        root.addView(makeBodyText("Overall comment: " + item.reviewerComment));
        root.addView(makeBodyText("Issue category: " + item.errorCategory));

        root.addView(space());

        root.addView(makeSectionTitle("Reviewer Votes"));
        if (item.reviewerVotes.isEmpty()) {
            root.addView(makeBodyText("Chưa có reviewer votes."));
        } else {
            for (ReviewerVote vote : item.reviewerVotes) {
                root.addView(makeBodyText(
                        vote.reviewerName + " • " + vote.status.toUpperCase(Locale.getDefault())
                                + "\n" + vote.comment
                                + "\n" + vote.reviewedAt
                ));
            }
        }

        root.addView(space());

        root.addView(makeSectionTitle("Object / Segment Notes"));
        if (item.reviewNotes.isEmpty()) {
            root.addView(makeBodyText("Không có review notes."));
        } else {
            for (ReviewNote note : item.reviewNotes) {
                root.addView(makeBodyText(
                        "Label: " + note.label +
                                "\nRange/BBox: " + note.rangeOrBox +
                                "\nComment: " + note.comment
                ));
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Review Detail")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }

    private View makePreviewBox(AuditTaskItem item) {
        TextView tv = new TextView(this);
        tv.setPadding(dp(14), dp(14), dp(14), dp(14));
        tv.setTextSize(14f);
        tv.setTextColor(0xFFCBD5E1);
        tv.setBackground(makeRoundedBackground(0xFF1E293B));

        String preview;
        switch (item.kind) {
            case "image":
                preview = "🖼️ Image preview placeholder\n\n" + item.previewContent;
                break;
            case "audio":
                preview = "🎵 Audio preview placeholder\n\n" + item.previewContent;
                break;
            case "text":
                preview = "📄 Text preview\n\n" + item.previewContent;
                break;
            default:
                preview = "📎 No preview available";
                break;
        }

        tv.setText(preview);
        return tv;
    }

    private TextView makeSectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFF8FAFC);
        tv.setTextSize(17f);
        tv.setPadding(0, 0, 0, dp(8));
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private TextView makeBodyText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF334155);
        tv.setTextSize(14f);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        tv.setBackground(makeRoundedBackground(0xFFF8FAFC));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        tv.setLayoutParams(params);
        return tv;
    }

    private View space() {
        View view = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(10)
        );
        view.setLayoutParams(params);
        return view;
    }

    private android.graphics.drawable.GradientDrawable makeRoundedBackground(int color) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(12));
        return drawable;
    }

    private int getStatusBgColor(String status) {
        switch (status) {
            case "approved":
                return 0x2622C55E;
            case "rejected":
                return 0x26EF4444;
            case "submitted":
                return 0x26F59E0B;
            case "in_progress":
                return 0x263B82F6;
            case "assigned":
            default:
                return 0x2664748B;
        }
    }

    private int getStatusTextColor(String status) {
        switch (status) {
            case "approved":
                return 0xFF22C55E;
            case "rejected":
                return 0xFFEF4444;
            case "submitted":
                return 0xFFF59E0B;
            case "in_progress":
                return 0xFF3B82F6;
            case "assigned":
            default:
                return 0xFF94A3B8;
        }
    }

    private int getReviewBgColor(String status) {
        if ("approved".equals(status)) return 0x2622C55E;
        if ("rejected".equals(status)) return 0x26EF4444;
        return 0x26F59E0B;
    }

    private int getReviewTextColor(String status) {
        if ("approved".equals(status)) return 0xFF22C55E;
        if ("rejected".equals(status)) return 0xFFEF4444;
        return 0xFFF59E0B;
    }

    private String getShortTaskId(String taskId) {
        if (taskId == null || taskId.length() <= 6) return taskId == null ? "-" : taskId;
        return taskId.substring(taskId.length() - 6);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private static class ReviewerVote {
        String reviewerName;
        String status;
        String comment;
        String reviewedAt;

        ReviewerVote(String reviewerName, String status, String comment, String reviewedAt) {
            this.reviewerName = reviewerName;
            this.status = status;
            this.comment = comment;
            this.reviewedAt = reviewedAt;
        }
    }

    private static class ReviewNote {
        String label;
        String rangeOrBox;
        String comment;

        ReviewNote(String label, String rangeOrBox, String comment) {
            this.label = label;
            this.rangeOrBox = rangeOrBox;
            this.comment = comment;
        }
    }

    private static class AuditTaskItem {
        String taskId;
        String fileName;
        String kind;
        String status;
        String annotatorResult;
        String previewContent;
        String reviewerComment;
        String errorCategory;
        String reviewedAt;
        List<ReviewerVote> reviewerVotes;
        List<ReviewNote> reviewNotes;

        AuditTaskItem(String taskId,
                      String fileName,
                      String kind,
                      String status,
                      String annotatorResult,
                      String previewContent,
                      String reviewerComment,
                      String errorCategory,
                      String reviewedAt,
                      List<ReviewerVote> reviewerVotes,
                      List<ReviewNote> reviewNotes) {
            this.taskId = taskId;
            this.fileName = fileName;
            this.kind = kind;
            this.status = status;
            this.annotatorResult = annotatorResult;
            this.previewContent = previewContent;
            this.reviewerComment = reviewerComment;
            this.errorCategory = errorCategory;
            this.reviewedAt = reviewedAt;
            this.reviewerVotes = reviewerVotes;
            this.reviewNotes = reviewNotes;
        }

        String getPreviewIcon() {
            switch (kind) {
                case "image":
                    return "🖼️";
                case "text":
                    return "📄";
                case "audio":
                    return "🎵";
                default:
                    return "📎";
            }
        }

        String getLabelTypeText() {
            switch (kind) {
                case "image":
                    return "BBox Annotation";
                case "text":
                    return "Text Span";
                case "audio":
                    return "Audio Label";
                default:
                    return "Other";
            }
        }

        String getStatusDisplayText() {
            switch (status) {
                case "in_progress":
                    return "IN PROGRESS";
                case "submitted":
                    return "SUBMITTED";
                case "approved":
                    return "APPROVED";
                case "rejected":
                    return "REJECTED";
                case "assigned":
                default:
                    return "ASSIGNED";
            }
        }

        String getReviewResultText() {
            if ("approved".equals(status)) return "Approved";
            if ("rejected".equals(status)) return "Rejected";
            return "Pending Review";
        }
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable onChange;

        SimpleItemSelectedListener(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onChange.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
