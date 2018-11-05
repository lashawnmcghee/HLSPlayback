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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lashawnmcghee.hlsplayback.util.LogTrace;
import com.lashawnmcghee.hlsplayback.util.TestStreams;

public class MainSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = MainSelectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Lets init our UIElements
     */
    private void init() {
        //Lets put our array of URLs into a simple array adapter to display to the user
        ListView choicesListView = findViewById(R.id.lv_choices);
        ArrayAdapter<String> choicesAdapter = new ArrayAdapter<>(this,
                R.layout.choice_list_item, TestStreams.getHlsArray());
        choicesListView.setAdapter(choicesAdapter);
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
        if(i > 0 && i <= TestStreams.getHlsArray().length) {
            //Since we have a header, we must subtract to get the correct index into the streams array
            int iStreamIndex = i - 1;
            Intent intent = new Intent(MainSelectionActivity.this, MediaPlayerActivity.class);
            intent.putExtra("urlIndex", iStreamIndex);
            startActivity(intent);
        }
    }
}
