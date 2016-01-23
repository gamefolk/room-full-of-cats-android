package org.gamefolk.roomfullofcats;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arcadeoftheabsurd.absurdengine.BitmapResourceManager;
import com.arcadeoftheabsurd.absurdengine.GameView;
import com.arcadeoftheabsurd.absurdengine.SoundManager;
import com.arcadeoftheabsurd.absurdengine.Timer.TimerAsync;
import com.arcadeoftheabsurd.absurdengine.Timer.TimerUI;
import com.arcadeoftheabsurd.j_utils.Delegate;
import com.arcadeoftheabsurd.j_utils.Vector2d;

public class CatsGame extends GameView {
    static final int NUM_CHANNELS = 4;
    private static final int SONG_CHANNEL = 0;
    private static final int BLIP_CHANNEL = 1;
    private static final int SCORE_CHANNEL = 2;
    private static final int GLITCH_CHANNEL = 3;

    private final int incSize = 20; // the amount by which to increase the size of cats as they collect    

    private CatsMap map;
    private int bucketsY[];

    private Vector2d mapLoc; // in pixels, the top left corner of the top left column of cats on the screen
    private Vector2d catSize; // in pixels, set according to the size of the screen in onSizeChanged()

    private int catsLimit;
    private int score;
    private long curLevelTime;

    private TimerAsync fallTimer;
    private TimerUI animationTimer;
    private Thread levelTimerThread;

    TextView timeView;
    TextView scoreView;
    TextView titleView;

    private BitmapResourceManager bitmapResources;

    private final Random rGen = new Random();

    private static final String TAG = "RoomFullOfCats";

    public CatsGame(Context context, GameLoadListener loadListener, ViewGroup viewRoot) {
        super(context, loadListener);

        scoreView = (TextView) findViewById(R.id.score);
        timeView = (TextView) findViewById(R.id.time);
        titleView = (TextView) findViewById(R.id.title);

        bitmapResources = new BitmapResourceManager(getResources(), 16);
        loadResources();
    }

    private void drawUI() {
        scoreView.setText("Score: " + score);
        timeView.setText("Time: " + curLevelTime);
    }

