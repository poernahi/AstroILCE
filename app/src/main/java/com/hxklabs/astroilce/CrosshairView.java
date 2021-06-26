package com.hxklabs.astroilce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class CrosshairView extends View {
    private static final int CROSSHAIR_LENGTH_SMALL = 30;
    private static final int CROSSHAIR_LENGTH_LARGE = 200;
    private static final int CROSSHAIR_GAP_SMALL = 10;
    private static final int CROSSHAIR_GAP_LARGE = 120;
    private static final int CROSSHAIR_THICKNESS = 1;
    private static final int CROSSHAIR_COLOR = Color.argb(190, 255, 0, 0);
    private static final int BOX_THICKNESS = 1;
    private static final int BOX_COLOR = Color.argb(150, 180, 180, 180);
    private static final float BOX_SCALE = 11.70f;

    private final Paint crosshairPaint = new Paint();
    private final Paint boxPaint = new Paint();

    private boolean drawBox = true;

    public CrosshairView(Context context) {
        super(context);
        initPaint();
    }

    public CrosshairView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initPaint();
    }

    private void initPaint() {
        crosshairPaint.setAntiAlias(false);
        crosshairPaint.setColor(CROSSHAIR_COLOR);
        crosshairPaint.setStrokeWidth(CROSSHAIR_THICKNESS);
        crosshairPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        boxPaint.setAntiAlias(false);
        boxPaint.setColor(BOX_COLOR);
        boxPaint.setStrokeWidth(BOX_THICKNESS);
        boxPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        canvas.drawLine(centerX + CROSSHAIR_GAP_SMALL, centerY,
                centerX + CROSSHAIR_GAP_SMALL + CROSSHAIR_LENGTH_SMALL, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY - CROSSHAIR_GAP_SMALL,
                centerX, centerY - CROSSHAIR_GAP_SMALL - CROSSHAIR_LENGTH_SMALL, crosshairPaint);
        canvas.drawLine(centerX - CROSSHAIR_GAP_SMALL, centerY,
                centerX - CROSSHAIR_GAP_SMALL - CROSSHAIR_LENGTH_SMALL, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY + CROSSHAIR_GAP_SMALL,
                centerX, centerY + CROSSHAIR_GAP_SMALL + CROSSHAIR_LENGTH_SMALL, crosshairPaint);

        canvas.drawLine(centerX + CROSSHAIR_GAP_LARGE, centerY,
                centerX + CROSSHAIR_GAP_LARGE + CROSSHAIR_LENGTH_LARGE, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY - CROSSHAIR_GAP_LARGE,
                centerX, centerY - CROSSHAIR_GAP_LARGE - CROSSHAIR_LENGTH_LARGE, crosshairPaint);
        canvas.drawLine(centerX - CROSSHAIR_GAP_LARGE, centerY,
                centerX - CROSSHAIR_GAP_LARGE - CROSSHAIR_LENGTH_LARGE, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY + CROSSHAIR_GAP_LARGE,
                centerX, centerY + CROSSHAIR_GAP_LARGE + CROSSHAIR_LENGTH_LARGE, crosshairPaint);


        if (drawBox) {
            float boxWidth = getWidth() / BOX_SCALE;
            float boxHeight = getHeight() / BOX_SCALE;
            float upperY = centerY - boxHeight / 2f;
            float leftX = centerX - boxWidth / 2f;
            float lowerY = centerY + boxHeight / 2f;
            float rightX = centerX + boxWidth / 2f;

            canvas.drawLine(leftX, upperY, rightX, upperY, boxPaint);
            canvas.drawLine(leftX, upperY, leftX, lowerY, boxPaint);
            canvas.drawLine(rightX, lowerY, rightX, upperY, boxPaint);
            canvas.drawLine(rightX, lowerY, leftX, lowerY, boxPaint);
        }
    }

    public void showBox() {
        drawBox = true;
        postInvalidate();
    }

    public void hideBox() {
        drawBox = false;
        postInvalidate();
    }
}
