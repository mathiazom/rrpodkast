package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

class PodsRepository {

    private static final String TAG = "RRP-PodsRepository";


    private static PodsRepository INSTANCE;

    private ArrayList<RRPod> retrievedPods;



    interface RetrievePodsCallback{
        void onPodsRetrieved(final ArrayList<RRPod> retrievedPods);
    }

    private PodsRepository(){

    }

    static PodsRepository getInstance(){

        if(INSTANCE == null){
            INSTANCE = new PodsRepository();
        }

        return INSTANCE;

    }

    void retrievePods(@NonNull Context context, @NonNull RRReader.PodType podType, @NonNull final RetrievePodsCallback retrievePodsCallback){

        if(retrievedPods != null){

            retrievePodsCallback.onPodsRetrieved(retrievedPods);

            return;

        }

        // Retrieve pods from RSS feed if WiFi is available, otherwise retrieve pods from offline cache

        Log.i(TAG,"IsConnected: " + ConnectionValidator.isConnected(context));

        if (ConnectionValidator.isConnected(context)) {

            PodsRSSRetriever.retrieve(context, podType, pods -> {

                retrievedPods = pods;

                retrievePodsCallback.onPodsRetrieved(pods);

            });

        } else {

            retrievedPods = new PodCacheHandle(context).retrieveCachedPods(podType);

            retrievePodsCallback.onPodsRetrieved(retrievedPods);

        }



    }



}
