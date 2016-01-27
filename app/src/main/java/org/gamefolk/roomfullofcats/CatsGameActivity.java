package org.gamefolk.roomfullofcats;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

        setContentView(R.layout.activity_cats_menu);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    deviceLoaderThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                startGame();
            }
        });

        loadGame();
    }

    @Override
    protected View initializeContentView() {
        adView = (AdView) contentView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return contentView;
    }

    private void loadGame() {
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

    private void startGame() {
        Log.v(TAG, "finished loading");
        Log.v(TAG, "ip: " + DeviceUtility.getLocalIp());
        Log.v(TAG, "user agent: " + DeviceUtility.getUserAgent());

        contentView = (RelativeLayout) getLayoutInflater().inflate(R.layout.activity_cats_game, null);
        gameView = new CatsGame(this, this, contentView);

        CatsGameManager.initialize(this, gameView);

        final Level level1 = CatsGameManager.loadLevel();

        CatsGameManager.displayLevelMessage(level1.message);

        gameLoaderThread = new Thread(new Runnable() {
            public void run() {
                gameView.init();
                gameView.makeLevel(level1);
                contentView.addView(gameView);
             }
        });
        gameLoaderThread.start();

        // call initializeGame and initializeContentView on the main thread, starting the game
        loadContent();
    }
}
