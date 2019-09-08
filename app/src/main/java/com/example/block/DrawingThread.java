package com.example.block;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Iterator;

public class DrawingThread extends Thread{

    private SurfaceHolder holder;
    private boolean stop = false;

    public DrawingThread(BlockView bview) {
        holder = bview.getHolder();
    }

    public void stopThread() {
        stop = true;
    }

    @Override
    public void run() {
        while(!stop) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            int startLeft = BlockView.startLeft;
            int startTop = BlockView.startTop;
            int blockSize = BlockView.blockSize;
            int mapPixel = BlockView.mapPixel;
            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                canvas.drawColor(Color.parseColor("#BBBBBB"));
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#202020"));
                RectF r = new RectF(startLeft, startTop, startLeft + mapPixel, startTop + mapPixel);
                canvas.drawRoundRect(r, 10, 10, paint);
                ArrayList<Point> nextBlock = BlockView.nextBlock;
                Point nextPos = BlockView.nextPos;
                if (nextBlock != null && nextPos != null) {
                    int nx = BlockView.nextPos.x;
                    int ny = BlockView.nextPos.y;
                    Paint alphaPaint = new Paint();
                    alphaPaint.setAlpha(50);
                    for (Point p : BlockView.nextBlock) {
                        Rect cell = new Rect(startLeft + (p.x + nx) * blockSize, startTop + (p.y + ny) * blockSize,
                                startLeft + (p.x + nx + 1) * blockSize, startTop + (p.y + ny + 1) * blockSize);
                        canvas.drawBitmap(BlockView.nextBit, null, cell, alphaPaint);
                    }
                }
                synchronized (BlockView.blockList) {
                    Iterator<Block> i = BlockView.blockList.iterator();
                    while(i.hasNext()){
                        Block b = i.next();
                        if(b.isEmpty()){
                            i.remove();
                            continue;
                        }
                        else{
                            b.drawBlock(canvas, startLeft + 5, startTop + 5, blockSize);
                        }
                    }
                }
                BlockView.clearedLineDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
