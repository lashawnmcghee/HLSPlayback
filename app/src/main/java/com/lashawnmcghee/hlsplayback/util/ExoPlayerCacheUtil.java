/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.util;

import android.content.Context;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.lashawnmcghee.hlsplayback.BuildConfig;
import com.lashawnmcghee.hlsplayback.HLSPlaybackApp;
import com.lashawnmcghee.hlsplayback.listeners.MediaDownloadTracker;

import java.io.File;

/**
 * The ExoPlayer Cache Utility is a singleton which may be used application/service wide.
 * This utility keeps the entire app in sync as to the cache location, tracked URIs, etc.
 * It allows anyone to check if a URI is stored in cache as well as the download and removal of a
 * unique URI.
 *
 * This utility will allow user to get both DownloadManager and DownloadTracker that are associated.
 *
 * For now, the etire demo was minimized to support HLS streams specifically but may be easily
 * expanded for DASH and other formats.
 */
public class ExoPlayerCacheUtil {
    private static final String TAG = ExoPlayerCacheUtil.class.getSimpleName();

    private static ExoPlayerCacheUtil instance = null;
    private Context mContext;

    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;

    private File mDownloadDirectory;
    private Cache mDownloadCache;
    private DownloadManager mDownloadManager;
    private MediaDownloadTracker mDownloadTracker;

    /**
     * Hidden class constructor.
     */
    private ExoPlayerCacheUtil() {
        //Do nothing...
    }

    /**
     * Hidden class constructor.
     */
    private ExoPlayerCacheUtil(Context context) {
        mContext = context;
        init();
    }

    /**
     * Get the one and only instance of this class.
     * The first calling thread will create an initial instance.
     * This method will only be synchronized on the first call,
     * thus it will not affect speed of our app.
     * @return
     */
    public static ExoPlayerCacheUtil getInstance(Context context) {
        synchronized(ExoPlayerCacheUtil.class) {
            if (instance == null) {
                instance = new ExoPlayerCacheUtil(context);
            }
        }
        return instance;
    }

    /**
     * Initialization of class workers.
     */
    private void init() {
        //Do nothing for now.
    }

    /**
     * Release this instance of the utility.
     */
    public void release() {
        mDownloadManager.release();
        mDownloadManager = null;
        mDownloadTracker = null;

        //Should we release the cache here? The background service works even when the app is not.
        //mDownloadCache.release();
        mDownloadCache = null;
        mDownloadDirectory = null;

        mContext = null;
        instance = null;
    }

    /**
     * Returns a {@link DataSource.Factory}.
     * Use of this data source will keep the app in sync with the created cache.
     */
    public DataSource.Factory buildDataSourceFactory() {
        HttpDataSource.Factory httpDSF = buildHttpDataSourceFactory();
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(mContext, httpDSF);
        Cache cache = getDownloadCache();
        CacheDataSourceFactory cacheDSF = buildReadOnlyCacheDataSource(upstreamFactory, cache);
        return cacheDSF;
    }

    /**
     * Returns a {@link HttpDataSource.Factory}.
     * Use of this data source will keep the app in sync with the created cache.
     */
    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(HLSPlaybackApp.getUserAgent());
    }

    /**
     * Returns whether extension renderers should be used.
     */
    public boolean useExtensionRenderers() {
        return "withExtensions".equals(BuildConfig.FLAVOR);
    }

    /**
     * Grab the download manager associated with this application.
     * The DownloadManager will be initialized if not already done.
     * @return
     */
    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return mDownloadManager;
    }

    /**
     * Grab the download tracker associated with this application.
     * The DownloadManager will be initialized if not already done.
     * @return
     */
    public MediaDownloadTracker getDownloadTracker() {
        initDownloadManager();
        return mDownloadTracker;
    }

    /**
     * Initialize our download manager and trackers associated with this application.
     */
    private synchronized void initDownloadManager() {
        if (mDownloadManager == null) {
            //first initialize the download manager
            Cache cache = getDownloadCache();
            HttpDataSource.Factory httpDSF = buildHttpDataSourceFactory();
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(cache, httpDSF);
            File actionFile = new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE);
            mDownloadManager = new DownloadManager(downloaderConstructorHelper,
                    MAX_SIMULTANEOUS_DOWNLOADS,
                    DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                    actionFile);

            //second initialize the download tracker
            DataSource.Factory dsf = buildDataSourceFactory();
            File trackerActionFile = new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE);
            mDownloadTracker = new MediaDownloadTracker(mContext, dsf,trackerActionFile);

            //allow our tracker to listen to the download manager
            mDownloadManager.addListener(mDownloadTracker);
        }
    }

    /**
     * Get the download cache to be used with this application.
     * @return
     */
    private synchronized Cache getDownloadCache() {
        if (mDownloadCache == null) {
            File rootDirectory = getDownloadDirectory();
            File downloadContentDirectory = new File(rootDirectory, DOWNLOAD_CONTENT_DIRECTORY);
            mDownloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return mDownloadCache;
    }

    /**
     * Get the download directory to be used with this application
     * @return
     */
    private File getDownloadDirectory() {
        if (mDownloadDirectory == null) {
            mDownloadDirectory = mContext.getExternalFilesDir(null);
            if (mDownloadDirectory == null) {
                mDownloadDirectory = mContext.getFilesDir();
            }
        }
        return mDownloadDirectory;
    }

    /**
     * Build the cache data source factory that will be used to play offline media.
     * @param upstreamFactory
     * @param cache
     * @return
     */
    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {

        FileDataSourceFactory fileDSF = new FileDataSourceFactory();
        CacheDataSourceFactory cacheDSF = new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                fileDSF,
                null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                null);

        return cacheDSF;
    }
}
