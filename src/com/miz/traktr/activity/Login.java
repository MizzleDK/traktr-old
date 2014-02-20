package com.miz.traktr.activity;

import com.miz.traktr.fragment.LoginFragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class Login extends BaseActivity {

	private static final String TAG = "LoginFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Fragment frag = getFragmentManager().findFragmentByTag(TAG);
		if (frag == null) {
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new LoginFragment(), TAG);
			ft.commit();
		}	
	}
}