/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.util;

import android.util.Log;

import java.util.IllegalFormatException;

/**
 * Simple Log Trace container for the Android Logging functions.
 * This class will also allow control of message output based on debug or release via
 * setting of the debug flag by using setDebug().
 */
public class LogTrace {
    public static boolean mDebug = true;


    public static void setDebug( boolean bflag ) {
        mDebug = bflag;
    }

    public static void d(String strTag, String strMsg, Object... args ) {
        if(!mDebug) {
            return;
        }

        try {
            Log.d( strTag, (strMsg != null && args.length > 0) ? String.format(strMsg, args) : strMsg );
        } catch(IllegalFormatException e) {
            e.printStackTrace();
        }
    }

	public static void i(String strTag, String strMsg, Object... args ) {
        if(!mDebug) {
            return;
        }

		try {
			Log.i( strTag, (strMsg != null && args.length > 0) ? String.format(strMsg, args) : strMsg );
		}
		catch(IllegalFormatException e) {
			e.printStackTrace();
		}
	}

    public static void e(String strTag, String strMsg, Object... args )
    {
        if(!mDebug) {
            return;
        }

        try {
            Log.e( strTag, (strMsg != null && args.length > 0) ? String.format(strMsg, args) : strMsg );
        }
        catch(IllegalFormatException e) {
            e.printStackTrace();
        }
    }

    public static void e(String strTag, String strMsg, Throwable thr )
    {
        if(!mDebug) {
            return;
        }

        Log.e( strTag, strMsg, thr );
    }

	public static void w(String strTag, String strMsg, Object... args )
	{
        if(!mDebug) {
            return;
        }

		try {
			Log.w( strTag, (strMsg != null && args.length > 0) ? String.format(strMsg, args) : strMsg );
		}
		catch(IllegalFormatException e) {
			e.printStackTrace();
		}
	}

	public static void v(String strTag, String strMsg, Object... args ) {
        if(!mDebug) {
            return;
        }

		try {
			Log.v( strTag, (strMsg != null && args.length > 0) ? String.format(strMsg, args) : strMsg );
		} catch(IllegalFormatException e) {
		    e.printStackTrace();
		}
	}
}
