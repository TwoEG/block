package com.example.block;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BlockView bView;
    private TextView bestView;
    private int bestScore;
    private TextView scoreView;
    private ImageView restart;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case BlockView.MSG_SCORE:
                    scoreView.setText(String.valueOf(bView.score));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(bView == null) {
            bView = findViewById(R.id.view);
            bView.attachHandler(handler);
            game(5000);
            bestScore = loadBestScore();
            bestView = findViewById(R.id.bestView);
            bestView.setText(String.valueOf(bestScore));
            scoreView = findViewById(R.id.scoreView);
            scoreView.setText("0");
        }
        restart = findViewById(R.id.restart);
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
        bView.clearMap();
        super.onDestroy();
    }

    private void game(final int time){
        if(bView.makeBlock()){
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
        editor.putInt("BestScore", bestScore);
        editor.commit();
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
