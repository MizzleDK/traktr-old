package com.miz.traktr.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.miz.traktr.R;
import com.miz.traktr.fragment.EpisodesFragment;

import static com.miz.traktr.util.Helper.CONTENT_ID;
import static com.miz.traktr.util.Helper.TITLE;
import static com.miz.traktr.util.Helper.SEASON;;

public class ShowSeasonDetails extends BaseActivity {

	private static final String TAG = "EpisodesFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		String title = extras.getString(TITLE);
		String showId = extras.getString(CONTENT_ID);
		int season = extras.getInt(SEASON);

		Fragment frag = getSupportFragmentManager().findFragmentByTag(TAG);
		if (frag == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, EpisodesFragment.newInstance(showId, title, season), TAG);
			ft.commit();
		}

		setTitle(title);
		if (season == 0)
			getActionBar().setSubtitle(R.string.specials);
		else
			getActionBar().setSubtitle(String.format(getString(R.string.season), season));
	}

}
