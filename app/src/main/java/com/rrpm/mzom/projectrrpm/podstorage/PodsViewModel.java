package com.rrpm.mzom.projectrrpm.podstorage;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.rss.PodsRSSRetriever;
import com.rrpm.mzom.projectrrpm.rss.RRReader;

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


    public interface RetrievePodsCallback{
        void onPodsRetrieved(@NonNull final ArrayList<RRPod> retrievedPods);
    }


    public void storePod(@NonNull final RRPod pod){
        storePod(pod,false);
    }

    public void storePod(@NonNull final RRPod pod, boolean post){

        final RRReader.PodType podType = pod.getPodType();

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


    public static class PodsPackageRequest {

        @NonNull private final RRReader.PodType[] podTypes;
        @NonNull private final PodsPackageRequest.PodsPackageCallback podsPackageCallback;

        public interface PodsPackageCallback{
            void onPodsPackage(@NonNull PodsPackage podsPackage);
        }

        public PodsPackageRequest(@NonNull final RRReader.PodType[] podTypes, @NonNull PodsPackageRequest.PodsPackageCallback podsPackageCallback){

            this.podTypes = podTypes;
            this.podsPackageCallback = podsPackageCallback;

        }

        @NonNull
        RRReader.PodType[] getPodTypes() {
            return podTypes;
        }

        @NonNull
        PodsPackageRequest.PodsPackageCallback getPodsPackageCallback() {
            return podsPackageCallback;
        }
    }


    public void requestPodsPackage(@NonNull final PodsPackageRequest podsPackageRequest){

        final RRReader.PodType[] podTypes = podsPackageRequest.getPodTypes();

        boolean alreadyAvailable = true;

        for (RRReader.PodType pT : podTypes){
            if(!podsPackage.hasPodList(pT)){
                Log.i(TAG,"Has " + pT.name() + ": " + podsPackage.hasPodList(pT));
                // Complete pods package not available yet
                alreadyAvailable = false;
            }
        }

        if(alreadyAvailable){
            podsPackageRequest.getPodsPackageCallback().onPodsPackage(podsPackage);
            return;
        }

        for (RRReader.PodType podType : podTypes){

            if(podsPackage.hasPodList(podType)){
                return;
            }

            getObservablePodList(podType, retrievedPods -> {

                for (RRReader.PodType pT : podTypes){
                    Log.i(TAG,"Has " + pT.name() + ": " + podsPackage.hasPodList(pT));
                    if(!podsPackage.hasPodList(pT)){
                        // Request has not been fulfilled yet
                        return;
                    }
                }

                // Request has been fulfilled
                podsPackageRequest.getPodsPackageCallback().onPodsPackage(podsPackage);

            });
        }

    }


    @NonNull
    public LiveData<ArrayList<RRPod>> getObservablePodList(@NonNull RRReader.PodType podType){

        return getObservablePodList(podType,null);

    }

    @NonNull
    private LiveData<ArrayList<RRPod>> getObservablePodList(@NonNull RRReader.PodType podType, @Nullable RetrievePodsCallback retrievePodsCallback){

        final RetrievePodsCallback callback = retrievedPodList -> {
            podsPackage.setPodList(podType, retrievedPodList);
            if(retrievePodsCallback != null){
                retrievePodsCallback.onPodsRetrieved(retrievedPodList);
            }
        };

        final Context context = getApplication();

        if(!podsPackage.hasPodList(podType) && !podsPackage.podListPrepared(podType)){

            podsPackage.prepareInsertion(podType);

            if(ConnectionValidator.isConnected(context)){

                PodsRSSRetriever.retrieve(context, podType, callback);

            }else{

                callback.onPodsRetrieved(new PodCacheHandle(context).retrieveCachedPodList(podType));

            }

        }

        final LiveData<ArrayList<RRPod>> observablePodList = podsPackage.getObservablePodList(podType);

        if(observablePodList == null){
            throw new RuntimeException("Observable pod list was null, event though podsPackage.hasPodList(podType) was true");
        }

        return observablePodList;

    }


    @Nullable
    public ArrayList<RRPod> getPodList(@NonNull RRReader.PodType podType){

        return podsPackage.getPodList(podType);

    }

}
