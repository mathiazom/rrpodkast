package com.rrpm.mzom.projectrrpm.poddownloading;

import android.app.Activity;

import com.rrpm.mzom.projectrrpm.annotations.NonEmpty;
import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.notifications.NotificationUtils;
import com.rrpm.mzom.projectrrpm.pod.RRPod;

import java.util.ArrayList;

import javax.validation.constraints.Null;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class DownloadingNotificationHandle {


    @NonNull private NotificationManagerCompat notificationManager;

    private NotificationCompat.Builder downloadingNotificationBuilder;

    private Activity activity;


    DownloadingNotificationHandle(@NonNull Activity activity){

        this.activity = activity;

        this.notificationManager = NotificationManagerCompat.from(activity);

    }


    private void initBuilder(){

        downloadingNotificationBuilder = new NotificationCompat.Builder(activity, NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID);

        downloadingNotificationBuilder
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(DownloadingConstants.DOWNLOAD_PROGRESS_MAX, 0, false)
                .setOngoing(true)
                .setContentIntent(
                        NotificationUtils.getNotificationClickIntent(
                                NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID,
                                activity
                        )
                );

    }


    void createNotification(@NonNull ArrayList<RRPod> downloadQueue) {


        if(downloadingNotificationBuilder == null){

            initBuilder();

        }


        if (downloadQueue.size() > 1) {

            downloadingNotificationBuilder.setStyle(
                    new NotificationCompat.BigTextStyle()
                            .bigText(getDownloadNotificationContentText(downloadQueue))
            );

        }

        downloadingNotificationBuilder
                .setContentTitle(getDownloadNotificationTitle(downloadQueue))
                .setContentText(downloadQueue.get(0).getTitle());


        notificationManager.notify(NotificationConstants.DOWNLOADING_NOTIFICATION_ID, downloadingNotificationBuilder.build());

    }

    void setDownloadProgress(int progress) {

        downloadingNotificationBuilder.setProgress(DownloadingConstants.DOWNLOAD_PROGRESS_MAX, progress, false);

        notificationManager.notify(NotificationConstants.DOWNLOADING_NOTIFICATION_ID, downloadingNotificationBuilder.build());


    }

    void setDownloadQueue(@Nullable ArrayList<RRPod> downloadQueue){

        if(downloadQueue == null || downloadQueue.isEmpty()){

            cancelNotification();

            return;

        }

        createNotification(downloadQueue);

    }

    void cancelNotification(){

        notificationManager.cancel(NotificationConstants.DOWNLOADING_NOTIFICATION_ID);

    }

    private static String getDownloadNotificationContentText(@NonNull @NonEmpty final ArrayList<RRPod> downloadQueue) {

        Assertions._assert(!downloadQueue.isEmpty(), "List was empty");

        if (downloadQueue.isEmpty()) {

            throw new RuntimeException("Download queue was empty");

        }


        if (downloadQueue.size() == 1) {

            return downloadQueue.get(0).getTitle();

        }


        final StringBuilder contentText = new StringBuilder();

        if (downloadQueue.size() > 0) {

            for (RRPod pod : downloadQueue) {

                contentText.append(pod.getTitle()).append("\n");

            }

        }

        return contentText.toString();

    }

    private static String getDownloadNotificationTitle(@NonNull final ArrayList<RRPod> downloadQueue) {

        if (downloadQueue.size() == 1) {

            return "Laster ned episode";

        }

        return "Laster ned " + downloadQueue.size() + " episoder";

    }







}
