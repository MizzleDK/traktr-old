package com.miz.traktr.activity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.miz.traktr.util.Helper;

/**
 * This is an invisible Activity not visible to the user.
 * Its purpose is to determine if the user has already logged
 * in with Trakt or if they need to sign up.
 * @author Michell
 *
 */
public class StartActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean hasAccount = Helper.hasAccount(this);
		
		// Create a launch Intent based on the account status
		Intent launchIntent = new Intent(this, hasAccount ? Main.class : Login.class);
		startActivity(launchIntent);
		
		// Finish the invisible Activity now that it has done its job.
		finish();
	}
	
}