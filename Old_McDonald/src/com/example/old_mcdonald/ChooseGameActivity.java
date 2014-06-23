package com.example.old_mcdonald;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseGameActivity extends Activity implements OnItemClickListener{
	
	private boolean backPressedFlag = false;
	private dbAdapter adapter;
	private ListView listView ;
	  
	//TODO open game activity + close adapter
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //remove title
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        
	        setContentView(R.layout.activity_choose_game);
	        
	        //set custom font
	        TextView txt = (TextView) findViewById(R.id.header);  
	        TextView txt2 = (TextView) findViewById(R.id.choose_game_header);  
	        Typeface font = Typeface.createFromAsset(getAssets(), "PermanentMarker.ttf");  
	        txt.setTypeface(font);
	        txt2.setTypeface(font);
	        
	        adapter = new dbAdapter(this);
	        
	        createList();
	 }
	
	 private void createList() {
		  // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        // Defined Array values to show in ListView
        String[] values = getGamesFromDB();

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        if(values == null){
        	values = new String[1];
        	values[0] = "No Games";
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(this);
		
	}
	private String[] getGamesFromDB() {
		Cursor cursor = adapter.getAllFromGame();
		int count;
		if(cursor == null)
			count = 0;
		else
			count = cursor.getCount();
		if(count != 0){
			String[] values =  new String[count];
			for(int i=0; i<count;i++){
				values[i] = String.valueOf(i+1);
			}
			cursor.close();
			return values;
		}
		else{
			cursor.close();
			return null;
		}
	}
	
	private String getGameLevelByID(int id){
		Cursor cursor = adapter.getLevelGameById(id);
		int count;
		if(cursor == null)
			count = 0;
		else
			count = cursor.getCount();
		if(count != 0){
			cursor.moveToFirst();
			String result = cursor.getString(0);
			cursor.close();
			return result;
		}
		cursor.close();
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent myIntent = new Intent(ChooseGameActivity.this, GameActivity.class);
		//String  gameId = (String) listView.getItemAtPosition(position);
		myIntent.putExtra("gameId", position+1);
		myIntent.putExtra("level", getGameLevelByID(position+1));
		adapter.close();
	    this.startActivity(myIntent);
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
