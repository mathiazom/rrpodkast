package com.rrpm.mzom.projectrrpm.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationUtils {


    private static final String TAG = "RRP-NotifiChannelUtils";


    public static void buildAndRegisterChannelGroups(@NonNull Context context, @NonNull NotificationChannelGroupBuilder... channelGroupBuilders){

        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if(notificationManager == null){
            Log.e(TAG,"Notification manager was null, will not register notification channel groups");
            return;
        }

        for(NotificationChannelGroupBuilder channelGroupBuilder: channelGroupBuilders){

            notificationManager.createNotificationChannelGroup(channelGroupBuilder.build(context));

        }

    }

    public static void buildAndRegisterChannels(@NonNull Context context, @NonNull NotificationChannelBuilder... channelBuilders){

        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if(notificationManager == null){
            Log.e(TAG,"Notification manager was null, will not register notification channels");
            return;
        }

        for (NotificationChannelBuilder channelBuilder : channelBuilders){

            notificationManager.createNotificationChannel(channelBuilder.build(context));

        }

    }








}
