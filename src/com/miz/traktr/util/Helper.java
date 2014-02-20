package com.miz.traktr.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Helper class to make certain operations easier.
 * Static calls are faster on Android, so methods
 * in this helper class should be used when possible.
 * @author Michell
 *
 */
public class Helper {

	// Account preferences
	public static final String PREF_HAS_ACCOUNT = "hasAccount";
	public static final String PREF_ACCOUNT_USERNAME = "accountUsername";
	public static final String PREF_ACCOUNT_PASS_SHA1 = "accountPassSHA1";
	public static final String PREF_ACCOUNT_REALNAME = "accountRealname";
	
	// Trakt API key
	public static final String TRAKT_API = "b85f6110fd2522022bc53614965415bf";
	
	/**
	 * Determines if an account exists.
	 * @param context Context used to get the default shared preferences.
	 * @return True if an account exists, false if not.
	 */
	public static boolean hasAccount(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_HAS_ACCOUNT, false);
	}
	
}