    public void makeLevel(final Level level) {
        map = new CatsMap(level.mapWidth, level.mapHeight);
        this.catsLimit = level.catsLimit;

        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleView.setText(level.title);
            }
        });

        bucketsY = new int[map.getWidth()];

        for (int col = 0; col < map.getWidth(); col++) {
            bucketsY[col] = map.getHeight() - 1;
        }

        levelTimerThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                new CountDownTimer(level.levelTime * 1000, 1) {
                    @Override
                    public void onFinish() {
                        Log.v(TAG, "game over");
                        animationTimer.pause();
                        fallTimer.pause();
                        map.clear();
                        CatsGameManager.curLevel++;

                        CatsGameManager.startLevel();
                    }

                    @Override
                    public void onTick(long remaining) {
                        curLevelTime = remaining;
                    }
                }.start();

                Looper.loop();
            }
        };


        animationTimer = new TimerUI(.2f, CatsGame.this, new Delegate() {
            public void function(Object... args) {
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        Cat cat = map.getCat(x, y);
                        if (cat != null) {
                            CatsGame.this.setSpriteBitmap(cat.sprite, cat.type.bitmapFrames[cat.curFrame++]);
                            if (cat.curFrame == cat.type.bitmapFrames.length) {
                                cat.curFrame = 0;
                            }
                        }
                    }
                }
            }
        });

        fallTimer = new TimerAsync(level.fallTime, this, new Delegate() {
            public void function(Object... args) {

                for (int col = 0; col < map.getWidth(); col++) {
                    for (int row = bucketsY[col] - 1; row >= 0; row--) {

                        Cat candidate = map.getCat(col, row);

                        if (candidate != null) {

                            Cat current = map.getCat(col, bucketsY[col]);

                            if (current != null && bucketsY[col] - row == 1) {

                                //System.out.println("current: " + current.type.name() + " " + bucketsY[col] + " candidate: " + candidate.type.name() + " " + row);

                                if (candidate.type == current.type) {

                                    current.sprite.resize(current.sprite.getWidth() + incSize, current.sprite.getHeight() + incSize);
                                    current.things++;

                                    if (current.things == catsLimit) {
                                        score++;
                                        if (!SoundManager.isPlaying(SCORE_CHANNEL)) {
                                            SoundManager.playSound(SCORE_CHANNEL);
                                        }
                                        map.setCat(col, bucketsY[col], null);

                                        if (bucketsY[col] < map.getHeight() - 1) {
                                            bucketsY[col]++;
                                        }
                                    }

                                    map.setCat(col, row, null);

                                } else {
                                    bucketsY[col]--;
                                }

                            } else {
                                candidate.sprite.translate(0, catSize.y);
                                map.setCat(col, row, null);
                                map.setCat(col, row + 1, candidate);
                            }
                        }
                    }
                }

                // fill the top row with new cats
                for (int x = 0; x < map.getWidth(); x++) {
                    if (map.getCat(x, 0) == null) {

                        CatType type = CatType.values()[rGen.nextInt(4)];
                        map.setCat(x, 0, new Cat(type, makeSprite(type.bitmapId, mapLoc.x + (x * catSize.x), mapLoc.y)));
                    } else {

                        Log.v(TAG, "game over");
                        animationTimer.pause();
                        fallTimer.pause();
                        map.clear();
                    }
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < bucketsY[x]; y++) {
                Cat cat = map.getCat(x, y);
                if (cat != null) {
                    if (cat.sprite.getBounds().contains((int) event.getX(), (int) event.getY())) {
                        map.setCat(x, y, null);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Cat cat = map.getCat(x, y);
                if (cat != null) {
                    cat.sprite.draw(canvas);
                }
            }
        }
        drawUI();
    }

    private void loadResources() {
        bitmapResources.loadBitmap(R.drawable.bluecat1);
        bitmapResources.loadBitmap(R.drawable.bluecat2);
        bitmapResources.loadBitmap(R.drawable.bluecat3);
        bitmapResources.loadBitmap(R.drawable.bluecatgb);
        bitmapResources.loadBitmap(R.drawable.graycat1);
        bitmapResources.loadBitmap(R.drawable.graycat2);
        bitmapResources.loadBitmap(R.drawable.graycat3);
        bitmapResources.loadBitmap(R.drawable.graycatgb);
        bitmapResources.loadBitmap(R.drawable.pinkcat1);
        bitmapResources.loadBitmap(R.drawable.pinkcat2);
        bitmapResources.loadBitmap(R.drawable.pinkcat3);
        bitmapResources.loadBitmap(R.drawable.pinkcatgb);
        bitmapResources.loadBitmap(R.drawable.stripecat1);
        bitmapResources.loadBitmap(R.drawable.stripecat2);
        bitmapResources.loadBitmap(R.drawable.stripecat3);
        bitmapResources.loadBitmap(R.drawable.stripecatgb);

        try {
            SoundManager.loadSound("catsphone.mp3", SONG_CHANNEL);
            SoundManager.loadSound("catsgbphone.mp3", GLITCH_CHANNEL);
            SoundManager.loadSound("blip.wav", BLIP_CHANNEL);
            SoundManager.loadSound("score.wav", SCORE_CHANNEL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setupGraphics() {
        setupGame(this.getWidth(), this.getHeight());
    }

    @Override
    protected void setupGame(int screenWidth, int screenHeight) {
        // room for each row/column of cats + 1 cat worth of margin on the sides
        int tempX = screenWidth / (map.getWidth() + 1);
        int tempY = screenHeight / (map.getHeight() + 1);

        int catXY = tempX < tempY ? tempX : tempY;

        catSize = new Vector2d(catXY, catXY);
        mapLoc = new Vector2d((screenWidth - (map.getWidth() * catSize.x)) / 2, (screenHeight - (map.getHeight() * catSize.y)) / 2);

        int frame1, frame2, frame3, glitchFrame;

        frame1 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.bluecat1), catSize);
        frame2 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.bluecat2), catSize);
        frame3 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.bluecat3), catSize);

        glitchFrame = loadBitmapResource(bitmapResources.getBitmap(R.drawable.bluecatgb), catSize);

        CatType.BLUECAT.bitmapFrames = new int[]{frame1, frame2, frame3, frame2};
        CatType.BLUECAT.glitchFrame = glitchFrame;
        CatType.BLUECAT.setBitmap(frame1);

        frame1 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.graycat1), catSize);
        frame2 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.graycat2), catSize);
        frame3 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.graycat3), catSize);

        glitchFrame = loadBitmapResource(bitmapResources.getBitmap(R.drawable.graycatgb), catSize);

        CatType.GRAYCAT.bitmapFrames = new int[]{frame1, frame2, frame3, frame2};
        CatType.GRAYCAT.glitchFrame = glitchFrame;
        CatType.GRAYCAT.setBitmap(frame1);

        frame1 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.pinkcat1), catSize);
        frame2 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.pinkcat2), catSize);
        frame3 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.pinkcat3), catSize);

        glitchFrame = loadBitmapResource(bitmapResources.getBitmap(R.drawable.pinkcatgb), catSize);

        CatType.PINKCAT.bitmapFrames = new int[]{frame1, frame2, frame3, frame2};
        CatType.PINKCAT.glitchFrame = glitchFrame;
        CatType.PINKCAT.setBitmap(frame1);

        frame1 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.stripecat1), catSize);
        frame2 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.stripecat2), catSize);
        frame3 = loadBitmapResource(bitmapResources.getBitmap(R.drawable.stripecat3), catSize);

        glitchFrame = loadBitmapResource(bitmapResources.getBitmap(R.drawable.stripecatgb), catSize);

        CatType.STRIPECAT.bitmapFrames = new int[]{frame1, frame2, frame3, frame2};
        CatType.STRIPECAT.glitchFrame = glitchFrame;
        CatType.STRIPECAT.setBitmap(frame1);
    }

    @Override
    protected void startGame() {
        fallTimer.start();
        animationTimer.start();
        levelTimerThread.start();
        //SoundManager.setVolume(GLITCH_CHANNEL, 0, 0);
        SoundManager.loopSound(SONG_CHANNEL);
        //SoundManager.loopSound(GLITCH_CHANNEL);
    }

    @Override
    protected void updateGame() {
        /*if (soundGlitching && rGen.nextFloat() > .95) {
            soundGlitching = false;
            SoundManager.setVolume(GLITCH_CHANNEL, 0, 0);
            SoundManager.setVolume(SONG_CHANNEL, 1, 1);
            
            for (int x = 0; x < mapSize.x; x++) {
                for (int y = 0; y < mapSize.y; y++) {
                    if (map[x][y] != null) {
                        map[x][y].toggleGlitch();
                    }
                }
            }
        } else if (rGen.nextFloat() > .99) {
            soundGlitching = true;
            SoundManager.setVolume(GLITCH_CHANNEL, 1, 1);
            SoundManager.setVolume(SONG_CHANNEL, 0, 0);
            
            for (int x = 0; x < mapSize.x; x++) {
                for (int y = 0; y < mapSize.y; y++) {
                    if (map[x][y] != null) {
                        map[x][y].toggleGlitch();
                    }
                }
            }
        }*/
    }
}
