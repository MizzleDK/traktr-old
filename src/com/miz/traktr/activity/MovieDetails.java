package com.miz.traktr.activity;

import com.miz.traktr.fragment.MovieDetailsFragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.POSTER;
import static com.miz.traktr.util.Helper.BACKDROP;
import static com.miz.traktr.util.Helper.YEAR;
import static com.miz.traktr.util.Helper.INVALID;

public class MovieDetails extends BaseActivity {

	private static final String TAG = "MovieDetailsFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		
		String movieId = extras.getString(CONTENT_ID, "");
		String title = extras.getString(TITLE, "");
		String poster = extras.getString(POSTER, INVALID);
		String backdrop = extras.getString(BACKDROP, INVALID);
		String year = extras.getString(YEAR, "");
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG);
		if (frag == null) {
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(android.R.id.content, MovieDetailsFragment.newInstance(movieId, title, poster, backdrop, year), TAG);
			ft.commit();
		}
	}
	
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
