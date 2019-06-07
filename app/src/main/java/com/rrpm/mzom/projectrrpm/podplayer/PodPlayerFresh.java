package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;


/**
 *
 * Class to control playback of {@link RRPod} with {@link MediaPlayerWrapper}
 *
 */

public class PodPlayerFresh implements PodPlayerControls, MediaPlayer.OnCompletionListener, Serializable {


    private static final String TAG = "RRP-PodPlayer";


    private RRPod pod;

    @NonNull
    private ExoPlayer exoPlayer;

    private ExtractorMediaSource.Factory mediaSourceFactory;


    private final PodsViewModel podsViewModel;

    private final PlayerPodViewModel playerPodViewModel;

    private final PodStorageHandle podStorageHandle;


    private PodPlayerPlaybackRegulator playbackRegulator;


    public PodPlayerFresh(@NonNull FragmentActivity activity) {

        // TODO: Release player when appropriate
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(activity);

        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(activity, Util.getUserAgent(activity, ((Context) activity).getApplicationInfo().name));

        mediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);

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
     * Gets the current {@link PodPlayerFresh#exoPlayer} playback position.
     *
     * @return the current progress in milliseconds.
     *
     */

    private int getProgress() {

        /*if (mp == null) {
            throw new MediaPlayerStateException("Cannot get progress, MediaPlayer was null");
        }

        switch (mp.getState()) {

            case IDLE:
            case ERROR:
                throw new MediaPlayerStateException("Cannot get progress because of invalid State, was " + mp.getState());
        }

        return mp.getCurrentPosition();*/

        return (int) exoPlayer.getCurrentPosition();

    }


    /**
     *
     * @return {@link PodPlayerFresh#exoPlayer#isPlaying()},
     * or {@code false} if {@link PodPlayerFresh#exoPlayer} has incorrect state
     *
     */

    /*private boolean isPlaying() {

        *//*return mp != null
                && mp.getState() != MediaPlayerWrapper.State.ERROR
                && mp.isPlaying();*//*

        return exoPlayer.getPlaybackState() == Player.STATE_READY && exoPlayer.getPlayWhenReady();

    }*/

    private boolean isPlaying() {
        return exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE
                && exoPlayer.getPlayWhenReady();
    }


    /**
     *
     * Prepares {@link PodPlayerFresh#exoPlayer} state and data source for playback of a given pod (without starting the playback).
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

        Uri podUri;

        if(pod.isDownloaded()){

            podUri = podStorageHandle.getPodUri(pod);

        }else{

            podUri = Uri.parse(pod.getUrl());

        }

        exoPlayer.prepare(mediaSourceFactory.createMediaSource(podUri));

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
     * Starts {@link PodPlayerFresh#exoPlayer} playback of given {@link RRPod}
     * Calls {@link #loadPod(RRPod, int)} if pod is not already loaded.
     *
     * @param pod:      the podcast episode to be played
     *
     */

    @Override
    public void playPod(@NonNull final RRPod pod) {

        if (this.pod == pod) {
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
        exoPlayer.setPlayWhenReady(true);

        onPlayingStateChanged();

    }


    /**
     *
     * Toggles {@link PodPlayerFresh#exoPlayer} playback playing state,
     * i.e. calls {@link #continuePod()} if not playing, or {@link PodPlayerFresh#pausePod()} if playing.
     *
     *
     */

    @Override
    public void pauseOrContinuePod() {

        Log.i(TAG,"PauseOrContinuePod");

        if (isPlaying()) {

            pausePod();

            return;
        }

        continuePod();

    }


    /**
     *
     * Pauses {@link PodPlayerFresh#exoPlayer} playback.
     *
     */

    @Override
    public void pausePod() {

        Log.i(TAG,"PausePod");

        if (!isPlaying()) {
            Log.i(TAG, "Not playing, no need to pause playback");
            return;
        }

        exoPlayer.setPlayWhenReady(false);

        onPlayingStateChanged();

    }


    /**
     *
     * Continues {@link PodPlayerFresh#exoPlayer} playback.
     *
     */

    @Override
    public void continuePod() {

        Log.i(TAG,"ContinuePod");

        if (pod == null) {
            throw new NullPointerException("Tried to continue playing when no pod was loaded");
        }

        if (isPlaying()) {
            Log.i(TAG, "Already playing, no need to continue playback");
            return;
        }

        if (!playbackRegulator.requestAudioFocus()) {
            Log.e(TAG, "AudioFocus not granted, playback request can therefore not be fulfilled");
            return;
        }

        exoPlayer.setPlayWhenReady(true);

        onPlayingStateChanged();

    }


    /**
     *
     * Skip/rewind {@link PodPlayerFresh#exoPlayer} playback position with a given amount of time.
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

        /*switch (mp.getState()) {

            case IDLE:
            case INITIALIZED:
            case STOPPED:
            case ERROR:
                throw new MediaPlayerStateException("Cannot jump playback because of invalid State, was " + mp.getState());
        }*/

        int duration = (int) exoPlayer.getDuration();

        if (duration == -1 && jump > 0) {

            Log.e(TAG, "Cannot skip playback forward because duration was not available");

            return;

        }

        final int progress = (int) exoPlayer.getCurrentPosition();

        seekTo(progress + jump);

    }


    /**
     *
     * Set{@link PodPlayerFresh#exoPlayer} playback position.
     *
     * @param progress: desired playback position
     *
     */

    @Override
    public void seekTo(int progress) {

        /*switch (mp.getState()) {

            case IDLE:
            case INITIALIZED:
            case STOPPED:
            case ERROR:
                throw new MediaPlayerStateException("Could not seek to timestamp because of invalid State, was " + mp.getState());
        }
*/
        exoPlayer.seekTo(progress);

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

}
