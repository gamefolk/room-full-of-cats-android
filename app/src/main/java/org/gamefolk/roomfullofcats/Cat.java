package org.gamefolk.roomfullofcats;

import com.arcadeoftheabsurd.absurdengine.Sprite;

public class Cat
{
	 public int things = 0;
     public CatType type;
     public Sprite sprite;
     
     int curFrame = 0;
     boolean glitched = false;
             
     public Cat (CatType type, Sprite sprite) {
         this.type = type;
         this.sprite = sprite;
     }
     
     /*public void toggleGlitch() {
         if (glitched) {
             glitched = false;
             CatsGame.this.setSpriteBitmap(Cat.this.sprite, Cat.this.type.bitmapFrames[curFrame]);
             animationTimer.resume();
         } else {
             glitched = true;
             animationTimer.pause();
             CatsGame.this.setSpriteBitmap(Cat.this.sprite, Cat.this.type.glitchFrame);
         }
     }*/
}

enum CatType
{
    BLUECAT, GRAYCAT, PINKCAT, STRIPECAT;
    
    int bitmapId = -1;
    
    public int[] bitmapFrames;
    public int glitchFrame;
    
    public void setBitmap(int bitmapId) {
        this.bitmapId = bitmapId;
    }
}
