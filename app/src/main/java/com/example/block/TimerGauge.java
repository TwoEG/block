package com.example.block;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TimerGauge extends View {

    private int width;
    private int height;
    private float ratio;
    private Paint paint;

    public TimerGauge(Context context) {
        this(context, null);
    }
    public TimerGauge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public TimerGauge(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        ratio = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setARGB(255, (int) (255 * (1 - ratio)), (int) (255 * ratio), 0);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawRect(0, 0 , width * ratio, height, paint);
        super.onDraw(canvas);
    }

    public void setAndDraw(int percent){
        this.ratio = (float) percent / 1000;
        invalidate();
    }
}
