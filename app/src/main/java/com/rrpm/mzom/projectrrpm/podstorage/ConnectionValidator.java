package com.rrpm.mzom.projectrrpm.podstorage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

// Class to check network connection'
public class ConnectionValidator {

    private static final String TAG = "RRP-ConnectionValidator";



    public static boolean isConnected(@NonNull Context context) {

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager == null){

            return false;

        }

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null){

            return false;

        }

        return networkInfo.isConnected();

    }


    public interface ConnectionListener {

        void onConnectionChanged(boolean isConnected);

    }


    public static void attemptToRegisterConnectionListener(@NonNull Context context, @NonNull ConnectionListener connectionListener) {


        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager == null){

            Log.e(TAG,"Connectivity manager was null");

            return;

        }

        final ConnectivityManager.NetworkCallback networkCallback;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);

                    connectionListener.onConnectionChanged(isConnected(context));
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);

                    connectionListener.onConnectionChanged(isConnected(context));
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();

                    connectionListener.onConnectionChanged(isConnected(context));
                }
            };

        }else{

            Log.e(TAG,"Build version was too old");

            return;

        }

        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),networkCallback);

    }

}
