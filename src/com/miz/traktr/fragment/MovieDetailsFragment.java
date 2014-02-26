package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.BACKDROP;
import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.POSTER;
import static com.miz.traktr.util.Helper.RETAINED;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.WATCHED;
import static com.miz.traktr.util.Helper.CHECK_IN;
import static com.miz.traktr.util.Helper.YEAR;
import static com.miz.traktr.util.Helper.WATCHLIST;
import static com.miz.traktr.util.Helper.COLLECTION;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
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

import com.miz.traktr.R;
import com.miz.traktr.util.Helper;
import com.miz.traktr.util.TraktMovie;
import com.squareup.picasso.Picasso;

public class MovieDetailsFragment extends Fragment {

	private TraktMovie mMovie;
	private TextView mTitle, mTagline, mDescription, mRuntime, mRating, mReleased, mCertification, mGenres;
	private ImageView mBackdrop, mCover;
	private View mContent, mLoading;

	private Typeface mTypeface, mLightTypeface;

	public MovieDetailsFragment() {}

	public static MovieDetailsFragment newInstance(String movieId, String title, String poster, String backdrop, String year) { 
		MovieDetailsFragment fragment = new MovieDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CONTENT_ID, movieId);
		bundle.putString(TITLE, title);
		bundle.putString(POSTER, poster);
		bundle.putString(BACKDROP, backdrop);
		bundle.putString(YEAR, year);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		Bundle arguments = getArguments();

