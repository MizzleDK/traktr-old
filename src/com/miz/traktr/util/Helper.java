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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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

	// Global finals used throughout the application
	public static final String CONTENT_ID = "contentId";
	public static final String TITLE = "title";
	public static final String POSTER = "poster";
	public static final String BACKDROP = "backdrop";
	public static final String YEAR = "year";
	public static final String INVALID = "invalid"; // Used as a placeholder for empty image URL's
	public static final String RETAINED = "retained";

	// Using integers over enumerations as they use less memory
	public static final int MOVIES = 0, TV_SHOWS = 1;
	public static final int WATCHED = 1000, CHECK_IN = 1001, WATCHLIST = 1002, COLLECTION = 1003;

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
				String json = httpclient.execute(httppost, responseHandler);

				JSONObject jObject = new JSONObject(json);

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
			String json = httpclient.execute(httppost, responseHandler);

			JSONObject jObject = new JSONObject(json);

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
	 * Get a JSONArray of trending content from Trakt
	 * @param context
	 * @param type Either {@link MOVIES} or {@link TV_SHOWS}.
	 * @return
	 */
	public static JSONArray getTrendingContent(Context context, int type) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost;

		if (type == MOVIES)
			httppost = new HttpPost("http://api.trakt.tv/movies/trending.json/" + Helper.TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/shows/trending.json/" + Helper.TRAKT_API);

		httppost.setHeader("Accept", "application/json");

		try {
			// Add authentication so Trakt returns user-specific data as well
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String json = httpclient.execute(httppost, responseHandler);

			return new JSONArray(json);

		} catch (Exception ignored) {}

		return new JSONArray();
	}

	/**
	 * Get movie details from Trakt.
	 * @param context
	 * @param id Either IMDb ID or TMDb ID.
	 * @return A TraktMovie object with the movie data.
	 */
	public static TraktMovie getMovie(Context context, String id) {
		TraktMovie movie = new TraktMovie();

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/movie/summary.json/" + Helper.TRAKT_API + "/" + id);
		httppost.setHeader("Accept", "application/json");

		try {
			// Add authentication so Trakt returns user-specific data as well
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String json = httpclient.execute(httppost, responseHandler);

			JSONObject jsonObject = new JSONObject(json);

			// General movie data
			movie.setTitle(jsonObject.getString("title"));
			movie.setYear(jsonObject.getString("year"));
			movie.setReleased(jsonObject.getLong("released"));
			movie.setUrl(jsonObject.getString("url"));
			movie.setTrailer(jsonObject.getString("trailer"));
			movie.setRuntime(jsonObject.getInt("runtime"));
			movie.setTagline(jsonObject.getString("tagline"));
			movie.setOverview(jsonObject.getString("overview"));
			movie.setCertification(jsonObject.getString("certification"));
			movie.setId(jsonObject.getString("imdb_id"));
			movie.setTmdbId(jsonObject.getString("tmdb_id"));
			movie.setPoster(jsonObject.getJSONObject("images").getString("poster"));
			movie.setBackdrop(jsonObject.getJSONObject("images").getString("fanart"));

			// Genres
			StringBuilder genres = new StringBuilder();
			JSONArray genreArray = jsonObject.getJSONArray("genres");
			for (int i = 0; i < genreArray.length(); i++)
				genres.append(genreArray.get(i) + ", ");
			if (genreArray.length() > 0)
				movie.setGenres(genres.substring(0, genres.length() - 2));
			else
				movie.setGenres("");

			// Ratings
			JSONObject ratings = jsonObject.getJSONObject("ratings");
			movie.setRatingsPercentage(ratings.getInt("percentage"));
			movie.setRatingsVotes(ratings.getInt("votes"));
			movie.setRatingsLoved(ratings.getInt("loved"));
			movie.setRatingsHated(ratings.getInt("hated"));
			
			// User specific data
			movie.setHasWatched(jsonObject.getBoolean("watched"));
			movie.setRating(jsonObject.getString("rating"));
			movie.setAdvancedRating(jsonObject.getInt("rating_advanced"));
			movie.setInWatchlist(jsonObject.getBoolean("in_watchlist"));
			movie.setInCollection(jsonObject.getBoolean("in_collection"));
			
			// The movie data has been loaded :-)
			movie.setHasLoaded(true);

		} catch (Exception ignored) {}

		return movie;
	}
	
	/**
	 * Sets the movie watched status for a list of movies.
	 * @param context
	 * @param movies
	 * @param hasWatched
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean setMovieWatchedStatus(Context context, List<TraktMovie> movies, boolean hasWatched) {
		if (movies.size() == 0)
			return false;

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;
		
		if (hasWatched)
			httppost = new HttpPost("http://api.trakt.tv/movie/seen/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/movie/unseen/" + TRAKT_API);
		
		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));

			JSONArray array = new JSONArray();
			int count = movies.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonMovie = new JSONObject();
				jsonMovie.put("imdb_id", movies.get(i).getId());
				jsonMovie.put("tmdb_id", movies.get(i).getTmdbId());
				jsonMovie.put("year", movies.get(i).getYear());
				jsonMovie.put("title", movies.get(i).getTitle());
				array.put(jsonMovie);
			}
			json.put("movies", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
	}

	/**
	 * Movie check in on Trakt
	 * @param movie
	 * @param c
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean checkInMovieTrakt(Context context, TraktMovie movie) {
		
		// Cancel any previously current check in
		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/movie/cancelcheckin/" + TRAKT_API);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

		} catch (Exception e) {}

		// Check in with the movie
		httppost = new HttpPost("http://api.trakt.tv/movie/checkin/" + TRAKT_API);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			nameValuePairs.add(new BasicNameValuePair("imdb_id", movie.getId()));
			nameValuePairs.add(new BasicNameValuePair("tmdb_id", movie.getTmdbId()));
			nameValuePairs.add(new BasicNameValuePair("title", movie.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("year", movie.getYear()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
	}
	
	/**
	 * Add or remove a list of movies from the watchlist on Trakt
	 * @param movies
	 * @param c
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean movieWatchlist(Context context, List<TraktMovie> movies, boolean toWatch) {
		if (movies.size() == 0)
			return false;
		
		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;
		
		if (toWatch)
			httppost = new HttpPost("http://api.trakt.tv/movie/watchlist/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/movie/unwatchlist/" + TRAKT_API);
		
		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));

			JSONArray array = new JSONArray();
			int count = movies.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonMovie = new JSONObject();
				jsonMovie.put("imdb_id", movies.get(i).getId());
				jsonMovie.put("tmdb_id", movies.get(i).getTmdbId());
				jsonMovie.put("year", movies.get(i).getYear());
				jsonMovie.put("title", movies.get(i).getTitle());
				array.put(jsonMovie);
			}
			json.put("movies", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
	}
	
	/**
	 * Add or remove a list of movies to / from the Trakt movie collection
	 * @param movies
	 * @param c
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean setMovieCollection(Context context, List<TraktMovie> movies, boolean addToCollection) {
		if (movies.size() == 0)
			return false;
		
		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;
		
		if (addToCollection)
			httppost = new HttpPost("http://api.trakt.tv/movie/library/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/movie/unlibrary/" + TRAKT_API);
		
		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));

			JSONArray array = new JSONArray();
			int count = movies.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonMovie = new JSONObject();
				jsonMovie.put("imdb_id", movies.get(i).getId());
				jsonMovie.put("tmdb_id", movies.get(i).getTmdbId());
				jsonMovie.put("year", movies.get(i).getYear());
				jsonMovie.put("title", movies.get(i).getTitle());
				array.put(jsonMovie);
			}
			json.put("movies", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
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

	/**
	 * Add a suffix before the file extension of an image URL.
	 * @param originalUrl
	 * @return URL with added suffix
	 */
	public static String convertCoverSize(String originalUrl) {
		if (originalUrl.contains(".jpg"))
			return originalUrl.replace(".jpg", "-300.jpg");
		return originalUrl;
	}

	/**
	 * Add a suffix before the file extension of an image URL.
	 * @param originalUrl
	 * @return URL with added suffix
	 */
	public static String convertBackdropSize(String originalUrl) {
		if (originalUrl.contains(".jpg"))
			return originalUrl.replace(".jpg", "-940.jpg");
		return originalUrl;
	}
}
