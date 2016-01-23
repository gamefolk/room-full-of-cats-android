package org.gamefolk.roomfullofcats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.arcadeoftheabsurd.absurdengine.DeviceUtility;
import com.eclipsesource.json.JsonObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

class Level
{
	int number;
	int mapWidth;
	int mapHeight;
	int levelTime;
	int fallTime;  // interval after which cats fall, in seconds
	int catsLimit; // the target number of cats of the same type to collect
	String message;
	String title;
}

public class CatsGameManager 
{
	static int curLevel = 1;
	private static Context context;
	private static CatsGame game;
	
	private static final String TAG = "RoomFullOfCats";
	
	static class ToastView extends TextView
	{
		public ToastView(Context context, String message) {
			super(context);
			setTextSize(DeviceUtility.isIOS() ? 12 : 20);
			setText(message);
		}
	}
	
	static void initialize(Context context, CatsGame game) {
		CatsGameManager.context = context;
		CatsGameManager.game = game;
	}
	
	static void displayLevelMessage(final String message) {
		((Activity)context).runOnUiThread(new Runnable() {
	        public void run() {
	        	Toast toast = new Toast(context.getApplicationContext());
	    		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	    		toast.setDuration(Toast.LENGTH_LONG);
	    		toast.setView(new ToastView(context.getApplicationContext(), message));
	    		toast.show();
	        }
	    });
	}
	
	static void startLevel() {
		Level level = loadLevel();
		displayLevelMessage(level.message);
		game.makeLevel(level);
		game.setupGraphics();
		game.startGame();
	}
	
	static Level loadLevel() {
		Level level = new Level();
		
		level.number = curLevel;
		
		if (curLevel == 1) {
			InputStream input = context.getResources().openRawResource(context.getResources().getIdentifier("level1", "raw", "org.gamefolk.roomfullofcats"));
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				try {
					Reader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					input.close();
				}
			}
			catch (IOException e) {
				Log.e(TAG, "error reading level json");
			}			
			Log.v(TAG, "json: " + writer.toString());
			JsonObject mainObject = JsonObject.readFrom(writer.toString());
			
			Log.v(TAG, "level: " + mainObject.get("levelTitle").asString());
			
			level.mapWidth = mainObject.get("columns").asInt();
			level.mapHeight = mainObject.get("rows").asInt();
			level.levelTime = mainObject.get("timeLimit").asInt();
			level.fallTime = 1;
			level.catsLimit = 3;
			level.message = mainObject.get("levelDescription").asString();
			level.title = mainObject.get("levelTitle").asString();
		} else if (curLevel == 2) {
			level.mapWidth = 5;
			level.mapHeight = 5;
			level.levelTime = 10;
			level.fallTime = 1;
			level.catsLimit = 3;
			level.message = "... but those people are fucking stupid.";
			level.title = "Level 2";
		}
		return level;
	}
}
