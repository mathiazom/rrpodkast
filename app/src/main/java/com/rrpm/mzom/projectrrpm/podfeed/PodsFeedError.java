package com.rrpm.mzom.projectrrpm.podfeed;

import com.rrpm.mzom.projectrrpm.debugging.MessagedError;

import androidx.annotation.NonNull;

public enum PodsFeedError implements MessagedError {


    FAILED_DOCUMENT_BUILD,
    INVALID_FEED_URL,
    INVALID_FEED_TYPE,
    FAILED_FEED_PARSE,
    NULL_FEED_LIST,
    EMPTY_FEED_LIST;


    @NonNull
    @Override
    public String getMessage() {
        return null;
    }
}
