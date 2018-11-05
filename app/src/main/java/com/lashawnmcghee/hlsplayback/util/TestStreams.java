/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback.util;

public final class TestStreams {
    private static final String[] HLS = {
            "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
            "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8"
    };

    private static final String HLS_TEST = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";

    /**
     * Provides an HLS URL string at the given index.
     * @param index Index of the URL to get.
     * @return Will return a string containing the URL at the given index.
     * If the index does not exist, a test URL will be given.
     */
    public static String getHLSLink(int index) {
        if(index >=0 && index < HLS.length) {
            return HLS[index];
        } else {
            return HLS_TEST;
        }
    }

    /**
     * Get the entire string array of HLS URLs.
     * @return String array of HLS URLs.
     */
    public static String[] getHlsArray() {
        return HLS;
    }
}
