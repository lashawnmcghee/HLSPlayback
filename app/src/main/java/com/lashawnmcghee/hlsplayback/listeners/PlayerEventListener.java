/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.listeners;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.lang.ref.WeakReference;

/**
 * This is our default event listener for any media player that implements the Player
 * interface.  We hold on to the player by a Weak reference to avoid memory leaks.
 */
public class PlayerEventListener implements Player.EventListener {
    private static final String TAG = PlayerEventListener.class.getSimpleName();

    private WeakReference<Context> mWeakContext;
    private WeakReference<Player> mWeakPlayer;
    private long mStartPosition = 0;

    /**
     * When this listener is used, we must grab a weak reference to the player and its context
     * since it is not provided by the interface.
     * @param context Context for the provided media player.
     * @param media Media player to creating events we will receive
     */
    public PlayerEventListener(Context context, Player media) {
        mWeakContext = new WeakReference<>(context);
        mWeakPlayer = new WeakReference<>(media);
    }

    /**
     * When this listener is used, we must grab a weak reference to the player and its context
     * since it is not provided by the interface.
     * @param context Context for the provided media player.
     * @param media Media player whos events we will receive
     * @param position Starting position of the player when it is prepared.
     */
    public PlayerEventListener(Context context, Player media, long position) {
        mWeakContext = new WeakReference<>(context);
        mWeakPlayer = new WeakReference<>(media);
        mStartPosition = position;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Player player = mWeakPlayer.get();
        if(player != null) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_BUFFERING:
                    break;
                case Player.STATE_READY:
                    if (mStartPosition > 0) {
                        player.seekTo(mStartPosition);
                        mStartPosition = 0;
                    }
                    break;
                case Player.STATE_ENDED:
                default:
                    break;
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Context ctx = mWeakContext.get();
        if(ctx != null) {
            Toast toast = Toast.makeText(ctx, error.toString(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}
