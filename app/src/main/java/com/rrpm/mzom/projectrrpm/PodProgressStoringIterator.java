package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import androidx.annotation.NonNull;


class PodProgressStoringIterator {


    private PodStorageHandle podStorageHandle;

    private TaskIterator taskIterator;

    private PodProgressRetriever progressRetriever;


    /**
     *
     * Interface responsible for providing pod progress inside storing iteration
     *
     */

    interface PodProgressRetriever {
        int retrieveProgress();
    }


    PodProgressStoringIterator(@NonNull final Context context, @NonNull final PodProgressRetriever progressRetriever){

        this.podStorageHandle = new PodStorageHandle(context);

        this.progressRetriever = progressRetriever;

    }


    /**
     *
     * Starts iteration of progress storing tasks according to {@link PodStorageConstants#SAVE_PROGRESS_FREQ_MS}
     *
     */

    void start(@NonNull final RRPod pod){

        taskIterator = new TaskIterator(
                new PodProgressStoringTask(podStorageHandle,pod, progressRetriever),
                PodStorageConstants.SAVE_PROGRESS_FREQ_MS
        );

        taskIterator.start();

    }


    /**
     *
     * Terminates iteration of progress storing tasks
     *
     */

    void stop(){

        taskIterator.stop();

    }


}
