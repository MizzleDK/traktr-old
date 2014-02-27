package com.miz.traktr.app;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

/**
 * The Application class is a singleton, so we can keep stuff
 * that's often used in here, i.e. typefaces.
 * @author Michell
 *
 */
public class TraktrApplication extends Application {

	private static HashMap<String, Typeface> mTypefaces = new HashMap<String, Typeface>();

	public static Typeface getOrCreateTypeface(Context context, String key) {
		if (!mTypefaces.containsKey(key))
			mTypefaces.put(key, Typeface.createFromAsset(context.getAssets(), key));
		return mTypefaces.get(key);
	}

}
