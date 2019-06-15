package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.io.File;
import java.util.ArrayList;

public class PodStorageHandle {

    private static final String TAG = "RRP-PodStorageHandle";



    private final Context context;

    private final MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

    private final SharedPreferences podProgressStorage;
    private final SharedPreferences lastPlayedPodStorage;

    private final File podStorageDirectory;

    public PodStorageHandle(final @NonNull Context context){

        this.context = context;

        this.podProgressStorage = context.getSharedPreferences(PodStorageConstants.POD_PROGRESS_STORAGE,0);
        this.lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        this.podStorageDirectory = new File(context.getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);

    }

    // Check if last played pod info is available
    public boolean lastPlayedPodIsAvailable() {

        // Get SharedPreferences storage related to last played pod
        final SharedPreferences lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        // Return whether or not last played pod type and id is set
        return lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_TYPE, null) != null &&
               lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_ID, null) != null;

    }


    // Retrieve last played pod type from device storage
    @Nullable
    public PodType getLastPlayedPodType() {

        // Get SharedPreferences storage related to last played pod
        final SharedPreferences lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        // Retrieve json string for last played pod type
        final String typeJson = lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_TYPE, null);

        // If json is null, return null
        if(typeJson == null){
            return null;
        }

        // Return last played pod type
        return new Gson().fromJson(typeJson, new TypeToken<PodType>() {}.getType());

    }



    // Retrieve last played pod id from device storage
    @Nullable
    public PodId getLastPlayedPodId() {

        // Get SharedPreferences storage related to last played pod
        final SharedPreferences lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        // Retrieve json string for last played pod id
        final String idJson = lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_ID, null);

        // If json is null, return null
        if(idJson == null){
            return null;
        }

        // Return last played pod id
        return new Gson().fromJson(idJson, new TypeToken<PodId>() {}.getType());

    }


    public void storePodAsLastPlayed(@NonNull final RRPod pod){

        //lastPlayedPodStorage.edit().putString(PodStorageConstants.LAST_PLAYED_POD,new Gson().toJson(pod)).apply();

        lastPlayedPodStorage.edit().putString(PodStorageConstants.LAST_PLAYED_POD_TYPE,new Gson().toJson(pod.getPodType())).apply();

        lastPlayedPodStorage.edit().putString(PodStorageConstants.LAST_PLAYED_POD_ID,new Gson().toJson(pod.getId())).apply();

    }

    private boolean podIsFullyDownloaded(@NonNull final RRPod pod){

        final File downloadedFile = new File(podStorageDirectory,pod.getId().toString());

        // Check if pod has at least been partly downloaded
        if(downloadedFile.exists()){

            // Check if the downloaded file is long enough (audio duration) to be considered fully downloaded
            return Math.abs(podDurationDownloaded(downloadedFile) - pod.getDuration()) < PodStorageConstants.STORED_DURATION_OFFSET_LIMIT;

        }

        return false;

    }

    private int podDurationDownloaded(@NonNull final File downloadedFile){

        metadataRetriever.setDataSource(downloadedFile.getPath());
        final String downloadedDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(downloadedDuration);

    }

    public Uri getPodUri(@NonNull final RRPod pod){

        // Directory of downloaded pods
        final File dir = new File(context.getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY);

        // Path to given pod
        return Uri.fromFile(new File(dir + File.separator + pod.getId().toString()));

    }

    public void applyPodStorageValues(@NonNull final RRPod pod){

        pod.setDownloadedState(podIsFullyDownloaded(pod));
        pod.setProgress(getStoredPodProgress(pod));

    }

    public void storePod(@NonNull final RRPod pod){

        final int progress = pod.getProgress();

        final String podProgressKey = pod.getId().toString() + PodStorageConstants.POD_PROGRESS_SUFFIX;

        podProgressStorage.edit().putInt(podProgressKey, progress).apply();

    }

    private int getStoredPodProgress(@NonNull RRPod pod){

        return podProgressStorage.getInt(pod.getId().toString() + PodStorageConstants.POD_PROGRESS_SUFFIX,0);

    }


    /**
     *
     * Calculates device space usage of pods inside {@param podsPackage}.
     *
     * @param podsPackage: pods (possibly multiple PodTypes) to include in space usage total.
     * @return total space usage of {@param podsPackage} in megabytes.
     *
     */

    public float calculateSpaceUsage(@NonNull final PodsPackage podsPackage) {

        float spaceUsage = 0;

        for (PodType podType : PodType.values()){

            final ArrayList<RRPod> podList = podsPackage.getPodList(podType);

            if(podList == null){
                // Pod type not represented
                continue;
            }

            for(RRPod pod : podList){
                if (pod.isDownloaded()) {
                    spaceUsage += new File(podStorageDirectory, pod.getId().toString()).length() / Math.pow(1024, 2);
                }
            }

        }

        return (float) Math.round(spaceUsage * 100) / 100;

    }


}
