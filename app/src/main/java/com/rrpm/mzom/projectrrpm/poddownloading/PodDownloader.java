package com.rrpm.mzom.projectrrpm.poddownloading;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsManager;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsConstants;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class PodDownloader {

    private static final String TAG = "RRP-PodDownloader";

    private static final String POD_DOWNLOADER_WIFI_LOCK_TAG = "com.rrpm.mzom.projectrrpm.PodDownloading.PodDownloader.POD_DOWNLOADER_WIFI_LOCK_TAG";

    private final Activity activity;

    private final PodsViewModel podsViewModel;

    private final PodDownloadsViewModel podDownloadsViewModel;

    private final PodStorageHandle podStorageHandle;

    private final DownloadingNotificationHandle downloadingNotificationHandle;

    private boolean isDownloading;


    private int downloadingProgress;

    public PodDownloader(@NonNull final FragmentActivity activity) {

        this.activity = activity;

        this.podStorageHandle = new PodStorageHandle(activity);

        this.downloadingNotificationHandle = new DownloadingNotificationHandle(activity);

        this.podsViewModel = ViewModelProviders.of(activity).get(PodsViewModel.class);

        this.podDownloadsViewModel = ViewModelProviders.of(activity).get(PodDownloadsViewModel.class);
        podDownloadsViewModel.getObservableDownloadQueue().observe(activity,downloadQueue -> {

            if(downloadQueue == null || downloadQueue.isEmpty()){

                downloadingNotificationHandle.cancelNotification();

                return;

            }

            downloadingNotificationHandle.setDownloadQueue(podDownloadsViewModel.getDownloadQueue());

        });


    }

    public void onPermissionsGranted() {

        downloadFromQueue();

    }


    /**
     * Adds the given {@link RRPod} to the downloadQueue,
     * as long as the pod is not already added to queue or downloaded
     *
     * @param pod: pod to download
     */

    public void requestPodDownload(@NonNull final RRPod pod) {


        Assertions._assert(!pod.isDownloaded(),"Pod already downloaded");


        if (podDownloadsViewModel.hasPodInQueue(pod)) {

            Log.i(TAG, "Pod already added to download queue");

            return;

        }

        podDownloadsViewModel.addPodToDownloadQueue(pod);

        if (!isDownloading) {

            downloadFromQueue();

        }

    }

    public void downloadFromQueue() {

        if (isDownloading) {

            Log.i(TAG, "Already downloading");

            return;

        }

        final RRPod nextPodInQueue = podDownloadsViewModel.getNextPodInQueue();

        if (nextPodInQueue == null) {

            Log.i(TAG, "No next pod in download queue");

            return;

        }

        downloadPod(nextPodInQueue);

    }

    private void downloadPod(@NonNull final RRPod pod) {

        if (!PermissionsManager.isAllPermissionsGranted(activity)) {

            Log.e(TAG, "Permissions not granted: " + PermissionsManager.getPermissionsToGrant(PermissionsConstants.ALL_PERMISSIONS, activity));

            PermissionsManager.retrieveAllPermissions(activity);

            return;

        }

        Log.i(TAG, "Downloading " + pod);

        final DownloadResultReceiver downloadReceiver = new DownloadResultReceiver(new Handler());
        downloadReceiver.setReceiver(getDownloadReceiver(pod));

        final Intent intent =
                new Intent(Intent.ACTION_SYNC, null, activity, PodDownloadService.class)
                        .putExtra(DownloadingConstants.DOWNLOAD_REQUEST_POD_ID, pod.getId())
                        .putExtra(DownloadingConstants.DOWNLOAD_REQUEST_POD_URL, pod.getUrl())
                        .putExtra(DownloadingConstants.DOWNLOAD_REQUEST_RECEIVER, downloadReceiver);

        final WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock;
        if (wifiManager != null) {

            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, POD_DOWNLOADER_WIFI_LOCK_TAG);

            wifiLock.acquire();

        }

        activity.startService(intent);

        isDownloading = true;

    }

    private DownloadResultReceiver.Receiver getDownloadReceiver(@NonNull final RRPod pod) {

        return (resultCode, resultData) -> {

            switch (resultCode) {
                case PodDownloadService.STATUS_FINISHED:

                    Log.i(TAG, "Pod download finished");

                    isDownloading = false;
                    podDownloadsViewModel.removePodFromDownloadQueue(pod);

                    // Re-check download state (true if download truly was successful)
                    podStorageHandle.insertPodUserData(pod);

                    podsViewModel.updatePodInStorage(pod);

                    downloadFromQueue();

                    break;

                case PodDownloadService.STATUS_ERROR:

                    // TODO: Handle download error

                    Log.e(TAG, "Pod download error");

                    break;

                case PodDownloadService.STATUS_PROGRESS:

                    downloadingProgress = (int) resultData.getFloat(DownloadingConstants.DOWNLOAD_PROGRESS_TAG);

                    podDownloadsViewModel.postDownloadProgress(pod, downloadingProgress);

                    downloadingNotificationHandle.setDownloadProgress(downloadingProgress);

                    break;

            }
        };

    }
}
