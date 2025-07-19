package com.example.palayan.Helper.AppHelper;

import static android.view.View.LAYER_TYPE_HARDWARE;

import static androidx.core.view.ViewCompat.setLayerType;

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
    private RectF rectF;

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

        rectF = new RectF();
        setLayerType(LAYER_TYPE_HARDWARE, null); // Required for clear effect
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw full screen background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Transparent cutout rectangle
        float rectWidth = 800f;
        float rectHeight = 500f;

        float left = (getWidth() - rectWidth) / 2;
        float top = (getHeight() - rectHeight) / 2;
        float right = left + rectWidth;
        float bottom = top + rectHeight;

        rectF.set(left, top, right, bottom);
        canvas.drawRoundRect(rectF, 30f, 30f, transparentPaint);
    }
}