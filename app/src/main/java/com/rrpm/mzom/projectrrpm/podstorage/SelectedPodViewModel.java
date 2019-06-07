package com.rrpm.mzom.projectrrpm.podstorage;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SelectedPodViewModel extends ViewModel {

    private static final String TAG = "RRP-SlctdPodViewModel";


    @NonNull private final MutableLiveData<RRPod> selectedPod = new MutableLiveData<>();

    public void selectPod(@NonNull final RRPod pod){

        this.selectedPod.setValue(pod);

    }

    @NonNull
    public LiveData<RRPod> getSelectedPodObservable(){

        return selectedPod;

    }


}
