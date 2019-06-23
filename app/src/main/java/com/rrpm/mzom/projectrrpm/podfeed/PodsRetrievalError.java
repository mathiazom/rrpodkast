package com.rrpm.mzom.projectrrpm.podfeed;

import com.rrpm.mzom.projectrrpm.debugging.MessagedError;

import androidx.annotation.NonNull;

public enum PodsRetrievalError implements MessagedError {


    ALREADY_REQUESTED("This specific pod retrieval has already been requested"),

    FAILED_READER("There was an internal error with the feed reader"),

    INVALID_FEED("The underlying feed was not valid");


    private String message;


    PodsRetrievalError(String message){

        this.message = message;

    }

    @NonNull
    @Override
    public String getMessage() {
        return this.message;
    }
}
