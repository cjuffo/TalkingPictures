package edu.miami.c09879016.projtwo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemLongClickListener,
		SimpleCursorAdapter.ViewBinder, TextToSpeech.OnInitListener,
		OnDismissListener {

	private static final int ADD_TEXT = 111;

	private ListView rowlist;
	private AlertDialog theDialog;
	private View dialogView;
	private TextToSpeech mySpeaker;
	private MySQLiteDataBase imageDB;
	private Cursor dbCursor;
	public String NoDescription = "No Description Set";
	private int current_id;
	private Cursor mediaCursor;
	private Cursor songCursor;
	private MediaPlayer mPlayer;
	public String audioFilename;
	public String fileTitle;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
//----Set Content View for MainActivity - activity_main.xml
		setContentView(R.layout.activity_main);

		imageDB = new MySQLiteDataBase(this);
		mySpeaker = new TextToSpeech(this, this);
		rowlist = (ListView) findViewById(R.id.the_list);
		
//----The following three methods are responsible for playing random song, generating database, populating the list
		runMediaPLayer();
		updateDB();
		generateList();
			Log.i("IN onCreate", "Created OK");

	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	
	public void onInit(int status) {

		Log.i("IN onInit", "Created OK");
		if (status == TextToSpeech.SUCCESS
				&& mySpeaker.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
			Toast.makeText(this, "TextSpeech is Ready", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Must Install Text To Speech",
					Toast.LENGTH_LONG).show();
			finish();
		}
//---- Toast to output the title of the song that is currently playing
		Toast.makeText(this, "Playing: " + fileTitle, Toast.LENGTH_LONG).show();
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("IN onDestroy", "Destroying");
		super.onDestroy();
		mPlayer.release();
		mediaCursor.close();
		songCursor.close();
		dbCursor.close();
		imageDB.close();
		mySpeaker.shutdown();
		finish();

	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	public void generateList() {
		SimpleCursorAdapter cursorAdapter;
		String[] displayFields = { "uri", "text_description" };
		int[] displayViews = { R.id.uri, R.id.text_description };
			Log.i("IN generateList", "GETCURSOR");
		mediaCursor = imageDB.getCursor();
			Log.i("IN generateList", "GETCURSORDONE");
		rowlist = (ListView) findViewById(R.id.the_list);
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.rowlayout,
				mediaCursor, displayFields, displayViews);
		Log.i("IN onCreate", "SIMPLECURSOROKAY");
		cursorAdapter.setViewBinder(this);
		rowlist.setAdapter(cursorAdapter);

//----Sets on item click listener. When item is clicked, it pauses the song, and shows dialog corresponding to the id of list
		rowlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mPlayer.pause();
				showDialog((int) id);

				// Bitmap will be used to determine if the URI at that location
				// in database is valid or not
				// BitmapFactor.decodeFile takes in a string path and sets it to
				// bp
				// if bp is still null, that means the path name is invalid
				// thus, I then delete that item from the database.
				// Referenced from stack overflow
				Bitmap bp = null;
				bp = BitmapFactory.decodeFile(imageDB.getURI((int) id));
				if (bp == null) {
					imageDB.deleteItemFromList(id);
					generateList();
				} else
					bp = null;
			}
		});
		rowlist.setOnItemLongClickListener(this);
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case ADD_TEXT:
//---- If return intent sends RESULT_OK, this restarts player, gets string sent back, and updates database if user did not click cancel
			if (resultCode == Activity.RESULT_OK) {

				mPlayer.start();

				String description_new;

				description_new = data.getStringExtra("description");

				if (description_new.equals("CANCELCANCELCANCELCANCEL")) {

					// do nothing- user has selected cancel
					// nothing has changed
				}

				else {
					//if user clicks save, and text was blank, reset to No Description Set
					if (description_new.equals(""))
						description_new = NoDescription;
					//----updates database at that specific position with new text description
					imageDB.updateText(description_new, current_id);
					
					//renew list with new text description
					generateList();
				}

			}
			else
				finish();

			break;
		}
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		return (false);
		

	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.dismiss_dialog:
			theDialog.dismiss();
			if (mySpeaker.isSpeaking())
				mySpeaker.stop();
			mPlayer.start();
			break;
		default:
			// do nothing
			break;
		}
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	protected Dialog onCreateDialog(int dialogId) {

		AlertDialog.Builder dialogBuilder;
		LayoutInflater dialogInflator;
		ImageView dia_view;
		dialogBuilder = new AlertDialog.Builder(this);
		dialogInflator = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		dialogView = dialogInflator.inflate(
				R.layout.ui_custom_dialog_example_dialog_layout,
				(ViewGroup) findViewById(R.id.dialog_root));
		dia_view = (ImageView) dialogView.findViewById(R.id.dialog_pic);
		dia_view.setImageURI(Uri.parse(imageDB.getURI(dialogId)));

		dialogBuilder.setView(dialogView);
		theDialog = dialogBuilder.create();
		theDialog.setOnDismissListener(this);

		return (theDialog);
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	@Override
	protected void onPrepareDialog(int dialogId, Dialog dialog) {
		ImageView dia_view;
		dia_view = (ImageView) dialogView.findViewById(R.id.dialog_pic);
		dia_view.setImageURI(Uri.parse(imageDB.getURI(dialogId)));
		speakFileName(imageDB.getTeDes(dialogId));
		super.onPrepareDialog(dialogId, dialog);
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	public void speakFileName(String whatToSay) {
		HashMap<String, String> speechParameters;
		if (whatToSay != null && whatToSay.length() > 0) {
			speechParameters = new HashMap<String, String>();
			speechParameters.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
					"WHAT_I_SAID");
			mySpeaker.speak(whatToSay, TextToSpeech.QUEUE_FLUSH,
					speechParameters);
		}
	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	

	public void runMediaPLayer() {

		int audioDataIndex;
		int titleIndex;
		int num_of_songs;
		int random_song;
		
		String[] queryFields = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA };

		songCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				queryFields, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//------Gets total count of all songs accessed by cursor
		num_of_songs = songCursor.getCount();
		Random rand = new Random();
//------Generates random number from 0 to size of num_of_songs
		random_song = rand.nextInt(num_of_songs);
//------ If there is one or more songs, we play a random one. Otherwise, play the default raw file
		if (num_of_songs > 0) {
			audioDataIndex = songCursor
					.getColumnIndex(MediaStore.Audio.Media.DATA);

			titleIndex = songCursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE);

			songCursor.moveToPosition(random_song);
			fileTitle = songCursor.getString(titleIndex);
			audioFilename = songCursor.getString(audioDataIndex);
			try {
				mPlayer = new MediaPlayer();
				mPlayer.setDataSource(audioFilename);
				mPlayer.prepare();
				mPlayer.setLooping(true);
				mPlayer.start();
			} catch (IOException e) {
				// do nothing
			}

		} else {
			mPlayer = new MediaPlayer();
			mPlayer = MediaPlayer.create(this, R.raw.pyt);
			mPlayer.setLooping(true);
			mPlayer.start();
		}

	}

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	private void updateDB() {

		int index;
		
		String[] queryFields = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };

		dbCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				queryFields, null, null,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);

		if (dbCursor.moveToFirst());

		do {
			index = dbCursor.getColumnIndex(MediaStore.Images.Media.DATA);

			ContentValues values = new ContentValues();
			String file = (dbCursor.getString(index));
			values.put("uri", file);
			values.put("text_description", NoDescription);
			imageDB.addToDB(values);
		} while (dbCursor.moveToNext());

	}

	// -----------------------------------------------------------------------------

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long rowId) {
//------Pause the song, set the global id variable to whatever rowID was that was clicked. Pass uri(string) bungle to description activity
		mPlayer.pause();
		current_id = (int) rowId;
		String tempUri = imageDB.getURI(current_id);
		Intent nexAct = new Intent();
		nexAct.setClassName("edu.miami.c09879016.projtwo",
				"edu.miami.c09879016.projtwo.AddDescription");

		nexAct.putExtra("URI", tempUri);
		startActivityForResult(nexAct, ADD_TEXT);

		return false;

	}

	@Override
	public void onDismiss(DialogInterface arg0) {

		theDialog.dismiss();
		if (mySpeaker.isSpeaking())
			mySpeaker.stop();
		mPlayer.start();

	}

}
// =============================================================================