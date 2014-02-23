package com.miz.traktr.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Helper class to make certain operations easier.
 * Static calls are faster on Android, so methods
 * in this helper class should be used when possible.
 * @author Michell
 *
 */
public class Helper {

	// Account preferences
	public static final String PREF_ACCOUNT_USERNAME = "accountUsername";
	public static final String PREF_ACCOUNT_PASS_SHA1 = "accountPassSHA1";
	public static final String PREF_ACCOUNT_REALNAME = "accountRealname";

	// Trakt API key
	public static final String TRAKT_API = "37e8c23aef817e0feb2c83a1a01e2f3953b5fb15";

	/**
	 * Determines if an account exists.
	 * @param context Context used to get the default shared preferences.
	 * @return True if an account exists, false if not.
	 */
	public static boolean hasAccount(Context context) {
		return !PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "").isEmpty() && 
				!PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "").isEmpty();
	}

	/**
	 * Determines if the device is currently connected to a network
	 * @param c - Context of the application
	 * @return True if connected to a network, else false
	 */
	public static boolean isOnline(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		int count = netInfo.length;
		for (int i = 0; i < count; i++)
			if (netInfo[i] != null && netInfo[i].isConnected()) return true;
		return false;
	}

	/**
	 * Attempt to log in on Trakt. If the log in succeeds, the credentials will be stored.
	 * @param username
	 * @param password SHA-1 encoded password.
	 * @return True if log in succeeded, false if not.
	 */
	public static boolean login(Context context, String username, String password) {
		boolean success = false;

		// Let's first attempt to log in with the supplied details
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://api.trakt.tv/account/test/" + TRAKT_API);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String html = httpclient.execute(httppost, responseHandler);

			JSONObject jObject = new JSONObject(html);

			String status = jObject.getString("status");
			success = status.equals("success");

		} catch (Exception e) {
			success = false;
		}

		if (success) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(PREF_ACCOUNT_USERNAME, username);
			editor.putString(PREF_ACCOUNT_PASS_SHA1, password);
			editor.commit();

			httpclient = new DefaultHttpClient();
			httppost = new HttpPost("http://api.trakt.tv/user/profile.json/" + TRAKT_API + "/" + username);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("username", username));
				nameValuePairs.add(new BasicNameValuePair("password", password));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String html = httpclient.execute(httppost, responseHandler);

				JSONObject jObject = new JSONObject(html);

				if (jObject.has("full_name")) {
					String name = jObject.getString("full_name");
					if (name.equals("null") || name.isEmpty())
						name = "";

					editor.putString(PREF_ACCOUNT_REALNAME, name);
				} else {
					editor.putString(PREF_ACCOUNT_REALNAME, "");
				}
				editor.commit();

			} catch (Exception e) {
				System.out.println("ERROR: " + e);
				success = false;
			}
		}

		return success;
	}

	/**
	 * Attempts to create an account given a user name, password and e-mail.
	 * @param context
	 * @param username
	 * @param password SHA-1 encoded password.
	 * @param email
	 * @return True if successful, false otherwise.
	 */
	public static boolean createAccount(Context context, String username, String password, String email) {
		boolean success = false;

		// Let's first attempt to log in with the supplied details
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://api.trakt.tv/account/create/" + TRAKT_API);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String html = httpclient.execute(httppost, responseHandler);

			JSONObject jObject = new JSONObject(html);

			String status = jObject.getString("status");
			success = status.equals("success");

		} catch (Exception e) {
			success = false;
		}
		
		if (success) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(PREF_ACCOUNT_USERNAME, username);
			editor.putString(PREF_ACCOUNT_PASS_SHA1, password);
			editor.putString(PREF_ACCOUNT_REALNAME, "");
			editor.commit();
		}

		return success;
	}

	/**
	 * Hash a String using SHA-1.
	 * @param text
	 * @return
	 */
	public static String SHA1(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes("utf-8"), 0, text.getBytes().length);
			byte[] sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Convert a String to hex representation.
	 * @param data
	 * @return
	 */
	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		int count = data.length;
		for (int i = 0; i < count; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Determines if a given String is a valid user name.
	 * @param username
	 * @return
	 */
	public static boolean isValidUsername(String username) {
		return username.length() >= 3 && username.length() <= 20;
	}

	/**
	 * Determines if a given String is a valid e-mail.
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email) {
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(email);
		return m.matches();
	}
}
