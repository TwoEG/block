package com.example.block;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private static BlockView bView;
    private TextView bestView;
    private int bestScore;
    private TextView scoreView;
    private TimerGauge timerG;
    private static Myhandler handler;
    private AdView mAdView;

    private GoogleSignInClient mGoogleSignInClient;

    public static final int MSG_SCORE = 8000;
    public static final int MSG_CLEAR = 8001;
    public static final int MSG_MOVING = 8002;
    public static final int MSG_UPDATE_TIMER = 8003;
    public static final int MSG_GAME = 8004;

    private static int RC_SIGN_IN = 9000;
    private static int RC_LEADERBOARD_UI = 9001;

    private int leftTime;
    private Thread gameThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        handler = new Myhandler(this);
        /* game start */
        viewInitialize();
        bestScore = loadBestScore();
        bestView.setText(String.valueOf(bestScore));
        scoreView.setText("0");
        bView.reset();
        bView.getNextBlock();
        bView.gamestate = BlockView.state.playing;
        leftTime = 5000;
        game();
        /* ad initialize */
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /* restart button */
        ImageView restart = findViewById(R.id.restart);
        restart.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                restart();
                return false;
            }
        });
        /* google sign in button */
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startSignInIntent();
            }
        });
        /* leaderboard button */
        findViewById(R.id.leaderboard).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showLeaderboard();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        signInSilently();
    }

    @Override
    protected void onDestroy() {
        if(gameThread != null) {
            gameThread.interrupt();
        }
        handler.removeCallbacksAndMessages(null);
        bView.clearMap();
        bView.stopThread();
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
                Log.d("MSG","SCORE");
                break;
            case MSG_CLEAR:
                bView.clearLine();
                bView.gamestate = BlockView.state.playing;
                Log.v("MSG","CLEAR");
                break;
            case MSG_MOVING:
                bView.gamestate = BlockView.state.animating;
                Log.d("MSG","MOVING");
                break;
            case MSG_GAME:
                game();
                break;
            case MSG_UPDATE_TIMER:
                timerG.setAndDraw(msg.arg1);
                break;

        }
    }

    private void viewInitialize(){
        bView = findViewById(R.id.view);
        bView.attachHandler(handler);
        bestView = findViewById(R.id.bestView);
        scoreView = findViewById(R.id.scoreView);
        timerG = findViewById(R.id.timerG);
    }

    private void game(){
        if(bView.makeNextBlock()){
            Timer timer = new Timer(leftTime, handler);
            gameThread = new Thread(timer);
            gameThread.start();
            if(leftTime > 2000) {
                leftTime = leftTime - 100;
            }
        }
        else{
            bView.gamestate = BlockView.state.stop;
            Toast.makeText(this, "game over", Toast.LENGTH_SHORT).show();
            if(bView.score > bestScore){
                bestScore = bView.score;
                saveBestScore(bestScore);
                bestView.setText(String.valueOf(bestScore));
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .submitScore(getString(R.string.leaderboard_high_score), bestScore);
            }
        }
    }

    private void restart(){
        if(gameThread != null) {
            gameThread.interrupt();
        }
        handler.removeCallbacksAndMessages(null);
        bView.reset();
        leftTime = 5000;
        game();
    }

    private void saveBestScore(int bestScore) {
        SharedPreferences preferences = getSharedPreferences("BlockGame", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("BestScore", bestScore);
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

    private void signInSilently() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            GoogleSignInAccount signedInAccount = account;
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);
            signInClient
                    .silentSignIn()
                    .addOnCompleteListener(
                            this,
                            new OnCompleteListener<GoogleSignInAccount>() {
                                @Override
                                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                    if (task.isSuccessful()) {
                                        // The signed in account is stored in the task's result.
                                        GoogleSignInAccount signedInAccount = task.getResult();
                                    } else {
                                        // Player will need to sign-in explicitly using via UI.
                                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                                        // Interactive Sign-in.
                                    }
                                }
                            });
        }
    }

    private void startSignInIntent() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            // updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("FAIL", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    private void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(getString(R.string.leaderboard_high_score))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }
}
