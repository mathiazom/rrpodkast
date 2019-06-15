package com.rrpm.mzom.projectrrpm.pod;

import com.rrpm.mzom.projectrrpm.podfeed.PodFeedConstants;

import java.net.URL;

import androidx.annotation.Nullable;

public enum PodType{


    MAIN_PODS(PodFeedConstants.MAIN_PODS_FEED_URL),

    ARCHIVE_PODS(PodFeedConstants.ARCHIVE_PODS_FEED_URL);


    @Nullable
    private URL feedUrl;


    PodType(@Nullable final URL feedUrl){
        this.feedUrl = feedUrl;
    }

    @Nullable
    public URL getFeedUrl() {
        return feedUrl;
    }
}
