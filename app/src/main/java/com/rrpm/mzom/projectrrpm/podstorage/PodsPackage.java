package com.rrpm.mzom.projectrrpm.podstorage;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.AssertUtils;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PodsPackage {


    private static final String TAG = "RRP-PodsPackage";


    @NonNull private final HashMap<PodType, MutableLiveData<ArrayList<RRPod>>> podsMap;


    PodsPackage(){

        this.podsMap = new HashMap<>();

    }

    void setPodList(@NonNull final PodType podType, @Nullable ArrayList<RRPod> pods){
        setPodList(podType, pods, false);
    }

    void setPodList(@NonNull final PodType podType, @Nullable ArrayList<RRPod> podList, final boolean post){

        AssertUtils._assert(podList != null, "Pod list was null");

        final MutableLiveData<ArrayList<RRPod>> mutablePods = podsMap.get(podType);

        if(mutablePods == null){
            throw new RuntimeException("Pods insertion has not been prepared");
        }

        if(post){
            mutablePods.postValue(podList);
        }else{
            mutablePods.setValue(podList);
        }

    }

    boolean hasPodList(@NonNull final PodType podType){

        final MutableLiveData<ArrayList<RRPod>> podList = podsMap.get(podType);

        return podList != null && podList.getValue() != null;

    }

    boolean podListIsObservable(@NonNull final PodType podType){

        return podsMap.get(podType) != null;

    }

    void createPodListObservable(@NonNull final PodType podType){

        Log.i(TAG,"Preparing insertion of " + podType);

        final MutableLiveData<ArrayList<RRPod>> mutableLiveData = new MutableLiveData<>();

        podsMap.put(podType,mutableLiveData);

    }

    @Nullable
    public LiveData<ArrayList<RRPod>> getObservablePodList(@NonNull final PodType podType){

        return podsMap.get(podType);

    }

    @Nullable
    public ArrayList<RRPod> getPodList(@NonNull final PodType podType){

        final LiveData<ArrayList<RRPod>> livePodList = getObservablePodList(podType);

        if(livePodList == null){
            return null;
        }

        return livePodList.getValue();

    }

}
