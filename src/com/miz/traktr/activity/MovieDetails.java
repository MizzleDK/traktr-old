package com.miz.traktr.activity;

import com.miz.traktr.R;
import com.miz.traktr.fragment.ActorsFragment;
import com.miz.traktr.fragment.MovieDetailsFragment;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.POSTER;
import static com.miz.traktr.util.Helper.BACKDROP;
import static com.miz.traktr.util.Helper.MOVIES;
import static com.miz.traktr.util.Helper.YEAR;
import static com.miz.traktr.util.Helper.INVALID;

public class MovieDetails extends BaseActivity implements ActionBar.TabListener {

	private String mMovieId, mTitle, mPoster, mBackdrop, mYear;
	private ViewPager mViewPager;
	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.viewpager);

		Bundle extras = getIntent().getExtras();

		mMovieId = extras.getString(CONTENT_ID, "");
		mTitle = extras.getString(TITLE, "");
		mPoster = extras.getString(POSTER, INVALID);
		mBackdrop = extras.getString(BACKDROP, INVALID);
		mYear = extras.getString(YEAR, "");

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(new TrendingAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().selectTab(mActionBar.getTabAt(position));
			}
		});

		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mActionBar.addTab(mActionBar.newTab().setText(R.string.overview).setTabListener(this));
		mActionBar.addTab(mActionBar.newTab().setText(R.string.actors).setTabListener(this));

		if (savedInstanceState != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt("selection", 0));
		}

		setTitle(mTitle);
		getActionBar().setSubtitle(mYear);
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
			case 0:
				return MovieDetailsFragment.newInstance(mMovieId, mTitle, mPoster, mBackdrop, mYear);
			default:
				return ActorsFragment.newInstance(mMovieId, MOVIES);
			}
		}  

		@Override  
		public int getCount() {  
			return 2;
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
}
