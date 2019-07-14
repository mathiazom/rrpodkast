package com.rrpm.mzom.projectrrpm.podplayer;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import androidx.annotation.NonNull;

public interface PodPlayerControls{

    void loadPod(@NonNull final RRPod pod);

    void playPod(@NonNull final RRPod pod);

    void pauseOrContinuePod();

    void pausePod();

    void continuePod();

    void jump(int jump);

    void seekTo(int progress);

}
