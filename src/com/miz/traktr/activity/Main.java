package com.miz.traktr.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.miz.traktr.R;
import com.miz.traktr.fragment.TrendingFragment;

import static com.miz.traktr.util.Helper.MOVIES;
import static com.miz.traktr.util.Helper.TV_SHOWS;

public class Main extends BaseActivity implements ActionBar.TabListener {

	private ViewPager mViewPager;
	private ActionBar mActionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.viewpager);
		
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setAdapter(new TrendingAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().selectTab(mActionBar.getTabAt(position));
			}
		});
		
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mActionBar.addTab(mActionBar.newTab().setText(R.string.movies).setTabListener(this));
		mActionBar.addTab(mActionBar.newTab().setText(R.string.tv_shows).setTabListener(this));
		
		if (savedInstanceState != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt("selection", 0));
		}

		setTitle(R.string.trending);
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
				return TrendingFragment.newInstance(MOVIES);
			default:
				return TrendingFragment.newInstance(TV_SHOWS);
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
}