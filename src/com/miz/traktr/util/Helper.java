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
	public static final String TYPE = "type";
	public static final String SEASON = "season";
	public static final String NUMBER = "number";
	public static final String JSON = "json";
	public static final String TVDB_ID = "tvdbId";
	public static final String ALTERNATE = "alternate";
	
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
	 * Sets the watched status for a list of episodes.
	 * @param context
	 * @param episodes
	 * @param hasWatched
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean setEpisodeWatchedStatus(Context context, List<TraktEpisode> episodes, boolean hasWatched) {
		if (episodes.size() == 0)
			return false;

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;

		if (hasWatched)
			httppost = new HttpPost("http://api.trakt.tv/show/episode/seen/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/show/episode/unseen/" + TRAKT_API);

		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));
			json.put("imdb_id", episodes.get(0).getShowId());
			json.put("tvdb_id", episodes.get(0).getShowId());

			JSONArray array = new JSONArray();
			int count = episodes.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonShow = new JSONObject();
				jsonShow.put("season", episodes.get(i).getSeason());
				jsonShow.put("episode", episodes.get(i).getEpisode());
				array.put(jsonShow);
			}
			json.put("episodes", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {
			System.out.println(e);
		}

		return false;
	}

	/**
	 * Movie check in on Trakt
	 * @param movie
	 * @param c
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean checkInMovie(Context context, TraktMovie movie) {

		// Cancel any previously current check in
		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/movie/cancelcheckin/" + TRAKT_API);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));
			httppost.setEntity(new StringEntity(json.toString()));

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
	 * TV show episode check in on Trakt
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean checkInEpisode(Context context, TraktEpisode episode) {

		// Cancel any previously current check in
		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/show/cancelcheckin/" + TRAKT_API);
		httppost.setHeader("Content-type", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));
			httppost.setEntity(new StringEntity(json.toString()));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

		} catch (Exception e) {}

		// Check in with the episode
		httppost = new HttpPost("http://api.trakt.tv/show/checkin/" + TRAKT_API);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(10);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			nameValuePairs.add(new BasicNameValuePair("tvdb_id", String.valueOf(episode.getShowTVDId())));
			nameValuePairs.add(new BasicNameValuePair("title", ""));
			nameValuePairs.add(new BasicNameValuePair("year", ""));
			nameValuePairs.add(new BasicNameValuePair("season", String.valueOf(episode.getSeason())));
			nameValuePairs.add(new BasicNameValuePair("episode", String.valueOf(episode.getEpisode())));
			nameValuePairs.add(new BasicNameValuePair("episode_tvdb_id", String.valueOf(episode.getId())));
			
			// Used to help debugging on Trakt
			String debuggin = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			nameValuePairs.add(new BasicNameValuePair("app_version", debuggin));
			nameValuePairs.add(new BasicNameValuePair("episode", debuggin));
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
	 * Add or remove a list of TV shows from the watchlist on Trakt
	 * @param shows
	 * @param c
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean tvShowWatchlist(Context context, List<TraktShow> shows, boolean toWatch) {
		if (shows.size() == 0)
			return false;

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;

		if (toWatch)
			httppost = new HttpPost("http://api.trakt.tv/show/watchlist/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/show/unwatchlist/" + TRAKT_API);

		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));

			JSONArray array = new JSONArray();
			int count = shows.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonShow = new JSONObject();
				jsonShow.put("tvdb_id", shows.get(i).getTVDbId());
				jsonShow.put("year", shows.get(i).getYear());
				jsonShow.put("title", shows.get(i).getTitle());
				array.put(jsonShow);
			}
			json.put("shows", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
	}
	
	/**
	 * Add or remove a list of TV show episodes from the watchlist on Trakt
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean episodeWatchlist(Context context, List<TraktEpisode> episodes, boolean toWatch) {
		if (episodes.size() == 0)
			return false;

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;

		if (toWatch)
			httppost = new HttpPost("http://api.trakt.tv/show/episode/watchlist/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/show/episode/unwatchlist/" + TRAKT_API);

		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));
			json.put("imdb_id", episodes.get(0).getShowId());
			json.put("tvdb_id", episodes.get(0).getShowId());

			JSONArray array = new JSONArray();
			int count = episodes.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonShow = new JSONObject();
				jsonShow.put("season", episodes.get(i).getSeason());
				jsonShow.put("episode", episodes.get(i).getEpisode());
				array.put(jsonShow);
			}
			json.put("episodes", array);

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
	 * Add or remove a list of episodes to / from the Trakt episode collection
	 * @return True if succeeded, false otherwise.
	 */
	public static boolean setEpisodeCollection(Context context, List<TraktEpisode> episodes, boolean addToCollection) {
		if (episodes.size() == 0)
			return false;

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;

		if (addToCollection)
			httppost = new HttpPost("http://api.trakt.tv/show/episode/library/" + TRAKT_API);
		else
			httppost = new HttpPost("http://api.trakt.tv/show/episode/unlibrary/" + TRAKT_API);

		httppost.setHeader("Accept", "application/json");

		try {
			JSONObject json = new JSONObject();
			json.put("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, ""));
			json.put("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, ""));
			json.put("imdb_id", episodes.get(0).getShowId());
			json.put("tvdb_id", episodes.get(0).getShowId());

			JSONArray array = new JSONArray();
			int count = episodes.size();
			for (int i = 0; i < count; i++) {
				JSONObject jsonShow = new JSONObject();
				jsonShow.put("season", episodes.get(i).getSeason());
				jsonShow.put("episode", episodes.get(i).getEpisode());
				array.put(jsonShow);
			}
			json.put("episodes", array);

			httppost.setEntity(new StringEntity(json.toString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httppost, responseHandler);

			return true;
		} catch (Exception e) {}

		return false;
	}
	
	/**
	 * Get TV show details from Trakt.
	 * @param context
	 * @param id Either IMDb ID or TVDb ID.
	 * @return A TraktShow object with the TV show data.
	 */
	public static TraktShow getShow(Context context, String id) {
		TraktShow show = new TraktShow();

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/show/summary.json/" + Helper.TRAKT_API + "/" + id);
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

			// General TV show data
			show.setTitle(jsonObject.getString("title"));
			show.setYear(jsonObject.getString("year"));
			show.setFirstAired(jsonObject.getLong("first_aired_utc"));
			show.setUrl(jsonObject.getString("url"));
			show.setRuntime(jsonObject.getInt("runtime"));
			show.setContinuing(jsonObject.getString("status").equals("Continuing"));
			show.setNetwork(jsonObject.getString("network"));
			show.setAirDay(jsonObject.getString("air_day"));
			show.setAirTime(jsonObject.getString("air_time"));
			show.setOverview(jsonObject.getString("overview"));
			show.setCertification(jsonObject.getString("certification"));
			show.setId(jsonObject.getString("imdb_id"));
			show.setTVDbId(jsonObject.getString("tvdb_id"));
			show.setPoster(jsonObject.getJSONObject("images").getString("poster"));
			show.setBackdrop(jsonObject.getJSONObject("images").getString("fanart"));

			// Genres
			StringBuilder genres = new StringBuilder();
			JSONArray genreArray = jsonObject.getJSONArray("genres");
			for (int i = 0; i < genreArray.length(); i++)
				genres.append(genreArray.get(i) + ", ");
			if (genreArray.length() > 0)
				show.setGenres(genres.substring(0, genres.length() - 2));
			else
				show.setGenres("");

			// Ratings
			JSONObject ratings = jsonObject.getJSONObject("ratings");
			show.setRatingsPercentage(ratings.getInt("percentage"));
			show.setRatingsVotes(ratings.getInt("votes"));
			show.setRatingsLoved(ratings.getInt("loved"));
			show.setRatingsHated(ratings.getInt("hated"));

			// User specific data
			show.setRating(jsonObject.getString("rating"));
			show.setAdvancedRating(jsonObject.getInt("rating_advanced"));
			show.setInWatchlist(jsonObject.getBoolean("in_watchlist"));

			// The TV show data has been loaded :-)
			show.setHasLoaded(true);

		} catch (Exception ignored) {}

		return show;
	}

	/**
	 * Get an array of TV show seasons for a given show.
	 * @param showId
	 * @return
	 */
	public static JSONArray getShowSeasons(String showId) {
		JSONArray result = new JSONArray();

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/show/seasons.json/" + TRAKT_API + "/" + showId);
		httppost.setHeader("Accept", "application/json");

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String json = httpclient.execute(httppost, responseHandler);

			result = new JSONArray(json);
		} catch (Exception e) {}

		return result;
	}

	/**
	 * Get an array of TV show episodes for a given show season.
	 * @param showId
	 * @return
	 */
	public static JSONArray getSeason(Context context, String showId, int season) {
		JSONArray result = new JSONArray();

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = new HttpPost("http://api.trakt.tv/show/season.json/" + TRAKT_API + "/" + showId + "/" + season);
		httppost.setHeader("Accept", "application/json");

		try {
			// Add authentication so Trakt returns user-specific data as well
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_USERNAME, "")));
			nameValuePairs.add(new BasicNameValuePair("password", PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCOUNT_PASS_SHA1, "")));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String json = httpclient.execute(httppost, responseHandler);

			result = new JSONArray(json);
		} catch (Exception e) {}

		return result;
	}

	/**
	 * Get an array of actors for a given movie or TV show.
	 * @param contentId
	 * @param type
	 * @return
	 */
	public static JSONArray getActors(String contentId, int type) {
		JSONArray result = new JSONArray();

		HttpClient httpclient = new DefaultHttpClient();	
		HttpPost httppost = null;

		if (type == MOVIES)
			httppost = new HttpPost("http://api.trakt.tv/movie/summary.json/" + Helper.TRAKT_API + "/" + contentId);
		else
			httppost = new HttpPost("http://api.trakt.tv/show/summary.json/" + Helper.TRAKT_API + "/" + contentId);
		
		httppost.setHeader("Accept", "application/json");

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String json = httpclient.execute(httppost, responseHandler);

			JSONObject temp = new JSONObject(json);
			result = temp.getJSONObject("people").getJSONArray("actors");
		} catch (Exception e) {}

		return result;
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
