package com.rrpm.mzom.projectrrpm;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

class FragmentLoader {


    private static final String TAG = "RRP-FragmentLoader";


    private FragmentActivity activity;


    FragmentLoader(@NonNull FragmentActivity activity){

        this.activity = activity;

    }


    void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment){

        loadFragment(frameLayout,fragment,true);

    }

    private void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment, boolean addToBackStack){
        loadFragment(frameLayout,fragment,0,0,0,0,addToBackStack);
    }

    private void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim, boolean addToBackStack){

        Log.i(TAG,"Started loading " + fragment.toString() + " to " + String.valueOf(frameLayout));

        final FragmentManager fragmentManager = activity.getSupportFragmentManager();

        final FragmentTransaction transaction =  fragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                .replace(frameLayout, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }


        // TODO: Inspect difference in below commit methods

        //transaction.commit();

        transaction.commitAllowingStateLoss();

        Log.i(TAG,"Committed " + fragment.toString() + " to " + String.valueOf(frameLayout));

    }


}
