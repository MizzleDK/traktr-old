package com.miz.traktr.util;

public class TraktMovie extends TraktContent {

	private String mTagline, mTmdbId;
	private long mReleased;
	private boolean mHasWatched, mInCollection;
	
	public String getTagline() {
		return mTagline;
	}
	
	public void setTagline(String tagline) {
		mTagline = tagline;
	}
	
	public String getTmdbId() {
		return mTmdbId;
	}
	
	public void setTmdbId(String tmdbId) {
		mTmdbId = tmdbId;
	}
	
	public long getReleased() {
		return mReleased;
	}
	
	public void setReleased(long released) {
		// Trakt data is in seconds and we need it in milliseconds
		mReleased = released * 1000;
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
}
