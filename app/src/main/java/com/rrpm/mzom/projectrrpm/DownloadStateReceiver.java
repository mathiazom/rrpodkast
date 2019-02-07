package com.rrpm.mzom.projectrrpm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadStateReceiver extends BroadcastReceiver {

    DownloadStateReceiverListener downloadStateReceiverListener;

    public interface DownloadStateReceiverListener{
        void updateDownloadProgress(String podName, float progress);
    }

    public DownloadStateReceiver(DownloadStateReceiverListener downloadStateReceiverListener){
        this.downloadStateReceiverListener = downloadStateReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        downloadStateReceiverListener.updateDownloadProgress((String)intent.getExtras().get("DOWNLOADING_PODKAST_NAME"),(float)intent.getExtras().get(DownloadService.Constants.EXTENDED_DATA_STATUS));
        //System.out.println("Intent received: " + intent + " with progress: " + intent.getExtras().get(DownloadService.Constants.EXTENDED_DATA_STATUS));
    }
}
