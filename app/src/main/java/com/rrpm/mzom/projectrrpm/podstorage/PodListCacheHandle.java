package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;

public class PodListCacheHandle {

    private static final String TAG = "RRP-PodListCacheHandle";


    private final Context context;

    private final SharedPreferences podListCache;


    PodListCacheHandle(@NonNull final Context context){

        this.context = context;

        this.podListCache = context.getSharedPreferences(PodCacheConstants.POD_CACHE_SHARED_PREFERENCES_NAME,0);

    }

    void cachePodList(@NonNull final ArrayList<RRPod> podList, final @NonNull PodType podType){

        podListCache
                .edit()
                .putString(getCacheKey(podType), new Gson().toJson(podList))
                .apply();

        Log.i(TAG,String.valueOf("Cached " + podList.size()) + " " + podType.toString() + " to " + getCacheKey(podType));

    }

    @Nullable
    ArrayList<RRPod> retrieveCachedPodList(final PodType podType){

        final String cacheJson = podListCache.getString(getCacheKey(podType), null);

        if(cacheJson == null){

            return null;

        }

        final ArrayList<RRPod> podList = new Gson().fromJson(cacheJson, new TypeToken<ArrayList<RRPod>>(){}.getType());

        final PodStorageHandle podStorageHandle = new PodStorageHandle(context);

        for(RRPod pod : podList){
            podStorageHandle.insertPodUserData(pod);
        }

        return podList;

    }

    private String getCacheKey(final PodType podType){

        return PodCacheConstants.CACHE_KEY_PREFIX + podType.name();

    }


}
