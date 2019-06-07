package com.rrpm.mzom.projectrrpm.ui;

import com.rrpm.mzom.projectrrpm.rss.RRReader;

public final class PodUIConstants {

    // How many milliseconds (or more) listened to for pod progress to be displayed
    public static final int SHOW_PROGRESS_LIMIT = 5000;

    static final int SHOW_AS_NEW_LIMIT = 2*24*60*60*1000;

    public static final RRReader.PodType DEFAULT_POD_TYPE = RRReader.PodType.MAIN_PODS;

    public static final int FIRST_PODCAST_YEAR = 2012;

    public static final String POD_DATE_FORMAT = "EEEE d. MMMM yyyy";

}
