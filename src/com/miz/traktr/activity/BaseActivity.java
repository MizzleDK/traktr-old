package com.miz.traktr.activity;

import com.miz.traktr.R;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(null); // Hide the ActionBar title
		getActionBar().setLogo(R.drawable.traktr_logo); // Set the ActionBar logo
	}
	
}
