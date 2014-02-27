package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.SEASON;
import static com.miz.traktr.util.Helper.NUMBER;
import static com.miz.traktr.util.Helper.JSON;
import static com.miz.traktr.util.Helper.TITLE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.miz.traktr.R;
import com.miz.traktr.activity.ShowEpisodeDetails;
import com.miz.traktr.util.CoverItem;
import com.miz.traktr.util.GridEpisode;
import com.miz.traktr.util.Helper;
import com.squareup.picasso.Picasso;

public class EpisodesFragment extends Fragment {

	private List<GridEpisode> mItems = new ArrayList<GridEpisode>();
	private int mImageThumbSize, mImageThumbSpacing, mSeason;
	private GridView mGridView;
	private ImageAdapter mAdapter;
	private String mShowId, mShowTitle, mJsonArray;

	public EpisodesFragment() {}

	public static EpisodesFragment newInstance(String showId, String showTitle, int season) { 
		EpisodesFragment fragment = new EpisodesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CONTENT_ID, showId);
		bundle.putString(TITLE, showTitle);
		bundle.putInt(SEASON, season);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.backdrop_thumbnail_width);

		mShowId = getArguments().getString(CONTENT_ID);
		mShowTitle = getArguments().getString(TITLE);
		mSeason = getArguments().getInt(SEASON);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.image_grid_fragment, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		mAdapter = new ImageAdapter(getActivity());

		mGridView = (GridView) v.findViewById(R.id.gridView);		
		mGridView.setEmptyView(v.findViewById(R.id.progress));
		mGridView.setColumnWidth(mImageThumbSize);
		mGridView.setAdapter(mAdapter);

		// Calculate the total column width to set item heights by factor 1.5
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(
									mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								mAdapter.setNumColumns(numColumns);
							}
						}
					}
				});
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent details = new Intent(getActivity(), ShowEpisodeDetails.class);
				details.putExtra(TITLE, mShowTitle);
				details.putExtra(SEASON, mSeason);
				details.putExtra(NUMBER, mItems.get(arg2).getNumber());
				details.putExtra(JSON, mJsonArray);
				details.putExtra(CONTENT_ID, mShowId);
				startActivity(details);
			}
		});

		new SeasonDetails(getActivity(), mShowId, mSeason).execute();
	}

	private class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private final Context mContext;
		private int mNumColumns = 0;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			CoverItem holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.grid_episode, container, false);
				holder = new CoverItem();
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.watched = (ImageView) convertView.findViewById(R.id.watched);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.subtext = (TextView) convertView.findViewById(R.id.gridCoverSubtitle);				
				convertView.setTag(holder);
			} else {
				holder = (CoverItem) convertView.getTag();
			}
			
			if (mItems.get(position).hasWatched())
				holder.watched.setVisibility(View.VISIBLE);
			else
				holder.watched.setVisibility(View.GONE);

			if (!mItems.get(position).getTitle().isEmpty())
				holder.text.setText(mItems.get(position).getTitle());
			else
				holder.text.setText(R.string.unknown);

			holder.subtext.setText(String.format(getString(R.string.episode), mItems.get(position).getEpisode()));

			Picasso.with(mContext).load(mItems.get(position).getImage()).into(holder);

			return convertView;
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}

	protected class SeasonDetails extends AsyncTask<String, String, String> {

		private Context mContext;
		private String mShowId;
		private int mSeason;

		public SeasonDetails(Context context, String showId, int season) {
			mContext = context;
			mShowId = showId;
			mSeason = season;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				JSONArray jArray = Helper.getSeason(mContext, mShowId, mSeason);

				mItems.clear();
				for (int i = 0; i < jArray.length(); i++) {
					mItems.add(new GridEpisode(
							jArray.getJSONObject(i).getString("title"),
							jArray.getJSONObject(i).getInt("season"),
							jArray.getJSONObject(i).getInt("episode"),
							jArray.getJSONObject(i).getInt("number"),
							jArray.getJSONObject(i).getBoolean("watched"),
							jArray.getJSONObject(i).getJSONObject("images").getString("screen")));
				}
				
				mJsonArray = jArray.toString();
				
			} catch (Exception ignored) {}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (isAdded())
				mAdapter.notifyDataSetChanged();
		}
	}
}
