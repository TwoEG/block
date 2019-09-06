package com.example.block;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static BlockView bView;
    private TextView bestView;
    private int bestScore;
    private TextView scoreView;
    private static Myhandler handler;

    public static final int MSG_SCORE = 32;
    public static final int MSG_CLEAR = 64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Myhandler(this);
        if(bView == null) {
            initialize();
            bestScore = loadBestScore();
            bestView.setText(String.valueOf(bestScore));
            scoreView.setText("0");
            bView.getNextBlock();
            bView.gamestate = BlockView.state.playing;
            game(5000);
        }
        else {
            initialize();
        }
        ImageView restart = findViewById(R.id.restart);
        restart.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                restart();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(isFinishing()){
            bView.clearMap();
            bView.stopThread();
        }
        else{
            bView.stopThread();
        }
        super.onDestroy();
    }

    public static class Myhandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public Myhandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg){
        switch(msg.what) {
            case MSG_SCORE:
                scoreView.setText(String.valueOf(bView.score));
                break;
            case MSG_CLEAR:
                bView.nextCheck();
                bView.clearLine();
                bView.gamestate = BlockView.state.playing;
        }
    }

    private void initialize(){
        bView = findViewById(R.id.view);
        bView.attachHandler(handler);
        bestView = findViewById(R.id.bestView);
        scoreView = findViewById(R.id.scoreView);
    }

    private void game(final int time){
        if(bView.makeNextBlock()){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(time > 2000) {
                        game(time - 100);
                    }
                    else{
                        game(2000);
                    }
                }
            }, time);
        }
        else{
            bView.gamestate = BlockView.state.stop;
            Toast.makeText(this, "game over", Toast.LENGTH_SHORT).show();
            if(bView.score > bestScore){
                bestScore = bView.score;
                saveBestScore(bestScore);
                bestView.setText(String.valueOf(bestScore));
            }
        }
    }

    private void restart(){
        handler.removeCallbacksAndMessages(null);
        bView.reset();
        game(5000);
    }

    private void saveBestScore(int bestScore) {
        SharedPreferences preferences = getSharedPreferences("BlockGame", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(!editor.commit()){
            Log.v("SAVE","FAIL");
        }
    }

    private int loadBestScore() {
        SharedPreferences preferences = getSharedPreferences("BlockGame", Context.MODE_PRIVATE);
        if (preferences.contains("BestScore")) {
            return preferences.getInt("BestScore", 0);
        } else {
            return 0;
        }
    }
}
