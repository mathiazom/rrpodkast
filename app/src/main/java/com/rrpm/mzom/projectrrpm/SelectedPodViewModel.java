package com.rrpm.mzom.projectrrpm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class SelectedPodViewModel extends ViewModel {

    private static final String TAG = "RRP-SlctdPodViewModel";


    private MutableLiveData<RRPod> selectedPod = new MutableLiveData<>();

    void selectPod(@NonNull final RRPod pod){

        this.selectedPod.setValue(pod);

    }

    LiveData<RRPod> getSelectedPod(){

        return selectedPod;

    }


}
