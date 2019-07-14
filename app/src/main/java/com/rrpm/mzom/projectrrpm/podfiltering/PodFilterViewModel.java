package com.rrpm.mzom.projectrrpm.podfiltering;

import android.util.Log;


import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;

import java.util.ArrayList;

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

    public boolean prepareMinimumPodFilter(@NonNull ArrayList<RRPod> podList){

        if(podList.isEmpty()){

            return false;

        }

        if(getPodFilter() == null){

            setPodFilter(new PodFilter(PodUtils.getDateRangeFromPodList(podList)));

        }

        return true;

    }

    public void resetPodFilter(){

        setPodFilter(null);

    }
}
