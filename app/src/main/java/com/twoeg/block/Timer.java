package com.twoeg.block;

import android.os.Handler;
import android.os.Message;

public class Timer implements Runnable {

    private int Time;
    private Handler handler;
    private boolean stop = false;

    Timer(int Time, Handler handler){
        this.Time = Time;
        this.handler = handler;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        while(!stop){
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
            int passedTime = (int) (System.currentTimeMillis() - start);
            int leftTime = Time - passedTime;
            if(leftTime < 0){
                handler.sendEmptyMessage(MainActivity.MSG_GAME);
                break;
            }
            Message m =  new Message();            m.what = MainActivity.MSG_UPDATE_TIMER;
            m.arg1 = (int) (((float) leftTime / (float) Time) * 1000);
            handler.sendMessage(m);
        }
    }

    public void stopThread(){
        stop = false;
    }
}
