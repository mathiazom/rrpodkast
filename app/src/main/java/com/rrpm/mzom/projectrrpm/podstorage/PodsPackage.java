package com.rrpm.mzom.projectrrpm.podstorage;


import com.rrpm.mzom.projectrrpm.debugging.Assertions;
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


    void setPodList(@NonNull final PodType podType, @NonNull ArrayList<RRPod> pods){

        setPodList(podType, pods, false);

    }


    /**
     *
     * Updates observable data, and therefore alerts all observers of the change.
     *
     * @param podType: Pod type of the pod list to be updated
     * @param podList: The updated version of the pod list
     * @param post: True if method is called from a background thread,
     *              false if called from the main thread.
     */

    void setPodList(@NonNull final PodType podType, @NonNull ArrayList<RRPod> podList, final boolean post){

        final MutableLiveData<ArrayList<RRPod>> mutablePods = assureMutablePodList(podType);

        // Check if method is called from main or background thread
        if(post){

            // Called from background thread
            mutablePods.postValue(podList);

        }else{

            // Called from main thread
            mutablePods.setValue(podList);

        }

    }

    boolean podListIsRetrieved(@NonNull final PodType podType){

        return podListIsObservable(podType) && getPodList(podType) != null;

    }

    boolean podListIsObservable(@NonNull final PodType podType){

        return getObservablePodList(podType) != null;

    }

    @NonNull
    private MutableLiveData<ArrayList<RRPod>> createObservablePodList(@NonNull final PodType podType){

        final MutableLiveData<ArrayList<RRPod>> mutableLiveData = new MutableLiveData<>();

        podsMap.put(podType,mutableLiveData);

        Assertions._assert(podListIsObservable(podType), "Pod list not observable after podsMap.put().");

        return mutableLiveData;

    }

    // Mutable observable for private use, will create new mutable if one does not exist for the given pod type
    @NonNull
    private MutableLiveData<ArrayList<RRPod>> assureMutablePodList(@NonNull final PodType podType){

        MutableLiveData<ArrayList<RRPod>> mutablePods = getMutablePodList(podType);

        if(!podListIsObservable(podType)){

            mutablePods = createObservablePodList(podType);

        }

        Assertions._assert(mutablePods != null, "Pod list observable was null despite use of createObservablePodList(podType).");

        return mutablePods;

    }

    @Nullable
    private MutableLiveData<ArrayList<RRPod>> getMutablePodList(@NonNull final PodType podType){

        return podsMap.get(podType);

    }


    // Casting to non-mutable observable for public use
    @NonNull
    LiveData<ArrayList<RRPod>> assureObservablePodList(@NonNull final PodType podType){

        return assureMutablePodList(podType);

    }

    @Nullable
    private LiveData<ArrayList<RRPod>> getObservablePodList(@NonNull final PodType podType){

        return getMutablePodList(podType);

    }

    @Nullable
    ArrayList<RRPod> getPodList(@NonNull final PodType podType){

        final LiveData<ArrayList<RRPod>> observablePodList = getObservablePodList(podType);

        if(observablePodList == null){

            return null;

        }

        return observablePodList.getValue();

    }

}
