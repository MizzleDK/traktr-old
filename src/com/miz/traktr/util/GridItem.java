package com.miz.traktr.util;

public class GridItem {
	
	private String mTitle, mYear, mId, mAlternateId, mPoster, mBackdrop;
	
	public GridItem(String title, String year, String id, String alternateId, String poster, String backdrop) {
		mTitle = title;
		mYear = year;
		mId = id;
		mAlternateId = alternateId;
		mPoster = poster;
		mBackdrop = backdrop;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getYear() {
		return mYear;
	}

	public String getId() {
		return mId;
	}
	
	public String getAlternateId() {
		return mAlternateId;
	}

	public String getPoster() {
		return mPoster;
	}
	
	public String getBackdrop() {
		return mBackdrop;
	}
}