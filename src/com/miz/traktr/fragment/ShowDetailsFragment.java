package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.BACKDROP;
import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.POSTER;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.WATCHLIST;
import static com.miz.traktr.util.Helper.YEAR;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

import com.miz.traktr.R;
import com.miz.traktr.app.TraktrApplication;
import com.miz.traktr.util.Helper;
import com.miz.traktr.util.TraktShow;
import com.squareup.picasso.Picasso;

public class ShowDetailsFragment extends Fragment {

	private TraktShow mShow;
	private TextView mTitle, mDescription, mAirs, mRuntime, mRating, mReleased, mCertification, mGenres;
	private ImageView mBackdrop, mCover;
	private View mContent, mLoading;
	private Typeface mTypeface, mLightTypeface;

	public ShowDetailsFragment() {}

	public static ShowDetailsFragment newInstance(String showId, String title, String poster, String backdrop, String year) { 
		ShowDetailsFragment fragment = new ShowDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CONTENT_ID, showId);
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

		// Create a new TraktShow object where we can put the TV show data
		mShow = new TraktShow();
		mShow.setId(arguments.getString(CONTENT_ID));
		mShow.setTitle(arguments.getString(TITLE));
		mShow.setPoster(arguments.getString(POSTER));
		mShow.setBackdrop(arguments.getString(BACKDROP));
		mShow.setYear(arguments.getString(YEAR));

		mTypeface = TraktrApplication.getOrCreateTypeface(getActivity(), "RobotoCondensed-Regular.ttf");
		mLightTypeface = TraktrApplication.getOrCreateTypeface(getActivity(), "Roboto-Light.ttf");
	}

	@Override
	public void onStart() {
		super.onStart();

		loadShow();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (mShow.hasLoaded()) {
			inflater.inflate(R.menu.show_details, menu);

			// Set title and icon depending on the watchlist status
			if (mShow.inWatchlist()) {
				menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_remove);
				menu.findItem(R.id.watchlist).setTitle(R.string.remove_from_watchlist);
			} else {
				menu.findItem(R.id.watchlist).setIcon(R.drawable.watchlist_add);
				menu.findItem(R.id.watchlist).setTitle(R.string.add_to_watchlist);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// We only want to process the menu events if the TV show data has loaded
		if (mShow.hasLoaded()) {
			switch (item.getItemId()) {
			case R.id.watchlist:
				watchlist();
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
		return inflater.inflate(R.layout.show_details, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		mContent = v.findViewById(R.id.content);
		mLoading = v.findViewById(R.id.progressLayout);

		mTitle = (TextView) v.findViewById(R.id.title);
		mDescription = (TextView) v.findViewById(R.id.description);
		mBackdrop = (ImageView) v.findViewById(R.id.backdrop);
		mCover = (ImageView) v.findViewById(R.id.cover);

		mAirs = (TextView) v.findViewById(R.id.airs);
		mRuntime = (TextView) v.findViewById(R.id.runtime);
		mRating = (TextView) v.findViewById(R.id.rating);
		mReleased = (TextView) v.findViewById(R.id.released);
		mCertification = (TextView) v.findViewById(R.id.certification);
		mGenres = (TextView) v.findViewById(R.id.genres);

		mTitle.setTypeface(mTypeface);
		mTitle.setText(mShow.getTitle());

		mAirs.setTypeface(mLightTypeface);
		mDescription.setTypeface(mLightTypeface);
		mRuntime.setTypeface(mLightTypeface);
		mRating.setTypeface(mLightTypeface);
		mReleased.setTypeface(mLightTypeface);
		mCertification.setTypeface(mLightTypeface);
		mGenres.setTypeface(mLightTypeface);

		loadImages();
	}

	private void loadImages() {
		Picasso.with(getActivity()).load(mShow.getPoster()).placeholder(R.drawable.loading_image).error(R.drawable.loading_image).into(mCover);
		Picasso.with(getActivity()).load(mShow.getBackdrop()).placeholder(R.drawable.fanart_dark).error(R.drawable.fanart_dark).into(mBackdrop);
	}

	private void loadShow() {
		ShowLoader showLoader = new ShowLoader(getActivity(), mShow.getId());
		showLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class ShowLoader extends AsyncTask<Void, Void, Void> {

		private Context mContext;
		private String mId;

		public ShowLoader(Context context, String showId) {
			mContext = context;
			mId = showId;
		}

		@Override
		public void onPreExecute() {
			setLoading(true);
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (!mShow.hasLoaded()) {			
				// Fetch the show details in the background
				mShow = Helper.getShow(mContext, mId);
			}

			return null;
		}

		@Override
		public void onPostExecute(Void result) {

			if (mShow.isContinuing()) {
				mAirs.setText(String.format(getString(R.string.show_airing), mShow.getAirDay(), mShow.getAirTime(), mShow.getNetwork()));
			} else {
				mAirs.setText(R.string.show_has_ended);
			}

			// Set the show overview
			mDescription.setText(mShow.getOverview());

			// Set the show genres
			if (!mShow.getGenres().isEmpty()) {
				mGenres.setText(mShow.getGenres());
			} else {
				mGenres.setText(R.string.stringNA);
			}

			// Set the show runtime
			try {
				int hours = mShow.getRuntime() / 60;
				int minutes = mShow.getRuntime() % 60;
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
				if (mShow.getRuntime() != 0) {
					mRuntime.setText(String.valueOf(mShow.getRuntime()));
				} else {
					mRuntime.setText(R.string.stringNA);
				}
			}

			// Set the show release date
			if (mShow.getFirstAired() != 0) {
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(mShow.getFirstAired());
				mReleased.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.getTime()));
			} else {
				mReleased.setText(R.string.stringNA);
			}

			// Set the mShow rating
			mRating.setText(Html.fromHtml(mShow.getRatingsPercentage() + "<small> %</small>"));

			// Set the show rating
			mCertification.setText(mShow.getCertification());

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
		intent.putExtra(Intent.EXTRA_TEXT, mShow.getUrl());
		startActivity(intent);
	}

	private void open() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(mShow.getUrl()));
		startActivity(intent);
	}

	private void watchlist() {
		mShow.setInWatchlist(!mShow.inWatchlist());

		performAction(WATCHLIST, mShow.inWatchlist());

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

			List<TraktShow> show = null;

			switch (mType) {
			case WATCHLIST:
				show = new ArrayList<TraktShow>();
				show.add(mShow);
				result = Helper.tvShowWatchlist(mContext, show, mPositiveChange);
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
				case WATCHLIST:
					Toast.makeText(mContext, mPositiveChange ? R.string.added_to_watchlist : R.string.removed_from_watchlist, Toast.LENGTH_SHORT).show();
					break;
				}
			} else {
				Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show();
			}
		}
	}
}