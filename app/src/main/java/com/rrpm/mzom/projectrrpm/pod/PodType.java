package com.rrpm.mzom.projectrrpm.pod;

import com.rrpm.mzom.projectrrpm.podfeed.PodsFeedConstants;

import java.net.URL;

import androidx.annotation.Nullable;

public enum PodType{


    MAIN_PODS(PodsFeedConstants.MAIN_PODS_FEED_URL),

    ARCHIVE_PODS(PodsFeedConstants.ARCHIVE_PODS_FEED_URL);


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
