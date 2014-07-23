package org.kautsig.humbleshare;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.kautsig.humbleshare.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * The main activity.
 */
@SuppressLint("SimpleDateFormat")
public class HumbleShareActivity extends Activity {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isApplicationConfigured()) {
			openShareView();
		} else {
			openConfigrationView();
		}
	}

	/**
	 * Checks if the minimal of the application is available.
	 */
	private boolean isApplicationConfigured() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return prefs.getString(ConfigKeys.WEBSERVICE_URL, null) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_configure:
			openConfigrationView();
			return true;
		case R.id.action_share:
			openShareView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Opens the share view.
	 */
	private void openShareView() {
		setContentView(R.layout.main);
		initalizeMainView();
		handleIntent();
	}

	/**
	 * Handles the intent passed to the application. Updates text fields.
	 */
	private void handleIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
			// Retrieve the text from the shared intent
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

			// Write date/time information
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String currentDate = sdf.format(new Date());
			EditText datePicker = (EditText) findViewById(R.id.dateTimePicker);
			datePicker.setText(currentDate);

			// Write the shared text to the text field for further
			// modification
			EditText editText1 = (EditText) findViewById(R.id.contentText);
			editText1.setText(sharedText);
		}
	}

	/**
	 * Opens the configuration view.
	 */
	private void openConfigrationView() {
		setContentView(R.layout.configuration);
		initializeConfigView();

		// Read the configuration and set text field values
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		String wsUrl = prefs.getString(ConfigKeys.WEBSERVICE_URL, "");
		EditText hostText = (EditText) findViewById(R.id.hostText);
		hostText.setText(wsUrl);

		String wsUser = prefs.getString(ConfigKeys.WEBSERVICE_USER, "");
		EditText userText = (EditText) findViewById(R.id.userText);
		userText.setText(wsUser);

		String wsPass = prefs.getString(ConfigKeys.WEBSERVICE_PASSWORD, "");
		EditText passwordText = (EditText) findViewById(R.id.passwordText);
		passwordText.setText(wsPass);
	}

	/**
	 * Initializes the main views button listeners.
	 */
	private void initalizeMainView() {
		// Initialize spinner options
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		int types = R.array.share_types;
		int item = android.R.layout.simple_spinner_item;
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, types, item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// Add an event listener to the buttons
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(postButtonListener);

		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(clearButtonListener);

		// Empty fields, set current date
		resetInputFields();
	}

	/**
	 * Initializes the configuration views button listeners.
	 */
	private void initializeConfigView() {
		Button savePrefsButton = (Button) findViewById(R.id.savePrefsButton);
		savePrefsButton.setOnClickListener(this.savePrefsButtonListener);
	}

	/**
	 * Reads values from the text fields and posts the share.
	 */
	private void postShare() {
		EditText dateTimePicker = (EditText) findViewById(R.id.dateTimePicker);
		String date = dateTimePicker.getText().toString();

		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		String type = spinner1.getSelectedItem().toString();

		EditText contentText = (EditText) findViewById(R.id.contentText);
		String content = contentText.getText().toString();
		
		new PostShareTask(HumbleShareActivity.this).execute(type, date, content);
	}

	/**
	 * Empties the input fields and sets the current date.
	 */
	private void resetInputFields() {
		EditText dateTimePicker = (EditText) findViewById(R.id.dateTimePicker);
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		EditText editText1 = (EditText) findViewById(R.id.contentText);

		// Set to current date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String currentDate = sdf.format(new Date());
		dateTimePicker.setText(currentDate);
		
		// Set spinner to default selection
		spinner1.setSelection(0);

		// Clear the text
		editText1.setText("");
	}

	/**
	 * Reads values from the text fields and saves the configuration.
	 */
	private void saveConfiguration() {
		EditText hostText = (EditText) findViewById(R.id.hostText);
		EditText userText = (EditText) findViewById(R.id.userText);
		EditText passwordText = (EditText) findViewById(R.id.passwordText);

		String url = hostText.getText().toString();
		String user = userText.getText().toString();
		String password = passwordText.getText().toString();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(ConfigKeys.WEBSERVICE_URL, url);
		editor.putString(ConfigKeys.WEBSERVICE_USER, user);
		editor.putString(ConfigKeys.WEBSERVICE_PASSWORD, password);
		editor.commit();

		openShareView();
	}

	/**
	 * The post button listener.
	 */
	private OnClickListener postButtonListener = new OnClickListener() {
		public void onClick(View v) {
			postShare();
		}
	};

	/**
	 * The clear button listener.
	 */
	private OnClickListener clearButtonListener = new OnClickListener() {
		public void onClick(View v) {
			resetInputFields();
		}
	};

	/**
	 * Saves preference button listener.
	 */
	private OnClickListener savePrefsButtonListener = new OnClickListener() {
		public void onClick(View v) {
			saveConfiguration();
		}
	};

}
