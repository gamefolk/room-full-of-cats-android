package org.gamefolk.roomfullofcats;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.arcadeoftheabsurd.absurdengine.DeviceUtility;
import com.arcadeoftheabsurd.absurdengine.Sprite;
import com.arcadeoftheabsurd.j_utils.Vector2d;
import com.adsdk.sdk.nativeads.BannerAdView;
import com.adsdk.sdk.nativeads.NativeAd;
import com.adsdk.sdk.nativeads.NativeAdManager;

public class CatsAd extends BannerAdView
{
	private NativeAdManager adManager;
	private HashMap<String, Vector2d> imageAssets = new HashMap<String, Vector2d>();
	private ArrayList<String> textTypes = new ArrayList<String>();
	private Sprite gamefolkBackground;
	private boolean adLoaded = false;
	private Paint backgroundPaint = new Paint();
	private Paint borderPaint = new Paint();
	private int borderWidth;
	private int backgroundRadius; 
	
	private static final String TAG = "RoomFullOfCats";
	
	public CatsAd(Context context) {
		super(context, 
			DeviceUtility.isIOS() ? 12 : 19, // text size
			DeviceUtility.isIOS() ? 5 : 15,  // text margin left
			5, Color.BLACK);
		borderWidth = DeviceUtility.isIOS() ? 1 : 3;
		backgroundRadius = DeviceUtility.isIOS() ? 20 : 50;
		backgroundPaint.setAntiAlias(true);
		backgroundPaint.setColor(0xFFF7F7F7); // iOS7ish gray color 
		borderPaint.setColor(Color.BLACK);
	}
	
	@Override
	public void adLoaded(final NativeAd ad) {
		Log.v(TAG, "ad loaded");
		
		adLoaded = true;
		this.setAssets(ad.getImageAsset("icon").sprite, ad.getTextAsset("description"));
		
		this.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (ad.getClickUrl() != null && !ad.getClickUrl().equals("")) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickUrl()));
					getContext().startActivity(intent);
				}
			}
		});
		super.adLoaded(ad);
	}

	@Override
	public void adFailedToLoad() {
		Log.e(TAG, "ad failed");
	}
	
	@Override
	protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
		super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
		
		gamefolkBackground = Sprite.fromResource(getResources(), R.drawable.gamefolksidebar, newWidth, -1);

		imageAssets.put("icon", new Vector2d(newHeight, newHeight));
		textTypes.add("description");
		adManager = new NativeAdManager(getContext(), this, ApiKeys.getMobFoxPublisherId(), null, textTypes, imageAssets);
		adManager.requestAd();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (gamefolkBackground != null && !adLoaded) {
			gamefolkBackground.draw(canvas);
		} else if (adLoaded) {
			// draw background
			canvas.drawCircle(getWidth() - backgroundRadius, backgroundRadius, backgroundRadius, backgroundPaint);
			canvas.drawRect(0, 0, getWidth() - backgroundRadius, getHeight(), backgroundPaint);
			canvas.drawRect(getWidth() - (backgroundRadius + 1), backgroundRadius, getWidth(), getHeight(), backgroundPaint);
			canvas.drawRect(getHeight(), 0, getHeight() + borderWidth, getHeight(), borderPaint);
		}
		super.dispatchDraw(canvas);
	}
}
