/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.interfaces;

/**
 * A simple interface used between the tracker and activities that want to reflect changes based on
 * media being downloaded to cache.
 */
public interface IDownloadListener {
    void onDownloadStatusChanged();
}
