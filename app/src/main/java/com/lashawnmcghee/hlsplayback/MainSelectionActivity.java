/*
 * Copyright (C) 2018 LLM HLS Playback Demo
 */
package com.lashawnmcghee.hlsplayback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lashawnmcghee.hlsplayback.adapters.MediaListAdapter;
import com.lashawnmcghee.hlsplayback.interfaces.IDownloadListener;
import com.lashawnmcghee.hlsplayback.util.ExoPlayerCacheUtil;
import com.lashawnmcghee.hlsplayback.util.LogTrace;
import com.lashawnmcghee.hlsplayback.util.TestStreams;

import java.util.Arrays;

public class MainSelectionActivity extends AppCompatActivity implements IDownloadListener,
        AdapterView.OnItemClickListener {
    private static final String TAG = MainSelectionActivity.class.getSimpleName();

    //adapter for rendering list choices
    MediaListAdapter mChoicesAdapter;

    //get the cache utility instance
    ExoPlayerCacheUtil mCacheUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mChoicesAdapter.notifyDataSetChanged();
        mCacheUtil.getDownloadTracker().addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCacheUtil.getDownloadTracker().removeListener(this);
    }

    /**
     * Lets init our UIElements
     */
    private void init() {
        mCacheUtil = ExoPlayerCacheUtil.getInstance(this);

        //Lets put our array of URLs into a simple array adapter to display to the user
        ListView choicesListView = findViewById(R.id.lv_choices);
        mChoicesAdapter = new MediaListAdapter(this,
                R.layout.media_list_item, Arrays.asList(TestStreams.getHlsArray()));
        choicesListView.setAdapter(mChoicesAdapter);
        choicesListView.setOnItemClickListener(this);

        //The below code are just dummy fillers to provide a header and footer divider
        LayoutInflater inflater = getLayoutInflater();
        TextView tvEmptyHeader = (TextView) inflater.inflate(R.layout.choice_list_item, null);
        TextView tvEmptyFooter = (TextView) inflater.inflate(R.layout.choice_list_item, null);
        choicesListView.addHeaderView(tvEmptyHeader);
        choicesListView.addFooterView(tvEmptyFooter);

        LogTrace.d(TAG, "Initialization complete.");
    }


    /**
     * Handle user clicks on the list view.
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int iCount = mChoicesAdapter.getCount();
        if(i > 0 && i <= iCount) {
            //Since we have a header, we must subtract to get the correct index into the streams array
            int iStreamIndex = i - 1;
            String sURL = mChoicesAdapter.getItem(iStreamIndex);
            Intent intent = new Intent(MainSelectionActivity.this, MediaPlayerActivity.class);
            intent.putExtra("urlString", sURL);
            startActivity(intent);
        }
    }

    @Override
    public void onDownloadStatusChanged() {
        if(mChoicesAdapter != null) {
            mChoicesAdapter.notifyDataSetChanged();
        }
    }
}
