package com.lashawnmcghee.hlsplayback.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lashawnmcghee.hlsplayback.R;
import com.lashawnmcghee.hlsplayback.util.ExoPlayerCacheUtil;

import java.util.List;

public class MediaListAdapter extends ArrayAdapter<String> {
    Context mContext;

    public MediaListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View createdView = null;
        ImageView ivCache;
        TextView tvURL;
        String url = getItem(position);
        Uri uri = Uri.parse(url);

        //Either recycle or create a new view for this item
        if(convertView != null) {
            createdView = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            createdView = inflater.inflate(R.layout.media_list_item, null);
        }

        //set our view tag
        createdView.setTag(url);

        //grab the cache icon element and fill it in based on item state in cache
        ivCache = createdView.findViewById(R.id.iv_cache);
        boolean bIsCached = ExoPlayerCacheUtil.getInstance(mContext)
                .getDownloadTracker()
                .isDownloaded(uri);
        setCacheIconState(bIsCached, ivCache, uri);

        //set the url text
        tvURL = createdView.findViewById(R.id.tv_url);
        tvURL.setText(url);

        return createdView;
    }

    /**
     * Sets the cache icon and click action for the cache icon position.
     * @param cached
     */
    private void setCacheIconState(boolean cached, ImageView cacheView, final Uri uri) {
        if(cached) {
            cacheView.setBackgroundResource(R.drawable.ic_delete_sweep_white_24dp);
            cacheView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExoPlayerCacheUtil.getInstance(getContext()).getDownloadTracker().removeMediaFromCache(uri);
                    view.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            cacheView.setBackgroundResource(R.drawable.baseline_save_alt_white_24dp);
            cacheView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExoPlayerCacheUtil.getInstance(getContext()).getDownloadTracker().downloadMediaToCache(mContext, uri);
                    view.setVisibility(View.INVISIBLE);
                }
            });
        }
        cacheView.setVisibility(View.VISIBLE);
    }
}
