package com.rrpm.mzom.projectrrpm.fragments;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

class FragmentLoader {


    private static final String TAG = "RRP-FragmentLoader";

    private final FragmentManager fragmentManager;


    FragmentLoader(@NonNull final FragmentManager fragmentManager){
        this(fragmentManager,null);
    }

    private FragmentLoader(@NonNull final FragmentManager fragmentManager, @Nullable FragmentManager.OnBackStackChangedListener onBackStackChangedListener){

        this.fragmentManager = fragmentManager;

        if(onBackStackChangedListener != null){
            fragmentManager.addOnBackStackChangedListener(onBackStackChangedListener);
        }

    }



    void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment, boolean addToBackStack){

        loadFragment(frameLayout,fragment,null,addToBackStack);

    }

    void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment, @Nullable FragmentAnimations fragmentAnimations, boolean addToBackStack){

        if(fragmentAnimations == null){

            loadFragment(
                    frameLayout,
                    fragment,
                    0, 0,0,0,
                    addToBackStack);

            return;
        }

        loadFragment(
                frameLayout,
                fragment,
                fragmentAnimations.getEnterAnim(),
                fragmentAnimations.getExitAnim(),
                fragmentAnimations.getPopEnterAnim(),
                fragmentAnimations.getPopExitAnim(),
                addToBackStack
        );

    }

    private void loadFragment(@IdRes int frameLayout, @NonNull final Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim, boolean addToBackStack){

        final String fragmentTag = fragment.getClass().getSimpleName();

        final FragmentTransaction transaction =  fragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                .replace(frameLayout, fragment,fragmentTag);

        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag);
        }


        // TODO: Inspect difference in below commit methods
        transaction.commit();
        //transaction.commitAllowingStateLoss();

        Log.i(TAG,"Committed " + fragment.toString() + " to " + String.valueOf(frameLayout));

    }


    void showFragment(@NonNull final Fragment fragment){

        showFragment(fragment,0,0,0,0);

    }

    void showFragment(@NonNull final Fragment fragment, @Nullable FragmentAnimations fragmentAnimations){

        if(fragmentAnimations == null){

            showFragment(fragment);

            return;

        }

        showFragment(
                fragment,
                fragmentAnimations.getEnterAnim(),
                fragmentAnimations.getExitAnim(),
                fragmentAnimations.getPopEnterAnim(),
                fragmentAnimations.getPopExitAnim()
        );

    }

    private void showFragment(@NonNull final Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim){

        final FragmentTransaction transaction =  fragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                .show(fragment);

        // TODO: Inspect difference in below commit methods
        transaction.commit();
        //transaction.commitAllowingStateLoss();

    }


    void hideFragment(@NonNull final Fragment fragment){

        hideFragment(fragment,null);

    }

    void hideFragment(@NonNull final Fragment fragment, @Nullable FragmentAnimations fragmentAnimations){

        if(fragmentAnimations == null){

            hideFragment(
                    fragment,
                    0,0,0,0
            );

            return;

        }

        hideFragment(
                fragment,
                fragmentAnimations.getEnterAnim(),
                fragmentAnimations.getExitAnim(),
                fragmentAnimations.getPopEnterAnim(),
                fragmentAnimations.getPopExitAnim()
        );

    }

    private void hideFragment(@NonNull final Fragment fragment, int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim){

        final FragmentTransaction transaction =  fragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                .hide(fragment);

        // TODO: Inspect difference in below commit methods
        transaction.commit();
        //transaction.commitAllowingStateLoss();


    }


}
