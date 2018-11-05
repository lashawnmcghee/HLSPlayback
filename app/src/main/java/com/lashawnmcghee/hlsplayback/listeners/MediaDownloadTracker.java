/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import com.lashawnmcghee.hlsplayback.R;
import com.lashawnmcghee.hlsplayback.interfaces.IDownloadListener;
import com.lashawnmcghee.hlsplayback.services.MediaDownloadService;
import com.lashawnmcghee.hlsplayback.util.LogTrace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A media download tracker based on a demo provided by ExoPlayer.
 */
public class MediaDownloadTracker implements DownloadManager.Listener {
    private static final String TAG = MediaDownloadTracker.class.getSimpleName();

    private final Context mContext;
    private final ActionFile mActionFile;
    private final Handler mActionFileWriteHandler;
    private final DataSource.Factory mDataSourceFactory;
    private final TrackNameProvider mTrackNameProvider;
    private final CopyOnWriteArraySet<IDownloadListener> mListeners;
    private final HashMap<Uri, DownloadAction> mTrackedDownloadStates;

    public MediaDownloadTracker(Context context,
                                DataSource.Factory dataSourceFactory,
                                File actionFile,
                                DownloadAction.Deserializer... deserializers) {

        mContext = context.getApplicationContext();
        mDataSourceFactory = dataSourceFactory;
        mActionFile = new ActionFile(actionFile);
        mTrackNameProvider = new DefaultTrackNameProvider(context.getResources());
        mListeners = new CopyOnWriteArraySet<>();
        mTrackedDownloadStates = new HashMap<>();

        //create handler thread for writing to the action file
        HandlerThread actionFileWriteThread = new HandlerThread("DownloadTracker");
        actionFileWriteThread.start();
        mActionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());

