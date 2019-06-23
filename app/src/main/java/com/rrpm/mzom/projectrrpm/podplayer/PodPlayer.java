package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.utils.MathUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;


/**
 *
 * Class to control playback of {@link RRPod} with {@link ExoPlayer}
 *
 */

public class PodPlayer implements PodPlayerControls, Serializable {


    private static final String TAG = "RRP-PodPlayer";


    private RRPod pod;

    @NonNull
    private ExoPlayer exoPlayer;

    private ExtractorMediaSource.Factory mediaSourceFactory;


    private final PodsViewModel podsViewModel;

    private final PlayerPodViewModel playerPodViewModel;

    private final PodStorageHandle podStorageHandle;


    private PodPlayerRegulator playbackRegulator;


    public PodPlayer(@NonNull FragmentActivity activity) {

        // TODO: Release player when appropriate
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(activity);

        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                onPlayingStateChanged();

                switch (playbackState){

                    case Player.STATE_READY:

                        if(playerPodViewModel.getPlayerDurationObservable().getValue() == null){

                            playerPodViewModel.setPlayerDuration(getDuration());

                        }

                        break;

                    case Player.STATE_BUFFERING:

                        // TODO: Display buffering in UI

                        break;

                    case Player.STATE_ENDED:

                        onCompletion();

                        break;

                }

            }
        });

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

                            podsViewModel.updatePodInStorage(pod,true);

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

        this.playbackRegulator = new PodPlayerRegulator(
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
     * Gets the current {@link PodPlayer#exoPlayer} playback position.
     *
     * @return the current progress in milliseconds.
     *
     */

    private int getProgress() {

        return (int) exoPlayer.getCurrentPosition();

    }


    /**
     *
     * Gets the {@link PodPlayer#exoPlayer} total duration.
     *
     * @return the duration in milliseconds.
     *
     */

    private int getDuration(){

        if(exoPlayer.getDuration() == C.TIME_UNSET){

            // Duration is unknown, fall back to less precise pod duration
            return pod.getDuration();

        }

        return (int) exoPlayer.getDuration();

    }


    /**
     *
     * @return {@link PodPlayer#exoPlayer#isPlaying()},
     * or {@code false} if {@link PodPlayer#exoPlayer} has incorrect state
     *
     */

    private boolean isPlaying() {
        return exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE
                && exoPlayer.getPlayWhenReady();
    }


    /**
     *
     * Prepares {@link PodPlayer#exoPlayer} state and data source for playback of a given pod (without starting the playback).
     *
     * @param pod:      the podcast episode to be played
     * @param progress: timestamp of where to start the pod from (in milliseconds)
     *
     */

    private boolean loadPod(@NonNull final RRPod pod, int progress) {

        //Log.i(TAG,"LoadPod");

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

        final MediaSource podSource = mediaSourceFactory.createMediaSource(podUri);

        exoPlayer.prepare(podSource);

        // Extract a more precise pod duration
        pod.setDuration(getDuration());

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
     * Starts {@link PodPlayer#exoPlayer} playback of given {@link RRPod}
     * Calls {@link #loadPod(RRPod, int)} if pod is not already loaded.
     *
     * @param pod:      the podcast episode to be played
     *
     */

    @Override
    public void playPod(@NonNull final RRPod pod) {

        if (this.pod == pod) {

            Log.i(TAG,"Pod was the same: " + this.pod + " vs. " + pod);

            return;

        }

        //Log.i(TAG,"PlayPod");

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

    }


    /**
     *
     * Toggles {@link PodPlayer#exoPlayer} playback playing state,
     * i.e. calls {@link #continuePod()} if not playing, or {@link PodPlayer#pausePod()} if playing.
     *
     *
     */

    @Override
    public void pauseOrContinuePod() {

        //Log.i(TAG,"PauseOrContinuePod");

        if (isPlaying()) {

            pausePod();

            return;
        }

        continuePod();

    }


    /**
     *
     * Pauses {@link PodPlayer#exoPlayer} playback.
     *
     */

    @Override
    public void pausePod() {

        //Log.i(TAG,"PausePod");

        if (!isPlaying()) {

            Log.i(TAG, "Not playing, no need to pause playback");

            return;

        }

        exoPlayer.setPlayWhenReady(false);

    }


    /**
     *
     * Continues {@link PodPlayer#exoPlayer} playback.
     *
     */

    @Override
    public void continuePod() {

        //Log.i(TAG,"ContinuePod");

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

    }


    /**
     *
     * Skip/rewind {@link PodPlayer#exoPlayer} playback position with a given amount of time.
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

        Assertions._assert(getDuration() >= 0, "Invalid playback duration");

        final int progress = (int) exoPlayer.getCurrentPosition();

        // Constrain the jump to the pod time frame
        final int jumpedProgress = MathUtils.constrainPositive(progress + jump, getDuration());

        seekTo(jumpedProgress);

    }




    /**
     *
     * Set {@link PodPlayer#exoPlayer} playback position.
     *
     * @param progress: desired playback position, will be constrained according to the playback duration
     *
     */

    @Override
    public void seekTo(int progress) {

        Assertions._assert(getDuration() >= 0, "Invalid playback duration");

        final int constrainedProgress = MathUtils.constrainPositive(progress,getDuration());

        exoPlayer.seekTo(constrainedProgress);

        //Log.i(TAG, "Seeking to " + MillisFormatter.toFormat(constrainedProgress, MillisFormatter.MillisFormat.HH_MM_SS));

        playerPodViewModel.postPlayerProgress(constrainedProgress);

        pod.setProgress(constrainedProgress);

        podsViewModel.updatePodInStorage(pod);

    }


    private void onPlayingStateChanged() {

        playerPodViewModel.setIsPlaying(isPlaying());

        playbackRegulator.regulate(isPlaying());

    }


    private void onCompletion() {

        Log.i(TAG, "PodPlayer completed");

        final ArrayList<RRPod> podList = podsViewModel.getPodList(pod.getPodType());

        Assertions._assert(podList != null, "Pod list associated with completed pod was null");

        final int completedIndex = podList.indexOf(pod);

        Assertions._assert(completedIndex != -1, "Completed pod index was not found");

        if(completedIndex == 0){

            Log.i(TAG,"No pod found after completed pod, no further playback");

            pausePod();

            return;

        }

        playPod(podList.get(completedIndex - 1));

    }

}
