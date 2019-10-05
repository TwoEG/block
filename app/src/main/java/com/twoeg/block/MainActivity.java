package com.twoeg.block;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private View gameCover;
    private static BlockView bView;
    private TextView bestView;
    private int bestScore;
    private TextView scoreView;
    private nextBlockView nbView;
    private TimerGauge timerG;
    private static Myhandler handler;
    private AdView mAdView;

    private GoogleSignInClient mGoogleSignInClient;
    private boolean loginTry = false;

    public static final int MSG_SCORE = 8000;
    public static final int MSG_CLEAR = 8001;
    public static final int MSG_MOVING = 8002;
    public static final int MSG_UPDATE_TIMER = 8003;
    public static final int MSG_GAME = 8004;
    public static final int MSG_NEXTBLOCK = 8005;

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
        /* ad initialize */
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /* start button */
        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                hideLabelContainer();
                /* restart button */
                findViewById(R.id.restart).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        restart();
                    }
                });
            }
        });
        /* google sign in button */
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startShareIntent();
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
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount g = GoogleSignIn.getLastSignedInAccount(this);
        if(g == null && !loginTry){
            loginTry = true;
            startSignInIntent();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("ON","resume");
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
            case MSG_NEXTBLOCK:
                nbView.setChar(msg.arg1, msg.arg2);
                break;
        }
    }

    private void viewInitialize(){
        gameCover = findViewById(R.id.gameCover);
        bView = findViewById(R.id.bview);
        bView.attachHandler(handler);
        bestView = findViewById(R.id.bestView);
        scoreView = findViewById(R.id.scoreView);
        nbView = findViewById(R.id.nextBlock);
        timerG = findViewById(R.id.timerG);
    }

    private void game(){
        if(bView.makeNextBlock()){
            Timer timer = new Timer(leftTime, handler);
            gameThread = new Thread(timer);
            gameThread.start();
            if(leftTime > 2000) {
                leftTime = leftTime - 50;
            }
        }
        else{
            showGameOver();
            bView.gamestate = BlockView.state.stop;
            Toast.makeText(this, "game over", Toast.LENGTH_SHORT).show();
            if(bView.score > bestScore){
                bestScore = bView.score;
                saveBestScore(bestScore);
                bestView.setText(String.valueOf(bestScore));
                GoogleSignInAccount g = GoogleSignIn.getLastSignedInAccount(this);
                if(g == null){
                    Toast.makeText(getApplicationContext(), "Please Login To Save Score", Toast.LENGTH_LONG).show();
                }
                else {
                    Games.getLeaderboardsClient(this, g)
                            .submitScore(getString(R.string.leaderboard_high_score), bestScore);
                }
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
        hideGameOver();
        game();
    }

    private void hideLabelContainer() {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gameCover.setVisibility(View.GONE);
                game();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gameCover.startAnimation(anim);
    }

    private void showGameOver() {
        final View v = findViewById(R.id.gameOver);
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(anim);
    }

    private void hideGameOver() {
        findViewById(R.id.gameOver).setVisibility(View.GONE);
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
        Log.v("start","sign");
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
        GoogleSignInAccount g = GoogleSignIn.getLastSignedInAccount(this);
        if(g == null){
            startSignInIntent();
        }
        else {
            Games.getLeaderboardsClient(this, g)
                    .getLeaderboardIntent(getString(R.string.leaderboard_high_score))
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    });
        }
    }

    private void startShareIntent() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        // Set default text message
        // 카톡, 이메일, MMS 다 이걸로 설정 가능
        //String subject = "문자의 제목";
        String text = getString(R.string.play_store_url);
        //intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        // Title of intent
        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
        startActivity(chooser);
    }
}
