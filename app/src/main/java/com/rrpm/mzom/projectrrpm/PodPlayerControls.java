package com.rrpm.mzom.projectrrpm;

import android.content.Context;

import androidx.annotation.NonNull;

interface PodPlayerControls{

    boolean loadPod(@NonNull final RRPod pod, @NonNull final Context context);

    void playPod(@NonNull final RRPod pod, @NonNull final Context context);

    void pauseOrContinuePod(@NonNull final Context context);

    void pausePod();

    void continuePod(@NonNull final Context context);

    void jump(int jump);

    void seekTo(int progress);

}
