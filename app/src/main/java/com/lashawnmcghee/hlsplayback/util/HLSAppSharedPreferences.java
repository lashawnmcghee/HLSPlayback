/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class encapsulates common app wide preferences that may be stored between sessions.
 * This also prevents distributed team members from using incorrect key values to store and
 * retrieve values for these preferences.
 */
public class HLSAppSharedPreferences {
    private static final String NULL_STRING = "";
    private Context mContext;
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;

    /**
     * Constuctor for this class.
     * @param context The application context associated with these preferences.
     * @param strName Unique name for the preference set to be used/modified.
     */
    public HLSAppSharedPreferences(Context context, String strName) {

        mContext = context;
        mPreference = context.getSharedPreferences(strName, Activity.MODE_PRIVATE);
        mEditor = mPreference.edit();
    }

    /**
     * Set last URL
     *
     * @param url Last viewed URL.
     */
    public void setLastViewedURL(String url) {
        mEditor.putString("LastViewedURL", url);
        mEditor.commit();
    }

    /**
     * Get last viewed URL.
     *
     * @return Returns the last viewed URL. If it does not exist,
     * a null string "" is returned for easy string comparisons.
     */
    public String getLastViewedURL() {
        return mPreference.getString("LastViewedURL", NULL_STRING);
    }

    /**
     * Set last viewed position.
     *
     * @param position Current position of the playing media.
     */
    public void setLastViewedPosition(long position) {
        mEditor.putLong("LastViewedPosition", position);
        mEditor.commit();
    }

    /**
     * Get last viewed position.
     *
     * @return
     */
    public long getLastViewedPosition() {
        return mPreference.getLong("LastViewedPosition", 0);
    }
}
