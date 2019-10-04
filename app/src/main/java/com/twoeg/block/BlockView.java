package com.twoeg.block;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

public class BlockView extends SurfaceView
        implements SurfaceHolder.Callback {

    public static float surfaceWidth;       // screen width
    public static float surfaceHeight;      // screen height

    public static final int mapNum = 8;     // map size
    public static int blockSize;            // block pixel
    public static int startLeft;            // left pixel
    public static int startTop;             // right pixel
    public static int mapPixel;             // map pixel size
    public DrawingThread thread;
    public static int score = 0;

    private static Handler handler = null;

    public static ArrayList<Block> blockList = new ArrayList<>();
    private static Block[][] blockMap = new Block[mapNum][mapNum];
    private static Point selected = new Point(0,0);

    public static ArrayList<Point> nextBlock;
    public static Point nextPos;
    public static Bitmap nextBit;

    public enum state{
        playing, animating, stop
    }
    public static state gamestate;

    private static final int ColorSize = 7;
    private static final int PosSize = 7;
    public static Bitmap[] bitmapList = new Bitmap[ColorSize];
    private static Bitmap grey;

    public static int[] clearXdraw = new int[mapNum];
    public static int[] clearYdraw = new int[mapNum];

    private int getX(float x){
        return ((int) x - startLeft) / blockSize;
    }
    private int getY(float y){
        return ((int) y - startTop) / blockSize;
    }

    public BlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        bitmapList[0] = BitmapFactory.decodeResource(getResources(), R.drawable.block_r);
        bitmapList[1] = BitmapFactory.decodeResource(getResources(), R.drawable.block_y);
        bitmapList[2] = BitmapFactory.decodeResource(getResources(), R.drawable.block_o);
        bitmapList[3] = BitmapFactory.decodeResource(getResources(), R.drawable.block_g);
        bitmapList[4] = BitmapFactory.decodeResource(getResources(), R.drawable.block_s);
        bitmapList[5] = BitmapFactory.decodeResource(getResources(), R.drawable.block_b);
        bitmapList[6] = BitmapFactory.decodeResource(getResources(), R.drawable.block_v);
        grey = BitmapFactory.decodeResource(getResources(), R.drawable.block_grey);

        setOnTouchListener(new OnSwipeTouchListener(context){
            @Override
            public void onSwipe(float x, float y, int tag) {
                if(gamestate != state.playing){return;}
                int px = getX(x);
                int py = getY(y);
                if(px < 0 || px >= mapNum || py < 0 || py >= mapNum){return;}
                Block b = blockMap[px][py];
                if(b == null){return;}
                    switch (tag) {
                        case 1:
                            b.moveTop(blockMap);
                            break;
                        case 2:
                            b.moveBottom(blockMap);
                            break;
                        case 4:
                            b.moveLeft(blockMap);
                            break;
                        case 8:
                            b.moveRight(blockMap);
                            break;
                    }
            }

            @Override
            public void Up(){
                Block b = blockMap[selected.x][selected.y];
                if(b != null){
                    b.highlight = false;
                }
            };

            @Override
            public void Down(float x, float y) {
                int px = getX(x);
                int py = getY(y);
                if(px < 0 || px >= mapNum || py < 0 || py >= mapNum){return;}
                selected.x = px;
                selected.y = py;
                Block b = blockMap[px][py];
                if(b != null){
                    b.highlight = true;
                }
            }
        });
    }

    public void startThread(){
        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new DrawingThread(this);
            thread.start();
        }
    }

    public void stopThread(){
        if(thread != null) {
            thread.stopThread();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        if(width > height){
            mapPixel = height;
            blockSize = (height - 10) / mapNum;
            startLeft = (width - height) / 2;
            startTop = 0;
        }
        else{
            mapPixel = width;
            blockSize = (width - 10) / mapNum;
            startLeft = 0;
            startTop = (height - width) / 2;
        }
        startThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    /* set characteristic of next block */
    public void getNextBlock(){
        Random gen = new Random();
        int colorIndex = getColorIndex();
        int posIndex = getPosIndex();
        nextBlock = BlockPos.L.get(posIndex);
        nextBit = bitmapList[colorIndex];

        Message m = new Message();
        m.what = MainActivity.MSG_NEXTBLOCK;
        m.arg1 = posIndex;
        m.arg2 = colorIndex;
        handler.sendMessage(m);
        nextPos = null;
    }

    /* makes next block on map */
    public boolean makeNextBlock(){
        if(!nextCheck()){
            return false;
        }
        else{
            ArrayList<Point> next = new ArrayList<>();
            for(Point p : nextBlock){
                next.add(new Point(p.x + nextPos.x , p.y + nextPos.y));
            }
            Block b = new Block(next, nextBit, handler);
            b.addMap(blockMap);
            synchronized (blockList) {
                blockList.add(b);
            }
            synchronized (nextBlock){
                getNextBlock();
            }
            handler.sendEmptyMessage(MainActivity.MSG_CLEAR);
            return true;
        }
    }

    /* checks if nextBlock can fit blockMap. return false if cannot find place */
    public boolean nextCheck(){
        if(nextPos == null){
            return findPlace();
        }
        else{
            for(Point p : nextBlock){
                if(blockMap[p.x + nextPos.x][p.y + nextPos.y] != null){
                    return findPlace();
                }
            }
            return true;
        }
    }

    /* set NextPos with NextBlock. return false if cannot find place */
    public boolean findPlace(){
        ArrayList<Point> candidate = new ArrayList<>();
        /* search map and add candidate points */
        for(int x = 0; x < mapNum; x++){
            for(int y = 0; y < mapNum; y++){
                boolean fit = true;
                for(Point p : nextBlock){
                    if(p.x + x < 0 || p.x + x >= mapNum ||
                            p.y + y < 0 || p.y + y >= mapNum){
                        fit = false;
                        break;
                    }
                    if(blockMap[p.x + x][p.y + y] != null){
                        fit = false;
                        break;
                    }
                }
                if(fit){
                    candidate.add(new Point(x, y));
                }
            }
        }
        int candNum = candidate.size();
        if(candNum <= 0){
            nextPos = null;
            return false;
        }
        Random gen = new Random();
        nextPos = candidate.get(gen.nextInt(candNum));
        return true;
    }

    /* vertical & horizontal line clear */
    public void clearLine(){
        int[] clearX = new int[mapNum];
        /* vertical search */
        for(int x = 0 ; x < mapNum; x++){
            boolean clear = true;
            for(int y = 0 ; y < mapNum; y++){
                if(blockMap[x][y] == null){
                    clear = false;
                    break;
                }
            }
            if(clear){
                clearX[x] = 1;
            }
        }
        int[] clearY = new int[mapNum];
        /* horizontal search */
        for(int y = 0 ; y < mapNum; y++){
            boolean clear = true;
            for(int x = 0 ; x < mapNum; x++){
                if(blockMap[x][y] == null){
                    clear = false;
                    break;
                }
            }
            if(clear){
                clearY[y] = 1;
            }
        }
        int cleared = 0;
        /* vertical clean */
        for(int x = 0 ; x < mapNum; x++){
            if(clearX[x] != 0){
                cleared++;
                for(int y = 0; y < mapNum; y++){
                    if(blockMap[x][y] != null) {
                        blockMap[x][y].deleteV(x, blockMap);
                        blockMap[x][y] = null;
                    }
                }
            }
        }
        /* horizontal clean */
        for(int y = 0 ; y < mapNum; y++){
            if(clearY[y] != 0){
                cleared++;
                for(int x = 0; x < mapNum; x++){
                    if(blockMap[x][y] != null) {
                        blockMap[x][y].deleteH(y, blockMap);
                        blockMap[x][y] = null;
                    }
                }
            }
        }
        /* for line draw */
        synchronized (clearXdraw){
            for(int i = 0; i < mapNum; i++){
                clearXdraw[i] = clearX[i] * 5;
            }
        }
        synchronized (clearYdraw){
            for(int i = 0; i < mapNum; i++){
                clearYdraw[i] = clearY[i] * 5;
            }
        }
        updateScore(cleared);
        nextCheck();
    }

    /* score update and send handler message */
    private void updateScore(int cleared){
        score = score + cleared * cleared * 100;
        if(handler != null){
            handler.sendEmptyMessage(MainActivity.MSG_SCORE);
        }
    }

    /* color index getter */
    private static int count = ColorSize;
    private static int[] leftIndex = new int[ColorSize];
    private int getColorIndex(){
        if(count == ColorSize){
            for(int i = 0; i < ColorSize; i++){
                leftIndex[i] = 1;
            }
            count = 0;
        }
        count++;
        Random gen = new Random();
        int index = gen.nextInt(ColorSize);
        while(leftIndex[index] == 0){
            index++;
            if(index == ColorSize){index = 0;}
        }
        leftIndex[index] = 0;
        return index;
    }

    /* pos index getter */
    private static int poscount = PosSize;
    private static int[] posleftIndex = new int[PosSize];
    private int getPosIndex(){
        if(poscount == PosSize){
            for(int i = 0; i < PosSize; i++){
                posleftIndex[i] = 1;
            }
            poscount = 0;
        }
        poscount++;
        Random gen = new Random();
        int index = gen.nextInt(PosSize);
        while(posleftIndex[index] == 0){
            index++;
            if(index == PosSize){index = 0;}
        }
        posleftIndex[index] = 0;
        index = index * 4 + gen.nextInt(4);
        return index;
    }

    public void attachHandler(Handler h){
        handler = h;
        for(Block b : blockList){
            b.attachHandler(h);
        }
    }

    public static void clearedLineDraw(Canvas canvas){
        synchronized (clearXdraw){
            for (int x = 0; x < mapNum; x++) {
                if(clearXdraw[x] != 0) {
                    for (int y = 0; y < mapNum; y++) {
                        Rect cell = new Rect(startLeft + x * blockSize, startTop + y * blockSize,
                                startLeft + (x + 1) * blockSize, startTop + (y + 1) * blockSize);
                        canvas.drawBitmap(grey, null, cell, null);
                    }
                    clearXdraw[x]--;
                }
            }
        }
        synchronized (clearYdraw){
            for (int y = 0; y < mapNum; y++) {
                if(clearYdraw[y] != 0) {
                    for (int x = 0; x < mapNum; x++) {
                        Rect cell = new Rect(startLeft + x * blockSize, startTop + y * blockSize,
                                startLeft + (x + 1) * blockSize, startTop + (y + 1) * blockSize);
                        canvas.drawBitmap(grey, null, cell, null);
                    }
                    clearYdraw[y]--;
                }
            }
        }
    }

    public void reset(){
        synchronized (blockList) {
            blockList.clear();
        }
        clearMap();
        score = 0;
        gamestate = state.playing;
        count = ColorSize;
    }

    public void clearMap(){
        for(int x = 0; x < mapNum; x++){
            for(int y = 0; y < mapNum; y++){
                blockMap[x][y] = null;
            }
        }
        synchronized (blockList){
            blockList.clear();
        }
    }
}
