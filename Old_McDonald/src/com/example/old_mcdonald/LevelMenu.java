package com.example.old_mcdonald;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class LevelMenu extends Activity {

	private boolean backPressedFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 //remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_level_menu);
		
		 //set custom font
        TextView txt = (TextView) findViewById(R.id.header);  
        Typeface font = Typeface.createFromAsset(getAssets(), "PermanentMarker.ttf");  
        txt.setTypeface(font); 
        TextView txt2 = (TextView) findViewById(R.id.new_game_header);  
        txt2.setTypeface(font); 
	}
	
	public void onLevelChosen(View v){
		 Intent myIntent = new Intent(LevelMenu.this, GameActivity.class);
		 myIntent.putExtra("level", levelRadioViewToString(v));
		 myIntent.putExtra("gameId", -1);
	     this.startActivity(myIntent);
	}
	
	private String levelRadioViewToString(View v){
		String game_level = new String();
		switch(v.getId()){
		 case R.id.easy:
			 game_level = "easy";
			 break;
		 case R.id.medium:
			 game_level = "medium";
			 break;
		 case R.id.hard:
			 game_level = "hard";
			 break;
		 }
		return game_level;
	}
	
	@Override
    public void onBackPressed(){
		backPressedFlag = true;
		super.onBackPressed();
    }
	
	@Override 
    public void onPause(){
        super.onPause();
        if(!backPressedFlag){
        	MainActivity.mp.stop();
        	finish();
        }
        backPressedFlag = false;
    }

}
