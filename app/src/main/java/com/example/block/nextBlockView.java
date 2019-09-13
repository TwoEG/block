package com.example.block;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class nextBlockView extends View {

    private int width;
    private int height;
    private int blockSize;
    private int Left;
    private int Top;
    private ArrayList<Point> posList;
    private Bitmap bit;

    public nextBlockView(Context context) {
        this(context, null);
    }
    public nextBlockView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    public nextBlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if(width > height){
            blockSize = height / 5;
            Left = (width - height) / 2;
            Top = 0;
        }
        else{
            blockSize = width / 5;
            Top = (height - width) / 2;
            Left = 0;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(posList == null || bit == null){return;}
        int maxX = 0;
        int maxY = 0;
        for(Point p : posList){
            if(p.x > maxX){maxX = p.x;}
            if(p.y > maxY){maxY = p.y;}
        }
        float biasX = (float) ((4 - maxX) / 2.0);
        float biasY = (float) ((4 - maxY) / 2.0);
        int startLeft = Left + (int) (blockSize * biasX);
        int startTop = Top + (int) (blockSize * biasY);
        for(Point p : posList){
            Rect cell = new Rect(startLeft + p.x * blockSize, startTop + p.y * blockSize,
                    startLeft + (p.x + 1) * blockSize, startTop + (p.y + 1) * blockSize);
            canvas.drawBitmap(bit, null, cell, null);
        }
    }

    public void setChar(int posIndex, int colorIndex){
        posList = BlockPos.L.get(posIndex);
        bit = BlockView.bitmapList[colorIndex];
        invalidate();
    }
}
