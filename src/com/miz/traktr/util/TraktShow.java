package com.miz.traktr.util;

import java.util.Calendar;
import java.util.Locale;

public class TraktShow extends TraktContent {

	private String mTVDbId, mNetwork, mAirDay, mAirTime;
	private long mFirstAired;
	private boolean mContinuing;

	public String getTVDbId() {
		return mTVDbId;
	}

	public void setTVDbId(String id) {
		mTVDbId = id;
	}

	public long getFirstAired() {
		return mFirstAired * 1000;
	}

	public void setFirstAired(long firstAired) {
		mFirstAired = firstAired;
	}

	public boolean isContinuing() {
		return mContinuing;
	}

	public void setContinuing(boolean continuing) {
		mContinuing = continuing;
	}
	
	public String getNetwork() {
		return mNetwork;
	}
	
	public void setNetwork(String network) {
		mNetwork = network;
	}
	
	public String getAirDay() {
		return mAirDay;
	}
	
	public int getDayOfWeek() {
		String day = getAirDay().toLowerCase(Locale.getDefault());
		if (day.contains("monday"))
			return Calendar.MONDAY;
		else if (day.contains("tuesday"))
			return Calendar.TUESDAY;
		else if (day.contains("wednesday"))
			return Calendar.WEDNESDAY;
		else if (day.contains("thursday"))
			return Calendar.THURSDAY;
		else if (day.contains("friday"))
			return Calendar.FRIDAY;
		else if (day.contains("saturday"))
			return Calendar.SATURDAY;
		else
			return Calendar.SUNDAY;
	}
	
	public void setAirDay(String airDay) {
		mAirDay = airDay;
	}
	
	public String getAirTime() {
		return mAirTime;
	}
	
	public void setAirTime(String airTime) {
		mAirTime = airTime;
	}
}
