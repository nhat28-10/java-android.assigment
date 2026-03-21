package com.example.groupassignment.annotator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;

import android.graphics.Matrix;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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

    private static final float MIN_BOX_SIZE = 20f;

    private final List<Box> boxes = new ArrayList<>();
    private Box currentBox = null;
    private String selectedLabel = "Default";
    private int selectedColor = Color.GREEN;
    private boolean readOnly = false;

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
        setBackgroundColor(Color.parseColor("#121212"));
        setScaleType(ScaleType.FIT_CENTER);

        paintBox = new Paint();
        paintBox.setStyle(Paint.Style.STROKE);
        paintBox.setStrokeWidth(6f);
        paintBox.setAntiAlias(true);
        paintBox.setShadowLayer(4, 0, 0, Color.BLACK);

        paintFill = new Paint();
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setAlpha(40);

        paintText = new Paint();
        paintText.setTextSize(36f);
        paintText.setAntiAlias(true);
        paintText.setColor(Color.WHITE);
        paintText.setFakeBoldText(true);

        paintTextBackground = new Paint();
        paintTextBackground.setStyle(Paint.Style.FILL);
        paintTextBackground.setColor(Color.parseColor("#CC000000"));

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

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            currentBox = null;
        }
        invalidate();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || readOnly || getDrawable() == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                PointF startPoint = mapPointToImage(event.getX(), event.getY(), false);
 codex/fix-annotation-overlay-for-reviewer
                if (startPoint == null) {

                if (startPoint == null || !isPointInsideImage(startPoint)) {
processing
                    return false;
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                currentBox = new Box(new RectF(startPoint.x, startPoint.y, startPoint.x, startPoint.y), selectedLabel, selectedColor);
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                PointF mappedPoint = mapPointToImage(event.getX(), event.getY(), true);
                if (currentBox != null && mappedPoint != null) {
                    currentBox.rect.right = mappedPoint.x;
                    currentBox.rect.bottom = mappedPoint.y;
                    invalidate();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                PointF mappedPoint = mapPointToImage(event.getX(), event.getY(), true);
                if (currentBox != null && mappedPoint != null) {
                    currentBox.rect.right = mappedPoint.x;
                    currentBox.rect.bottom = mappedPoint.y;
                    RectF normalized = normalizeRect(currentBox.rect);
                    if (normalized.width() >= MIN_BOX_SIZE && normalized.height() >= MIN_BOX_SIZE) {
                        boxes.add(new Box(normalized, currentBox.label, currentBox.color));
                    }
                    currentBox = null;
                    invalidate();
                    return true;
                }
                currentBox = null;
                invalidate();
                return false;
            }
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
            drawBox(canvas, box, false);
        }

        if (currentBox != null) {
            drawBox(canvas, currentBox, true);
        }
    }

    public void undo() {
        if (readOnly) {
            return;
        }
        if (!boxes.isEmpty()) {
            boxes.remove(boxes.size() - 1);
            invalidate();
        }
    }

    public void clear() {
        if (readOnly) {
            return;
        }
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
        } catch (JSONException ignored) {
        }
        return array.toString();
    }

    public void loadBoxesFromJson(String json) {
        boxes.clear();
        currentBox = null;
        if (TextUtils.isEmpty(json) || !json.trim().startsWith("[")) {
            invalidate();
            return;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                float x = (float) obj.optDouble("x", 0);
                float y = (float) obj.optDouble("y", 0);
                float w = (float) obj.optDouble("w", 0);
                float h = (float) obj.optDouble("h", 0);
                if (w <= 0 || h <= 0) {
                    continue;
                }
                String label = obj.optString("label", "Unknown");
                String colorStr = obj.optString("color", "#00FF00");
                int color = parseBoxColor(colorStr);
                boxes.add(new Box(new RectF(x, y, x + w, y + h), label, color));
            }
        } catch (JSONException ignored) {
            boxes.clear();
        }
        invalidate();
    }

    private void drawBox(Canvas canvas, Box box, boolean isCurrentBox) {
        RectF viewRect = mapRectToView(normalizeRect(box.rect));
        if (viewRect == null) {
            return;
        }

        if (isCurrentBox) {
            canvas.drawRect(viewRect, paintCurrent);
            return;
        }

        paintFill.setColor(box.color);
        paintFill.setAlpha(30);
        canvas.drawRect(viewRect, paintFill);

        paintBox.setColor(box.color);
        canvas.drawRect(viewRect, paintBox);

        String label = TextUtils.isEmpty(box.label) ? "Unknown" : box.label;
        float textWidth = paintText.measureText(label);
        float backgroundTop = Math.max(0f, viewRect.top - 45f);
        float backgroundBottom = backgroundTop + 45f;
        canvas.drawRect(viewRect.left, backgroundTop, viewRect.left + textWidth + 10f, backgroundBottom, paintTextBackground);
        canvas.drawText(label, viewRect.left + 5f, backgroundBottom - 10f, paintText);
    }

    @Nullable
    private RectF mapRectToView(RectF imageRect) {
codex/fix-annotation-overlay-for-reviewer
        Drawable drawable = getDrawable();
        RectF imageDisplayRect = getImageDisplayRect();
        if (drawable == null || imageDisplayRect == null) {
            return null;
        }

        float imageWidth = drawable.getIntrinsicWidth();
        float imageHeight = drawable.getIntrinsicHeight();
        if (imageWidth <= 0f || imageHeight <= 0f) {
            return null;
        }

        return new RectF(
                imageDisplayRect.left + (imageRect.left / imageWidth) * imageDisplayRect.width(),
                imageDisplayRect.top + (imageRect.top / imageHeight) * imageDisplayRect.height(),
                imageDisplayRect.left + (imageRect.right / imageWidth) * imageDisplayRect.width(),
                imageDisplayRect.top + (imageRect.bottom / imageHeight) * imageDisplayRect.height()
        );

        Matrix matrix = getImageMatrix();
        Drawable drawable = getDrawable();
        if (matrix == null || drawable == null) {
            return null;
        }
        RectF viewRect = new RectF(imageRect);
        matrix.mapRect(viewRect);
        return viewRect;
    }

    @Nullable
    private PointF mapPointToImage(float viewX, float viewY, boolean clampToBounds) {
        Drawable drawable = getDrawable();
 codex/fix-annotation-overlay-for-reviewer
        RectF imageDisplayRect = getImageDisplayRect();
        if (drawable == null || imageDisplayRect == null || imageDisplayRect.width() <= 0f || imageDisplayRect.height() <= 0f) {
            return null;
        }

        if (!clampToBounds && !imageDisplayRect.contains(viewX, viewY)) {
            return null;
        }

        float clampedX = clamp(viewX, imageDisplayRect.left, imageDisplayRect.right);
        float clampedY = clamp(viewY, imageDisplayRect.top, imageDisplayRect.bottom);

        float imageX = ((clampedX - imageDisplayRect.left) / imageDisplayRect.width()) * drawable.getIntrinsicWidth();
        float imageY = ((clampedY - imageDisplayRect.top) / imageDisplayRect.height()) * drawable.getIntrinsicHeight();
        return new PointF(imageX, imageY);
    }

    @Nullable
    private RectF getImageDisplayRect() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }

        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (drawableWidth <= 0f || drawableHeight <= 0f || availableWidth <= 0f || availableHeight <= 0f) {
            return null;
        }

        float scale = Math.min(availableWidth / drawableWidth, availableHeight / drawableHeight);
        float displayedWidth = drawableWidth * scale;
        float displayedHeight = drawableHeight * scale;
        float left = getPaddingLeft() + ((availableWidth - displayedWidth) / 2f);
        float top = getPaddingTop() + ((availableHeight - displayedHeight) / 2f);
        return new RectF(left, top, left + displayedWidth, top + displayedHeight);

        Matrix imageMatrix = getImageMatrix();
        if (drawable == null || imageMatrix == null) {
            return null;
        }
        Matrix inverse = new Matrix();
        if (!imageMatrix.invert(inverse)) {
            return null;
        }
        float[] pts = new float[]{viewX, viewY};
        inverse.mapPoints(pts);
        if (clampToBounds) {
            pts[0] = clamp(pts[0], 0f, drawable.getIntrinsicWidth());
            pts[1] = clamp(pts[1], 0f, drawable.getIntrinsicHeight());
        }
        return new PointF(pts[0], pts[1]);
    }

    private boolean isPointInsideImage(PointF point) {
        Drawable drawable = getDrawable();
        return drawable != null
                && point.x >= 0f && point.x <= drawable.getIntrinsicWidth()
                && point.y >= 0f && point.y <= drawable.getIntrinsicHeight();

    }

    private RectF normalizeRect(RectF rect) {
        return new RectF(
                Math.min(rect.left, rect.right),
                Math.min(rect.top, rect.bottom),
                Math.max(rect.left, rect.right),
                Math.max(rect.top, rect.bottom)
        );
    }

    private int parseBoxColor(String colorStr) {
        try {
            return Color.parseColor(colorStr);
        } catch (IllegalArgumentException ignored) {
            return Color.GREEN;
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
