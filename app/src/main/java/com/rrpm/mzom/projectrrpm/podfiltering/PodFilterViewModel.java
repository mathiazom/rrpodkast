package com.rrpm.mzom.projectrrpm.podfiltering;

import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PodFilterViewModel extends ViewModel {



    private static final String TAG = "RRP-PodFilterViewModel";


    @NonNull private final MutableLiveData<PodFilter> observablePodFilter = new MutableLiveData<>();


    @NonNull
    public MutableLiveData<PodFilter> getObservablePodFilter() {
        return observablePodFilter;
    }

    @Nullable
    public PodFilter getPodFilter(){

        return observablePodFilter.getValue();

    }

    public void setPodFilter(@Nullable final PodFilter podFilter){

        observablePodFilter.setValue(podFilter);

    }

    public void resetPodFilter(){

        Log.i(TAG,"Resetting pod filter");

        setPodFilter(null);

    }
}
