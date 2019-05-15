package com.rrpm.mzom.projectrrpm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.TimerTask;

class PodProgressStoringTask extends TimerTask {


    private static final String TAG = "RRP-PodProgressStoring";


    private RRPod pod;

    private PodStorageHandle podStorageHandle;

    private PodProgressStoringIterator.PodProgressRetriever progressRetriever;

    private PodProgressStoringTaskCallback callback;

    interface PodProgressStoringTaskCallback{
        void onPodProgressStored();
    }

    PodProgressStoringTask(@NonNull final PodStorageHandle podStorageHandle, @NonNull final RRPod pod, @NonNull PodProgressStoringIterator.PodProgressRetriever progressRetriever){
        this(podStorageHandle,pod, progressRetriever,null);
    }

    PodProgressStoringTask(@NonNull final PodStorageHandle podStorageHandle, @NonNull final RRPod pod, @NonNull PodProgressStoringIterator.PodProgressRetriever progressRetriever, @Nullable PodProgressStoringTaskCallback callback){

        this.podStorageHandle = podStorageHandle;

        this.pod = pod;

        this.progressRetriever = progressRetriever;

        this.callback = callback;

    }


    @Override
    public void run() {

        int progress = progressRetriever.retrieveProgress();

        if(progress == -1){
            throw new IllegalStateException("Progress returned after request was invalid");
        }

        Log.i(TAG,"Storing progress of " + pod);

        pod.setProgress(progress);

        podStorageHandle.storePodProgress(pod);

        if(this.callback != null){
            callback.onPodProgressStored();
        }

    }
}
