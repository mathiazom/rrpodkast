package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.TimerTask;


/**
 *
 * Class to control playback of {@link RRPod} with {@link MediaPlayerWrapper}
 *
 */

class PodPlayer implements PodPlayerControls, MediaPlayer.OnCompletionListener, Serializable {


    private static final String TAG = "RRP-PodPlayer";


    private RRPod pod;

    private static MediaPlayerWrapper mp;


    private PlayerPodViewModel playerPodViewModel;

    private final PodStorageHandle podStorageHandle;


    private PodPlayerPlaybackRegulator playbackRegulator;


    @Nullable
    private OnPodPlayerCompletionListener onPodPlayerCompletionListener;

    interface OnPodPlayerCompletionListener {

        void onPodPlayerCompletion(@NonNull final RRPod completedPod, @NonNull final PodPlayer podPlayer);

    }


    PodPlayer(@NonNull FragmentActivity activity, @Nullable OnPodPlayerCompletionListener onPodPlayerCompletionListener) {

        this.podStorageHandle = new PodStorageHandle(activity);

        this.onPodPlayerCompletionListener = onPodPlayerCompletionListener;

        this.playerPodViewModel = ViewModelProviders.of(activity).get(PlayerPodViewModel.class);

        initPlaybackRegulator(activity);

    }


    private void initPlaybackRegulator(@NonNull Context context){

        final TaskIterator progressStoringIterator = new TaskIterator(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (pod != null) {
                            pod.setProgress(getProgress());
                            podStorageHandle.storePodProgress(pod);
                        }
                    }
                },
                PodStorageConstants.SAVE_PROGRESS_FREQ_MS
        );

        final TaskIterator progressNotificationIterator = new TaskIterator(
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
                        progressNotificationIterator,
                        progressStoringIterator
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

    private boolean loadPod(@NonNull final RRPod pod, int progress, @NonNull final Context context) {

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

        mp = new MediaPlayerWrapper(this);

        // Prevent system from killing the playback process
        mp.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        if (pod.isDownloaded()) {

            try {

                // Load pod to MediaPlayer
                mp.setDataSource(context, new PodStorageHandle(context).getPodUri(pod));

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

            Log.e(TAG, "ERROR preparing MediaPlayer: \n" + e.toString());
            e.printStackTrace();
            return false;

        }

        playerPodViewModel.setPlayerPod(pod);

        playbackRegulator.setPlayerPod(pod);

        seekTo(progress);

        // Store now playing pod as last played
        new PodStorageHandle(context).storePodAsLastPlayed(pod);

        return true;

    }


    /**
     *
     * {@code progress} defaults to {@link RRPod#getProgress()}
     *
     * @see PodPlayer#loadPod(RRPod, int, Context)
     *
     */

    @Override
    public boolean loadPod(@NonNull final RRPod pod, @NonNull final Context context) {

        return loadPod(pod, pod.getProgress(), context);

    }


    /**
     *
     * Starts {@link PodPlayer#mp} playback of given {@link RRPod}
     * Calls {@link PodPlayer#loadPod(RRPod, int, Context)} if pod is not already loaded.
     *
     * @param pod:      the podcast episode to be played
     * @param progress: timestamp of where to start the pod (in milliseconds)
     *
     */

    private void playPod(@NonNull final RRPod pod, int progress, @NonNull final Context context) {

        Log.i(TAG,"PlayPod");

        if (!loadPod(pod, progress, context)) {
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
     * {@code progress} defaults to {@link RRPod#getProgress()}
     *
     * @see PodPlayer#playPod(RRPod, int, Context)
     *
     */

    @Override
    public void playPod(@NonNull final RRPod pod, @NonNull final Context context) {

        if (mp != null && this.pod == pod) {
            pauseOrContinuePod(context);
            return;
        }

        playPod(pod, pod.getProgress(), context);

    }


    /**
     *
     * Toggles {@link PodPlayer#mp} playback playing state,
     * i.e. calls {@link PodPlayer#continuePod(Context)} if not playing, or {@link PodPlayer#pausePod()} if playing.
     *
     * @param context: used in {@link PodPlayer#continuePod(Context)} and {@link PodPlayer#pausePod()}
     *
     */

    @Override
    public void pauseOrContinuePod(@NonNull final Context context) {

        Log.i(TAG,"PauseOrContinuePod");

        if (!mp.isPlaying()) {

            continuePod(context);

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
     * @param context: used in {@link LocalBroadcastManager#getInstance(Context)} to send broadcast.
     *
     */

    @Override
    public void continuePod(@NonNull final Context context) {

        Log.i(TAG,"ContinuePod");

        if (pod == null) {
            throw new NullPointerException("Tried to continue playing when no pod was loaded");
        }

        if (mp == null) {
            Log.i(TAG, "MediaPlayer was null when continuing pod. Will try to fulfill request by starting playback from scratch");
            playPod(pod, pod.getProgress(), context);
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

        // Notify pod data change
        new PodProgressStoringTask(
                podStorageHandle,
                pod,
                this::getProgress,
                () -> playerPodViewModel.setPlayerPod(pod)
        ).run();

    }


    private void onPlayingStateChanged() {

        playerPodViewModel.setIsPlaying(isPlaying());

        playbackRegulator.regulate(isPlaying());

    }


    /**
     *
     * Calls {@link PodPlayer#onPodPlayerCompletionListener} when {@link PodPlayer#mp} calls {@link MediaPlayer.OnCompletionListener}
     *
     * @param mp: completed {@link MediaPlayer}
     *
     */

    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.i(TAG, "PodPlayer completed");

        if (onPodPlayerCompletionListener == null) {
            Log.i(TAG, "No given OnPodPlayerCompletionListener");
            return;
        }

        onPodPlayerCompletionListener.onPodPlayerCompletion(pod, this);

    }


    class MediaPlayerStateException extends RuntimeException {

        MediaPlayerStateException(String msg) {
            super(msg);
        }

    }


}
