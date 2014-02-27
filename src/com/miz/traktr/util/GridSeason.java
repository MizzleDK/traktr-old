package com.miz.traktr.util;

public class GridSeason {
	
	private String mPoster;
	private int mSeason, mEpisodeCount;
	
	public GridSeason(int season, int episodeCount, String poster) {
		mSeason = season;
		mEpisodeCount = episodeCount;
		mPoster = poster;
	}

	public int getSeason() {
		return mSeason;
	}
	
	public int getEpisodeCount() {
		return mEpisodeCount;
	}

	public String getPoster() {
		return mPoster;
	}
}