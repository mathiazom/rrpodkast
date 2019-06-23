package com.rrpm.mzom.projectrrpm.poddownloading;


import android.app.Application;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;

import java.util.ArrayList;
import java.util.HashMap;

import javax.validation.constraints.Null;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PodDownloadsViewModel extends AndroidViewModel {


    private static final String TAG = "RRP-DownloadsViewModel";

    @NonNull private final MutableLiveData<ArrayList<RRPod>> observableDownloadQueue = new MutableLiveData<>();

    @NonNull
    private final HashMap<RRPod,MutableLiveData<Float>> downloadProgresses = new HashMap<>();

    public PodDownloadsViewModel(@NonNull Application application) {

        super(application);

        // Retrieve download queue from device storage
        this.observableDownloadQueue.setValue(new PodStorageHandle(application).getStoredPodDownloadQueue());

    }


    @NonNull
    public LiveData<ArrayList<RRPod>> getObservableDownloadQueue(){

        return this.observableDownloadQueue;

    }


    @Nullable
    ArrayList<RRPod> getDownloadQueue(){

        return observableDownloadQueue.getValue();

    }

    private boolean downloadQueueIsEmpty(){

        return this.observableDownloadQueue.getValue() == null ||
               this.observableDownloadQueue.getValue().isEmpty();

    }


    void addPodToDownloadQueue(@NonNull final RRPod pod){

        final ArrayList<RRPod> downloadQueue = observableDownloadQueue.getValue();

        if(downloadQueue == null){

            observableDownloadQueue.setValue(new ArrayList<>());

            // Retry adding to download queue
            addPodToDownloadQueue(pod);

            return;

        }

        downloadQueue.add(pod);

        // Prepare download progress observation
        createObservableDownloadProgress(pod);

        // Alert observers that download queue has changed
        observableDownloadQueue.setValue(downloadQueue);

        updateDownloadQueueInStorage(downloadQueue);

    }

    void removePodFromDownloadQueue(@NonNull final RRPod pod){

        downloadProgresses.remove(pod);

        final ArrayList<RRPod> downloadQueue = observableDownloadQueue.getValue();

        if(downloadQueue == null){

            return;

        }

        final boolean removed = downloadQueue.remove(pod);

        Log.i(TAG,pod + " removed successfully: " + removed);

        observableDownloadQueue.setValue(downloadQueue);

        updateDownloadQueueInStorage(downloadQueue);

    }

    boolean hasPodInQueue(@NonNull RRPod pod){

        return this.observableDownloadQueue.getValue() != null &&
               this.observableDownloadQueue.getValue().contains(pod);

    }

    @Nullable
    RRPod getNextPodInQueue(){

        if(downloadQueueIsEmpty()){

            return null;

        }

        Assertions._assert(this.observableDownloadQueue.getValue() != null, "Download queue was null despite downloadQueueIsEmpty() returning false.");

        return this.observableDownloadQueue.getValue().get(0);

    }


    private void updateDownloadQueueInStorage(@Nullable ArrayList<RRPod> downloadQueue){

        new PodStorageHandle(getApplication()).storePodDownloadQueue(downloadQueue);

    }


    void postDownloadProgress(@NonNull final RRPod pod, float downloadProgress){

        if(observableDownloadQueue.getValue() == null){

            observableDownloadQueue.setValue(new ArrayList<>());

            updateDownloadQueueInStorage(observableDownloadQueue.getValue());

        }

        final MutableLiveData<Float> observableProgress = downloadProgresses.get(pod);

        // Check if downloaded pod is a new addition to the queue
        if(observableProgress == null){

            createObservableDownloadProgress(pod);

            // Retry setting download progress
            postDownloadProgress(pod,downloadProgress);

            return;

        }

        observableProgress.setValue(downloadProgress);

    }

    private void createObservableDownloadProgress(@NonNull final RRPod pod){

        final MutableLiveData<Float> observableProgress = new MutableLiveData<>();
        observableProgress.setValue(0f);

        downloadProgresses.put(pod,observableProgress);

    }

    @Nullable
    public LiveData<Float> getObservableDownloadProgress(@NonNull final RRPod pod){

        return downloadProgresses.get(pod);

    }






}
