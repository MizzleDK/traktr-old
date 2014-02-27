package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.SEASON;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.TVDB_ID;

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
import com.miz.traktr.activity.ShowSeasonDetails;
import com.miz.traktr.util.CoverItem;
import com.miz.traktr.util.GridSeason;
import com.miz.traktr.util.Helper;
import com.squareup.picasso.Picasso;

public class SeasonsFragment extends Fragment {

	private List<GridSeason> mItems = new ArrayList<GridSeason>();
	private String mShowId, mTitle;
	private int mImageThumbSize, mImageThumbSpacing, mTVDbId;
	private GridView mGridView;
	private ImageAdapter mAdapter;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public SeasonsFragment() {}

	public static SeasonsFragment newInstance(String showId, String showTitle, int tvdbId) { 
		SeasonsFragment pageFragment = new SeasonsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CONTENT_ID, showId);
		bundle.putString(TITLE, showTitle);
		bundle.putInt(TVDB_ID, tvdbId);
		pageFragment.setArguments(bundle);
		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		mShowId = getArguments().getString(CONTENT_ID);
		mTitle = getArguments().getString(TITLE);
		mTVDbId = getArguments().getInt(TVDB_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.image_grid_fragment, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		v.findViewById(R.id.root_layout).setBackgroundResource(R.color.light_background);
		
		mAdapter = new ImageAdapter(getActivity());

		mGridView = (GridView) v.findViewById(R.id.gridView);		
		mGridView.setEmptyView(v.findViewById(R.id.progress));
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
				Intent i = new Intent(getActivity(), ShowSeasonDetails.class);
				i.putExtra(CONTENT_ID, mShowId);
				i.putExtra(SEASON, mItems.get(arg2).getSeason());
				i.putExtra(TITLE, mTitle);
				i.putExtra(TVDB_ID, mTVDbId);
				startActivity(i);
			}
		});

		new ShowSeasons(mShowId).execute();
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
				convertView = inflater.inflate(R.layout.grid_item, container, false);
				holder = new CoverItem();
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.subtext = (TextView) convertView.findViewById(R.id.gridCoverSubtitle);				
				convertView.setTag(holder);
			} else {
				holder = (CoverItem) convertView.getTag();
			}

			if (mItems.get(position).getSeason() == 0)
				holder.text.setText(R.string.specials);
			else
				holder.text.setText(String.format(getString(R.string.season), mItems.get(position).getSeason()));
			
			int episodeCount = mItems.get(position).getEpisodeCount();
			holder.subtext.setText(episodeCount + " " + getResources().getQuantityString(R.plurals.episode, episodeCount, episodeCount));

			Picasso.with(mContext).load(mItems.get(position).getPoster()).into(holder);

			return convertView;
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}

	protected class ShowSeasons extends AsyncTask<String, String, String> {

		private String mShowId;

		public ShowSeasons(String showId) {
			mShowId = showId;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				JSONArray jArray = Helper.getShowSeasons(mShowId);

				mItems.clear();
				for (int i = 0; i < jArray.length(); i++) {
					mItems.add(new GridSeason(
							jArray.getJSONObject(i).getInt("season"),
							jArray.getJSONObject(i).getInt("episodes"),
							jArray.getJSONObject(i).getString("poster")));
				}				

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
