package com.example.palayan.Helper.AppHelper;

import static android.view.View.LAYER_TYPE_HARDWARE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlayView extends View {

    private Paint backgroundPaint;
    private Paint transparentPaint;
    private Paint borderPaint;
    private Paint linePaint;
    private RectF rectF;
    private float linePosition = 0;
    private boolean movingDown = true;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#B3000000")); // translucent black

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5f);

        rectF = new RectF();
        setLayerType(LAYER_TYPE_HARDWARE, null); // Required for clear effect
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw full screen background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Transparent square cutout
        float size = Math.min(getWidth(), getHeight()) * 0.8f; // 80% of smaller dimension

        float left = (getWidth() - size) / 2;
        float top = (getHeight() - size) / 2;
        float right = left + size;
        float bottom = top + size;

        rectF.set(left, top, right, bottom);

        // Clear the square area
        canvas.drawRoundRect(rectF, 30f, 30f, transparentPaint);

        // Draw green border around square
        canvas.drawRoundRect(rectF, 30f, 30f, borderPaint);

        // Animate scan line
        if (movingDown) {
            linePosition += 8;
            if (linePosition >= size) {
                movingDown = false;
            }
        } else {
            linePosition -= 8;
            if (linePosition <= 0) {
                movingDown = true;
            }
        }

        float y = top + linePosition;
        canvas.drawLine(left + 20, y, right - 20, y, linePaint);

        // Redraw every frame
        postInvalidateOnAnimation();
    }
}
