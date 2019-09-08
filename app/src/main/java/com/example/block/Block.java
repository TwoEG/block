package com.example.block;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class Block {

    public boolean highlight;

    private ArrayList<Point> pointList;
    private Bitmap blockBitmap;
    private Point moveVector;

    private Handler handler;

    public Block(ArrayList<Point> A, Bitmap B, Handler H) {
        pointList = A;
        blockBitmap = B;
        handler = H;
        moveVector = new Point(0, 0);
    }

    public void drawBlock(Canvas canvas, int left, int top, int blockSize){
        synchronized (pointList) {
            /* selected draw */
            if (highlight) {
                for (Point p : pointList) {
                    Rect cell = new Rect(left + p.x * blockSize - 5, top + p.y * blockSize - 5,
                            left + (p.x + 1) * blockSize + 5, top + (p.y + 1) * blockSize + 5);
                    canvas.drawBitmap(blockBitmap, null, cell, null);
                }
            /* vertical move */
            }
            else if(moveVector.x != 0) {
                for (Point p : pointList) {
                    Rect cell = new Rect(left + (p.x + moveVector.x) * blockSize, top + p.y * blockSize,
                            left + (p.x + 1 + moveVector.x) * blockSize, top + (p.y + 1) * blockSize);
                    canvas.drawBitmap(blockBitmap, null, cell, null);
                }
                if(moveVector.x > 0) {moveVector.x--;}
                else if(moveVector.x < 0) {moveVector.x++;}
                if(moveVector.x == 0) {handler.sendEmptyMessage(MainActivity.MSG_CLEAR);}
            /* horizontal move */
            }
            else if(moveVector.y != 0){
                for (Point p : pointList) {
                    Rect cell = new Rect(left + p.x * blockSize, top + (p.y + moveVector.y) * blockSize,
                            left + (p.x + 1) * blockSize, top + (p.y + 1 + moveVector.y) * blockSize);
                    canvas.drawBitmap(blockBitmap, null, cell, null);
                }
                if(moveVector.y > 0) {moveVector.y--;}
                else if(moveVector.y < 0) {moveVector.y++;}
                if(moveVector.y == 0) {handler.sendEmptyMessage(MainActivity.MSG_CLEAR);}
            /* normal draw */
            }
            else {
                for (Point p : pointList) {
                    Rect cell = new Rect(left + p.x * blockSize, top + p.y * blockSize,
                                        left + (p.x + 1) * blockSize, top + (p.y + 1) * blockSize);
                    canvas.drawBitmap(blockBitmap, null, cell, null);
                }
            }
        }
    }

    public void moveTop(Block[][] blockMap){
        deleteMap(blockMap);
        int delta = 1;
        while(true){
            for(Point p : pointList){
                if(p.y + delta * -1 < 0 ||
                        blockMap[p.x][p.y + delta * -1] != null){
                    Point d = new Point(0, 1 - delta);
                    changePoints(d);
                    addMap(blockMap);
                    moveVector.y = delta - 1;
                    if(delta - 1 > 0){
                        handler.sendEmptyMessage(MainActivity.MSG_MOVING);
                    }
                    return;
                };
            }
            delta++;
        }
    }

    public void moveBottom(Block[][] blockMap){
        deleteMap(blockMap);
        int delta = 1;
        while(true){
            for(Point p : pointList){
                if(p.y + delta * 1 >= BlockView.mapNum ||
                        blockMap[p.x][p.y + delta * 1] != null){
                    Point d = new Point(0, delta - 1);
                    changePoints(d);
                    addMap(blockMap);
                    moveVector.y = 1 - delta;
                    if(delta - 1 > 0){
                        handler.sendEmptyMessage(MainActivity.MSG_MOVING);
                    }
                    return;
                };
            }
            delta++;
        }
    }

    public void moveLeft(Block[][] blockMap){
        deleteMap(blockMap);
        int delta = 1;
        while(true){
            for(Point p : pointList){
                if(p.x + delta * -1 < 0 ||
                    blockMap[p.x + delta * -1][p.y] != null){
                    Point d = new Point(1 - delta, 0);
                    changePoints(d);
                    addMap(blockMap);
                    moveVector.x = delta - 1;
                    if(delta - 1 > 0){
                        handler.sendEmptyMessage(MainActivity.MSG_MOVING);
                    }
                    return;
                };
            }
            delta++;
        }
    }

    public void moveRight(Block[][] blockMap){
        deleteMap(blockMap);
        int delta = 1;
        while(true){
            for(Point p : pointList){
                if(p.x + delta * 1 >= BlockView.mapNum ||
                        blockMap[p.x + delta * 1][p.y] != null){
                    Point d = new Point(delta - 1, 0);
                    changePoints(d);
                    addMap(blockMap);
                    moveVector.x = 1 - delta;
                    if(delta - 1 > 0){
                        handler.sendEmptyMessage(MainActivity.MSG_MOVING);
                    }
                    return;
                };
            }
            delta++;
        }
    }

    private void changePoints(Point d){
        synchronized (pointList) {
            for (Point p : pointList) {
                p.x = p.x + d.x;
                p.y = p.y + d.y;
            }
        }
    }

    public void addMap(Block[][] blockMap){
        for(Point p : pointList){
            blockMap[p.x][p.y] = this;
        }
    }

    public void deleteMap(Block[][] blockMap){
        for(Point p : pointList){
            blockMap[p.x][p.y] = null;
        }
    }

    public void deleteV(int x, Block[][] blockMap){
        synchronized (pointList) {
            Iterator<Point> i = pointList.iterator();
            while (i.hasNext()) {
                Point p = i.next();
                if (p.x == x) {
                    i.remove();
                }
            }
        }
        if(isEmpty()) return;
        /* divide */
        ArrayList<Point> oldList = new ArrayList<>();
        ArrayList<Point> newList = new ArrayList<>(pointList);
        newList.remove(pointList.get(0));
        oldList.add(pointList.get(0));
        boolean flag = true;
        while(flag){
            for(int index = 0; index < oldList.size(); index++){
                Point rp = oldList.get(index);
                flag = false;
                Iterator<Point> i = newList.iterator();
                while(i.hasNext()){
                    Point np = i.next();
                    if((rp.x + 1 == np.x) && (rp.y == np.y) ||
                       (rp.x - 1 == np.x) && (rp.y == np.y) ||
                       (rp.y + 1 == np.y) && (rp.x == np.x) ||
                       (rp.y - 1 == np.y) && (rp.x == np.x)){
                        oldList.add(np);
                        i.remove();
                        flag = true;
                    }
                }
            }
        }
        synchronized (BlockView.blockList){
            synchronized (pointList){
                pointList.removeAll(newList);
            }
            Block b = new Block(newList, blockBitmap, handler);
            b.addMap(blockMap);
            BlockView.blockList.add(b);
        }
    }

    public void deleteH(int y, Block[][] blockMap){
        synchronized (pointList) {
            Iterator<Point> i = pointList.iterator();
            while (i.hasNext()) {
                Point p = i.next();
                if (p.y == y) {
                    i.remove();
                }
            }
        }
        if(isEmpty()) return;
        /* divide */
        ArrayList<Point> oldList = new ArrayList<>();
        ArrayList<Point> newList = new ArrayList<>(pointList);
        newList.remove(pointList.get(0));
        oldList.add(pointList.get(0));
        boolean flag = true;
        while(flag){
            for(int index = 0; index < oldList.size(); index++){
                Point rp = oldList.get(index);
                flag = false;
                Iterator<Point> i = newList.iterator();
                while(i.hasNext()){
                    Point np = i.next();
                    if((rp.x + 1 == np.x) && (rp.y == np.y) ||
                            (rp.x - 1 == np.x) && (rp.y == np.y) ||
                            (rp.y + 1 == np.y) && (rp.x == np.x) ||
                            (rp.y - 1 == np.y) && (rp.x == np.x)){
                        oldList.add(np);
                        i.remove();
                        flag = true;
                    }
                }
            }
        }
        synchronized (BlockView.blockList){
            synchronized (pointList){
                pointList.removeAll(newList);
            }
            Block b = new Block(newList, blockBitmap, handler);
            b.addMap(blockMap);
            BlockView.blockList.add(b);
        }
    }

    public boolean isEmpty(){
        return (pointList.size() == 0);
    }

    public void attachHandler(Handler h){
        handler = h;
    }
}
