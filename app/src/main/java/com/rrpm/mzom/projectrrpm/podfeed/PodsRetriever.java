package com.rrpm.mzom.projectrrpm.podfeed;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.rrpm.mzom.projectrrpm.podstorage.PodCacheHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;

public class PodsRetriever {

    private static final String TAG = "RRP-PodsRetriever";




    public static void retrieve(@NonNull final Context context, @NonNull final PodType podType, @NonNull final PodsRetrievalCallback.RetrievePodListCallback retrievePodsCallback){

        new AsyncTask<Void,Void,ArrayList<RRPod>>(){

            @Override
            protected ArrayList<RRPod> doInBackground(Void... voids) {

                final PodsFeedReader reader = new PodsFeedReader();

                final ArrayList<RRPod> pods;
                try {

                    pods = reader.readPodsFeed(podType);

                } catch (PodsFeedReader.InvalidFeedException e) {

                    retrievePodsCallback.onFail(PodRetrievalError.FAILED_READING_FEED);

                    return null;
                }

                final PodStorageHandle podStorageHandle = new PodStorageHandle(context);

                for(RRPod pod : pods){
                    podStorageHandle.applyPodStorageValues(pod);
                }

                new PodCacheHandle(context).cachePodList(pods, podType);

                return pods;
            }

            @Override
            protected void onPostExecute(ArrayList<RRPod> podList) {
                super.onPostExecute(podList);

                if(podList == null){

                    return;

                }

                retrievePodsCallback.onPodListRetrieved(podList);

            }

        }.execute();

    }

}
