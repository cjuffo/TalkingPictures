package edu.miami.c09879016.projtwo;

import edu.miami.c09879016.projtwo.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//=============================================================================
public class AddDescription extends Activity {
	// -----------------------------------------------------------------------------

	// -----------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_and_display);

		String uriString = this.getIntent().getStringExtra("URI");

		Uri myUri = Uri.parse(uriString);
		ImageView pictureView;

		pictureView = (ImageView) findViewById(R.id.selected_picture);
		pictureView.setImageURI(myUri);

	}

	// -----------------------------------------------------------------------------

	public void myClickHandler(View view) {

		Intent retIntent;
		String newtext;
		EditText descrip_text = ((EditText) findViewById(R.id.descrip));

		switch (view.getId()) {
		case R.id.save_it:
			retIntent = new Intent();
			newtext = descrip_text.getText().toString();
			retIntent.putExtra("description", newtext);
			setResult(RESULT_OK, retIntent);
			finish();
			break;
			
		case R.id.cancel_it:
			retIntent = new Intent();
			newtext = "CANCELCANCELCANCELCANCEL";
			retIntent.putExtra("description", newtext);
			setResult(RESULT_OK, retIntent);
			finish();
			break;

		default:
			break;
		}
	}

}
// =============================================================================