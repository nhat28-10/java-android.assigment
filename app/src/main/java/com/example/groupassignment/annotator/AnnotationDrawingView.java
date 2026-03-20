package com.example.groupassignment.annotator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnnotationDrawingView extends AppCompatImageView {

    public static class Box {
        public RectF rect;
        public String label;
        public int color;

        public Box(RectF rect, String label, int color) {
            this.rect = rect;
            this.label = label;
            this.color = color;
        }
    }

    private final List<Box> boxes = new ArrayList<>();
    private Box currentBox = null;
    private String selectedLabel = "Default";
    private int selectedColor = Color.GREEN;
    
    private Paint paintBox;
    private Paint paintText;
    private Paint paintTextBackground;
    private Paint paintCurrent;
    private Paint paintFill;

    public AnnotationDrawingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Thiết lập màu nền tối cho toàn bộ View
        setBackgroundColor(Color.parseColor("#121212"));

        paintBox = new Paint();
        paintBox.setStyle(Paint.Style.STROKE);
        paintBox.setStrokeWidth(6f);
        paintBox.setAntiAlias(true);
        paintBox.setShadowLayer(4, 0, 0, Color.BLACK); // Đổ bóng cho đường viền

        paintFill = new Paint();
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setAlpha(40); // Độ trong suốt của ruột box

        paintText = new Paint();
        paintText.setTextSize(36f);
        paintText.setAntiAlias(true);
        paintText.setColor(Color.WHITE);
        paintText.setFakeBoldText(true);

        paintTextBackground = new Paint();
        paintTextBackground.setStyle(Paint.Style.FILL);
        paintTextBackground.setColor(Color.parseColor("#CC000000")); // Nền đen cho text nhãn

        paintCurrent = new Paint();
        paintCurrent.setColor(Color.CYAN);
        paintCurrent.setStyle(Paint.Style.STROKE);
        paintCurrent.setStrokeWidth(4f);
        paintCurrent.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));
    }

    public void setSelectedLabel(String label) {
        this.selectedLabel = label;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                currentBox = new Box(new RectF(x, y, x, y), selectedLabel, selectedColor);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentBox != null) {
                    currentBox.rect.right = x;
                    currentBox.rect.bottom = y;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (currentBox != null) {
                    RectF r = currentBox.rect;
                    RectF normalized = new RectF(
                            Math.min(r.left, r.right),
                            Math.min(r.top, r.bottom),
                            Math.max(r.left, r.right),
                            Math.max(r.top, r.bottom)
                    );
                    if (normalized.width() > 20 && normalized.height() > 20) {
                        boxes.add(new Box(normalized, selectedLabel, selectedColor));
                    }
                    currentBox = null;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                currentBox = null;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Box box : boxes) {
            paintFill.setColor(box.color);
            paintFill.setAlpha(30);
            canvas.drawRect(box.rect, paintFill);

            paintBox.setColor(box.color);
            canvas.drawRect(box.rect, paintBox);
            
            float textWidth = paintText.measureText(box.label);
            canvas.drawRect(box.rect.left, box.rect.top - 45, box.rect.left + textWidth + 10, box.rect.top, paintTextBackground);
            canvas.drawText(box.label, box.rect.left + 5, box.rect.top - 10, paintText);
        }

        if (currentBox != null) {
            canvas.drawRect(currentBox.rect, paintCurrent);
        }
    }

    public void undo() {
        if (!boxes.isEmpty()) {
            boxes.remove(boxes.size() - 1);
            invalidate();
        }
    }

    public void clear() {
        boxes.clear();
        invalidate();
    }

    public String getBoxesAsJson() {
        JSONArray array = new JSONArray();
        try {
            for (Box box : boxes) {
                JSONObject obj = new JSONObject();
                obj.put("label", box.label);
                obj.put("x", box.rect.left);
                obj.put("y", box.rect.top);
                obj.put("w", box.rect.width());
                obj.put("h", box.rect.height());
                obj.put("color", String.format("#%06X", (0xFFFFFF & box.color)));
                array.put(obj);
            }
        } catch (JSONException ignored) {}
        return array.toString();
    }

    public void loadBoxesFromJson(String json) {
        boxes.clear();
        if (TextUtils.isEmpty(json) || !json.startsWith("[")) return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                float x = (float) obj.optDouble("x", 0);
                float y = (float) obj.optDouble("y", 0);
                float w = (float) obj.optDouble("w", 0);
                float h = (float) obj.optDouble("h", 0);
                String label = obj.optString("label", "Unknown");
                String colorStr = obj.optString("color", "#00FF00");
                boxes.add(new Box(new RectF(x, y, x + w, y + h), label, Color.parseColor(colorStr)));
            }
        } catch (JSONException ignored) {}
        invalidate();
    }
}
