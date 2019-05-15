package com.rrpm.mzom.projectrrpm;

final class PodStorageConstants {


    // How many milliseconds (or less) left for the progressBar bar to be greyed out (marking pod as listened to)
    static final int LISTENED_TO_LIMIT = 30000;

    // Determines how close the stored and actual pod duration must be to be considered equal
    static final int STORED_DURATION_OFFSET_LIMIT = 1000;

    // How often the progressBar display should be refreshed (in milliseconds)
    static final int PROGRESS_REFRESH_FREQ_MS = 500;

    // How often the player progressBar should be saved to Shared Preferences (in milliseconds)
    static final int SAVE_PROGRESS_FREQ_MS = 5000;


    /** WARNING: Changing this value will make old downloads unavailable **/
    static final String POD_STORAGE_SUB_DIRECTORY = "RRPods";

    static final String POD_PROGRESS_STORAGE = "pod_progress_storage";

    static final String POD_PROGRESS_SUFFIX = "_pod_progress";

    static final String LAST_PLAYED_POD_STORAGE = "last_played_pod_storage";

    static final String LAST_PLAYED_POD_ID = "last_played_pod_id";


}