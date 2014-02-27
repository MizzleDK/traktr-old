package com.miz.traktr.util;

public class TraktEpisode extends TraktContent {

	private String mShowId;
	private int mSeason, mEpisode, mTVDBId;
	private long mFirstAired;
	private boolean mHasWatched, mInCollection;
	
	public String getShowId() {
		return mShowId;
	}
	
	public void setShowId(String showId) {
		mShowId = showId;
	}
	
	public int getSeason() {
		return mSeason;
	}
	
	public void setSeason(int season) {
		mSeason = season;
	}
	
	public int getEpisode() {
		return mEpisode;
	}
	
	public void setEpisode(int episode) {
		mEpisode = episode;
	}
	
	public boolean hasWatched() {
		return mHasWatched;
	}
	
	public void setHasWatched(boolean hasWatched) {
		mHasWatched = hasWatched;
	}
	
	public boolean inCollection() {
		return mInCollection;
	}
	
	public void setInCollection(boolean inCollection) {
		mInCollection = inCollection;
	}
	
	public long getFirstAired() {
		return mFirstAired * 1000;
	}

	public void setFirstAired(long firstAired) {
		mFirstAired = firstAired;
	}
	
	public int getShowTVDId() {
		return mTVDBId;
	}
	
	public void setTVDbId(int id) {
		mTVDBId = id;
	}
}
