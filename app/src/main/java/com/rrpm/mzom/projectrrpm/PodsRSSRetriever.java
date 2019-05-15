package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.rrpm.mzom.projectrrpm.RRReader.PodType;

import java.util.ArrayList;

class PodsRSSRetriever {

    private static final String TAG = "RRP-PodsRSSRetriever";

    static void retrieve(@NonNull final Context context, final PodType podType, final PodsRepository.RetrievePodsCallback retrievePodsCallback){

        new AsyncTask<Void,Void,ArrayList<RRPod>>(){

            @Override
            protected ArrayList<RRPod> doInBackground(Void... voids) {

                final RRReader reader = new RRReader();
                reader.readPodsFeed(podType);

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

                new PodCacheHandle(context).cachePods(pods, podType);

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
