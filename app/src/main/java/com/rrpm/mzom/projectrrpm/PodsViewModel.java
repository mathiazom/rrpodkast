package com.rrpm.mzom.projectrrpm;

import android.app.Application;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PodsViewModel extends AndroidViewModel {

    private static final String TAG = "RRP-PodsViewModel";


    @NonNull private PodsPackage podsPackage = new PodsPackage();


    public PodsViewModel(@NonNull Application application) {
        super(application);
    }




    PodsPackage getPodsPackage(){

        return this.podsPackage;

    }

    LiveData<ArrayList<RRPod>> retrievePods(@NonNull final RRReader.PodType podType){

        if(!podsPackage.hasPods(podType)){
            podsPackage.prepareInsertion(podType);
            PodsRepository.getInstance().retrievePods(
                    getApplication(),
                    podType,
                    retrievedPods -> podsPackage.setPods(podType,retrievedPods)
            );
        }

        return podsPackage.getLivePods(podType);

    }

}
