package com.rrpm.mzom.projectrrpm;


import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PodUtils {

    private static final String TAG = "RRP-PodUtils";


    /**
     *
     * Search the given pods list to find any pod where the {@link PodId} equals {@param podId}.
     *
     * @param podId: Search target to find associated pod.
     * @param pods: Pods to search through.
     *
     * @return target pod inside {@param pods}, or null if no such pod could be found.
     */

    @Nullable
    static RRPod getPodFromId(PodId podId, @NonNull final ArrayList<RRPod> pods){

        for(RRPod pod : pods){

            if(pod.getId().equals(podId)){

                return pod;

            }

        }

        return null;

    }


    /**
     *
     * Determines the index of a pod inside given pods list that has equal {@link PodId} as the given pod.
     *
     * @param pod: Search target to find equal index of.
     * @param pods: Pods to search through.
     *
     * @return index of equal pod inside {@param pods}, or -1 if no equal pod could be found.
     *
     */

    static int getEqualPodIndex(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> pods){

        for(RRPod p : pods){

            if(p.getId().equals(pod.getId())){

                if(p.equals(pod)){
                    Log.i(TAG,"Same pod object, no need to use this method");
                }

                return pods.indexOf(p);

            }

        }

        return -1;

    }


    /**
     *
     * Finds any pod inside the given pods list that has equal {@link PodId} as the given pod.
     * Searches with {@link PodUtils#getEqualPodIndex(RRPod, ArrayList)} to find the index of any such pod.
     *
     * @param pod: Search target to find an equal of.
     * @param pods: Pods to search through.
     *
     * @return equal pod inside {@param pods}, or {@code null} if no equal pod could be found
     *
     */

    @Nullable
    static RRPod getEqualPod(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> pods){

        int index = getEqualPodIndex(pod,pods);

        if(index != -1){

            return pods.get(index);

        }

        return null;

    }


    static void updatePodInList(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> pods){

        final int equalIndex = PodUtils.getEqualPodIndex(pod,pods);

        if(equalIndex == -1){
            Log.e(TAG,"Could not find index for pod to be updated");
            return;
        }

        // Update pod in data
        pods.set(equalIndex,pod);

    }



}
