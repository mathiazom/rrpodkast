package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

public class ServiceRestarter extends BroadcastReceiver {

    private static final String TAG = "RRP-ServiceRestarter";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        Log.i(TAG, "Service tried to stop");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, PodPlayerService.class));
        } else {
            context.startService(new Intent(context, PodPlayerService.class));
        }

    }

}
