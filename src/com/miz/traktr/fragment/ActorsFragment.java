package com.miz.traktr.fragment;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.TYPE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.miz.traktr.util.CoverItem;
import com.miz.traktr.util.GridActor;
import com.miz.traktr.util.Helper;
import com.squareup.picasso.Picasso;

public class ActorsFragment extends Fragment {

	private List<GridActor> mItems = new ArrayList<GridActor>();
	private String mContentId;
	private int mImageThumbSize, mImageThumbSpacing, mType;
	private GridView mGridView;
	private ImageAdapter mAdapter;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ActorsFragment() {}

	public static ActorsFragment newInstance(String contentId, int type) { 
		ActorsFragment pageFragment = new ActorsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CONTENT_ID, contentId);
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

		mContentId = getArguments().getString(CONTENT_ID);
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
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("http://www.imdb.com/find?s=nm&q=" + mItems.get(arg2).getName().replace(" ", "+")));
				startActivity(i);
			}
		});

		new ActorSearch(mContentId, mType).execute();
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

			if (!mItems.get(position).getName().equals("null"))
				holder.text.setText(mItems.get(position).getName());
			else
				holder.text.setText(R.string.unknown);

			if (!mItems.get(position).getCharacter().equals("null"))
				holder.subtext.setText(mItems.get(position).getCharacter());
			else
				holder.subtext.setText(R.string.unknown);

			if (!mItems.get(position).getPoster().contains("avatar-large"))
				Picasso.with(mContext).load(mItems.get(position).getPoster()).into(holder);
			else
				Picasso.with(mContext).load(R.drawable.no_actor).into(holder);

			return convertView;
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}

	protected class ActorSearch extends AsyncTask<String, String, String> {

		private String mContentId;
		private int mType;

		public ActorSearch(String contentId, int type) {
			mContentId = contentId;
			mType = type;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				JSONArray jArray = Helper.getActors(mContentId, mType);

				mItems.clear();
				for (int i = 0; i < jArray.length(); i++) {
					mItems.add(new GridActor(
							jArray.getJSONObject(i).getString("name"),
							jArray.getJSONObject(i).getString("character"),
							jArray.getJSONObject(i).getJSONObject("images").getString("headshot")));
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
