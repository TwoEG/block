package com.twoeg.block;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {

    private final int TOP    = 1;
    private final int BOTTOM = 2;
    private final int LEFT   = 4;
    private final int RIGHT  = 8;

    private float firstTouchX;
    private float firstTouchY;
    private float SWIPE_THRESHOLD = 50.0f;

    private boolean stop = false;

    public OnSwipeTouchListener (Context ctx){
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Register the first touch on TouchDown and this should not change unless finger goes up.
                firstTouchX = event.getX();
                firstTouchY = event.getY();
                //As the event is consumed, return true
                Down(event.getX(), event.getY());
                result = true;
                stop = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if(stop){
                    result = true;
                    break;
                }
                //CurrentX/Y are the continues changing values of one single touch session. Change
                //when finger slides on view
                float currentX = event.getX();
                float currentY = event.getY();
                //DeltaX/Y are the difference between current touch and the value when finger first touched screen.
                //If its negative that means current value is on left side of first touchdown value i.e Going left and
                //vice versa.
                float deltaX = currentX - firstTouchX;
                float deltaY = currentY - firstTouchY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    //Horizontal swipe
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX > 0) {
                            //means we are going right
                            stop = true;
                            Up();
                            onSwipe(firstTouchX, firstTouchY, RIGHT);
                        } else {
                            //means we are going left
                            stop = true;
                            Up();
                            onSwipe(firstTouchX, firstTouchY, LEFT);
                        }
                    }
                } else {
                    //It's a vertical swipe
                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0) {
                            //means we are going down
                            stop = true;
                            Up();
                            onSwipe(firstTouchX, firstTouchY, BOTTOM);
                        } else {
                            //means we are going up
                            stop = true;
                            Up();
                            onSwipe(firstTouchX, firstTouchY, TOP);
                        }
                    }
                }

                result = true;
                break;

            case MotionEvent.ACTION_UP:
                //Clean UP
                firstTouchX = 0.0f;
                firstTouchY = 0.0f;
                Up();
                result = true;
                break;

            default:
                result = false;
                break;
        }

        return result;
    }

    /* implementation */
    public void Up(){};
    public void Down(float x, float y){}
    public void onSwipe(float x, float y, int tag){}
}