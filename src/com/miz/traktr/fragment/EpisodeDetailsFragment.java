package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.CHECK_IN;
import static com.miz.traktr.util.Helper.COLLECTION;
import static com.miz.traktr.util.Helper.JSON;
import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.WATCHED;
import static com.miz.traktr.util.Helper.WATCHLIST;
import static com.miz.traktr.util.Helper.TVDB_ID;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.miz.traktr.R;
import com.miz.traktr.app.TraktrApplication;
import com.miz.traktr.util.Helper;
import com.miz.traktr.util.TraktEpisode;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EpisodeDetailsFragment extends Fragment {

	private TraktEpisode mEpisode;
	private TextView mTitle, mDescription, mReleased, mRating;
	private ImageView mBackdrop;
	private Typeface mTypeface, mLightTypeface;

	public EpisodeDetailsFragment(){}

	public static EpisodeDetailsFragment newInstance(String json, String showId, int tvdbId) {
		EpisodeDetailsFragment frag = new EpisodeDetailsFragment();
		Bundle b = new Bundle();
		b.putString(JSON, json);
		b.putString(CONTENT_ID, showId);
		b.putInt(TVDB_ID, tvdbId);
		frag.setArguments(b);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		mTypeface = TraktrApplication.getOrCreateTypeface(getActivity(), "RobotoCondensed-Regular.ttf");
		mLightTypeface = TraktrApplication.getOrCreateTypeface(getActivity(), "Roboto-Light.ttf");
		
		loadJson(getArguments().getString(JSON), getArguments().getString(CONTENT_ID), getArguments().getInt(TVDB_ID));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.episode_details, menu);

		// Set title and icon depending on the watched status
		if (mEpisode.hasWatched()) {
			menu.findItem(R.id.watched).setIcon(R.drawable.ic_action_glasses_watched);
			menu.findItem(R.id.watched).setTitle(R.string.mark_as_unwatched);
		} else {
			menu.findItem(R.id.watched).setIcon(R.drawable.ic_action_glasses);
			menu.findItem(R.id.watched).setTitle(R.string.mark_as_watched);
		}

		// Set title and icon depending on the watchlist status
		if (mEpisode.inWatchlist()) {
			menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_remove);
			menu.findItem(R.id.watchlist).setTitle(R.string.remove_from_watchlist);
		} else {
			menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_add);
			menu.findItem(R.id.watchlist).setTitle(R.string.add_to_watchlist);
		}

		// Set title and icon depending on the collection status
		if (mEpisode.inCollection()) {
			menu.findItem(R.id.collection).setIcon(R.drawable.ic_action_folder_closed);
			menu.findItem(R.id.collection).setTitle(R.string.remove_from_collection);
		} else {
			menu.findItem(R.id.collection).setIcon(R.drawable.ic_action_folder_open);
			menu.findItem(R.id.collection).setTitle(R.string.add_to_collection);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.checkin:
			checkIn();
			break;
		case R.id.watched:
			watched();
			break;
		case R.id.watchlist:
			watchlist();
			break;
		case R.id.collection:
			collection();
			break;
		case R.id.share:
			share();
			break;
		case R.id.open:
			open();
			break;
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.episode_details, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		v.findViewById(R.id.content).setVisibility(View.VISIBLE);
		v.findViewById(R.id.progressLayout).setVisibility(View.GONE);

		mTitle = (TextView) v.findViewById(R.id.title);
		mDescription = (TextView) v.findViewById(R.id.description);
		mBackdrop = (ImageView) v.findViewById(R.id.backdrop);

		mReleased = (TextView) v.findViewById(R.id.released);
		mRating = (TextView) v.findViewById(R.id.rating);

		mTitle.setTypeface(mTypeface);
		mTitle.setText(mEpisode.getTitle());

		mDescription.setTypeface(mLightTypeface);
		mReleased.setTypeface(mLightTypeface);
		mRating.setTypeface(mLightTypeface);

		// Set the episode overview
		if (!mEpisode.getOverview().isEmpty())
			mDescription.setText(mEpisode.getOverview());
		else
			mDescription.setText(R.string.no_description);

		// Set the show release date
		if (mEpisode.getFirstAired() != 0) {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTimeInMillis(mEpisode.getFirstAired());
			mReleased.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.getTime()));
		} else {
			mReleased.setText(R.string.stringNA);
		}

		// Set the mShow rating
		mRating.setText(Html.fromHtml(mEpisode.getRatingsPercentage() + "<small> %</small>"));

		loadImage();
	}

	private void loadImage() {
		Picasso.with(getActivity()).load(mEpisode.getBackdrop()).placeholder(R.drawable.fanart_dark).error(R.drawable.fanart_dark).into(mBackdrop);
	}

	private void loadJson(String json, String showId, int showTVDbId) {
		try {
			JSONObject jsonObject = new JSONObject(json);

			mEpisode = new TraktEpisode();

			mEpisode.setShowId(showId);
			mEpisode.setTVDbId(showTVDbId);
			mEpisode.setTitle(jsonObject.getString("title"));
			mEpisode.setEpisode(jsonObject.getInt("episode"));
			mEpisode.setSeason(jsonObject.getInt("season"));
			mEpisode.setFirstAired(jsonObject.getLong("first_aired_utc"));
			mEpisode.setUrl(jsonObject.getString("url"));
			mEpisode.setOverview(jsonObject.getString("overview"));
			mEpisode.setId(String.valueOf(jsonObject.getInt("tvdb_id")));
			mEpisode.setBackdrop(jsonObject.getString("screen"));

			// Ratings
			JSONObject ratings = jsonObject.getJSONObject("ratings");
			mEpisode.setRatingsPercentage(ratings.getInt("percentage"));
			mEpisode.setRatingsVotes(ratings.getInt("votes"));
			mEpisode.setRatingsLoved(ratings.getInt("loved"));
			mEpisode.setRatingsHated(ratings.getInt("hated"));

			// User specific data
			mEpisode.setHasWatched(jsonObject.getBoolean("watched"));
			mEpisode.setRating(jsonObject.getString("rating"));
			mEpisode.setAdvancedRating(jsonObject.getInt("rating_advanced"));
			mEpisode.setInWatchlist(jsonObject.getBoolean("in_watchlist"));
			mEpisode.setInCollection(jsonObject.getBoolean("in_collection"));
		} catch (JSONException ignored) {}
	}

	private void invalidateOptionsMenu() {
		if (isAdded())
			getActivity().invalidateOptionsMenu();
	}

	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mEpisode.getUrl());
		startActivity(intent);
	}

	private void open() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(mEpisode.getUrl()));
		startActivity(intent);
	}

	private void checkIn() {
		performAction(CHECK_IN, false);
	}

	private void watched() {
		mEpisode.setHasWatched(!mEpisode.hasWatched());

		performAction(WATCHED, mEpisode.hasWatched());

		invalidateOptionsMenu();
	}

	private void watchlist() {
		mEpisode.setInWatchlist(!mEpisode.inWatchlist());

		performAction(WATCHLIST, mEpisode.inWatchlist());

		invalidateOptionsMenu();
	}

	private void collection() {
		mEpisode.setInCollection(!mEpisode.inCollection());

		performAction(COLLECTION, mEpisode.inCollection());

		invalidateOptionsMenu();
	}

	private void performAction(int type, boolean positiveChange) {
		if (isAdded())
			new PerformAction(getActivity(), type, positiveChange).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class PerformAction extends AsyncTask<Void, Void, Boolean> {

		private Context mContext;
		private int mType;
		private boolean mPositiveChange;

		public PerformAction(Context context, int type, boolean positiveChange) {
			mContext = context;
			mType = type;
			mPositiveChange = positiveChange;
		}
		
		@Override
		protected void onPreExecute() {
			Toast.makeText(mContext, R.string.please_wait, Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;

			List<TraktEpisode> episode = null;

			switch (mType) {
			case WATCHED:
				episode = new ArrayList<TraktEpisode>();
				episode.add(mEpisode);
				result = Helper.setEpisodeWatchedStatus(mContext, episode, mPositiveChange);
				break;
			case CHECK_IN:
				result = Helper.checkInEpisode(mContext, mEpisode);
				break;
			case WATCHLIST:
				episode = new ArrayList<TraktEpisode>();
				episode.add(mEpisode);
				result = Helper.episodeWatchlist(mContext, episode, mPositiveChange);
				break;
			case COLLECTION:
				episode = new ArrayList<TraktEpisode>();
				episode.add(mEpisode);
				result = Helper.setEpisodeCollection(mContext, episode, mPositiveChange);
				break;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				// Vibrate for a short while when the Toast is shown
				Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(200);

				switch (mType) {
				case WATCHED:
					Toast.makeText(mContext, mPositiveChange ? R.string.marked_episode_as_watched : R.string.marked_episode_as_unwatched, Toast.LENGTH_SHORT).show();
					break;
				case CHECK_IN:
					Toast.makeText(mContext, R.string.checked_in, Toast.LENGTH_SHORT).show();
					break;
				case WATCHLIST:
					Toast.makeText(mContext, mPositiveChange ? R.string.added_to_watchlist : R.string.removed_from_watchlist, Toast.LENGTH_SHORT).show();
					break;
				case COLLECTION:
					Toast.makeText(mContext, mPositiveChange ? R.string.added_to_collection : R.string.removed_from_collection, Toast.LENGTH_SHORT).show();
					break;
				}
			} else {
				Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
