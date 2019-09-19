package com.twoeg.block;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Iterator;

public class DrawingThread extends Thread{

    private SurfaceHolder holder;
    private boolean stop = false;
    private Paint paint = new Paint();

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
            int mapNum = BlockView.mapNum;

            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                /* background */
                canvas.drawColor(Color.parseColor("#BBBBBB"));
                paint.setColor(Color.parseColor("#202020"));
                RectF r = new RectF(startLeft, startTop, startLeft + mapPixel, startTop + mapPixel);
                canvas.drawRoundRect(r, 10, 10, paint);
                paint.setColor(Color.parseColor("#303030"));
                for (int x = 1; x < mapNum; x++){
                    canvas.drawLine(blockSize * x + 4, 10, blockSize * x + 4, mapPixel - 10, paint);
                    canvas.drawLine(blockSize * x + 5, 10, blockSize * x + 5, mapPixel - 10, paint);
                }
                for (int y = 1; y < mapNum; y++){
                    canvas.drawLine(10, blockSize * y + 4, mapPixel - 10, blockSize * y + 4, paint);
                    canvas.drawLine(10, blockSize * y + 5, mapPixel - 10, blockSize * y + 5, paint);
                }
                /* next block draw */
                ArrayList<Point> nextBlock = BlockView.nextBlock;
                Point nextPos = BlockView.nextPos;
                if (nextBlock != null && nextPos != null) {
                    int nx = nextPos.x;
                    int ny = nextPos.y;
                    Paint alphaPaint = new Paint();
                    alphaPaint.setAlpha(60);
                    for (Point p : nextBlock) {
                        Rect cell = new Rect(startLeft + (p.x + nx) * blockSize, startTop + (p.y + ny) * blockSize,
                                startLeft + (p.x + nx + 1) * blockSize, startTop + (p.y + ny + 1) * blockSize);
                        canvas.drawBitmap(BlockView.nextBit, null, cell, alphaPaint);
                    }
                }
                /* current block draw */
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
                /* clear draw */
                BlockView.clearedLineDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
