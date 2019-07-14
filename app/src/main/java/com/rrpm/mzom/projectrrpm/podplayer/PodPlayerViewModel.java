package com.rrpm.mzom.projectrrpm.podplayer;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PodPlayerViewModel extends ViewModel {


    private static final String TAG = "RRP-PodPlayerViewModel";


    @NonNull
    private final MutableLiveData<RRPod> playerPodObservable = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Integer> playerProgressObservable = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Integer> playerDurationObservable = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Boolean> isPlayingObservable = new MutableLiveData<>();


    // Only used when activity is restored and pod player instance must be restored
    private PodPlayer mostRecentPodPlayer;



    @NonNull
    public PodPlayer restorePodPlayerWithActivity(@NonNull FragmentActivity fragmentActivity){

        if (mostRecentPodPlayer == null) {

            mostRecentPodPlayer = new PodPlayer(fragmentActivity);

        }

        mostRecentPodPlayer.regulate();

        return mostRecentPodPlayer;

    }




    public void setPlayerPod(@NonNull RRPod pod) {
        this.playerPodObservable.setValue(pod);
    }

    @NonNull
    public LiveData<RRPod> getPlayerPodObservable() {
        return this.playerPodObservable;
    }

    @Nullable
    public RRPod getPlayerPod(){

        return playerPodObservable.getValue();

    }


    public void postPlayerProgress(int progress) {

        this.playerProgressObservable.postValue(progress);

    }

    @NonNull
    LiveData<Integer> getPlayerDurationObservable() {
        return this.playerDurationObservable;
    }

    public void setPlayerDuration(int duration) {

        this.playerDurationObservable.setValue(duration);

    }

    @NonNull
    public LiveData<Integer> getPlayerProgressObservable() {
        return this.playerProgressObservable;
    }


    public void setIsPlaying(boolean isPlaying) {

        this.isPlayingObservable.setValue(isPlaying);
    }

    public boolean isPlaying() {

        return isPlayingObservable.getValue() != null && isPlayingObservable.getValue();

    }

    @NonNull
    public LiveData<Boolean> getIsPlayingObservable() {
        return this.isPlayingObservable;
    }

}
