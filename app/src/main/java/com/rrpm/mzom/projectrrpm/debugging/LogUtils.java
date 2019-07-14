package com.rrpm.mzom.projectrrpm.debugging;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

import androidx.annotation.NonNull;

public class LogUtils {


    public static void logSharedPreferences(@NonNull SharedPreferences sharedPreferences, @NonNull String logTAG){

        final Map<String,?> keys = sharedPreferences.getAll();

        final StringBuilder logMessageBuilder = new StringBuilder();

        logMessageBuilder.append("\n");

        logMessageBuilder.append("Shared preferences key-value pairs from ").append(sharedPreferences.toString()).append(": {").append("\n");

        for(Map.Entry<String,?> entry : keys.entrySet()){

            logMessageBuilder.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");

        }

        logMessageBuilder.append("}");

        Log.i(logTAG, logMessageBuilder.toString());

    }








}
