package com.rrpm.mzom.projectrrpm;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

class DownloadResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    DownloadResultReceiver(Handler handler) {
        super(handler);
    }

    void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
