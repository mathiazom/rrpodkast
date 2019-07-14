package com.rrpm.mzom.projectrrpm.podstorage;


import android.util.Log;

import com.rrpm.mzom.projectrrpm.annotations.NonEmpty;
import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podfiltering.DateRange;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;
import java.util.Calendar;

import javax.validation.constraints.Null;

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

                Assertions._assert(!p.equals(pod),"Same pod object, no need to use this method");

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


    @Nullable
    public static RRPod getPodFromId(@NonNull final PodId podId, @NonNull final ArrayList<RRPod> podList){

        for(RRPod pod : podList){

            if(pod.getId().equals(podId)){

                return pod;

            }

        }

        return null;

    }


    static void updatePodInList(@NonNull final RRPod pod, @NonNull final ArrayList<RRPod> podList){

        int index = podList.indexOf(pod);

        if (index < 0) {

            final int equalIndex = PodUtils.getEqualPodIndex(pod, podList);

            Assertions._assert(equalIndex != -1, "Pod was not found inside the given pod list");

            if(equalIndex == -1){

                Log.e(TAG,"Could not find index for pod to be updated");

                return;

            }

            index = equalIndex;

        }

        // Update pod in data
        podList.set(index,pod);

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

        for (PodType podType : PodType.values()){

            final ArrayList<RRPod> podList = podsPackage.getPodList(podType);

            if(podList == null){

                continue;

            }

            for (RRPod pod : podList){

                if (pod.isDownloaded()){

                    totalDownloaded++;

                }

            }

        }

        return totalDownloaded;

    }



    @NonNull
    public static DateRange getDateRangeFromPodList(@NonNull @NonEmpty final ArrayList<RRPod> podList){

        Assertions._assert(!podList.isEmpty(),"List was empty");

        return DateUtils.getDateRangeFromList(podList, RRPod::getDate);

    }


    /**
     *
     * Checks if all pods in a given {@param feedPodList} are represented in a given {@param cachedPodList}.
     *
     * @param cachedPodList: Pod list to be tested for "outdatedness"
     * @param feedPodList: The most up to date pod list
     *
     * @return True if cachedPodList up to date, false otherwise (including if cached pod list is null).
     */

    static boolean cachedPodListIsUpToDate(@Nullable ArrayList<RRPod> cachedPodList, @NonNull ArrayList<RRPod> feedPodList){

        if(cachedPodList == null){

            return false;

        }

        boolean isUpToDate = true;

        // Extract all pod ids from the cached pod list
        final ArrayList<PodId> cachedPodListIds = getPodIdsFromPodList(cachedPodList);

        // Check if the cached pod list contains all pod ids from the feed pod list
        for(RRPod feedPod : feedPodList){

            // Determine if the given pod is represented in the cached pod list
            final boolean idIsCached = cachedPodListIds.contains(feedPod.getId());

            if(!idIsCached){

                Log.e(TAG,"Following pod was not cached: " + feedPod.getTitle());

                // Cached pod list failed the test, and is therefore not fully synced
                isUpToDate = false;

            }

        }

        // Cached pod list has passed all tests, and is therefore fully synced
        return isUpToDate;


    }


    public static boolean podListContainsPodId(@NonNull ArrayList<RRPod> podList, @NonNull PodId podId){

        final ArrayList<PodId> podIds = getPodIdsFromPodList(podList);

        return podIds.contains(podId);

    }

    @NonNull
    private static ArrayList<PodId> getPodIdsFromPodList(@NonNull ArrayList<RRPod> podList){

        final ArrayList<PodId> podIdList = new ArrayList<>();

        for(RRPod pod : podList){

            Assertions._assert(!podIdList.contains(pod.getId()),"Duplicate pod id in pod list");

            podIdList.add(pod.getId());

        }

        return podIdList;

    }





}
