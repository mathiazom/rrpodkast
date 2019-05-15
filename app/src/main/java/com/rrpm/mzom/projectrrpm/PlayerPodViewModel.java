package com.rrpm.mzom.projectrrpm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class PlayerPodViewModel extends ViewModel {


    @NonNull private MutableLiveData<RRPod> playerPod = new MutableLiveData<>();

    @NonNull private MutableLiveData<Integer> playerProgress = new MutableLiveData<>();

    @NonNull private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();


    void setPlayerPod(@NonNull RRPod pod){

        this.playerPod.setValue(pod);
    }

    @NonNull
    LiveData<RRPod> getPlayerPod(){
        return this.playerPod;
    }


    void postPlayerProgress(int progress){

        this.playerProgress.postValue(progress);

    }

    LiveData<Integer> getPlayerProgress(){

        return this.playerProgress;

    }


    void setIsPlaying(boolean isPlaying){

        this.isPlaying.setValue(isPlaying);

    }

    boolean isPlaying(){

        final MutableLiveData<Boolean> isPlayingData = getIsPlayingData();

        return isPlayingData.getValue() != null && isPlayingData.getValue();

    }

    @NonNull
    MutableLiveData<Boolean> getIsPlayingData(){

        return this.isPlaying;

    }

}
