package com.rrpm.mzom.projectrrpm;

import android.support.annotation.NonNull;

public interface PodPlayerController {

    void playPod(@NonNull final RRPod pod);

    void pausePod();

    void continuePod();

    void stopPod();

}
