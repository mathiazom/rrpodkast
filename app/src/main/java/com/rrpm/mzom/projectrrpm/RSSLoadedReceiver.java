package com.rrpm.mzom.projectrrpm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by Mathias on 19.01.2018.
 */

public class RSSLoadedReceiver extends BroadcastReceiver {

    RSSLoadedReceiverListener mRSSLoadedReceiverListener;

    public RSSLoadedReceiver(RSSLoadedReceiverListener rssLoadedReceiverListener){
        this.mRSSLoadedReceiverListener = rssLoadedReceiverListener;
    }

    public interface RSSLoadedReceiverListener{
        void RRSLoaded(ArrayList<ArrayList<RRPod>> masterlist);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(intent.getExtras());
        mRSSLoadedReceiverListener.RRSLoaded((ArrayList<ArrayList<RRPod>>)intent.getExtras().get("RSS_MASTER_LIST"));
    }
}
