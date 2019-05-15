package com.rrpm.mzom.projectrrpm;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;


class PermissionsManager {

    private static final String TAG = "RRP-PermissionsManager";


    static boolean isAllPermissionsGranted(@NonNull final Activity activity){

        return permissionsGranted(PermissionsConstants.ALL_PERMISSIONS, activity);

    }

    static void retrieveAllPermissions(@NonNull final Activity activity){

        retrievePermissions(PermissionsConstants.ALL_PERMISSIONS, activity);

    }

    private static boolean permissionsGranted(@NonNull final String[] permissions, @NonNull final Activity activity){

        return getPermissionsToGrant(permissions,activity).size() == 0;

    }

    private static void retrievePermissions(@NonNull final String[] permissions, @NonNull final Activity activity) {

        final ArrayList<String> permissionsToGrant = getPermissionsToGrant(permissions,activity);

        if(permissionsToGrant.size() == 0){

            Log.i(TAG,"All permissions granted");

            return;

        }

        ActivityCompat.requestPermissions(
                activity,
                permissionsToGrant.toArray(new String[0]),
                PermissionsConstants.APP_PERMISSIONS_REQUEST
        );

    }

    static ArrayList<String> getPermissionsToGrant(@NonNull final String[] permissions, @NonNull final Activity activity){

        final ArrayList<String> permissionsToGrant = new ArrayList<>();

        for(String p : permissions){

            if(ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED){

                permissionsToGrant.add(p);

            }

        }

        return permissionsToGrant;

    }


    /**
     *
     * Checks if all permissions from request have been granted by user
     *
     * @param requestCode identifying permissions request
     * @param grantResults array of individual permission statuses
     *
     * @return {@code true} if all request permissions have been granted
     *         {@code false} if any permission has been denied,
     *         or requestCode does not match PermissionsConstants.APP_PERMISSIONS_REQUEST
     */

    static boolean allRequestPermissionsGranted(int requestCode, @NonNull int[] grantResults){

        if (requestCode == PermissionsConstants.APP_PERMISSIONS_REQUEST){

            for(int r : grantResults){

                if(r == PackageManager.PERMISSION_DENIED){

                    return false;

                }

            }

            return true;

        }

        return false;

    }


}
