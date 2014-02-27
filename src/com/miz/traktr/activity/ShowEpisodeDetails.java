package com.miz.traktr.activity;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.NUMBER;
import static com.miz.traktr.util.Helper.JSON;
import static com.miz.traktr.util.Helper.SEASON;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.TVDB_ID;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.miz.traktr.R;
import com.miz.traktr.fragment.EpisodeDetailsFragment;

public class ShowEpisodeDetails extends BaseActivity implements ActionBar.TabListener {

	private List<JSONObject> mItems = new ArrayList<JSONObject>();
	private String mShowId, mTitle, mJson;
	private int mSeason, mIndex, mTVDbId;
	private ViewPager mViewPager;
	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.viewpager);

		Bundle extras = getIntent().getExtras();

		mShowId = extras.getString(CONTENT_ID, "");
		mTitle = extras.getString(TITLE, "");
		mJson = extras.getString(JSON);
		mSeason = extras.getInt(SEASON);
		mIndex = extras.getInt(NUMBER);
		mTVDbId = extras.getInt(TVDB_ID);

		loadJson();

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(new TrendingAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mActionBar.selectTab(mActionBar.getTabAt(position));
			}
		});

		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < mItems.size(); i++)
			try {
				mActionBar.addTab(mActionBar.newTab().setText(String.format(getString(R.string.episode), mItems.get(i).getInt("episode"))).setTabListener(this));
			} catch (JSONException ignored) {}


		if (savedInstanceState != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt("selection", 0));
		}

		setTitle(mTitle);
		if (mSeason == 0)
			getActionBar().setSubtitle(R.string.specials);
		else
			getActionBar().setSubtitle(String.format(getString(R.string.season), mSeason));
		
		mActionBar.selectTab(mActionBar.getTabAt(mIndex - 1)); // ActionBar index starts at 0
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("selection", mViewPager.getCurrentItem());
	}

	private class TrendingAdapter extends FragmentPagerAdapter {

		public TrendingAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override  
		public Fragment getItem(int index) {
			switch (index) {
			default:
				return EpisodeDetailsFragment.newInstance(mItems.get(index).toString(), mShowId, mTVDbId);
			}
		}  

		@Override  
		public int getCount() {  
			return mItems.size();
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onStart() {
		super.onStart();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadJson() {
		try {
			JSONArray jArray = new JSONArray(mJson);
			for (int i = 0; i < jArray.length(); i++)
				mItems.add(jArray.getJSONObject(i));
		} catch (JSONException ignored) {}
	}
}