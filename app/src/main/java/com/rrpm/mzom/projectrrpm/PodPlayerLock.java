package com.rrpm.mzom.projectrrpm;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;

class PodPlayerLock {


    private Context context;

    private WifiManager wifiManager;

    private WifiManager.WifiLock wifiLock;


    PodPlayerLock(@NonNull final Context context){

        this.context = context;

        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    void activateWakeLock(@NonNull final MediaPlayerWrapper mp){

        // Prevent system from stopping playback when device is idle
        mp.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

    }

    void activateWifiLock(){

        // Prevent system from disabling wifi when device is idle
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

    }

    void disableWifiLock(){

        if(wifiLock == null || !wifiLock.isHeld()){
            return;
        }
        wifiLock.release();

    }


}
