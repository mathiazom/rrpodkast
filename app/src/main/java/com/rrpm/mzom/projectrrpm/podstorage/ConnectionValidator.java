package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.net.ConnectivityManager;
import androidx.annotation.NonNull;

// Class to check network connection'
public class ConnectionValidator {

    public static boolean isConnected(@NonNull Context context) {

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();

    }

}