        //deserialize saved actions which may be used and filters
        if(deserializers.length > 0) {
            loadTrackedActions(deserializers);
        } else {
            loadTrackedActions(DownloadAction.getDefaultDeserializers());
        }
    }

    /**
     * Determines if the provided URI already exists in our download cache.
     * Note: It may be partial or complete based on network availability during initial download.
     * @param uri
     * @return
     */
    public boolean isDownloaded(Uri uri) {
        boolean bDownloaded = mTrackedDownloadStates.containsKey(uri);
        return bDownloaded;
    }

    /**
     * Gets a list of stream keys that are available for a given URI.
     * @param uri URI who keys are to be retrieved.
     * @return Returns a list of StreamKeys corresponding to the provided uri and an empty List
     * otherwise.
     */
    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!mTrackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        }
        return mTrackedDownloadStates.get(uri).getKeys();
    }

    /**
     * Attempts to download a given URI to cache.
     * This will provide a dialog to the end user so they may select the tracks they would like
     * downloaded for the adaptive stream.
     * @param activityContext Context of the activity or application responsible for the call.
     * @param uri URI to the media to be downloaded.
     */
    public void downloadMediaToCache(Context activityContext, Uri uri) {
        //check if media already exists in cache
        if(!isDownloaded(uri)) {
            //since media is not in cache, ask end user which tracks to download
            String sName = uri.toString();
            HlsDownloadHelper hlsHelper = new HlsDownloadHelper(uri, mDataSourceFactory);
            StartDownloadDialogHelper helper = new StartDownloadDialogHelper(
                    activityContext,
                    hlsHelper,
                    sName);
            helper.prepare();
        }
    }

    /**
     * Removes downloaded media from cache.
     * @param uri URI of the media to remove from cache.
     */
    public void removeMediaFromCache(Uri uri) {
        //first ensure the media is in cache
        if(isDownloaded(uri)) {
            //since the media exists in cache, lets remove it
            String sName = uri.toString();
            HlsDownloadHelper hlsHelper = new HlsDownloadHelper(uri, mDataSourceFactory);
            DownloadAction removeAction = hlsHelper.getRemoveAction(Util.getUtf8Bytes(sName));
            startServiceWithAction(removeAction);
        }
    }

    /**
     * Add a listener to know when a download or remove is complete.
     * @param listener A class which implements the IDownloadListener interface.
     */
    public void addListener(IDownloadListener listener) {
        mListeners.add(listener);
    }

    /**
     * Remove a listener from this tracker.
     * @param listener A class which implements the IDownloadListener interface.
     */
    public void removeListener(IDownloadListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onInitialized(DownloadManager downloadManager) {
        // Do nothing.
    }

    /**
     * When task states change, we will want to do some actions and notify our listeners.
     * @param downloadManager
     * @param taskState
     */
    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, DownloadManager.TaskState taskState) {
        DownloadAction action = taskState.action;
        Uri uri = action.uri;
        if ((action.isRemoveAction && taskState.state == DownloadManager.TaskState.STATE_COMPLETED)
                || (!action.isRemoveAction && taskState.state == DownloadManager.TaskState.STATE_FAILED)) {
            // A download has been removed, or has failed. Stop tracking it.
            if (mTrackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged();
            }
        }
    }

    /**
     *
     * @param downloadManager
     */
    @Override
    public void onIdle(DownloadManager downloadManager) {
        // Do nothing.
    }


    /**
     *
     * @param deserializers
     */
    private void loadTrackedActions(DownloadAction.Deserializer[] deserializers) {
        try {
            DownloadAction[] allActions = mActionFile.load(deserializers);
            for (DownloadAction action : allActions) {
                mTrackedDownloadStates.put(action.uri, action);
            }
        } catch (IOException e) {
            LogTrace.e(TAG, "Failed to load tracked actions", e);
        }
    }

    /**
     * Notify our listeners and store our download action to a file.
     */
    private void handleTrackedDownloadStatesChanged() {
        for (IDownloadListener listener : mListeners) {
            listener.onDownloadStatusChanged();
        }

        final DownloadAction[] actions = mTrackedDownloadStates.values().toArray(new DownloadAction[0]);
        mActionFileWriteHandler.post(
                () -> {
                    try {
                        mActionFile.store(actions);
                    } catch (IOException e) {
                        LogTrace.e(TAG, "Failed to store tracked actions", e);
                    }
                });
    }

    /**
     * Start downloading one or more URI(s) and notify our listeners.
     * @param action
     */
    private void startDownload(DownloadAction action) {
        if (mTrackedDownloadStates.containsKey(action.uri)) {
            // This content is already being downloaded. Do nothing.
            return;
        }
        mTrackedDownloadStates.put(action.uri, action);
        handleTrackedDownloadStatesChanged();
        startServiceWithAction(action);
    }

    /**
     * Starts our DownloadService with an action whether adding or removing.
     * @param action
     */
    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(mContext, MediaDownloadService.class, action, false);
    }

    /**
     * Inner support class for the download dialog creation.
     */
    private final class StartDownloadDialogHelper
            implements DownloadHelper.Callback, DialogInterface.OnClickListener {

        private final DownloadHelper downloadHelper;
        private final String name;

        private final AlertDialog.Builder builder;
        private final View dialogView;
        private final List<TrackKey> trackKeys;
        private final ArrayAdapter<String> trackTitles;
        private final ListView representationList;

        public StartDownloadDialogHelper(
                Context context, DownloadHelper downloadHelper, String name) {
            this.downloadHelper = downloadHelper;
            this.name = name;
            builder =
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.exo_download_description)
                            .setPositiveButton(android.R.string.ok, this)
                            .setNegativeButton(android.R.string.cancel, null);

            // Inflate with the builder's context to ensure the correct style is used.
            LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());
            dialogView = dialogInflater.inflate(R.layout.start_download_dialog, null);

            trackKeys = new ArrayList<>();
            trackTitles =
                    new ArrayAdapter<>(
                            builder.getContext(), android.R.layout.simple_list_item_multiple_choice);
            representationList = dialogView.findViewById(R.id.representation_list);
            representationList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            representationList.setAdapter(trackTitles);
        }

        public void prepare() {
            downloadHelper.prepare(this);
        }

        @Override
        public void onPrepared(DownloadHelper helper) {
            for (int i = 0; i < downloadHelper.getPeriodCount(); i++) {
                TrackGroupArray trackGroups = downloadHelper.getTrackGroups(i);
                for (int j = 0; j < trackGroups.length; j++) {
                    TrackGroup trackGroup = trackGroups.get(j);
                    for (int k = 0; k < trackGroup.length; k++) {
                        trackKeys.add(new TrackKey(i, j, k));
                        trackTitles.add(mTrackNameProvider.getTrackName(trackGroup.getFormat(k)));
                    }
                }
            }
            if (!trackKeys.isEmpty()) {
                builder.setView(dialogView);
            }
            builder.create().show();
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(mContext.getApplicationContext(),
                    R.string.download_start_error,
                    Toast.LENGTH_LONG).show();
            LogTrace.e(TAG, "Failed to start download", e);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ArrayList<TrackKey> selectedTrackKeys = new ArrayList<>();
            for (int i = 0; i < representationList.getChildCount(); i++) {
                if (representationList.isItemChecked(i)) {
                    selectedTrackKeys.add(trackKeys.get(i));
                }
            }
            if (!selectedTrackKeys.isEmpty() || trackKeys.isEmpty()) {
                // We have selected keys, or we're dealing with single stream content.
                DownloadAction downloadAction =
                        downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), selectedTrackKeys);
                startDownload(downloadAction);
            }
        }
    }
}
