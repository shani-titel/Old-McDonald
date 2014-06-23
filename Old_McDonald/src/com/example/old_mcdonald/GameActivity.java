package com.example.old_mcdonald;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
	final private int LOSE = 0;
	final private int MATCH_WIN = 1;
	final private int GAME_WIN = 2;
	final private int EASY_SEQ_LENGTH = 5;
	final private int MEDIUM_SEQ_LENGTH = 10;
	final private int HARD_SEQ_LENGTH = 15;
	
	private enum Animal {Cow, Pig, Sheep, Dog, Chicken, None};
	private String game_level;
	private List<Animal> deviceAnimalSeq;
	private List<Animal> userAnimalSeq;
	private boolean isUserTurn= false;
	private boolean isPlaying = false;
	private MediaPlayer mPlayer;
	private ImageView circle;
	private List<MediaPlayer> mpArray;
	
	private dbAdapter gameAdapter;
	private int gameID;
	private boolean isReplay;
	private int currPlayerMove;
	private List<Animal> deviceDBSeq;
	private List<Animal> userDBSeq;
	private int replay_i;
	
	private boolean backPressedFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 //remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_game);
		
		gameAdapter = new dbAdapter(this);
		
		game_level = new String();
		deviceAnimalSeq = new ArrayList<Animal>(); 
		userAnimalSeq = new ArrayList<Animal>(); 
		mPlayer = new MediaPlayer();
		mpArray = new ArrayList<MediaPlayer>(); 
		
		 //set custom font
        TextView txt = (TextView) findViewById(R.id.header);  
        Typeface font = Typeface.createFromAsset(getAssets(), "PermanentMarker.ttf");  
        txt.setTypeface(font); 
		
        game_level = getIntent().getStringExtra("level");
        
        gameID= getIntent().getIntExtra("gameId", -1);
        Log.i("info", "GameId = " + gameID);
        isReplay = true;
        currPlayerMove= 0;
        if(gameID == -1){ //is not replay
        	gameID = getGamesIDFromDB();
        	Log.i("info", "not replay. GameId = " + gameID);
        	isReplay = false;
        	currPlayerMove= -1;
        	gameAdapter.insertToGame(game_level);//add game to db
        	startGame();
        }
        else
        	replayGame();
	}
	
	private int replayPlayerTurn(){
		userAnimalSeq.clear();

		//prepare current sequence (DBseq --> userSeq)
		while(userDBSeq != null && currPlayerMove<userDBSeq.size() && !(userDBSeq.get(currPlayerMove).name().equals("None"))){
			Log.i("info",userDBSeq.get(currPlayerMove).name());
			Log.i("info", "WHILE- currPlayerMove= "+currPlayerMove);
			userAnimalSeq.add(userDBSeq.get(currPlayerMove)); //generate animal sequence
			currPlayerMove++;
			Log.i("info", "WHILE INC- currPlayerMove= "+currPlayerMove);
		}
		currPlayerMove++;
		Log.i("info", "END WHILE- currPlayerMove= "+currPlayerMove);
		replayAnimalSequence(userAnimalSeq, true);
		checkWinAndLevel();
		setFlowerVisibilty(true);
		
		return 0;//TODO
	}
	
	private int replayDeviceTurn(int i){
		
		if(deviceDBSeq != null && deviceDBSeq.size() > i){
			if(i==0){
				for(int j=0; j<getCurrentLevelSeqLength(game_level); j++){
					deviceAnimalSeq.add(deviceDBSeq.get(i)); //generate animal sequence
				}
			}
			else
				deviceAnimalSeq.add(deviceDBSeq.get(i));
			replayAnimalSequence(deviceAnimalSeq, false);
		}
		else
			Toast.makeText(this, "Finished replaying game", Toast.LENGTH_SHORT).show();
		return 0;//TODO
	}
	
	private void replayGame(){
		deviceDBSeq = getMovesFromDB(false);
		userDBSeq = getMovesFromDB(true);		
		replay_i = 0;
		
		replayDeviceTurn(replay_i);
	}
	
	private void startGame(){				
		userAnimalSeq.clear();
		for(int i=0; i<getCurrentLevelSeqLength(game_level); i++){
			addAnimalToDeviceSequence(); //generate animal sequence
		}
		playAnimalSequence(deviceAnimalSeq);//play sound sequence + show circle play(sequence)
	}
	
	private List<Animal> getMovesFromDB(boolean isPlayer) {
		Cursor cursor;
		List<Animal> values = new ArrayList<Animal>();
		if(isPlayer)
			cursor = gameAdapter.getMovesFromPlayerByID(gameID);
		else
			cursor = gameAdapter.getMovesFromDeviceByID(gameID);
		int count;
		if(cursor == null)
			count = 0;
		else
			count = cursor.getCount();
		if(count != 0){
			cursor.moveToFirst();
			for(int i=0; i<count;i++){
				values.add(Animal.valueOf(cursor.getString(0)));
				cursor.moveToNext();
			}
			cursor.close();
			return values;
		}
		else{
			cursor.close();
			return null;
		}
	}
	
	private int getCurrentLevelSeqLength(String level){
		if(level.equalsIgnoreCase("easy"))
			return  1;
		else if(level.equalsIgnoreCase("medium"))
			return EASY_SEQ_LENGTH;
		else //*if(level.equalsIgnoreCase("hard"))*/
			return MEDIUM_SEQ_LENGTH;
	}
	
	private int checkWinAndLevel(){
		if(userAnimalSeq.size() == deviceAnimalSeq.size()){
			for(int i=0; i<userAnimalSeq.size(); i++){//check that all user animals are equal to device by order
				if(!userAnimalSeq.get(i).equals(deviceAnimalSeq.get(i))){
					showCustomDialog(LOSE);
					return LOSE; 
				}
			}
			//check level
			if(game_level == "easy" && userAnimalSeq.size() == EASY_SEQ_LENGTH){
				game_level = "medium";
			}
			else if(game_level == "medium" && userAnimalSeq.size() == MEDIUM_SEQ_LENGTH){
				game_level = "hard";
			}
			else if(game_level == "hard" && userAnimalSeq.size() == HARD_SEQ_LENGTH){
				showCustomDialog(GAME_WIN);
				return GAME_WIN;
			}
			Toast.makeText(getApplicationContext(), "You did it!", Toast.LENGTH_SHORT).show();
			return MATCH_WIN;
		}
		else{
			showCustomDialog(LOSE);
			return LOSE;
		}
	}
	
	private Animal findAnimalByView(View view){
		Animal animal = null;
		switch(view.getId()){
		case R.id.cow:
			animal = Animal.Cow;
			break;
		case R.id.pig:
			animal = Animal.Pig;
			break;
		case R.id.chicken:
			animal = Animal.Chicken;
			break;
		case R.id.dog:
			animal = Animal.Dog;
			break;
		case R.id.ship:
			animal = Animal.Sheep;
			break;
		}
		return animal;
	}
	
	private int findSoundIdByAnimal(Animal animal){
		int soundId=0;
		switch(animal){
			case Chicken:
				soundId = R.raw.cockrel_crow;
				break;
			case Cow:
				soundId = R.raw.cow_moo;
				break;
			case Dog:
				soundId = R.raw.dog_bark;
				break;
			case Pig:
				soundId = R.raw.pig_grunt;
				break;
			case Sheep:
				soundId = R.raw.sheep_bleat;
				break;
		}
		return soundId;
	}
	
	private int findCircleIdByAnimal(Animal animal){
		int circleId=0;
		switch(animal){
			case Chicken:
				circleId = R.id.chicken_circle;
				break;
			case Cow:
				circleId = R.id.cow_circle;
				break;
			case Dog:
				circleId = R.id.dog_circle;
				break;
			case Pig:
				circleId = R.id.pig_circle;
				break;
			case Sheep:
				circleId = R.id.sheep_circle;
				break;
		}
		return circleId;
	}
	
	private void playAnimal(Animal animal){
		if(!isPlaying){
			int soundId=0, circleId=0;
			soundId = findSoundIdByAnimal(animal);
			circleId = findCircleIdByAnimal(animal);
			
			circle = (ImageView) findViewById(circleId);
			circle.setVisibility(View.VISIBLE);
			mPlayer = MediaPlayer.create(this, soundId);
			isPlaying = true;
			mPlayer.start();
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					isPlaying = false;
					mp.stop();
					mp.release();
					circle.setVisibility(View.INVISIBLE);  
					Log.i("info","player completed. is playing = false");
				}
			});
		}
	
	}
	
	public void onAnimalClick(View clicked){
		if(isUserTurn && !isPlaying && !isReplay){
			Animal animal = findAnimalByView(clicked);
			playAnimal(animal);
			userAnimalSeq.add(animal);
			if(!isReplay)
				gameAdapter.insertToPlayer(gameID, animal.name());
		}
	}
	
	public void onFlowerClick(View v){
		if(isUserTurn && !isPlaying && !isReplay){
			gameAdapter.insertToPlayer(gameID, "None");
			int result = checkWinAndLevel();
			if(result == MATCH_WIN){
				isUserTurn = false;
				setFlowerVisibilty(false);
				Log.i("info","user was right, now display dialog");
				startGame();	
			}
		}
	}

	
	private void replayAnimalSequence(List<Animal> sequence, final boolean isPlayerTurn){

			Log.i("info","inside play device sequence");
			mpArray.clear();
			if(isPlayerTurn == false)//device is about to play
				setFlowerVisibilty(false);
			for(int i=0; i<sequence.size(); i++){//populate array with sounds according to sequence array
				MediaPlayer mp = MediaPlayer.create(this, findSoundIdByAnimal(sequence.get(i)));			
				mpArray.add(mp);
			}
			Log.i("info","finished creating sounds");
			for(int i=0; i<sequence.size()-1; i++){ //set next mediaPlayers
				mpArray.get(i).setNextMediaPlayer(mpArray.get(i+1));
			}
			Log.i("info","finished setting nextMediaPlayer");		
			
			for(int i=0; i<sequence.size(); i++){
				Log.i("info","about to play "+sequence.get(i)+" sound");
				
				if(i == sequence.size()-1){ //last player needs to show flower button
					mpArray.get(i).setOnCompletionListener(new OnCompletionListener() {
								
						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.stop();
							mp.release();
							if(isPlayerTurn == false){//device finished playing
								if(!backPressedFlag)
									replayPlayerTurn();//launch player sequence
							}
							else{//player finished playing
								if(!backPressedFlag)
									replayDeviceTurn(++replay_i);//launch device sequence
							}
						}
					});
				}
				else{
					mpArray.get(i).setOnCompletionListener(new OnCompletionListener() {
						
						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.stop();
							mp.release();
						}
					});
				}
			}
			if(mpArray.size() > 0)
				mpArray.get(0).start();//actually and finally playyyy
	}
	
	private void playAnimalSequence(List<Animal> sequence){

		Log.i("info","inside play device sequence");
		mpArray.clear();
		for(int i=0; i<sequence.size(); i++){//populate array with sounds according to sequence array
			MediaPlayer mp = MediaPlayer.create(this, findSoundIdByAnimal(sequence.get(i)));			
			mpArray.add(mp);
		}
		Log.i("info","finished creating sounds");
		for(int i=0; i<sequence.size()-1; i++){ //set next mediaPlayers
			mpArray.get(i).setNextMediaPlayer(mpArray.get(i+1));
		}
		Log.i("info","finished setting nextMediaPlayer");		
		
		for(int i=0; i<sequence.size(); i++){
			Log.i("info","about to play "+sequence.get(i)+" sound");
			
			if(i == sequence.size()-1){ //last player needs to show flower button
				mpArray.get(i).setOnCompletionListener(new OnCompletionListener() {
							
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.stop();
						mp.release();
						isUserTurn = true;
						setFlowerVisibilty(true);
					}
				});
			}
			else{
				mpArray.get(i).setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.stop();
						mp.release();
					}
				});
			}
		}
		mpArray.get(0).start();//actually and finally playyyy
}
	
	private void addAnimalToDeviceSequence(){
		Random rand = new Random();
		Animal a = Animal.values()[rand.nextInt(Animal.values().length)];
		deviceAnimalSeq.add(a);
		if(!isReplay)
			gameAdapter.insertToDevice(gameID, a.name());
		Log.i("info",a+" added to device sequence and DB.");
	}
	
	private int getGamesIDFromDB() {
		Cursor cursor = gameAdapter.getAllFromGame();
		int count;
		if(cursor == null)
			count = 0;
		else
			count = cursor.getCount();
		cursor.close();
		return ++count;
	}
	
	private void setFlowerVisibilty(boolean visible){
		Button flower = (Button)findViewById(R.id.flower);
		if(visible)
			flower.setVisibility(View.VISIBLE);
		else
			flower.setVisibility(View.INVISIBLE);
	}

	
	public void onExitToMain(View v){
		mPlayer.release();
		gameAdapter.close();
		for(int i=0; i< mpArray.size(); i++){
			mpArray.get(i).release();
		}
		Intent intent = new Intent(this, MainActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
	    startActivity(intent);
	}
	
	private void showCustomDialog(int state){
		int layout;
		Log.i("info","inside showCustomDialog.");
		if(state == LOSE)
			layout = R.layout.lose_dialog_layout;
		else
			layout = R.layout.win_dialog_layout;
		
		Log.i("info","layout = "+layout);
		Dialog dialog = new Dialog(this){
        	@Override
        	public boolean onTouchEvent(MotionEvent event) {
            // Tap anywhere to close dialog.
            onExitToMain(new View(getApplicationContext()));
            return true;
          }
        };
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.show();
	}
	
	@Override
    public void onBackPressed(){
		backPressedFlag = true;
		super.onBackPressed();
    }
	
	@Override 
    public void onPause(){
        super.onPause();
        if(!backPressedFlag){//HOME pressed
        	gameAdapter.close();
        	finish();
        }
        backPressedFlag = false;
    }

}
