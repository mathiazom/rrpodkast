package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.rss.RRReader;

import java.util.ArrayList;

public class PodCacheHandle {

    private static final String TAG = "RRP-PodCacheHandle";


    private final Context context;

    private final SharedPreferences podCache;


    public PodCacheHandle(@NonNull final Context context){

        this.context = context;

        this.podCache = context.getSharedPreferences(PodCacheConstants.POD_CACHE_SHARED_PREFERENCES_NAME,0);

    }

    public void cachePodList(@NonNull final ArrayList<RRPod> podList, final RRReader.PodType podType){

        podCache.edit().putString(getCacheKey(podType), new Gson().toJson(podList)).apply();

        Log.i(TAG,String.valueOf(podList.size()) + " podList cached to " + getCacheKey(podType));

    }

    public ArrayList<RRPod> retrieveCachedPodList(final RRReader.PodType podType){

        final String cacheJson = podCache.getString(getCacheKey(podType), null);

        if(cacheJson == null){

            return new ArrayList<>();

        }

        final ArrayList<RRPod> podList = new Gson().fromJson(cacheJson, new TypeToken<ArrayList<RRPod>>() {}.getType());

        final PodStorageHandle podStorageHandle = new PodStorageHandle(context);

        for(RRPod pod : podList){
            podStorageHandle.applyPodStorageValues(pod);
        }

        return podList;

    }

    private String getCacheKey(final RRReader.PodType podType){

        return PodCacheConstants.CACHE_KEY_PREFIX + podType.name();

    }


}
