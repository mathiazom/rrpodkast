package com.rrpm.mzom.projectrrpm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;

import java.util.ArrayList;

class PodDownloader {

    private static final String TAG = "RRP-PodDownloader";

    private static final String POD_DOWNLOADER_WIFI_LOCK_TAG = "com.rrpm.mzom.projectrrpm.PodDownloader.POD_DOWNLOADER_WIFI_LOCK_TAG";

    static final String DOWNLOAD_REQUEST_RECEIVER = "com.rrpm.mzom.projectrrpm.PodDownloader.DOWNLOAD_REQUEST_RECEIVER";
    static final String DOWNLOAD_REQUEST_POD_URL = "com.rrpm.mzom.projectrrpm.PodDownloader.DOWNLOAD_REQUEST_URL";
    static final String DOWNLOAD_REQUEST_POD_ID = "com.rrpm.mzom.projectrrpm.PodDownloader.DOWNLOAD_REQUEST_ID";

    private final Activity activity;

    private final PodsViewModel podsViewModel;

    private final PodStorageHandle podStorageHandle;

    private boolean isDownloading;

    @NonNull
    private ArrayList<RRPod> downloadQueue = new ArrayList<>();

    private int downloadingProgress;

    PodDownloader(@NonNull final FragmentActivity activity){
        this.activity = activity;
        this.podsViewModel = ViewModelProviders.of(activity).get(PodsViewModel.class);
        this.podStorageHandle = new PodStorageHandle(activity);
    }

    void setDownloadQueue(@NonNull final ArrayList<RRPod> downloadQueue){
        this.downloadQueue = downloadQueue;
    }

    /**
     *
     * Adds the given {@link RRPod} to the downloadQueue,
     * as long as the pod is not already added to queue or downloaded
     *
     * @param pod: pod to download
     */

    void requestPodDownload(@NonNull final RRPod pod){

        if(pod.isDownloaded()){
            Log.e(TAG,"Pod already downloaded");
            return;
        }

        if(downloadQueue.indexOf(pod) != -1){
            Log.i(TAG,"Pod already added to download queue");
            return;
        }

        downloadQueue.add(pod);

        if(!isDownloading){
            downloadFromQueue();
        }

    }

    void downloadFromQueue(){

        if(isDownloading){
            Log.i(TAG,"Already isDownloading");
            return;
        }

        if(downloadQueue.size() == 0){
            return;
        }

        downloadPod(downloadQueue.get(0));

    }

    private void downloadPod(@NonNull final RRPod pod) {

        if (!PermissionsManager.isAllPermissionsGranted(activity)){

            Log.e(TAG,"Permissions not granted: " + PermissionsManager.getPermissionsToGrant(PermissionsConstants.ALL_PERMISSIONS,activity));

            PermissionsManager.retrieveAllPermissions(activity);

        }

        Log.i(TAG,"Downloading " + pod);

        final DownloadResultReceiver downloadReceiver = new DownloadResultReceiver(new Handler());
        downloadReceiver.setReceiver(getDownloadReceiver(pod, activity));

        final Intent intent =
                new Intent(Intent.ACTION_SYNC, null, activity, DownloadService.class)
                .putExtra(DOWNLOAD_REQUEST_POD_ID,pod.getId())
                .putExtra(DOWNLOAD_REQUEST_POD_URL,pod.getUrl())
                .putExtra(DOWNLOAD_REQUEST_RECEIVER, downloadReceiver);

        final WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock;
        if(wifiManager != null){
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, POD_DOWNLOADER_WIFI_LOCK_TAG);
            wifiLock.acquire();
        }

        activity.startService(intent);

        isDownloading = true;

    }

    private DownloadResultReceiver.Receiver getDownloadReceiver(@NonNull final RRPod pod, @NonNull final Context context) {

        return (resultCode, resultData) -> {

            switch (resultCode) {
                case DownloadService.STATUS_FINISHED:

                    isDownloading = false;
                    downloadQueue.remove(pod);

                    // Re-check download state (true if download actually was successful)
                    podStorageHandle.applyPodStorageValues(pod);

                    podsViewModel.getPodsPackage().updatePod(pod);

                    downloadFromQueue();

                    break;

                case DownloadService.STATUS_ERROR:

                    // TODO: Handle download error

                    break;

                case DownloadService.STATUS_PROGRESS:

                    downloadingProgress = (int) resultData.getFloat(DownloadService.DOWNLOAD_PROGRESS_TAG);

                    Log.i(TAG,"Download progress: " + String.valueOf(downloadingProgress));

                    break;

            }
        };

    }


    /**
     *
     * Retrieve the RRPod that is currently being downloaded
     *
     * @return RRPod object representing the pod that is currently being downloaded,
     *         or {@code null} if nothing is downloading.
     */

    @Nullable
    RRPod getDownloadingPod(){

        if(!isDownloading){
            return null;
        }

        return downloadQueue.get(0);

    }


    int getDownloadingProgress(){

        return this.downloadingProgress;

    }

}
