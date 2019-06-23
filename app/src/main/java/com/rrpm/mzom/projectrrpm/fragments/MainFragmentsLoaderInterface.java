package com.rrpm.mzom.projectrrpm.fragments;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import androidx.annotation.NonNull;

public interface MainFragmentsLoaderInterface {

    void loadPodFragment(@NonNull final RRPod pod);

    void loadPodPlayerFragment();

    void hidePodPlayerFragment();

    void loadSmallPodPlayerFragment();

    void hideSmallPodPlayerFragment();

    void loadFilterFragment();

    void hideFilterFragment();

}
