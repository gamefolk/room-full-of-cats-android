package org.gamefolk.roomfullofcats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class CatsSplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cats_splash);

        LinearLayout layout = (LinearLayout) findViewById(R.id.splash_layout);
        layout.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
               startActivity(new Intent(CatsSplashActivity.this, CatsMenuActivity.class));
               finish();
           }
        });
    }
}
