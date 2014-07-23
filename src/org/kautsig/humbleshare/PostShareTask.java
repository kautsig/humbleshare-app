package org.kautsig.humbleshare;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

/**
 * Task for handling webservice request asynchronously.
 */
class PostShareTask extends AsyncTask<String, Void, String> {

	/** The logger tag. */
	private static final String TAG = "HumbleShareActivity";

	/** The main activity instance. */
	private final HumbleShareActivity genericShareActivity;

	/**
	 * Constructor.
	 *
	 * @param genericShareActivity
	 *            the main activity
	 */
	public PostShareTask(HumbleShareActivity genericShareActivity) {
		this.genericShareActivity = genericShareActivity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String doInBackground(String... params) {
		return requestWebService(params[0], params[1], params[2]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(genericShareActivity.getApplicationContext(), result, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Sends the texts to the configured webservice.
	 *
	 * @param type
	 *            the share type
	 * @param timestamp
	 *            the timestamp
	 * @param sharedText
	 *            the text to share
	 */
	protected String requestWebService(String type, String timestamp, String sharedText) {
		try {
			// Read the configuration
			Context context = genericShareActivity.getApplicationContext();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String wsUrl = prefs.getString(ConfigKeys.WEBSERVICE_URL, "");
			String wsUser = prefs.getString(ConfigKeys.WEBSERVICE_USER, "");
			String wsPass = prefs.getString(ConfigKeys.WEBSERVICE_PASSWORD, "");

			// Construct the JSON String
			Map<String, String> comment = new HashMap<String, String>();
			comment.put("type", type);
			comment.put("timestamp", timestamp);
			comment.put("content", sharedText);
			String json = new GsonBuilder().create().toJson(comment, Map.class);

			// Construct the http request
			HttpPost httpPost = new HttpPost(wsUrl);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			byte[] utf8JsonString = json.getBytes("UTF8");
			httpPost.setEntity(new ByteArrayEntity(utf8JsonString));

			// Construct the client and execute the request
			URI uri = new URI(wsUrl);

			// Remind: The trusting client falls back to default behavior if no keystore is found
			TrustingHttpClient client = new TrustingHttpClient(genericShareActivity.getApplicationContext());

			// Optional basic authentication
			if (!TextUtils.isEmpty(wsUser) && !TextUtils.isEmpty(wsPass)) {
				AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_SCHEME);
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(wsUser, wsPass);
				client.getCredentialsProvider().setCredentials(authScope, credentials);
			}

			// Execute the request
			HttpResponse response = client.execute(httpPost);

			// Provide user feedback based on the http status code
			if (response.getStatusLine().getStatusCode() != 201) {
				// In case of error, let the user know the response status code
				return "Server responded with status code " + response.getStatusLine().getStatusCode();
			} else {
				// If id did work so far, we show a success message
				return "Shared item successfully.";
			}

		} catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage(), ioe);
			return ioe.getMessage();
		} catch (URISyntaxException use) {
			Log.e(TAG, use.getMessage(), use);
			return use.getMessage();
		}
	}
}