		// Create a new TraktMovie object where we can put the movie data
		mMovie = new TraktMovie();
		mMovie.setId(arguments.getString(CONTENT_ID));
		mMovie.setTitle(arguments.getString(TITLE));
		mMovie.setPoster(arguments.getString(POSTER));
		mMovie.setBackdrop(arguments.getString(BACKDROP));
		mMovie.setYear(arguments.getString(YEAR));

		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "RobotoCondensed-Regular.ttf");
		mLightTypeface = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
	}

	@Override
	public void onStart() {
		super.onStart();

		loadMovie();

		// Set the title of the Activity and subtitle of the ActionBar
		getActivity().setTitle(mMovie.getTitle());
		getActivity().getActionBar().setSubtitle(mMovie.getYear());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (mMovie.hasLoaded()) {
			inflater.inflate(R.menu.movie_details, menu);

			// Set title and icon depending on the watched status
			if (mMovie.hasWatched()) {
				menu.findItem(R.id.watched).setIcon(R.drawable.ic_action_glasses_watched);
				menu.findItem(R.id.watched).setTitle(R.string.mark_as_unwatched);
			} else {
				menu.findItem(R.id.watched).setIcon(R.drawable.ic_action_glasses);
				menu.findItem(R.id.watched).setTitle(R.string.mark_as_watched);
			}

			// Set title and icon depending on the watchlist status
			if (mMovie.inWatchlist()) {
				menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_remove);
				menu.findItem(R.id.watchlist).setTitle(R.string.remove_from_watchlist);
			} else {
				menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_add);
				menu.findItem(R.id.watchlist).setTitle(R.string.add_to_watchlist);
			}
			
			// Set title and icon depending on the collection status
			if (mMovie.inCollection()) {
				menu.findItem(R.id.collection).setIcon(R.drawable.ic_action_folder_closed);
				menu.findItem(R.id.collection).setTitle(R.string.remove_from_collection);
			} else {
				menu.findItem(R.id.collection).setIcon(R.drawable.ic_action_folder_open);
				menu.findItem(R.id.collection).setTitle(R.string.add_to_collection);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// We only want to process the menu events if the movie data has loaded
		if (mMovie.hasLoaded()) {
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
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.movie_details, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		mContent = v.findViewById(R.id.content);
		mLoading = v.findViewById(R.id.progressLayout);

		mTitle = (TextView) v.findViewById(R.id.title);		
		mTagline = (TextView) v.findViewById(R.id.tagline);
		mDescription = (TextView) v.findViewById(R.id.description);
		mBackdrop = (ImageView) v.findViewById(R.id.backdrop);
		mCover = (ImageView) v.findViewById(R.id.cover);

		mRuntime = (TextView) v.findViewById(R.id.runtime);
		mRating = (TextView) v.findViewById(R.id.rating);
		mReleased = (TextView) v.findViewById(R.id.released);
		mCertification = (TextView) v.findViewById(R.id.certification);
		mGenres = (TextView) v.findViewById(R.id.genres);

		mTitle.setTypeface(mTypeface);
		mTitle.setText(mMovie.getTitle());

		mDescription.setTypeface(mLightTypeface);
		mRuntime.setTypeface(mLightTypeface);
		mRating.setTypeface(mLightTypeface);
		mReleased.setTypeface(mLightTypeface);
		mCertification.setTypeface(mLightTypeface);
		mGenres.setTypeface(mLightTypeface);

		loadImages();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(RETAINED, true);

		super.onSaveInstanceState(outState);
	}

	private void loadImages() {
		Picasso.with(getActivity()).load(mMovie.getPoster()).placeholder(R.drawable.loading_image).error(R.drawable.loading_image).into(mCover);
		Picasso.with(getActivity()).load(mMovie.getBackdrop()).placeholder(R.drawable.fanart_dark).error(R.drawable.fanart_dark).into(mBackdrop);
	}

	private void loadMovie() {
		MovieLoader movieLoader = new MovieLoader(getActivity(), mMovie.getId());
		movieLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class MovieLoader extends AsyncTask<Void, Void, Void> {

		private Context mContext;
		private String mId;

		public MovieLoader(Context context, String movieId) {
			mContext = context;
			mId = movieId;
		}

		@Override
		public void onPreExecute() {
			setLoading(true);
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (!mMovie.hasLoaded()) {			
				// Fetch the movie details in the background
				mMovie = Helper.getMovie(mContext, mId);
			}

			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			// Set the movie tag line
			if (!mMovie.getTagline().isEmpty())
				mTagline.setText(mMovie.getTagline());
			else
				mTagline.setVisibility(View.GONE);

			// Set the movie overview
			mDescription.setText(mMovie.getOverview());

			// Set the movie genres
			if (!mMovie.getGenres().isEmpty()) {
				mGenres.setText(mMovie.getGenres());
			} else {
				mGenres.setText(R.string.stringNA);
			}

			// Set the movie runtime
			try {
				int hours = mMovie.getRuntime() / 60;
				int minutes = mMovie.getRuntime() % 60;
				String hours_string = hours + " " + getResources().getQuantityString(R.plurals.hour, hours, hours);
				String minutes_string = minutes + " " + getResources().getQuantityString(R.plurals.minute, minutes, minutes);
				if (hours > 0) {
					if (minutes == 0)
						mRuntime.setText(hours_string);
					else
						mRuntime.setText(hours_string + " " + minutes_string);
				} else {
					mRuntime.setText(minutes_string);
				}
			} catch (Exception e) { // Fall back if something goes wrong
				if (mMovie.getRuntime() != 0) {
					mRuntime.setText(String.valueOf(mMovie.getRuntime()));
				} else {
					mRuntime.setText(R.string.stringNA);
				}
			}

			// Set the movie release date
			if (mMovie.getReleased() != 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(mMovie.getReleased());
				mReleased.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.getTime()));
			} else {
				mReleased.setText(R.string.stringNA);
			}

			// Set the movie rating
			mRating.setText(Html.fromHtml(mMovie.getRatingsPercentage() + "<small> %</small>"));

			// Set the movie rating
			mCertification.setText(mMovie.getCertification());

			setLoading(false);

			invalidateOptionsMenu();
		}
	}

	private void invalidateOptionsMenu() {
		if (isAdded())
			getActivity().invalidateOptionsMenu();
	}

	private void setLoading(boolean isLoading) {
		if (isLoading) {
			mContent.setVisibility(View.GONE);
			mLoading.setVisibility(View.VISIBLE);
		} else {
			mContent.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.GONE);
		}
	}

	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mMovie.getUrl());
		startActivity(intent);
	}

	private void open() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(mMovie.getUrl()));
		startActivity(intent);
	}

	private void checkIn() {
		performAction(CHECK_IN, false);
	}

	private void watched() {
		mMovie.setHasWatched(!mMovie.hasWatched());

		performAction(WATCHED, mMovie.hasWatched());

		invalidateOptionsMenu();
	}

	private void watchlist() {
		mMovie.setInWatchlist(!mMovie.inWatchlist());

		performAction(WATCHLIST, mMovie.inWatchlist());

		invalidateOptionsMenu();
	}

	private void collection() {
		mMovie.setInCollection(!mMovie.inCollection());

		performAction(COLLECTION, mMovie.inCollection());

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
		protected Boolean doInBackground(Void... params) {
			boolean result = false;

			List<TraktMovie> movie = null;

			switch (mType) {
			case WATCHED:
				movie = new ArrayList<TraktMovie>();
				movie.add(mMovie);
				result = Helper.setMovieWatchedStatus(mContext, movie, mPositiveChange);
				break;
			case CHECK_IN:
				result = Helper.checkInMovieTrakt(mContext, mMovie);
				break;
			case WATCHLIST:
				movie = new ArrayList<TraktMovie>();
				movie.add(mMovie);
				result = Helper.movieWatchlist(mContext, movie, mPositiveChange);
				break;
			case COLLECTION:
				movie = new ArrayList<TraktMovie>();
				movie.add(mMovie);
				result = Helper.setMovieCollection(mContext, movie, mPositiveChange);
				break;
			}

			return result;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (result) {
				// Vibrate for a short while when the Toast is shown
				Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(200);

				switch (mType) {
				case WATCHED:
					Toast.makeText(mContext, mPositiveChange ? R.string.marked_movie_as_watched : R.string.marked_movie_as_unwatched, Toast.LENGTH_SHORT).show();
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