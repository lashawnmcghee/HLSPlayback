/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.lashawnmcghee.hlsplayback.interfaces.IDownloadListener;
import com.lashawnmcghee.hlsplayback.listeners.MediaDownloadTracker;
import com.lashawnmcghee.hlsplayback.listeners.PlayerEventListener;
import com.lashawnmcghee.hlsplayback.util.ExoPlayerCacheUtil;
import com.lashawnmcghee.hlsplayback.util.HLSAppSharedPreferences;
import com.lashawnmcghee.hlsplayback.util.TestStreams;

import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity implements IDownloadListener {
    private static final String TAG = MediaPlayerActivity.class.getSimpleName();

    //This is our main video view
    private PlayerView mPlayerView;
    private TextView mTitleView;
    private ImageView mDownloadButton;
    private ImageView mDeleteButton;

    //Our main player
    private SimpleExoPlayer mPlayer;
    private PlayerEventListener mEventListener;

    //the selected stream URL
    private String mStreamLink;
    private long mLastPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        bindUIElements();
    }

    /**
     * Lets bind our UIElements
     * This is normally taken care of by ButterKnife but showing work here...
     */
    private void bindUIElements() {
        mPlayerView = findViewById(R.id.pv_primary);
        mTitleView = findViewById(R.id.tv_title);
        mDownloadButton = findViewById(R.id.btn_download);
        mDeleteButton = findViewById(R.id.btn_delete);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get the media URL from our Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("urlIndex")) {
                int idx = bundle.getInt("urlIndex", -1);
                mStreamLink = TestStreams.getHLSLink(idx);
            } else {
                finish();
            }
        }
        mTitleView.setText(mStreamLink);

        //add ourself to listen for cache
        ExoPlayerCacheUtil.getInstance(this).getDownloadTracker().addListener(this);

        //safely start up the media
        startPlayer(mStreamLink);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove ourself as a listener for cache
        ExoPlayerCacheUtil.getInstance(this).getDownloadTracker().removeListener(this);

        //release player
        releasePlayer();
    }

    /**
     * Allows quick toggle of buttons based on provided media state.
     * @param hasDownload True if the media is not in cache and can be downloaded. False if the
     *                    media is already in our tracker.
     */
    private void updateButtons(boolean hasDownload) {
        if(hasDownload) {
            mDownloadButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.GONE);
        } else {
            mDownloadButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Safely start playing media whos URL has been supplied in our intent.
     * TODO: Move the model functionality to another class but keep the UI logic
     */
    private void startPlayer(String sMediaURL) {
        //release existing player if is was not destroyed
        if(mPlayer != null) {
            releasePlayer();
        }

        //convert to uri
        Uri uriToPlay = Uri.parse(sMediaURL);

        //is this media already in cache?
        MediaDownloadTracker tracker = ExoPlayerCacheUtil.getInstance(this).getDownloadTracker();
        if(!tracker.isDownloaded(uriToPlay)) {
            //give life to the download button
            mDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloadMedia(uriToPlay);
                    mDownloadButton.setVisibility(View.GONE);
                }
            });

            //fix button visibility
            updateButtons(true);
        } else {
            //give life to the delete button
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMedia(uriToPlay);
                    mDeleteButton.setVisibility(View.GONE);
                }
            });

            //fix button visibility
            updateButtons(false);
        }

        //init player
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        mPlayerView.setPlayer(mPlayer);

        //setup our media source by using the same factory setting as our cache components
        DataSource.Factory dsf = ExoPlayerCacheUtil.getInstance(this).buildDataSourceFactory();
        List<StreamKey> streamKeysList = tracker.getOfflineStreamKeys(uriToPlay);
        DefaultHlsPlaylistParserFactory hlsPlaylistParserFactory =
                new DefaultHlsPlaylistParserFactory(streamKeysList);
        MediaSource hms = new HlsMediaSource.Factory(dsf)
                .setPlaylistParserFactory(hlsPlaylistParserFactory)
                .createMediaSource(uriToPlay);

        //resume if necessary
        checkForResume();

        //lets create a listener to handle positioning as well as error reporting
        mEventListener = new PlayerEventListener(getApplicationContext(), mPlayer, mLastPosition);
        mPlayer.addListener(mEventListener);

        //now lets prepare our player in repeat mode and allow it to play when ready
        mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        mPlayer.prepare(hms);
        mPlayer.setPlayWhenReady(true);
    }

    /**
     * Common release for existing player
     */
    private void releasePlayer() {
        if(mPlayer != null) {
            long lngPosition = mPlayer.getCurrentPosition();
            if(lngPosition > 0) {
                HLSAppSharedPreferences prefs = HLSPlaybackApp.getPrefs();
                prefs.setLastViewedURL(mStreamLink);
                prefs.setLastViewedPosition(lngPosition);
            }
            mPlayerView.setPlayer(null);
            mPlayer.removeListener(mEventListener);
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * Checks to see if we need to resume video since it was viewed previously.
     * TODO: Move this...
     */
    private void checkForResume() {
        HLSAppSharedPreferences prefs = HLSPlaybackApp.getPrefs();
        String sLastURL = prefs.getLastViewedURL();
        if(sLastURL.equals(mStreamLink)) {
            mLastPosition = prefs.getLastViewedPosition();
        }
    }

    /**
     * Downloads media from a given URI if it is not already in cache.
     * @param uri URI to the media to be downloaded.
     */
    private void downloadMedia(Uri uri) {
        //download the selected media
        MediaDownloadTracker tracker = ExoPlayerCacheUtil.getInstance(this).getDownloadTracker();
        if(!tracker.isDownloaded(uri)) {
            tracker.downloadMediaToCache(this, uri);
        }
    }

    /**
     * Removes media from a given URL if it is in cache.
     * @param uri URI to the media to be downloaded.
     */
    private void removeMedia(Uri uri) {
        //download the selected media
        MediaDownloadTracker tracker = ExoPlayerCacheUtil.getInstance(this).getDownloadTracker();
        if(tracker.isDownloaded(uri)) {
            tracker.removeMediaFromCache(uri);
            finish();
        } else {
            updateButtons(true);
        }
    }

    /**
     * Implementation of the download change listener.
     * For now it is a nudge when there is an error or sucess.
     * TODO: Modify the internal utility to provide true error/success values.
     */
    @Override
    public void onDownloadStatusChanged() {
        recreate();
    }
}
