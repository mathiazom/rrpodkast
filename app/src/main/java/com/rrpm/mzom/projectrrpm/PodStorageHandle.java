package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

class PodStorageHandle {

    private static final String TAG = "RRP-PodStorageHandle";



    private final Context context;

    private final MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

    private final SharedPreferences podProgressStorage;
    private final SharedPreferences lastPlayedPodStorage;

    private final File podStorageDirectory;

    PodStorageHandle(final @NonNull Context context){

        this.context = context;

        this.podProgressStorage = context.getSharedPreferences(PodStorageConstants.POD_PROGRESS_STORAGE,0);
        this.lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        this.podStorageDirectory = new File(context.getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);

    }

    @Nullable
    PodId getLastPlayedPodId(){

        final SharedPreferences lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        final String lastPlayedIdJson = lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_ID, null);

        if(lastPlayedIdJson == null){
            Log.e(TAG,"No last played pod found");
            return null;
        }

        return new Gson().fromJson(lastPlayedIdJson, new TypeToken<PodId>() {}.getType());

    }

    void storePodAsLastPlayed(@NonNull final RRPod pod){

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

    Uri getPodUri(@NonNull final RRPod pod){

        // Directory of downloaded pods
        final File dir = new File(context.getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY);

        // Path to given pod
        return Uri.fromFile(new File(dir + File.separator + pod.getId().toString()));

    }

    void applyPodStorageValues(@NonNull final RRPod pod){

        pod.setDownloadedState(podIsFullyDownloaded(pod));
        pod.setProgress(getStoredPodProgress(pod));

    }

    void storePodProgress(@NonNull final RRPod pod){

        final int progress = pod.getProgress();

        final String podProgressKey = pod.getId().toString() + PodStorageConstants.POD_PROGRESS_SUFFIX;

        podProgressStorage.edit().putInt(podProgressKey, progress).apply();

        //Log.i(TAG,"Stored progress (" + MillisFormatter.toFormat(progress, MillisFormatter.MillisFormat.HH_MM_SS) + ") for " + pod.getTitle());

    }

    private int getStoredPodProgress(@NonNull RRPod pod){

        return podProgressStorage.getInt(pod.getId().toString() + PodStorageConstants.POD_PROGRESS_SUFFIX,0);

    }


}
