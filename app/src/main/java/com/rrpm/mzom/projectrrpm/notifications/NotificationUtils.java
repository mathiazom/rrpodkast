package com.rrpm.mzom.projectrrpm.notifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;



public class NotificationUtils {


    private static final String TAG = "RRP-NotifiChannelUtils";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void buildAndRegisterChannelGroups(@NonNull Context context, @NonNull NotificationChannelGroupBuilder... channelGroupBuilders) {

        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (notificationManager == null) {

            Log.e(TAG, "Notification manager was null, will not register notification channel groups");

            return;

        }

        for (NotificationChannelGroupBuilder channelGroupBuilder : channelGroupBuilders) {

            notificationManager.createNotificationChannelGroup(channelGroupBuilder.build(context));

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void buildAndRegisterChannels(@NonNull Context context, @NonNull NotificationChannelBuilder... channelBuilders) {

        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (notificationManager == null) {

            Log.e(TAG, "Notification manager was null, will not register notification channels");

            return;

        }

        for (NotificationChannelBuilder channelBuilder : channelBuilders) {

            notificationManager.createNotificationChannel(channelBuilder.build(context));

        }

    }


    private static final int CLICK_INTENT_REQUEST_CODE = 2573;

    @NonNull
    public static PendingIntent getNotificationClickIntent(@NonNull String notificationChannelId, @NonNull Activity activity) {

        final Intent notificationClickIntent = new Intent(activity, activity.getClass())
                .putExtra(
                        NotificationConstants.INTENT_CLICKED_NOTIFICATION_ID_EXTRA_NAME,
                        notificationChannelId
                );

        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(
                activity,
                CLICK_INTENT_REQUEST_CODE,
                notificationClickIntent,
                0
        );


    }


}
