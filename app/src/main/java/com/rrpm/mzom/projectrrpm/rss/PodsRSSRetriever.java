package com.rrpm.mzom.projectrrpm.rss;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.rrpm.mzom.projectrrpm.podstorage.PodCacheHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.rss.RRReader.PodType;

import java.util.ArrayList;

public class PodsRSSRetriever {

    private static final String TAG = "RRP-PodsRSSRetriever";

    public static void retrieve(@NonNull final Context context, @NonNull final PodType podType, @NonNull final PodsViewModel.RetrievePodsCallback retrievePodsCallback){

        new AsyncTask<Void,Void,ArrayList<RRPod>>(){

            @Override
            protected ArrayList<RRPod> doInBackground(Void... voids) {

                final RRReader reader = new RRReader();
                reader.readPodsFeed(podType);

                Log.i(TAG,"Reading pods of type " + podType);

                try {

                    reader.join();

                } catch (InterruptedException e) {

                    return null;
                }

                final ArrayList<RRPod> pods = reader.getRetrievedPods();

                if(pods == null){

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
            protected void onPostExecute(ArrayList<RRPod> pods) {
                super.onPostExecute(pods);

                retrievePodsCallback.onPodsRetrieved(pods);

            }

        }.execute();

    }

}
