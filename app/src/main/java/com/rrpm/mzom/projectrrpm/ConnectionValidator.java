package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.net.ConnectivityManager;
import androidx.annotation.NonNull;

// Class to check network connection'
class ConnectionValidator {

    static boolean isConnected(@NonNull Context context) {

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();

    }

}
