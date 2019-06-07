package com.rrpm.mzom.projectrrpm.poddownloading;


import com.rrpm.mzom.projectrrpm.pod.RRPod;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PodDownloadsViewModel extends ViewModel {


    private static final String TAG = "RRP-DownloadsViewModel";

    @NonNull private final MutableLiveData<ArrayList<RRPod>> observableDownloadingPods = new MutableLiveData<>();

    @NonNull
    private final HashMap<RRPod,MutableLiveData<Float>> progression = new HashMap<>();


    @NonNull
    public LiveData<ArrayList<RRPod>> getDownloadingPodsObservable(){

        return this.observableDownloadingPods;

    }


    @Nullable
    public LiveData<Float> getObservablePodProgress(@NonNull final RRPod pod){

        return progression.get(pod);

    }


    public void postDownloadProgress(@NonNull final RRPod pod, float downloadProgress){

        if(observableDownloadingPods.getValue() == null){
            observableDownloadingPods.setValue(new ArrayList<>());
        }

        MutableLiveData<Float> observableProgress = progression.get(pod);

        if(!observableDownloadingPods.getValue().contains(pod) || observableProgress == null){

            observableProgress = new MutableLiveData<>();
            observableProgress.setValue(downloadProgress);

            progression.put(pod,observableProgress);

            final ArrayList<RRPod> downloadingPods = observableDownloadingPods.getValue();

            downloadingPods.add(pod);

            observableDownloadingPods.setValue(downloadingPods);

            return;

        }

        observableProgress.setValue(downloadProgress);

    }

    public void removePod(@NonNull final RRPod pod){

        progression.remove(pod);

        final ArrayList<RRPod> downloadingPods = observableDownloadingPods.getValue();

        if(downloadingPods == null){
            return;
        }

        downloadingPods.remove(pod);

        observableDownloadingPods.setValue(downloadingPods);

    }



}
