package com.miz.traktr.util;

public abstract class TraktContent {

	private String mTitle, mId, mPoster, mBackdrop, mYear, mUrl, mOverview, mCertification, mGenres, mRating;
	private int mRuntime, mRatingsPercentage, mRatingsVotes, mRatingsLoved, mRatingsHated, mRatingAdvanced;
	private boolean mHasLoaded, mInWatchlist;

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getPoster() {
		return mPoster;
	}

	public void setPoster(String poster) {
		mPoster = poster;
	}

	public String getBackdrop() {
		return mBackdrop;
	}

	public void setBackdrop(String backdrop) {
		mBackdrop = backdrop;
	}

	public String getYear() {
		return mYear;
	}

	public void setYear(String year) {
		mYear = year;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public int getRuntime() {
		return mRuntime;
	}

	public void setRuntime(int runtime) {
		mRuntime = runtime;
	}

	public String getOverview() {
		return mOverview;
	}

	public void setOverview(String overview) {
		mOverview = overview;
	}

	public String getCertification() {
		return mCertification;
	}

	public void setCertification(String certification) {
		mCertification = certification;
	}

	public String getGenres() {
		return mGenres;
	}

	public void setGenres(String genres) {
		mGenres = genres;
	}

	public int getRatingsPercentage() {
		return mRatingsPercentage;
	}

	public void setRatingsPercentage(int ratingsPercentage) {
		mRatingsPercentage = ratingsPercentage;
	}

	public int getRatingsVotes() {
		return mRatingsVotes;
	}

	public void setRatingsVotes(int ratingsVotes) {
		mRatingsVotes = ratingsVotes;
	}

	public int getRatingsLoved() {
		return mRatingsLoved;
	}

	public void setRatingsLoved(int ratingsLoved) {
		mRatingsLoved = ratingsLoved;
	}

	public int getRatingsHated() {
		return mRatingsHated;
	}

	public void setRatingsHated(int ratingsHated) {
		mRatingsHated = ratingsHated;
	}
	
	public boolean hasLoaded() {
		return mHasLoaded;
	}
	
	public void setHasLoaded(boolean hasLoaded) {
		mHasLoaded = hasLoaded;
	}
	
	public String getRating() {
		return mRating;
	}
	
	public void setRating(String rating) {
		mRating = rating;
	}
	
	public int getAdvancedRating() {
		return mRatingAdvanced;
	}
	
	public void setAdvancedRating(int advancedRating) {
		mRatingAdvanced = advancedRating;
	}
	
	public boolean inWatchlist() {
		return mInWatchlist;
	}
	
	public void setInWatchlist(boolean toWatch) {
		mInWatchlist = toWatch;
	}
}