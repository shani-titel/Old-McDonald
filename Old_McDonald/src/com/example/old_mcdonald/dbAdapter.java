package com.example.old_mcdonald;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class dbAdapter {
	// Database fields
		public static final String KEY_ROWID = "_id";
		public static final String KEY_LEVEL = "level";
		public static final String KEY_ANIMAL = "animal";
		private static final String PLAYER_TABLE = "player";
		private static final String DEVICE_TABLE = "device";
		private static final String GAME_TABLE = "game";
		public static final String KEY_GAME = "gameId";
		private Context context;
		private SQLiteDatabase database;
		private dbHelper dbHelper;

		public dbAdapter(Context context) {
			this.context = context;
			open();
		}

		public dbAdapter open() throws SQLException {
			dbHelper = new dbHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
		}
		
		// Put String values into a Content object
		private ContentValues createGameContentValues(String level) {
			ContentValues values = new ContentValues();
			values.put(KEY_LEVEL, level);
			return values;
		}
		
		private ContentValues createMoveContentValues(int gameId, String animal) {
			ContentValues values = new ContentValues();
			values.put(KEY_ANIMAL, animal);
			values.put(KEY_GAME, gameId);
			return values;
		}
		
		// Create a new table. If the table is successfully created return the new
		// rowId for that note, otherwise return a -1 to indicate failure.
		public long insertToGame(String level) {
			ContentValues initialValues = createGameContentValues(level);

			return database.insert(GAME_TABLE, null, initialValues);
		}
		
		public long insertToPlayer(int gameId, String animal) {
			ContentValues initialValues = createMoveContentValues(gameId, animal);

			return database.insert(PLAYER_TABLE, null, initialValues);
		}
		
		public long insertToDevice(int gameId, String animal) {
			ContentValues initialValues = createMoveContentValues(gameId, animal);

			return database.insert(DEVICE_TABLE, null, initialValues);
		}
		
		//Return a Cursor over the list of all games in game table
		public Cursor getAllFromGame() {			
			String query = "SELECT  * FROM " + GAME_TABLE;
		    return database.rawQuery(query, null);
		}
		
		//Return a Cursor over the list of all games in game table
		public Cursor getAllFromPlayer() {
			String query = "SELECT  * FROM " + PLAYER_TABLE;
		    return database.rawQuery(query, null);
		}
		
		//Return a Cursor over the list of all games in game table
		public Cursor getAllFromDevice() {
			String query = "SELECT  * FROM " + DEVICE_TABLE;
		    return database.rawQuery(query, null);
		}
		
		//Return a Cursor over the list of all games in game table
		public Cursor getMovesFromPlayerByID(int id) {
			String query = "SELECT " + KEY_ANIMAL + " FROM " + PLAYER_TABLE + " WHERE " + id + "= " + KEY_GAME;
		    return database.rawQuery(query, null);
		}
		
		//Return a Cursor over the list of all games in game table
		public Cursor getMovesFromDeviceByID(int id) {
			String query = "SELECT " + KEY_ANIMAL + "  FROM " + DEVICE_TABLE + " WHERE " + id + "= " + KEY_GAME;
		    return database.rawQuery(query, null);
		}
		
		//	Return a Cursor positioned at the defined todo
		public Cursor selectGameById(long rowId) throws SQLException {
			Cursor mCursor = database.query(true, GAME_TABLE, new String[] {
					KEY_ROWID, KEY_LEVEL },
					KEY_ROWID + "=" + rowId, null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}
		
		public Cursor getLevelGameById(long rowId) throws SQLException {
			
			String query = "SELECT " + KEY_LEVEL + " FROM " + GAME_TABLE + " WHERE " + rowId + "= " + KEY_ROWID;
			return database.rawQuery(query, null);
		}
	}
