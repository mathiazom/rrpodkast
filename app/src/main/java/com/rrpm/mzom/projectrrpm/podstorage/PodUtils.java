package com.rrpm.mzom.projectrrpm.podstorage;


import android.util.Log;

import com.rrpm.mzom.projectrrpm.annotations.NonEmpty;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podfiltering.DateRange;
import com.rrpm.mzom.projectrrpm.rss.RRReader;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PodUtils {

    private static final String TAG = "RRP-PodUtils";


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

    private static int getEqualPodIndex(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> pods){

        for(RRPod p : pods){

            if(p.getId().equals(pod.getId())){

                if(p.equals(pod)){
                    //Log.i(TAG,"Same pod object, no need to use this method");
                    throw new RuntimeException("Same pod object, no need to use this method");
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
    public static RRPod getEqualPod(@NonNull final RRPod pod, @Nullable final ArrayList<RRPod> pods){

        if(pods == null){
            return null;
        }

        int index = getEqualPodIndex(pod,pods);

        if(index != -1){

            return pods.get(index);

        }

        return null;

    }


    static void updatePodInList(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> pods){

        int index = pods.indexOf(pod);

        if(index == -1){

            final int equalIndex = PodUtils.getEqualPodIndex(pod,pods);

            if(equalIndex == -1){
                Log.e(TAG,"Could not find index for pod to be updated");
                return;
            }

            index = equalIndex;

        }

        // Update pod in data
        pods.set(index,pod);

    }


    public static long getPodRecency(@NonNull final RRPod pod){

        long podTime = pod.getDate().getTime();
        long currentTime = Calendar.getInstance().getTime().getTime();
        return currentTime - podTime;

    }


    /**
     *
     * Counts total pods inside {@param podsPackage} that satisfy {@link RRPod#isDownloaded()}
     *
     * @param podsPackage: pods to include in tally.
     * @return total downloaded pods.
     */

    public static int totalDownloadedPods(@NonNull final PodsPackage podsPackage) {

        int totalDownloaded = 0;

        for (RRReader.PodType podType : RRReader.PodType.values()){

            final ArrayList<RRPod> podList = podsPackage.getPodList(podType);

            if(podList == null){
                continue;
            }

            for (RRPod pod : podList){
                if (pod.isDownloaded()) totalDownloaded++;
            }

        }

        return totalDownloaded;

    }



    @NonNull
    public static DateRange getDateRangeFromPodList(@NonNull @NonEmpty final ArrayList<RRPod> podList){

        return DateUtils.getDateRangeFromList(podList, RRPod::getDate);

    }






}
