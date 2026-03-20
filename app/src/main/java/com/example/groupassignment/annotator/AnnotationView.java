package com.example.groupassignment.annotator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnnotationView extends AppCompatImageView {

    public static class BoundingBox {
        public RectF rect;
        public String label;

        public BoundingBox(RectF rect, String label) {
            this.rect = rect;
            this.label = label;
        }
    }

    private List<BoundingBox> boxes = new ArrayList<>();
    private RectF currentRect = null;
    private Paint boxPaint;
    private Paint textPaint;
    private Paint currentBoxPaint;
    private String selectedLabel = "Default";
    private boolean showLabels = true;

    public AnnotationView(Context context) {
        super(context);
        init();
    }

    public AnnotationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(5f, 0, 0, Color.BLACK);

        currentBoxPaint = new Paint();
        currentBoxPaint.setColor(Color.YELLOW);
        currentBoxPaint.setStyle(Paint.Style.STROKE);
        currentBoxPaint.setStrokeWidth(3f);
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        invalidate();
    }

    public void setLabels(List<String> labels) {
        if (labels != null && !labels.isEmpty()) {
            this.selectedLabel = labels.get(0);
        }
    }

    public void setSelectedLabel(String label) {
        this.selectedLabel = label;
    }

    public List<BoundingBox> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<BoundingBox> boxes) {
        this.boxes = boxes != null ? boxes : new ArrayList<>();
        invalidate();
    }

    public String getBoxesAsJson() {
        JSONArray jsonArray = new JSONArray();
        try {
            for (BoundingBox box : boxes) {
                JSONObject obj = new JSONObject();
                obj.put("label", box.label);
                obj.put("x1", box.rect.left);
                obj.put("y1", box.rect.top);
                obj.put("x2", box.rect.right);
                obj.put("y2", box.rect.bottom);
                jsonArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    public void setBoxesFromJson(String json) {
        boxes.clear();
        if (json == null || json.isEmpty() || !json.startsWith("[")) {
            invalidate();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                RectF rect = new RectF(
                        (float) obj.getDouble("x1"),
                        (float) obj.getDouble("y1"),
                        (float) obj.getDouble("x2"),
                        (float) obj.getDouble("y2")
                );
                boxes.add(new BoundingBox(rect, obj.getString("label")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (BoundingBox box : boxes) {
            canvas.drawRect(box.rect, boxPaint);
            if (showLabels) {
                canvas.drawText(box.label, box.rect.left, box.rect.top - 10, textPaint);
            }
        }

        if (currentRect != null) {
            canvas.drawRect(currentRect, currentBoxPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentRect = new RectF(x, y, x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentRect != null) {
                    currentRect.right = x;
                    currentRect.bottom = y;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (currentRect != null) {
                    currentRect.right = x;
                    currentRect.bottom = y;
                    // Ensure the rect is positive
                    RectF finalRect = new RectF(
                            Math.min(currentRect.left, currentRect.right),
                            Math.min(currentRect.top, currentRect.bottom),
                            Math.max(currentRect.left, currentRect.right),
                            Math.max(currentRect.top, currentRect.bottom)
                    );
                    if (finalRect.width() > 10 && finalRect.height() > 10) {
                        boxes.add(new BoundingBox(finalRect, selectedLabel));
                    }
                    currentRect = null;
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void clearLast() {
        if (!boxes.isEmpty()) {
            boxes.remove(boxes.size() - 1);
            invalidate();
        }
    }

    public void clearAll() {
        boxes.clear();
        invalidate();
    }
}
