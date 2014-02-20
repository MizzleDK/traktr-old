package com.miz.traktr.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
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
	public static final String PREF_HAS_ACCOUNT = "hasAccount";
	public static final String PREF_ACCOUNT_USERNAME = "accountUsername";
	public static final String PREF_ACCOUNT_PASS_SHA1 = "accountPassSHA1";
	public static final String PREF_ACCOUNT_REALNAME = "accountRealname";
	
	// Trakt API key
	public static final String TRAKT_API = "b85f6110fd2522022bc53614965415bf";
	
	/**
	 * Determines if an account exists.
	 * @param context Context used to get the default shared preferences.
	 * @return True if an account exists, false if not.
	 */
	public static boolean hasAccount(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_HAS_ACCOUNT, false);
	}
	
	/**
	 * Attempt to log in on Trakt.
	 * @param username
	 * @param password
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
			nameValuePairs.add(new BasicNameValuePair("password", SHA1(password)));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String html = httpclient.execute(httppost, responseHandler);

			JSONObject jObject = new JSONObject(html);

			String status = jObject.getString("status");
			success = status.equals("success");

		} catch (Exception e) {
			System.out.println(e);
			success = false;
		}
		
		if (success) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(PREF_ACCOUNT_USERNAME, username);
			editor.putString(PREF_ACCOUNT_PASS_SHA1, SHA1(password));
			editor.commit();

			httpclient = new DefaultHttpClient();
			httppost = new HttpPost("http://api.trakt.tv/user/profile.json/" + TRAKT_API + "/" + username);

			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("username", username));
				nameValuePairs.add(new BasicNameValuePair("password", SHA1(password)));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String html = httpclient.execute(httppost, responseHandler);

				JSONObject jObject = new JSONObject(html);

				String name = jObject.getString("full_name");
				if (name.equals("null") || name.isEmpty())
					name = "";

				editor.putString(PREF_ACCOUNT_REALNAME, name);
				editor.commit();

			} catch (Exception e) {
				success = false;
			}
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
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
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
}
