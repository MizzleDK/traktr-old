package com.miz.traktr.util;

public class GridActor {
	
	private String mName, mCharacter, mPoster;
	
	public GridActor(String name, String character, String poster) {
		mName = name;
		mCharacter = character;
		mPoster = poster;
	}

	/**
	 * Get actor name
	 * @return Actor name or "null" if no mapping exists.
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Get character name
	 * @return Character name or "null" if no mapping exists.
	 */
	public String getCharacter() {
		return mCharacter;
	}

	public String getPoster() {
		return mPoster;
	}
}