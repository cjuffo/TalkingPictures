package edu.miami.c09879016.projtwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteDataBase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "imagesDB";
	private static final String TABLE_IMAGEINFO = "images";
	private static final String KEY_ID = "_id";
	private static final String KEY_URI = "uri";
	private static final String KEY_TEXT = "text_description";

	public MySQLiteDataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_IMAGEINFO + "("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_URI
				+ " TEXT UNIQUE, " + KEY_TEXT + " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGEINFO);
		onCreate(db);
	}

	// -----------------------------------------------------------------------------
	public boolean deleteItemFromList(long songID) {
		SQLiteDatabase db = this.getWritableDatabase();
		return (db.delete(TABLE_IMAGEINFO, "_id =" + songID, null) > 0);
	}

	// -----------------------------------------------------------------------------

	// Adding to database
	public void addToDB(ContentValues info) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.insert(TABLE_IMAGEINFO, null, info);
	}

	// Updating text of description at specified position
	public int updateText(String NewDesc, int ID) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TEXT, NewDesc);
		// updating row
		return db.update(TABLE_IMAGEINFO, values, KEY_ID + " = ?",
				new String[] { String.valueOf(ID) });
	}

	// Getting URI
	String getURI(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_IMAGEINFO, new String[] { KEY_ID,
				KEY_URI, KEY_TEXT }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		return (cursor.getString(1));
	}

	// -----------------------------------------------------------------------------
	public ContentValues DataFromCursor(Cursor cursor) {

		String[] fieldNames;
		int index;
		ContentValues imgData;

		if (cursor != null && cursor.moveToFirst()) {
			fieldNames = cursor.getColumnNames();
			imgData = new ContentValues();
			for (index = 0; index < fieldNames.length; index++) {
				if (fieldNames[index].equals("_id")) {
					imgData.put("_id", cursor.getInt(index));
				} else if (fieldNames[index].equals("uri")) {
					imgData.put("uri", cursor.getInt(index));
				} else if (fieldNames[index].equals("text_description")) {
					imgData.put("text_description", cursor.getString(index));
				}
			}
			return (imgData);
		} else {
			return (null);
		}
	}

	// Getting Text Description - Returns String of text description at specified position
	String getTeDes(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_IMAGEINFO, new String[] { KEY_ID,
				KEY_URI, KEY_TEXT }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return (cursor.getString(2));
	}

	// =============================================================================
	public Cursor getCursor() {
		Log.i("IN onCreate", "GETCURSORdb1");
		String[] fieldNames = { "_id", "uri", "text_description" };
		SQLiteDatabase db = this.getReadableDatabase();
		Log.i("IN onCreate", "GETCURSORdb2");
		return (db.query(TABLE_IMAGEINFO, fieldNames, null, null, null, null,
				"_id"));
	}

}
// =============================================================================
