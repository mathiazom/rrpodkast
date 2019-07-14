package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rrpm.mzom.projectrrpm.debugging.LogUtils;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class PodStorageHandle {


    private static final String TAG = "RRP-PodStorageHandle";



    /**
     * Retriever used to extract metadata from pod downloads.
     */
    private final MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();


    /**
     * Storage space for all pod playback progresses.
     */
    private final SharedPreferences podProgressStorage;


    /**
     * Storage space for all values associated with the last played pod.
     */
    private final SharedPreferences lastPlayedPodStorage;


    private final SharedPreferences podDownloadQueueStorage;


    /**
     * Storage space for all pod downloads.
     */
    private final File podStorageDirectory;



    public PodStorageHandle(final @NonNull Context context){

        this.podProgressStorage = context.getSharedPreferences(PodStorageConstants.POD_PROGRESS_STORAGE,0);

        this.lastPlayedPodStorage = context.getSharedPreferences(PodStorageConstants.LAST_PLAYED_POD_STORAGE,0);

        this.podDownloadQueueStorage = context.getSharedPreferences(PodStorageConstants.POD_DOWNLOAD_QUEUE_STORAGE,0);

        this.podStorageDirectory = new File(context.getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);

    }



    public void logAllSharedPreferences(){

        LogUtils.logSharedPreferences(podProgressStorage,TAG);

        LogUtils.logSharedPreferences(lastPlayedPodStorage,TAG);

        LogUtils.logSharedPreferences(podDownloadQueueStorage,TAG);

    }





    /**
     *
     * Defines the file name to be used for a given {@link RRPod}.
     *
     * @param pod: The {@link RRPod} on which the file name will be based.
     *
     * @return The {@link RRPod}'s file name.
     *
     */

    private String getPodFileName(@NonNull RRPod pod){

        return pod.getId().toString();

    }


    /**
     *
     * Retrieves the {@link PodType} stored with {@link PodStorageHandle#storePodAsLastPlayed(RRPod)}.
     *
     * @return The {@link PodType} associated with the last played pod.
     *
     */

    @Nullable
    public PodType getLastPlayedPodType() {


        // Retrieve json string for last played pod type
        final String typeJson = lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_TYPE, null);

        // If json is null, return null
        if(typeJson == null){

            return null;

        }

        // Return last played pod type
        return new Gson().fromJson(typeJson, new TypeToken<PodType>() {}.getType());

    }


    /**
     *
     * Retrieves the {@link PodId} stored with {@link PodStorageHandle#storePodAsLastPlayed(RRPod)}.
     *
     * @return The {@link PodId} associated with the last played pod.
     *
     */

    @Nullable
    public PodId getLastPlayedPodId() {

        // Retrieve json string for last played pod id
        final String idJson = lastPlayedPodStorage.getString(PodStorageConstants.LAST_PLAYED_POD_ID, null);

        // If json is null, return null
        if(idJson == null){

            return null;

        }

        // Return last played pod id
        return new Gson().fromJson(idJson, new TypeToken<PodId>() {}.getType());

    }


    /**
     *
     * Stores {@link PodType} and {@link PodId} of given {@link RRPod} as last played.
     * This allows a full instance of the last played {@link RRPod} to be retrieved at a later time.
     *
     * @param pod: The {@link RRPod} from which the last played {@link PodType} and {@link PodId} will be retrieved.
     *
     */

    public void storePodAsLastPlayed(@NonNull final RRPod pod){

        lastPlayedPodStorage.edit().putString(PodStorageConstants.LAST_PLAYED_POD_TYPE, new Gson().toJson(pod.getPodType())).apply();

        lastPlayedPodStorage.edit().putString(PodStorageConstants.LAST_PLAYED_POD_ID, new Gson().toJson(pod.getId())).apply();

    }


    /**
     *
     * Checks if the {@link RRPod}'s downloaded file exists, and is long enough (audio duration) to be considered fully downloaded.
     *
     * @param pod: The {@link RRPod} to be checked.
     *
     * @return true if the offset between the duration of the pod download and the expected duration is within the {@link PodStorageConstants#STORED_DURATION_OFFSET_LIMIT},
     *         false otherwise.
     *
     */

    private boolean podIsFullyDownloaded(@NonNull final RRPod pod){

        final File downloadedFile = getPodDownloadFile(pod);

        // Check if pod has at least been partly downloaded
        if(downloadedFile.exists()){

            // Check if the downloaded file is long enough (audio duration) to be considered fully downloaded
            return Math.abs(getFilePlaybackDuration(downloadedFile) - pod.getDuration()) < PodStorageConstants.STORED_DURATION_OFFSET_LIMIT;

        }

        return false;

    }


    /**
     *
     * Retrieves the download location, as a {@link File}, of a given {@link RRPod}.
     *
     * @param pod: The {@link RRPod} for which the download location should be designated.
     *
     * @return The download location as a {@link File}.
     *
     */

    private File getPodDownloadFile(@NonNull final RRPod pod){

        return new File(podStorageDirectory,getPodFileName(pod));

    }


    /**
     *
     * Extracts the duration metadata from an audio playback {@link File}.
     *
     * @param file: The {@link File} from which the duration metadata is extracted.
     *
     * @return The playback duration in milliseconds.
     *
     */

    private int getFilePlaybackDuration(@NonNull final File file){

        metadataRetriever.setDataSource(file.getPath());

        final String downloadedDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        return Integer.parseInt(downloadedDuration);

    }


    /**
     *
     * Retrieves the {@link Uri} of the downloaded {@link File} associated with a given {@link RRPod}.
     *
     * @param pod: The target {@link RRPod} with which the {@link Uri} will be associated.
     *
     * @return The {@link Uri} of the downloaded {@link File}.
     *
     */

    @NonNull
    public Uri getPodUri(@NonNull final RRPod pod){

        return Uri.fromFile(getPodDownloadFile(pod));

    }


    /**
     *
     * Inserts the most recent user data associated with the given {@link RRPod}.
     * The user data is retrieved from the {@link PodStorageHandle#podStorageDirectory}.
     *
     * @param pod: The {@link RRPod} to which its associated user data will be applied.
     *
     */

    public void insertPodUserData(@NonNull final RRPod pod){

        pod.setDownloadedState(podIsFullyDownloaded(pod));

        pod.setProgress(getStoredPodProgress(pod));

    }


    /**
     *
     * Stores a given {@link RRPod}'s storage-dependant values to the {@link PodStorageHandle#podStorageDirectory}.
     * Current storage-dependant values include: progress.
     *
     * @param pod: The {@link RRPod} from which the storage-dependant values will be extracted.
     *
     */

    void storePod(@NonNull final RRPod pod){

        storePodProgress(pod);

    }


    /**
     *
     * Stores a given {@link RRPod}'s playback progress to the {@link PodStorageHandle#podStorageDirectory}.
     *
     * @param pod: The {@link RRPod} from which the playback progress will be extracted.
     *
     */

    private void storePodProgress(@NonNull final RRPod pod){

        final int progress = pod.getProgress();

        final String podProgressKey = getPodFileName(pod) + PodStorageConstants.POD_PROGRESS_SUFFIX;

        podProgressStorage.edit().putInt(podProgressKey, progress).apply();

    }


    /**
     *
     * Retrieves the playback progress stored with {@link PodStorageHandle#storePodProgress(RRPod)}.
     *
     * @param pod: The {@link RRPod} with which the playback progress will be associated.
     *
     * @return The {@link RRPod}'s playback progress.
     */

    private int getStoredPodProgress(@NonNull RRPod pod){

        return podProgressStorage.getInt(getPodFileName(pod) + PodStorageConstants.POD_PROGRESS_SUFFIX,0);

    }



    public void storePodDownloadQueue(@Nullable ArrayList<RRPod> downloadQueue){

        podDownloadQueueStorage
                .edit()
                .putString(PodStorageConstants.POD_DOWNLOAD_QUEUE_STORAGE_KEY, downloadQueue == null ? null : new Gson().toJson(downloadQueue))
                .apply();

    }


    @Nullable
    public ArrayList<RRPod> getStoredPodDownloadQueue(){

        final String queueJson = podDownloadQueueStorage.getString(PodStorageConstants.POD_DOWNLOAD_QUEUE_STORAGE_KEY,null);

        if(queueJson == null){

            // No download queue in storage

            return null;

        }

        return new Gson().fromJson(queueJson,new TypeToken<ArrayList<RRPod>>(){}.getType());

    }







    /**
     *
     * Calculates device space usage of {@link RRPod}s inside a {@link PodsPackage}.
     *
     * @param podsPackage: Collection of pods (possibly multiple PodTypes) to include in space usage total.
     *
     * @return total space usage of {@param podsPackage} in megabytes.
     *
     */

    public float calculateStorageSpaceUsage(@NonNull final PodsPackage podsPackage) {

        float spaceUsage = 0;

        for (PodType podType : PodType.values()){

            final ArrayList<RRPod> podList = podsPackage.getPodList(podType);

            if(podList == null){

                // Pod type not represented
                continue;

            }

            for(RRPod pod : podList){

                if (pod.isDownloaded()) {

                    spaceUsage += getPodDownloadFile(pod).length() / Math.pow(1024, 2);

                }

            }

        }

        return (float) Math.round(spaceUsage * 100) / 100;

    }


}
