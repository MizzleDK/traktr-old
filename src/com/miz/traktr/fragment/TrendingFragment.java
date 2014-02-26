package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.MOVIES;
import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.POSTER;
import static com.miz.traktr.util.Helper.BACKDROP;
import static com.miz.traktr.util.Helper.YEAR;

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
import com.miz.traktr.activity.MovieDetails;
import com.miz.traktr.activity.ShowDetails;
import com.miz.traktr.util.CoverItem;
import com.miz.traktr.util.GridItem;
import com.miz.traktr.util.Helper;
import com.squareup.picasso.Picasso;

public class TrendingFragment extends Fragment {

	private static final String TYPE = "type";

	private List<GridItem> mItems = new ArrayList<GridItem>();
	private int mImageThumbSize, mImageThumbSpacing, mType;
	private GridView mGridView;
	private ImageAdapter mAdapter;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public TrendingFragment() {}

	public static TrendingFragment newInstance(int type) { 
		TrendingFragment pageFragment = new TrendingFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TYPE, type);
		pageFragment.setArguments(bundle);
		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		mType = getArguments().getInt(TYPE);
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
				// Create the details Intent depending on what was pressed
				Intent detailsIntent = new Intent(getActivity(), mType == MOVIES ? MovieDetails.class : ShowDetails.class);
				
				// Put the content ID or alternate ID as an Intent extra, depending on which is available
				detailsIntent.putExtra(CONTENT_ID, !mItems.get(arg2).getId().isEmpty() ? mItems.get(arg2).getId() : mItems.get(arg2).getAlternateId());
				detailsIntent.putExtra(TITLE, mItems.get(arg2).getTitle());
				detailsIntent.putExtra(POSTER, mItems.get(arg2).getPoster());
				detailsIntent.putExtra(BACKDROP, mItems.get(arg2).getBackdrop());
				detailsIntent.putExtra(YEAR, mItems.get(arg2).getYear());
				
				// Start the details view
				startActivity(detailsIntent);
			}
		});

		new TrendingContent(getActivity(), mType).execute();
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

			holder.text.setText(mItems.get(position).getTitle());
			holder.subtext.setText(mItems.get(position).getYear());

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

	protected class TrendingContent extends AsyncTask<String, String, String> {

		private Context mContext;
		private int mType;

		public TrendingContent(Context context, int type) {
			mContext = context;
			mType = type;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				JSONArray jArray = Helper.getTrendingContent(mContext, mType);

				mItems.clear();
				for (int i = 0; i < jArray.length(); i++) {
					mItems.add(new GridItem(
							jArray.getJSONObject(i).getString("title"),
							jArray.getJSONObject(i).getString("year"),
							jArray.getJSONObject(i).getString("imdb_id"),
							mType == MOVIES ? jArray.getJSONObject(i).getString("tmdb_id") : jArray.getJSONObject(i).getString("tvdb_id"),
									Helper.convertCoverSize(jArray.getJSONObject(i).getJSONObject("images").getString("poster")),
									Helper.convertBackdropSize(jArray.getJSONObject(i).getJSONObject("images").getString("fanart"))));
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
