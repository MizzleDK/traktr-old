package com.miz.traktr.util;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A custom ImageView class that shows all images in a 16:10 format.
 * @author Michell
 *
 */
public class BackdropImageView extends ImageView {

	public BackdropImageView(Context context) {
		super(context);
	}

	public BackdropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackdropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int) (width / 1.6);
		setMeasuredDimension(width, height);
	}
}