package com.rrpm.mzom.projectrrpm.debugging;

import com.rrpm.mzom.projectrrpm.BuildConfig;

import androidx.annotation.NonNull;

public class AssertUtils {


    public static void _assert(boolean condition, @NonNull String message) {

        // Make sure debugging is enabled
        if(!BuildConfig.DEBUG){
            return;
        }

        if (!condition) {
            throw new FalseAssertionException(message);
        }

    }

    private static class FalseAssertionException extends RuntimeException{

        FalseAssertionException(String msg){
            super(msg);
        }

    }



}
