package com.rrpm.mzom.projectrrpm.podplayer;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerPodViewModel extends ViewModel {


    @NonNull
    private final MutableLiveData<RRPod> playerPod = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Integer> playerProgress = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();


    void setPlayerPod(@NonNull RRPod pod) {
        this.playerPod.setValue(pod);
    }

    @NonNull
    public LiveData<RRPod> getPlayerPodObservable() {
        return this.playerPod;
    }


    void postPlayerProgress(int progress) {

        this.playerProgress.postValue(progress);

    }

    @NonNull
    public LiveData<Integer> getPlayerProgressObservable() {
        return this.playerProgress;
    }


    void setIsPlaying(boolean isPlaying) {
        this.isPlaying.setValue(isPlaying);
    }

    public boolean isPlaying() {

        return isPlaying.getValue() != null && isPlaying.getValue();

    }

    @NonNull
    public LiveData<Boolean> getIsPlayingObservable() {
        return this.isPlaying;
    }

}
