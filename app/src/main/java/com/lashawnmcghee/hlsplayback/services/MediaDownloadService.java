/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.services;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.lashawnmcghee.hlsplayback.R;
import com.lashawnmcghee.hlsplayback.util.ExoPlayerCacheUtil;
import com.lashawnmcghee.hlsplayback.util.LogTrace;

/**
 * Download service to enable cache downloading.
 */
public class MediaDownloadService extends DownloadService {
    public static final String TAG = MediaDownloadService.class.getSimpleName();

    private static final String NOTIFICATION_CHANNEL_ID = "download_channel";
    private static final int SCHEDULER_JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    /**
     * Default constructor.
     */
    public MediaDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name);

        LogTrace.d(TAG, "MediaDownloadService has been created.");
    }

    /**
     * Returns a {@link DownloadManager} to be used to downloaded content. Called only once in the
     * life cycle of the service. The service will call {@link DownloadManager#startDownloads()} and
     * {@link DownloadManager#stopDownloads} as necessary when requirements returned by {@link
     * #getRequirements()} are met or stop being met.
     *
     * Use of our cache utility gives us the ability to control the cache used as well as cache location.
     */
    @Override
    protected DownloadManager getDownloadManager() {
        Context ctx = getApplicationContext();
        ExoPlayerCacheUtil util = ExoPlayerCacheUtil.getInstance(ctx);
        DownloadManager dm = util.getDownloadManager();
        return dm;
    }

    /**
     * Returns a {@link PlatformScheduler} to restart the service when requirements allowing downloads to take
     * place are met. If {@code null}, the service will only be restarted if the process is still in
     * memory when the requirements are met.
     */
    @Nullable
    @Override
    protected PlatformScheduler getScheduler() {
        PlatformScheduler scheduler = null;
        if(Build.VERSION.SDK_INT >= 21) {
            scheduler = new PlatformScheduler(this, SCHEDULER_JOB_ID);
            LogTrace.d(TAG, "A Platform scheduler was produced.");
        }
        return scheduler;
    }

    /**
     * Should be overridden in the subclass if the service will be run in the foreground.
     *
     * <p>Returns a notification to be displayed when this service running in the foreground.
     *
     * <p>This method is called when there is a task state change and periodically while there are
     * active tasks.
     *
     * <p>On API level 26 and above, this method may also be called just before the service stops,
     * with an empty {@code taskStates} array. The returned notification is used to satisfy system
     * requirements for foreground services.
     *
     * @param taskStates The states of all current tasks.
     * @return The foreground notification to display.
     */
    @Override
    protected Notification getForegroundNotification(DownloadManager.TaskState[] taskStates) {
        Notification progressNotification = DownloadNotificationUtil.buildProgressNotification(
                this,
                R.drawable.exo_controls_play,
                NOTIFICATION_CHANNEL_ID,
                null,
                null,
                taskStates);

        return progressNotification;
    }

    /**
     * Called when the state of a task changes.
     *
     * @param taskState The state of the task.
     */
    @Override
    protected void onTaskStateChanged(TaskState taskState) {
        //Ignore removal of cache items
        if (taskState.action.isRemoveAction) {
            return;
        }

        //lets build a notification based on the tasks state
        Notification notification = null;

        switch (taskState.state) {
            case TaskState.STATE_COMPLETED:
                notification = DownloadNotificationUtil.buildDownloadCompletedNotification(
                        this,
                        R.drawable.exo_controls_play,
                        NOTIFICATION_CHANNEL_ID,
                        null,
                        Util.fromUtf8Bytes(taskState.action.data));
                break;
            case TaskState.STATE_FAILED:
                notification = DownloadNotificationUtil.buildDownloadFailedNotification(
                        this,
                        R.drawable.exo_controls_play,
                        NOTIFICATION_CHANNEL_ID,
                        null,
                        Util.fromUtf8Bytes(taskState.action.data));
                break;
            case TaskState.STATE_CANCELED:
                Toast.makeText(getApplicationContext(), "Download was canceled!", Toast.LENGTH_LONG);
            case TaskState.STATE_QUEUED:
            case TaskState.STATE_STARTED:
            default:
                break;
        }

        //Now lets set the new notification
        int notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId;
        NotificationUtil.setNotification(this, notificationId, notification);
    }
}
