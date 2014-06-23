package com.example.old_mcdonald;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dbHelper extends SQLiteOpenHelper {
	
	public dbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	private static final String DATABASE_NAME = "oldMacdonaldDB";

	private static final int DATABASE_VERSION = 1;

	// table creation SQL statement
	private static final String GAME_TABLE_CREATE = 
			"create table game (_id integer primary key autoincrement, level integer not null)";
	
	private static final String DEVICE_TABLE_CREATE = 
			"create table device (_id integer primary key autoincrement, " +
			"gameId integer not null, animal TEXT not null)";
	
	private static final String PLAYER_TABLE_CREATE = 
			"create table player (_id integer primary key autoincrement, " +
			"gameId integer not null, animal TEXT not null)";

	// Called on database creation
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(GAME_TABLE_CREATE);
		Log.i("info","CREATED GAME TABLE");
		database.execSQL(DEVICE_TABLE_CREATE);
		Log.i("info","CREATED DEVICE TABLE");
		database.execSQL(PLAYER_TABLE_CREATE);
		Log.i("info","CREATED PLAYER TABLE");
	}
	// Called on database upgrade, e.g. increasing the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
