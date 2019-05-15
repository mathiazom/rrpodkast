package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

class PodCacheHandle {

    private static final String TAG = "RRP-PodCacheHandle";


    private final Context context;

    private SharedPreferences podCache;


    PodCacheHandle(@NonNull final Context context){

        this.context = context;

        this.podCache = context.getSharedPreferences(PodCacheConstants.POD_CACHE_SHARED_PREFERENCES_NAME,0);

    }

    void cachePods(@NonNull final ArrayList<RRPod> pods, final RRReader.PodType podType){

        podCache.edit().putString(getCacheKey(podType), new Gson().toJson(pods)).apply();

        Log.i(TAG,String.valueOf(pods.size()) + " pods cached to " + getCacheKey(podType));

    }

    ArrayList<RRPod> retrieveCachedPods(final RRReader.PodType podType){

        final String cacheJson = podCache.getString(getCacheKey(podType), null);

        if(cacheJson == null){

            return new ArrayList<>();

        }

        final ArrayList<RRPod> pods = new Gson().fromJson(cacheJson, new TypeToken<ArrayList<RRPod>>() {}.getType());

        final PodStorageHandle podStorageHandle = new PodStorageHandle(context);

        for(RRPod pod : pods){
            podStorageHandle.applyPodStorageValues(pod);
        }

        return pods;

    }

    private String getCacheKey(final RRReader.PodType podType){

        return PodCacheConstants.CACHE_KEY_PREFIX + podType.name();

    }


}
