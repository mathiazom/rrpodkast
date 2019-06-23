package com.rrpm.mzom.projectrrpm.podstorage;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalError;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalCallback;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.podfeed.FeedPodsRetriever;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PodsViewModel extends AndroidViewModel {

    private static final String TAG = "RRP-PodsViewModel";


    /**
     * Central repository for retrieved pods.
     */
    @NonNull private final PodsPackage podsPackage;

    /**
     * Access to various device-stored pod values.
     */
    @NonNull private final PodStorageHandle podStorageHandle;


    public PodsViewModel(@NonNull Application application) {

        super(application);

        this.podsPackage = new PodsPackage();

        this.podStorageHandle = new PodStorageHandle(application);

    }





    public void updatePodInStorage(@NonNull final RRPod pod){

        updatePodInStorage(pod,false);

    }

    public void updatePodInStorage(@NonNull final RRPod pod, boolean post){

        final PodType podType = pod.getPodType();

        ArrayList<RRPod> podList = podsPackage.getPodList(podType);

        if(podList == null){

            Log.e(TAG,"Pod list was null, could not store pod");

            return;

        }

        PodUtils.updatePodInList(pod, podList);

        podsPackage.setPodList(podType, podList, post);

        podStorageHandle.storePod(pod);

    }



    public void requestPodsPackage(@NonNull final PodType[] podTypes, @NonNull PodsRetrievalCallback.PodsPackageRetrievalCallback podsPackageCallback){

        // Gather all non-retrieved pod types
        final ArrayList<PodType> nonRetrieved = new ArrayList<>();
        for (PodType podType : podTypes){

            if(!podsPackage.podListIsRetrieved(podType)){

                nonRetrieved.add(podType);

            }

        }

        // If all pod types have already been successfully retrieved,
        // return the current pods package to the caller
        if(nonRetrieved.isEmpty()){

            podsPackageCallback.onPodsPackageRetrieved(podsPackage);

            return;

        }

        // Keep track of whether or not the caller has been notified with the newly retrieved pod lists
        final boolean[] returnedPodsToCaller = {false};

        final PodsRetrievalCallback.PodListRetrievalCallback callback = new PodsRetrievalCallback.PodListRetrievalCallback() {
            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                if(returnedPodsToCaller[0]){

                    // The retrieved pod lists have already been returned to the caller
                    return;

                }

                // Iterate pod types to check if all requested pod lists have been retrieved
                for (PodType pT : podTypes){

                    if(!podsPackage.podListIsRetrieved(pT)){

                        // Retrieval request has not been fulfilled yet
                        return;

                    }

                }

                // Request has been fulfilled, notify the caller
                podsPackageCallback.onPodsPackageRetrieved(podsPackage);

                returnedPodsToCaller[0] = true;

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                // Request failed, notify the caller
                podsPackageCallback.onFail(error);

            }
        };

        // Iterate over gathered pod types and retrieve their respective pod lists
        for (PodType podType : nonRetrieved){

            // Assert that the pod type is not already retrieved
            //Assertions._assert(podsPackage.podListIsRetrieved(podType),"Pod list retrieval check returned opposite results after just a few lines");

            requestPodListRetrieval(podType, callback);

        }
    }


    /**
     *
     * Attempt to retrieve a pod list of the given {@link PodType}.
     *
     * The process will happen in to stages:
     *
     * - From cache:
     *  The pod list will first be retrieved from the local cache.
     *  The caller will be alerted of the retrieved pod list at this stage.
     *
     * - From feed:
     *  The process will later continue on the background thread to read
     *  the external pod feed and gather any newly published data.
     *  The caller will NOT be alerted of any retrieved pod lists at this stage.
     *
     *
     * @param podType: The {@link PodType} of pod list to be retrieved
     * @param retrievePodsCallback: Will be called after retrieval from the cache, but NOT from the feed.
     *
     */

    public void requestPodListRetrieval(@NonNull PodType podType, @NonNull PodsRetrievalCallback.PodListRetrievalCallback retrievePodsCallback){


        //Log.i(TAG,"TIMESTAMP OF REQUEST: " + System.currentTimeMillis());

        final long requestTimestamp = System.currentTimeMillis();


        // Called after retrieval from cache or feed
        final PodsRetrievalCallback.PodListRetrievalCallback baseCallback = new PodsRetrievalCallback.PodListRetrievalCallback() {
            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                // Populate retrieved pod list with user-data
                for(RRPod pod : retrievedPodList){
                    podStorageHandle.insertPodUserData(pod);
                }

                // Alert all observers of the newly retrieved pod list
                podsPackage.setPodList(podType, retrievedPodList);

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                retrievePodsCallback.onFail(error);

            }
        };

        // Called after cache retrieval
        final PodsRetrievalCallback.PodListRetrievalCallback cachedPodListCallback = new PodsRetrievalCallback.PodListRetrievalCallback() {

            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                Log.i(TAG, "Retrieved " + retrievedPodList.size() + " " + podType + " from cache (took " + String.valueOf(System.currentTimeMillis() - requestTimestamp) + " ms.)");

                baseCallback.onPodListRetrieved(retrievedPodList);

                // Alert retrieval requester
                retrievePodsCallback.onPodListRetrieved(retrievedPodList);

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                baseCallback.onFail(error);

            }

        };

        final Context context = getApplication();

        // Retrieve pod list from cache
        final ArrayList<RRPod> cachedPodList = new PodListCacheHandle(context).retrieveCachedPodList(podType);

        // Make sure pod list from cache is valid
        if(cachedPodList != null){

            /*// TEST: Remove latest pod to simulate uncaught pod release

            final RRPod removePod = cachedPodList.remove(0);
            Log.i(TAG,"TESTING: Removed " + removePod + " from cached pod list");

            new PodListCacheHandle(context).cachePodList(cachedPodList,podType);

            // TEST END*/

            cachedPodListCallback.onPodListRetrieved(cachedPodList);

        }


        // Check if device is prepared to request retrieval of the feed pod list
        if(!ConnectionValidator.isConnected(context)){

            Log.i(TAG,"Device has no network connection, retrieval of the feed pod list will therefore not be requested");

            return;

        }

        // Called after feed retrieval. Does NOT alert retrieval requester.
        final PodsRetrievalCallback.PodListRetrievalCallback feedPodListCallback = new PodsRetrievalCallback.PodListRetrievalCallback() {
            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                Log.i(TAG, "Retrieved " + retrievedPodList.size() + " " + podType + " from feed (took " + String.valueOf(System.currentTimeMillis() - requestTimestamp) + " ms.)");

                baseCallback.onPodListRetrieved(retrievedPodList);

                final PodListCacheHandle cacheHandle = new PodListCacheHandle(context);

                final boolean cacheIsUpToDate = PodUtils.cachedPodListIsUpToDate(cacheHandle.retrieveCachedPodList(podType),retrievedPodList);

                Log.i(TAG,"Cached pod list is up to date: " + cacheIsUpToDate);

                if(!cacheIsUpToDate){

                    new PodListCacheHandle(context).cachePodList(retrievedPodList, podType);

                    // TODO: Alert user if cached pod list was out of date

                }

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                baseCallback.onFail(error);

            }
        };

        // Request the more robust, but slower, retrieval of pods directly from the feed
        FeedPodsRetriever.retrieve(podType, feedPodListCallback);

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
    public LiveData<ArrayList<RRPod>> assureObservablePodList(@NonNull PodType podType){

        return podsPackage.assureObservablePodList(podType);

    }


    @Nullable
    public ArrayList<RRPod> getPodList(@NonNull PodType podType){

        return podsPackage.getPodList(podType);

    }

}
