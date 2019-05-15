package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class PodDownloadQueueHandle {


    private static final String DOWNLOAD_QUEUE_STORAGE_NAME = "com.rrpm.mzom.projectrrpm.PodDownloadQueueHandle.DOWNLOAD_QUEUE_STORAGE_TAG";
    private static final String DOWNLOAD_QUEUE_STORAGE_KEY = "com.rrpm.mzom.projectrrpm.PodDownloadQueueHandle.DOWNLOAD_QUEUE_STORAGE_TAG";

    private final SharedPreferences downloadQueueStorage;


    private PodDownloadQueueHandle(@NonNull Context context){

        this.downloadQueueStorage = context.getSharedPreferences(DOWNLOAD_QUEUE_STORAGE_NAME,0);

    }

    void storeDownloadQueue(@NonNull final ArrayList<RRPod> downloadQueue){

        downloadQueueStorage.edit().putString(DOWNLOAD_QUEUE_STORAGE_KEY, new Gson().toJson(downloadQueue)).apply();

    }

    ArrayList<RRPod> retrieveDownloadQueue(@NonNull ArrayList<RRPod> pods){

        final String downloadQueueJson = downloadQueueStorage.getString(DOWNLOAD_QUEUE_STORAGE_KEY, null);

        if(downloadQueueJson == null){

            return new ArrayList<>();

        }

        return new Gson().fromJson(downloadQueueJson, new TypeToken<ArrayList<RRPod>>() {}.getType());

    }





}
