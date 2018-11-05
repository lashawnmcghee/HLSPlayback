/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.google.android.exoplayer2.util.Util;
import com.lashawnmcghee.hlsplayback.util.ExoPlayerCacheUtil;
import com.lashawnmcghee.hlsplayback.util.HLSAppSharedPreferences;

/**
 * Class representation for the Application.
 */
public class HLSPlaybackApp extends Application {
    private static final String TAG = HLSPlaybackApp.class.getSimpleName();

    //application level shared preferences
    private static HLSAppSharedPreferences mPrefs;

    //application userAgent
    protected static String mUserAgent;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    /**
     * Initialize application level members
     */
    private void init() {
        Context context = getApplicationContext();
        String sAppName = getString(R.string.app_name);
        mPrefs = new HLSAppSharedPreferences(context, sAppName);
        mUserAgent = Util.getUserAgent(this, sAppName);
        ExoPlayerCacheUtil.getInstance(context);
    }

    /**
     * Get the application shared preferences.
     * @return
     */
    public static HLSAppSharedPreferences getPrefs() {
        return mPrefs;
    }

    /**
     * Get the user agent for this application.
     * @return
     */
    public static String getUserAgent() {
        return mUserAgent;
    }
}
