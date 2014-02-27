package com.miz.traktr.util;

public class GridEpisode {

	private String mTitle, mImage;
	private int mSeason, mEpisode, mNumber;
	private boolean mWatched;
	
	public GridEpisode(String title, int season, int episode, int number, boolean watched, String image) {
		mTitle = title;
		mSeason = season;
		mEpisode = episode;
		mNumber = number;
		mWatched = watched;
		mImage = image;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getSeason() {
		return mSeason;
	}
	
	public int getEpisode() {
		return mEpisode;
	}
	
	public int getNumber() {
		return mNumber;
	}
	
	public boolean hasWatched() {
		return mWatched;
	}
	
	public String getImage() {
		return mImage;
	}
}
