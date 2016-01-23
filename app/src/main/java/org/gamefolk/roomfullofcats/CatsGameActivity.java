package org.gamefolk.roomfullofcats;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.arcadeoftheabsurd.absurdengine.DeviceUtility;
import com.arcadeoftheabsurd.absurdengine.GameActivity;
import com.arcadeoftheabsurd.absurdengine.SoundManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class CatsGameActivity extends GameActivity {
    private CatsGame gameView;
    private AdView adView;
    private RelativeLayout contentView;

    private Thread deviceLoaderThread;
    private Thread gameLoaderThread;

    private static final String TAG = "RoomFullOfCats";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cats_game);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadGame();
        startGame();
    }

    @Override protected View initializeContentView() {
        return contentView;
    }

    private void loadGame() {
        Log.v(TAG, "checking ad services");
        Log.v(TAG, "ad services available");

        Log.v(TAG, "getting device info");

        DeviceUtility.setUserAgent(this);

        deviceLoaderThread = new Thread(new Runnable() {
            public void run() {
                SoundManager.initializeSound(getAssets(), CatsGame.NUM_CHANNELS);
                DeviceUtility.setLocalIp();
            }
        });
        deviceLoaderThread.start();
    }

    protected CatsGame initializeGame() {
        try {
            gameLoaderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return gameView;
    }

    @SuppressWarnings("deprecation")
    private void startGame() {
        Log.v(TAG, "finished loading");
        Log.v(TAG, "ip: " + DeviceUtility.getLocalIp());
        Log.v(TAG, "user agent: " + DeviceUtility.getUserAgent());

        contentView = (RelativeLayout) findViewById(R.id.game_view);
        gameView = new CatsGame(this, this, contentView);

        CatsGameManager.initialize(this, gameView);

        final Level level1 = CatsGameManager.loadLevel();

        CatsGameManager.displayLevelMessage(level1.message);

        gameLoaderThread = new Thread(new Runnable() {
            public void run() {
                gameView.makeLevel(level1);

                contentView.addView(gameView, new LayoutParams(LayoutParams.FILL_PARENT, 0, .80f));
            }
        });
        gameLoaderThread.start();

        // call initializeGame and initializeContentView on the main thread, starting the game
        initializeGame();
        initializeContentView();
        loadContent();
    }
}

