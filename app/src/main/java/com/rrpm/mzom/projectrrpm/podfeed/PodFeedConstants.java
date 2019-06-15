package com.rrpm.mzom.projectrrpm.podfeed;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.Nullable;

public final class PodFeedConstants {


    private static final String MAIN_PODS_FEED_URL_STRING = "https://podkast.nrk.no/program/radioresepsjonen.rss";

    @Nullable
    public static URL MAIN_PODS_FEED_URL = null;

    static {
        try {
            MAIN_PODS_FEED_URL = new URL(MAIN_PODS_FEED_URL_STRING);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String ARCHIVE_PODS_FEED_URL_STRING = "https://podkast.nrk.no/program/radioresepsjonens_arkivpodkast.rss";

    @Nullable
    public static URL ARCHIVE_PODS_FEED_URL = null;

    static {
        try {
            ARCHIVE_PODS_FEED_URL = new URL(ARCHIVE_PODS_FEED_URL_STRING);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    static final String ELEMENT_ID_TAG_NAME = "guid";

    static final String RAW_DATE_TAG_NAME = "pubDate";

    static final String URL_ITEM_NAME = "url";

    static final String DURATION_TAG_NAME = "itunes:duration";

    static final String TITLE_TAG_NAME = "title";

    static final String DESCRIPTION_TAG_NAME = "description";



    public static final String RR_FACEBOOK_PAGE_URL = "https://www.facebook.com/radioresepsjonenp13";


}
