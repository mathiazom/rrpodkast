package com.rrpm.mzom.projectrrpm;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

class PodsPackage {


    private static final String TAG = "RRP-PodsPackage";


    @NonNull private HashMap<RRReader.PodType, MutableLiveData<ArrayList<RRPod>>> podsMap = new HashMap<>();


    PodsPackage(){


    }


    void setPods(@NonNull final RRReader.PodType podType, @Nullable ArrayList<RRPod> pods){

        final MutableLiveData<ArrayList<RRPod>> mutablePods = podsMap.get(podType);

        if(mutablePods == null){
            throw new RuntimeException("Pods insertion has not been prepared");
        }

        mutablePods.setValue(pods);

        podsMap.put(podType,mutablePods);

    }

    boolean hasPods(@NonNull final RRReader.PodType podType){

        return podsMap.get(podType) != null;

    }

    void prepareInsertion(@NonNull final RRReader.PodType podType){

        podsMap.put(podType,new MutableLiveData<>());

    }

    @Nullable
    LiveData<ArrayList<RRPod>> getLivePods(@NonNull final RRReader.PodType podType){

        if(podsMap.get(podType) == null){
            prepareInsertion(podType);
        }

        return podsMap.get(podType);

    }

    void updatePod(@NonNull final RRPod pod){

        final MutableLiveData<ArrayList<RRPod>> livePods = podsMap.get(pod.getPodType());

        if(livePods == null){
            Log.e(TAG,"Pods not available");
            return;
        }

        final ArrayList<RRPod> tempPods = livePods.getValue();

        if(tempPods == null){
            Log.e(TAG,"Pods not available");
            return;
        }

        PodUtils.updatePodInList(pod,tempPods);

        // Register data change
        livePods.setValue(tempPods);

    }


}
