package com.example.block;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Iterator;

public class DrawingThread extends Thread{

    private SurfaceHolder holder;
    private boolean stop = false;
    private Bitmap block;

    public DrawingThread(BlockView bview) {
        holder = bview.getHolder();
    }

    public void stopThread() {
        stop = true;
    }

    @Override
    public void run() {
        while(!stop) {
            Canvas canvas = holder.lockCanvas();
            int startLeft = BlockView.startLeft;
            int startTop = BlockView.startTop;
            int blockSize = BlockView.blockSize;
            int mapPixel = BlockView.mapPixel;
            if(canvas != null) {
                canvas.drawColor(Color.parseColor("#474747"));
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#202020"));
                RectF r = new RectF(startLeft, startTop, startLeft + mapPixel, startTop + mapPixel);
                canvas.drawRoundRect(r, 10, 10, paint);
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
