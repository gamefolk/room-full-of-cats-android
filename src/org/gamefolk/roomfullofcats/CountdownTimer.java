package org.gamefolk.roomfullofcats;

import com.arcadeoftheabsurd.absurdengine.GameView;
import com.arcadeoftheabsurd.absurdengine.Timer.TimerUI;
import com.arcadeoftheabsurd.j_utils.Delegate;

public abstract class CountdownTimer    
{
    private TimerUI timer;
    private int iterationsLeft;

    public abstract void onTic(int remaining);
    public abstract void onFinish();
    
    public CountdownTimer(int iterations, float interval, GameView view) {
        iterationsLeft = iterations;

        timer = new TimerUI(interval, view, new Delegate() {
            @Override
            public void function(Object... args) {
                onTic(--iterationsLeft); 
                if (iterationsLeft == 0) {
                    onFinish();
                    timer.end();
                }
            }
        });
    }

    public void start() {
        timer.start();
    }
}

