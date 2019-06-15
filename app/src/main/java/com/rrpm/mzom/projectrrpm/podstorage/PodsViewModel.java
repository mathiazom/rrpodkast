package com.rrpm.mzom.projectrrpm.podstorage;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.AssertUtils;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podfeed.PodRetrievalError;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalCallback;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetriever;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PodsViewModel extends AndroidViewModel {

    private static final String TAG = "RRP-PodsViewModel";


    @NonNull private final PodsPackage podsPackage;

    @NonNull private final PodStorageHandle podStorageHandle;


    public PodsViewModel(@NonNull Application application) {

        super(application);

        this.podsPackage = new PodsPackage();

        this.podStorageHandle = new PodStorageHandle(application);

    }





    public void storePod(@NonNull final RRPod pod){
        storePod(pod,false);
    }

    public void storePod(@NonNull final RRPod pod, boolean post){

        final PodType podType = pod.getPodType();

        final LiveData<ArrayList<RRPod>> livePods = podsPackage.getObservablePodList(podType);

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

        podsPackage.setPodList(podType,tempPods,post);

        podStorageHandle.storePod(pod);

    }



    public void requestPodsPackage(@NonNull final PodType[] podTypes, @NonNull PodsRetrievalCallback.RetrievePodsPackageCallback podsPackageCallback){

        boolean alreadyAvailable = true;

        for (PodType pT : podTypes){
            if(!podsPackage.hasPodList(pT)){
                Log.i(TAG,"Has " + pT.name() + ": " + podsPackage.hasPodList(pT));
                // Complete pods package not available yet
                alreadyAvailable = false;
            }
        }

        if(alreadyAvailable){
            podsPackageCallback.onPodsPackageRetrieved(podsPackage);
            return;
        }

        for (PodType podType : podTypes){

            if(podsPackage.hasPodList(podType)){
                return;
            }

            requestPodList(podType, new PodsRetrievalCallback.RetrievePodListCallback() {
                @Override
                public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                    for (PodType pT : podTypes){

                        if(!podsPackage.hasPodList(pT)){
                            // Request has not been fulfilled yet
                            return;
                        }

                    }

                    // Request has been fulfilled
                    podsPackageCallback.onPodsPackageRetrieved(podsPackage);
                }

                @Override
                public void onFail(@NonNull PodRetrievalError error) {

                    podsPackageCallback.onFail(error);

                }
            });
        }
    }


    private void retrievePodList(@NonNull PodType podType, @NonNull PodsRetrievalCallback.RetrievePodListCallback retrievePodsCallback){

        if (podsPackage.hasPodList(podType) || podsPackage.podListIsObservable(podType)) {

            retrievePodsCallback.onFail(PodRetrievalError.ALREADY_REQUESTED);

            return;
        }

        podsPackage.createPodListObservable(podType);

        final PodsRetrievalCallback.RetrievePodListCallback callback = new PodsRetrievalCallback.RetrievePodListCallback() {
            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                podsPackage.setPodList(podType, retrievedPodList);

                retrievePodsCallback.onPodListRetrieved(retrievedPodList);

            }

            @Override
            public void onFail(@NonNull PodRetrievalError error) {
                retrievePodsCallback.onFail(error);
            }
        };

        final Context context = getApplication();

        if(ConnectionValidator.isConnected(context)){

            PodsRetriever.retrieve(context, podType, callback);

        }else{

            callback.onPodListRetrieved(new PodCacheHandle(context).retrieveCachedPodList(podType));

        }


    }


    public void requestPodList(@NonNull PodType podType, @NonNull PodsRetrievalCallback.RetrievePodListCallback retrievePodsCallback){

        // Start retrieval of pod list if not already available
        if (podsPackage.podListIsObservable(podType)) {

            retrievePodsCallback.onFail(PodRetrievalError.ALREADY_REQUESTED);

            return;
        }

        retrievePodList(podType,retrievePodsCallback);

    }


    /**
     *
     * Provides access to the observable pod list of specified pod type.
     * Does NOT request retrieval of the pod list.
     *
     * @param podType: Specifies which pod list to observe
     * @return Observable pod list
     *
     */

    @NonNull
    public LiveData<ArrayList<RRPod>> getObservablePodList(@NonNull PodType podType){

        if(!podsPackage.podListIsObservable(podType)){
            podsPackage.createPodListObservable(podType);
        }

        final LiveData<ArrayList<RRPod>> observablePodList = podsPackage.getObservablePodList(podType);

        AssertUtils._assert(observablePodList != null,"Observable pod list was null, event though podsPackage.hasPodList(podType) was true");

        return observablePodList;

    }


    @Nullable
    public ArrayList<RRPod> getPodList(@NonNull PodType podType){

        return podsPackage.getPodList(podType);

    }

}
