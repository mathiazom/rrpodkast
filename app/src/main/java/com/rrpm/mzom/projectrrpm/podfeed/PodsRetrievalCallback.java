package com.rrpm.mzom.projectrrpm.podfeed;

import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodsPackage;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public interface PodsRetrievalCallback {


    void onFail(@NonNull PodRetrievalError error);


    interface RetrievePodListCallback extends PodsRetrievalCallback{

        void onPodListRetrieved(@NonNull final ArrayList<RRPod> retrievedPodList);

    }

    interface RetrievePodsPackageCallback extends PodsRetrievalCallback {

        void onPodsPackageRetrieved(@NonNull final PodsPackage podsPackage);

    }


}
