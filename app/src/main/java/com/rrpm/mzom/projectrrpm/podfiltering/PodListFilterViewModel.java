package com.rrpm.mzom.projectrrpm.podfiltering;

import com.rrpm.mzom.projectrrpm.podfiltering.PodFilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PodListFilterViewModel extends ViewModel {


    @NonNull private final MutableLiveData<PodFilter> observablePodFilter = new MutableLiveData<>();


    @NonNull
    public MutableLiveData<PodFilter> getObservablePodFilter() {
        return observablePodFilter;
    }

    @NonNull
    public PodFilter getPodFilter(){

        if(observablePodFilter.getValue() == null){
            return PodFilter.noFilter();
        }

        return observablePodFilter.getValue();

    }

    public void setPodFilter(@NonNull final PodFilter podFilter){

        observablePodFilter.setValue(podFilter);

    }

    public void resetPodFilter(){

        setPodFilter(PodFilter.noFilter());

    }
}
