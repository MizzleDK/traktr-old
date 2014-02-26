package com.miz.traktr.util;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.miz.traktr.R;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class CoverItem implements Target {

	public TextView text, subtext;
	public ImageView cover;
	private static final int ANIMATION_DURATION = 200;

	@Override
	public void onBitmapFailed(Drawable arg0) {
		cover.setBackgroundColor(Color.TRANSPARENT);
		cover.setImageResource(R.drawable.loading_image);
		ObjectAnimator.ofFloat(cover, "alpha", 0f, 1f).setDuration(ANIMATION_DURATION).start();
	}
	@Override
	public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
		cover.setBackgroundColor(Color.TRANSPARENT);
		cover.setImageBitmap(arg0);
		ObjectAnimator.ofFloat(cover, "alpha", 0f, 1f).setDuration(ANIMATION_DURATION).start();
	}
	@Override
	public void onPrepareLoad(Drawable arg0) {
		cover.setBackgroundColor(Color.WHITE);
		cover.setImageBitmap(null);
	}
}