package org.gamefolk.roomfullofcats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.arcadeoftheabsurd.absurdengine.Sprite;

public class CatsSplashActivity extends Activity
{
	private class SplashView extends View
	{
		private Sprite logo;
		
		public SplashView(Context context) {
			super(context);
			setBackgroundColor(Color.BLACK);
		}
		
		@Override
		protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
			logo = Sprite.fromResource(getResources(), R.drawable.gamefolklogo, newWidth, newWidth);
			logo.setLocation(0, (newHeight - logo.getHeight()) / 2);
			invalidate();
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (logo != null) {
				logo.draw(canvas);
			}
			super.onDraw(canvas);
		}
	}
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   
        
        super.onCreate(savedInstanceState);
		
		SplashView splashView = new SplashView(this);
        splashView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				CatsSplashActivity.this.startActivity(new Intent(CatsSplashActivity.this, CatsGameActivity.class));
				CatsSplashActivity.this.finish();
			}
		});
        setContentView(splashView);
	}
}
