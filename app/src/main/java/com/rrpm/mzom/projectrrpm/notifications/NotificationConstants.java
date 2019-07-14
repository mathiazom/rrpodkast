package com.rrpm.mzom.projectrrpm.notifications;

import android.app.NotificationManager;
import android.os.Build;

import com.rrpm.mzom.projectrrpm.R;

import androidx.annotation.RequiresApi;


public final class NotificationConstants {


    public static final String INTENT_CLICKED_NOTIFICATION_ID_EXTRA_NAME = "com.rrpm.mzom.projectrrpm.notifications.NotificationConstants.INTENT_CLICKED_NOTIFICATION_ID_EXTRA_NAME";


    // Download notifications

    private static final String DOWNLOAD_NOTIFICATIONS_GROUP_ID = "com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants.DOWNLOAD_NOTIFICATIONS_GROUP_ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static final NotificationChannelGroupBuilder DOWNLOAD_NOTIFICATIONS_GROUP_BUILDER = new NotificationChannelGroupBuilder()
            .setId(DOWNLOAD_NOTIFICATIONS_GROUP_ID)
            .setName(R.string.download_notifications_group_name);


    public static final String DOWNLOADING_NOTIFICATION_CHANNEL_ID = "com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static final NotificationChannelBuilder DOWNLOADING_NOTIFICATION_CHANNEL_BUILDER = new NotificationChannelBuilder()
            .setId(NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID)
            .setName(R.string.downloading_notification_channel_name)
            .setDescription(R.string.downloading_notification_channel_description)
            .setImportance(NotificationManager.IMPORTANCE_LOW)
            .setGroupId(NotificationConstants.DOWNLOAD_NOTIFICATIONS_GROUP_ID);

    public static final int DOWNLOADING_NOTIFICATION_ID = 54345;


    private static final String COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_ID = "com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants.COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static final NotificationChannelBuilder COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_BUILDER = new NotificationChannelBuilder()
            .setId(NotificationConstants.COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_ID)
            .setName(R.string.completed_downloads_notification_channel_name)
            .setDescription(R.string.completed_downloads_notification_channel_description)
            .setImportance(NotificationManager.IMPORTANCE_LOW)
            .setGroupId(NotificationConstants.DOWNLOAD_NOTIFICATIONS_GROUP_ID);



    // Playback notifications

    private static final String PLAYBACK_NOTIFICATIONS_GROUP_ID = "com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants.PLAYBACK_NOTIFICATIONS_GROUP_ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static final NotificationChannelGroupBuilder PLAYBACK_NOTIFICATIONS_GROUP_BUILDER = new NotificationChannelGroupBuilder()
            .setId(PLAYBACK_NOTIFICATIONS_GROUP_ID)
            .setName(R.string.playback_notifications_group_name);


    public static final String PLAYER_NOTIFICATION_CHANNEL_ID = "com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants.PLAYER_NOTIFICATION_CHANNEL_ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static final NotificationChannelBuilder PLAYER_NOTIFICATION_CHANNEL_BUILDER = new NotificationChannelBuilder()
            .setId(NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_ID)
            .setName(R.string.player_notification_channel_name)
            .setDescription(R.string.player_notification_channel_description)
            .setImportance(NotificationManager.IMPORTANCE_LOW)
            .setGroupId(NotificationConstants.PLAYBACK_NOTIFICATIONS_GROUP_ID);


    public static final int PLAYER_NOTIFICATION_ID = 76589;






}
