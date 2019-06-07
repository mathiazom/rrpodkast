package com.rrpm.mzom.projectrrpm.poddownloading;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.activities.MainActivity;
import com.rrpm.mzom.projectrrpm.annotations.NonEmpty;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsManager;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsConstants;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class PodDownloader {

    private static final String TAG = "RRP-PodDownloader";

    private static final String POD_DOWNLOADER_WIFI_LOCK_TAG = "com.rrpm.mzom.projectrrpm.PodDownloading.PodDownloader.POD_DOWNLOADER_WIFI_LOCK_TAG";

    static final String DOWNLOAD_REQUEST_RECEIVER = "com.rrpm.mzom.projectrrpm.PodDownloading.PodDownloader.DOWNLOAD_REQUEST_RECEIVER";
    static final String DOWNLOAD_REQUEST_POD_URL = "com.rrpm.mzom.projectrrpm.PodDownloading.PodDownloader.DOWNLOAD_REQUEST_URL";
    static final String DOWNLOAD_REQUEST_POD_ID = "com.rrpm.mzom.projectrrpm.PodDownloading.PodDownloader.DOWNLOAD_REQUEST_ID";

    private final Activity activity;

    private final PodsViewModel podsViewModel;

    private final PodDownloadsViewModel podDownloadsViewModel;

    private final PodStorageHandle podStorageHandle;

    private boolean isDownloading;

    @NonNull
    private ArrayList<RRPod> downloadQueue = new ArrayList<>();

    private int downloadingProgress;

    public PodDownloader(@NonNull final FragmentActivity activity){

        this.activity = activity;

        this.podsViewModel = ViewModelProviders.of(activity).get(PodsViewModel.class);

        this.podDownloadsViewModel = ViewModelProviders.of(activity).get(PodDownloadsViewModel.class);

        this.podStorageHandle = new PodStorageHandle(activity);

    }

    public void onPermissionsGranted(){

        downloadFromQueue();

    }

    public void setDownloadQueue(@NonNull final ArrayList<RRPod> downloadQueue){
        this.downloadQueue = downloadQueue;
    }

    /**
     *
     * Adds the given {@link RRPod} to the downloadQueue,
     * as long as the pod is not already added to queue or downloaded
     *
     * @param pod: pod to download
     */

    public void requestPodDownload(@NonNull final RRPod pod){

        if(pod.isDownloaded()){
            Log.e(TAG,"Pod already downloaded");
            return;
        }

        if(downloadQueue.indexOf(pod) != -1){
            Log.i(TAG,"Pod already added to download queue");
            return;
        }

        downloadQueue.add(pod);

        initDownloadNotification();

        if(!isDownloading){
            downloadFromQueue();
        }

    }

    public void downloadFromQueue(){

        if(isDownloading){
            Log.i(TAG,"Already downloading");
            return;
        }

        if(downloadQueue.isEmpty()){
            Log.i(TAG,"Download queue is empty");
            return;
        }

        downloadPod(downloadQueue.get(0));

    }

    private void downloadPod(@NonNull final RRPod pod) {

        if (!PermissionsManager.isAllPermissionsGranted(activity)){

            Log.e(TAG,"Permissions not granted: " + PermissionsManager.getPermissionsToGrant(PermissionsConstants.ALL_PERMISSIONS,activity));

            PermissionsManager.retrieveAllPermissions(activity);

            return;

        }

        Log.i(TAG,"Downloading " + pod);

        final DownloadResultReceiver downloadReceiver = new DownloadResultReceiver(new Handler());
        downloadReceiver.setReceiver(getDownloadReceiver(pod));

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

        initDownloadNotification();

    }

    private DownloadResultReceiver.Receiver getDownloadReceiver(@NonNull final RRPod pod) {

        return (resultCode, resultData) -> {

            switch (resultCode) {
                case DownloadService.STATUS_FINISHED:

                    isDownloading = false;
                    downloadQueue.remove(pod);
                    podDownloadsViewModel.removePod(pod);

                    // Re-check download state (true if download actually was successful)
                    podStorageHandle.applyPodStorageValues(pod);

                    podsViewModel.storePod(pod);

                    if(downloadQueue.isEmpty()){
                        notificationManager.cancel(NotificationConstants.DOWNLOADING_NOTIFICATION_ID);
                        return;
                    }

                    downloadFromQueue();

                    break;

                case DownloadService.STATUS_ERROR:

                    // TODO: Handle download error

                    break;

                case DownloadService.STATUS_PROGRESS:

                    downloadingProgress = (int) resultData.getFloat(DownloadingConstants.DOWNLOAD_PROGRESS_TAG);

                    podDownloadsViewModel.postDownloadProgress(pod,downloadingProgress);

                    updateDownloadNotification(downloadingProgress);

                    break;

            }
        };

    }



    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder downloadingNotificationBuilder;


    private void initDownloadNotification(){

        if(downloadQueue.isEmpty()){

            Log.e(TAG,"Download queue was empty, will not display downloading notification");

            return;

        }

        if(notificationManager == null){
            notificationManager = NotificationManagerCompat.from(activity);
        }

        downloadingNotificationBuilder = new NotificationCompat.Builder(activity, NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID);

        if(downloadQueue.size() > 1){

            downloadingNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getDownloadNotificationContentText(downloadQueue)));

        }

        downloadingNotificationBuilder
                .setContentTitle(getDownloadNotificationTitle(downloadQueue))
                .setContentText(downloadQueue.get(0).getTitle())
                .setSmallIcon(R.drawable.ic_round_get_app_24px)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(DownloadingConstants.DOWNLOAD_PROGRESS_MAX,downloadingProgress,false)
                .setOngoing(true);
                /*.setContentIntent(
                        PendingIntent.getActivity(
                                activity,
                                0,
                                new Intent(activity, MainActivity.class),
                                0
                        )
                );*/

        notificationManager.notify(NotificationConstants.DOWNLOADING_NOTIFICATION_ID, downloadingNotificationBuilder.build());

    }

    private void updateDownloadNotification(int downloadingProgress){

        downloadingNotificationBuilder.setProgress(DownloadingConstants.DOWNLOAD_PROGRESS_MAX,downloadingProgress,false);

        notificationManager.notify(NotificationConstants.DOWNLOADING_NOTIFICATION_ID, downloadingNotificationBuilder.build());


    }

    private static String getDownloadNotificationContentText(@NonNull @NonEmpty final ArrayList<RRPod> downloadQueue){

        if(downloadQueue.isEmpty()){
            throw new RuntimeException("Download queue was empty");
        }


        if(downloadQueue.size() == 1){
            return downloadQueue.get(0).getTitle();
        }


        final StringBuilder contentText = new StringBuilder();

        if(downloadQueue.size() > 0){

            for (RRPod pod : downloadQueue){

                contentText.append(pod.getTitle()).append("\n");

            }

        }

        return contentText.toString();

    }

    private static String getDownloadNotificationTitle(@NonNull final ArrayList<RRPod> downloadQueue){

        if(downloadQueue.size() == 1){

            return "Laster ned episode";

        }

        return "Laster ned " + downloadQueue.size() + " episoder";

    }


}
