package com.rrpm.mzom.projectrrpm.podfeed;

public enum PodRetrievalError {


    ALREADY_REQUESTED("This specific pod retrieval has already been requested"),

    FAILED_READING_FEED("There was an external error in the pods feed");


    private String message;


    PodRetrievalError(String message){

        this.message = message;

    }

    public String getMessage() {
        return message;
    }
}
