package com.example.old_mcdonald;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static MediaPlayer mp;
	private boolean activitySwitchFlag = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);
        
        //set custom font
        TextView txt = (TextView) findViewById(R.id.header);  
        Typeface font = Typeface.createFromAsset(getAssets(), "PermanentMarker.ttf");  
        txt.setTypeface(font);
        
        //play app music
        mp = MediaPlayer.create(getApplicationContext(), R.raw.theme1);
        mp.setLooping(true);
        mp.start();
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();
			}
		});
    }
    
    public void onNewGame(View view){
    	Intent myIntent = new Intent(this, LevelMenu.class);
    	activitySwitchFlag = true;
    	this.startActivity(myIntent);
    }
    
    public void onReplayGame(View view){
    	Intent intent = new Intent(this, ChooseGameActivity.class);
    	activitySwitchFlag = true;
    	this.startActivity(intent);
    }
    
    
    @Override
    public void onBackPressed(){
    	mp.stop();
    	finish();
    }
    
    @Override 
    public void onPause(){
    	Log.i("info","*** inside onPause ***");
        super.onPause();
        if(!activitySwitchFlag){
        	mp.stop();
        	finish();
        }
        activitySwitchFlag = false;
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(!mp.isPlaying())
    		mp.start();
    }
}
