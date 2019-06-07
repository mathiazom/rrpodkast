package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimerTask;


/**
 *
 * Class to control playback of {@link RRPod} with {@link MediaPlayerWrapper}
 *
 */

public class PodPlayer implements PodPlayerControls, MediaPlayer.OnCompletionListener, Serializable {


    private static final String TAG = "RRP-PodPlayer";


    private final Context context;

    private RRPod pod;

    private static MediaPlayerWrapper mp;


    private final PodsViewModel podsViewModel;

    private final PlayerPodViewModel playerPodViewModel;

    private final PodStorageHandle podStorageHandle;


    private PodPlayerPlaybackRegulator playbackRegulator;


    public PodPlayer(@NonNull FragmentActivity activity) {

        this.context = activity;

        this.podStorageHandle = new PodStorageHandle(activity);

        this.playerPodViewModel = ViewModelProviders.of(activity).get(PlayerPodViewModel.class);

        this.podsViewModel = ViewModelProviders.of(activity).get(PodsViewModel.class);

        initPlaybackRegulator(activity);

    }


    private void initPlaybackRegulator(@NonNull Context context){

        final TaskIterator progressStoringIterator = new TaskIterator(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (pod != null) {
                            pod.setProgress(getProgress());
                            podsViewModel.storePod(pod,true);
                        }
                    }
                },
                PodStorageConstants.SAVE_PROGRESS_FREQ_MS
        );

        final TaskIterator viewModelProgressIterator = new TaskIterator(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (pod != null) {
                            int progress = getProgress();
                            pod.setProgress(progress);
                            playerPodViewModel.postPlayerProgress(progress);
                        }
                    }
                },
                PodStorageConstants.PROGRESS_REFRESH_FREQ_MS
        );

        this.playbackRegulator = new PodPlayerPlaybackRegulator(
                context,
                this,
                new TaskIterator[]{
                        progressStoringIterator,
                        viewModelProgressIterator
                }
        );

    }


    /**
     *
     * Gets the current {@link PodPlayer#mp} playback position.
     *
     * @return the current progress in milliseconds.
     *
     */

    private int getProgress() {

        if (mp == null) {
            throw new MediaPlayerStateException("Cannot get progress, MediaPlayer was null");
        }

        switch (mp.getState()) {

            case IDLE:
            case ERROR:
                throw new MediaPlayerStateException("Cannot get progress because of invalid State, was " + mp.getState());
        }

        return mp.getCurrentPosition();

    }


    /**
     *
     * @return {@link PodPlayer#mp#isPlaying()},
     * or {@code false} if {@link PodPlayer#mp} has incorrect state
     *
     */

    private boolean isPlaying() {

        return mp != null
                && mp.getState() != MediaPlayerWrapper.State.ERROR
                && mp.isPlaying();

    }


    /**
     *
     * Prepares {@link PodPlayer#mp} state and data source for playback of a given pod (without starting the playback).
     *
     * @param pod:      the podcast episode to be played
     * @param progress: timestamp of where to start the pod from (in milliseconds)
     *
     */

    private boolean loadPod(@NonNull final RRPod pod, int progress) {

        Log.i(TAG,"LoadPod");

        if (this.pod == pod) {

            Log.i(TAG, "Pod already loaded");
            return true;

        }

        if (this.pod != null && isPlaying()) {

            Log.i(TAG, "New pod requested for playback, stopping current pod playback");
            pausePod();

        }

        this.pod = pod;

        if (mp != null) {

            // Free resources before creating new MediaPlayer
            mp.release();
        }

        mp = new MediaPlayerWrapper(context,this);

        if (pod.isDownloaded()) {

            try {

                Log.i(TAG,"Setting offline URI: " + podStorageHandle.getPodUri(pod));

                // Load pod to MediaPlayer
                mp.setDataSource(context, podStorageHandle.getPodUri(pod));

            } catch (IOException e) {

                Log.e(TAG, "ERROR setting MediaPlayer data source (downloaded pod): \n" + e.toString());
                e.printStackTrace();
                return false;

            }

        } else {

            try {

                // Load pod to MediaPlayer
                mp.setDataSource(pod.getUrl());

            } catch (IOException e) {

                Log.e(TAG, "ERROR setting MediaPlayer data source, streaming URL was " + pod.getUrl() + "\n" + e.toString());
                e.printStackTrace();
                return false;

            }

        }

        try {

            mp.prepare();

        } catch (IOException e) {

            Log.e(TAG, "Error preparing MediaPlayer : \n" + e.toString());
            e.printStackTrace();
            return false;

        }

        playerPodViewModel.setPlayerPod(pod);

        playbackRegulator.setPlayerPod(pod);

        seekTo(progress);

        // Store now playing pod as last played
        podStorageHandle.storePodAsLastPlayed(pod);

        return true;

    }


    /**
     *
     * {@code progress} defaults to {@link RRPod#getProgress()}
     *
     * @see #loadPod(RRPod, int)
     *
     */

    @Override
    public boolean loadPod(@NonNull final RRPod pod) {

        return loadPod(pod, pod.getProgress());

    }


    /**
     *
     * Starts {@link PodPlayer#mp} playback of given {@link RRPod}
     * Calls {@link #loadPod(RRPod, int)} if pod is not already loaded.
     *
     * @param pod:      the podcast episode to be played
     *
     */

    public void playPod(@NonNull final RRPod pod) {

        if (mp != null && this.pod == pod) {
            Log.i(TAG,"Pod was the same: " + this.pod + " vs. " + pod);
            //pauseOrContinuePod();
            return;
        }

        Log.i(TAG,"PlayPod");

        if (!loadPod(pod, pod.getProgress())) {
            Log.e(TAG, "Pod loading failed, could not start playback");
            return;
        }

        if (!playbackRegulator.requestAudioFocus()) {
            Log.e(TAG, "AudioFocus not granted, playback request can therefore not be fulfilled");
            return;
        }

        // Start playback
        mp.start();


        onPlayingStateChanged();

    }


    /**
     *
     * Toggles {@link PodPlayer#mp} playback playing state,
     * i.e. calls {@link #continuePod()} if not playing, or {@link PodPlayer#pausePod()} if playing.
     *
     *
     */

    @Override
    public void pauseOrContinuePod() {

        Log.i(TAG,"PauseOrContinuePod");

        if (!mp.isPlaying()) {

            continuePod();

            return;
        }

        pausePod();

    }


    /**
     *
     * Pauses {@link PodPlayer#mp} playback.
     *
     */

    @Override
    public void pausePod() {

        Log.i(TAG,"PausePod");

        if (mp == null) {
            throw new MediaPlayerStateException("MediaPlayer was null, cannot pause playback");
        }

        if (!mp.isPlaying()) {
            Log.i(TAG, "Not playing, no need to pause playback");
            return;
        }

        mp.pause();

        onPlayingStateChanged();

    }


    /**
     *
     * Continues {@link PodPlayer#mp} playback.
     *
     */

    @Override
    public void continuePod() {

        Log.i(TAG,"ContinuePod");

        if (pod == null) {
            throw new NullPointerException("Tried to continue playing when no pod was loaded");
        }

        if (mp == null) {
            Log.i(TAG, "MediaPlayer was null when continuing pod. Will try to fulfill request by starting playback from scratch");
            playPod(pod);
            return;
        }

        if (mp.isPlaying()) {
            Log.i(TAG, "Already playing, no need to continue playback");
            return;
        }

        if (!playbackRegulator.requestAudioFocus()) {
            Log.e(TAG, "AudioFocus not granted, playback request can therefore not be fulfilled");
            return;
        }

        mp.start();

        onPlayingStateChanged();

    }


    /**
     *
     * Skip/rewind {@link PodPlayer#mp} playback position with a given amount of time.
     * <p>
     * A jump will always be constrained, meaning it will never result in a negative playback position
     * or a playback position beyond the duration of the playback source.
     *
     * @param jump: amount of milliseconds to jump with,
     *              {@code jump > 0} will skip the playback forwards.
     *              {@code jump < 0} will rewind the playback backwards.
     *
     */

    @Override
    public void jump(int jump) {

        if (mp == null) {
            throw new MediaPlayerStateException("MediaPlayer was null, cannot jump playback");
        }

        switch (mp.getState()) {

            case IDLE:
            case INITIALIZED:
            case STOPPED:
            case ERROR:
                throw new MediaPlayerStateException("Cannot jump playback because of invalid State, was " + mp.getState());
        }

        int duration = mp.getDuration();

        if (duration == -1 && jump > 0) {

            Log.e(TAG, "Cannot skip playback forward because duration was not available");

            return;

        }

        seekTo(mp.getCurrentPosition() + jump);

    }


    /**
     *
     * Set{@link PodPlayer#mp} playback position.
     *
     * @param progress: desired playback position
     *
     */

    @Override
    public void seekTo(int progress) {

        if (mp == null) {

            throw new MediaPlayerStateException("MediaPlayer was null, could not seek to timestamp");

        }
        switch (mp.getState()) {

            case IDLE:
            case INITIALIZED:
            case STOPPED:
            case ERROR:
                throw new MediaPlayerStateException("Could not seek to timestamp because of invalid State, was " + mp.getState());
        }

        mp.seekTo(progress);

        Log.i(TAG, "Seeking to " + MillisFormatter.toFormat(progress, MillisFormatter.MillisFormat.HH_MM_SS));

        playerPodViewModel.postPlayerProgress(progress);

        pod.setProgress(progress);

        podsViewModel.storePod(pod);

    }


    private void onPlayingStateChanged() {

        playerPodViewModel.setIsPlaying(isPlaying());

        playbackRegulator.regulate(isPlaying());

    }



    @Override
    public void onCompletion(@NonNull MediaPlayer mp) {

        Log.i(TAG, "PodPlayer completed");

        playerPodViewModel.setIsPlaying(false);

        final ArrayList<RRPod> podList = podsViewModel.getPodList(pod.getPodType());

        Log.i(TAG,"Player completion listener");

        if(podList == null){
            throw new RuntimeException("Pod list associated with completed pod was null");
        }

        final int completedIndex = podList.indexOf(pod);

        switch (completedIndex){

            case -1:

                throw new RuntimeException("Completed pod index was not found");

            case 0:

                Log.i(TAG,"No pod found after completed pod, no further playback");

                pausePod();

                break;

            default:

                playPod(podList.get(completedIndex - 1));

                break;
        }

    }


    class MediaPlayerStateException extends RuntimeException {

        MediaPlayerStateException(String msg) {
            super(msg);
        }

    }


}
