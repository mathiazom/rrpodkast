package com.rrpm.mzom.projectrrpm.podfeed;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;

public class FeedPodsRetriever {

    private static final String TAG = "RRP-FeedPodsRetriever";


    public static void retrieve(@NonNull final PodType podType, @NonNull final PodsRetrievalCallback.PodListRetrievalCallback retrievePodsCallback){

        new AsyncTask<Void,Void,ArrayList<RRPod>>(){

            @Override
            protected ArrayList<RRPod> doInBackground(Void... voids) {

                final PodsFeedReader reader = new PodsFeedReader();

                ArrayList<RRPod> podList;

                try {

                    podList = reader.readPodsFeed(podType);

                } catch (PodsFeedReader.InvalidFeedException exception) {

                    final PodsFeedError exceptionError = exception.getError();

                    PodsRetrievalError error;

                    if (exceptionError == PodsFeedError.FAILED_DOCUMENT_BUILD ||
                            exceptionError == PodsFeedError.FAILED_FEED_PARSE) {

                        error = PodsRetrievalError.FAILED_READER;

                    } else if (exceptionError == PodsFeedError.INVALID_FEED_URL ||
                            exceptionError == PodsFeedError.NULL_FEED_LIST ||
                            exceptionError == PodsFeedError.EMPTY_FEED_LIST) {

                        error = PodsRetrievalError.INVALID_FEED;

                    } else {

                        Assertions._assert(false, "Invalid error type");

                        return null;

                    }

                    retrievePodsCallback.onFail(error);

                    return null;

                }


                return podList;

            }

            @Override
            protected void onPostExecute(ArrayList<RRPod> podList) {
                super.onPostExecute(podList);


                if(podList == null){

                    Log.e(TAG,"Pod list was null after retrieval");

                    return;

                }

                retrievePodsCallback.onPodListRetrieved(podList);


            }
        }.execute();

    }

}