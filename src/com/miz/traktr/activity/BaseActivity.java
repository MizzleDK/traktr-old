package com.miz.traktr.activity;

import com.miz.traktr.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(null); // Hide the ActionBar title
		getActionBar().setIcon(R.drawable.white_app_icon); // Set the ActionBar icon
	}
	
}
