package org.gamefolk.roomfullofcats;

import com.arcadeoftheabsurd.absurdengine.DeviceUtility;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LevelUIView extends RelativeLayout
{
	TextView scoreView;
	TextView titleView;
	TextView timeView;

	public LevelUIView(Context context) {
		super(context);
		
		scoreView = new TextView(context);
		titleView = new TextView(context);
		timeView = new TextView(context);
				
		LayoutParams scoreParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		scoreParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		scoreParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		LayoutParams timeParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		timeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		timeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		scoreView.setLayoutParams(scoreParams);
		titleView.setLayoutParams(titleParams);
		timeView.setLayoutParams(timeParams);
		
		scoreView.setTextSize(DeviceUtility.isIOS() ? 12 : 20);
        titleView.setTextSize(DeviceUtility.isIOS() ? 12 : 20);
        timeView.setTextSize(DeviceUtility.isIOS() ? 12 : 20);
        
        this.addView(scoreView);
        this.addView(titleView);
        this.addView(timeView);
	}

}
