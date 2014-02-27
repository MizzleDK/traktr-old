package com.miz.traktr.util;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A custom ImageView class that shows all images in a 1:1.78 format.
 * @author Michell
 *
 */
public class EpisodeImageView extends ImageView {

	public EpisodeImageView(Context context) {
		super(context);
	}

	public EpisodeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EpisodeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int) (width / 1.778);
		setMeasuredDimension(width, height);
	}
}