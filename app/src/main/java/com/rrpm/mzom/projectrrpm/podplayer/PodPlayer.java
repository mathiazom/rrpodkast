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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.utils.MathUtils;

import java.io.Serializable;
import java.util.TimerTask;

import androidx.annotation.NonNull;


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

    @NonNull private ExtractorMediaSource.Factory mediaSourceFactory;

    @NonNull private PodStorageHandle podStorageHandle;


    @NonNull private PodPlayerRegulator playbackRegulator;


    private OnCompletionListener onCompletionListener;

    interface OnCompletionListener{

        void onCompletion();

    }


    PodPlayer(@NonNull final Context context) {


        // TODO: Release player when appropriate
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context);

        this.exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                onPlayingStateChanged(isPlaying());

                switch (playbackState){

                    case Player.STATE_READY:

                        /*if(playerPodViewModel.getPlayerDurationObservable().getValue() == null){

                            playerPodViewModel.setPlayerDuration(getDuration());

                        }*/

                        break;

                    case Player.STATE_BUFFERING:

                        // TODO: Display buffering in UI

                        Log.i(TAG,"Buffering");

                        break;

                    case Player.STATE_ENDED:

                        //onCompletion();

                        if(onCompletionListener != null){

                            onCompletionListener.onCompletion();

                        }

                        break;

                }

            }
        });

        this.mediaSourceFactory = new ExtractorMediaSource.Factory(

                new DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(
                                context,
                                (context).getApplicationInfo().name
                        )
                )

        );


        this.podStorageHandle = new PodStorageHandle(context);

        this.playbackRegulator = new PodPlayerRegulator(context, this);

    }


    public void addPlayerEventListener(@NonNull final Player.EventListener eventListener){

        this.exoPlayer.addListener(eventListener);


    }

    public void addPlaybackIterator(@NonNull final TaskIterator playbackIterator){

        playbackRegulator.addPlaybackIterator(playbackIterator);

    }


    public RRPod getPod(){

        return this.pod;

    }


    /**
     *
     * Gets the current {@link PodPlayer#exoPlayer} playback position.
     *
     * @return the current progress in milliseconds.
     *
     */

    int getProgress() {

        return (int) exoPlayer.getCurrentPosition();

    }


    /**
     *
     * Gets the {@link PodPlayer#exoPlayer} total duration.
     *
     * @return the duration in milliseconds.
     *
     */

    int getDuration(){

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

    boolean isPlaying() {
        return exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE
                && exoPlayer.getPlayWhenReady();
    }


    boolean isLoaded(){

        return pod != null;

    }


    /**
     *
     * Prepares {@link PodPlayer#exoPlayer} state and data source for playback of a given pod (without starting the playback).
     *
     * @param pod:      the podcast episode to be played
     * @param progress: timestamp of where to start the pod from (in milliseconds)
     *
     */

    private void loadPod(@NonNull final RRPod pod, int progress) {

        //Log.i(TAG,"LoadPod");

        if (this.pod == pod) {

            // Pod already loaded, no further actions required
            return;

        }

        if (isLoaded() && isPlaying()) {

            // New pod requested for playback, stopping current pod playback
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

        playbackRegulator.setPlayerPod(pod);

        seekTo(progress);

        // Store now playing pod as last played
        podStorageHandle.storePodAsLastPlayed(pod);

    }


    /**
     *
     * {@code progress} defaults to {@link RRPod#getProgress()}
     *
     * @see #loadPod(RRPod, int)
     *
     */

    @Override
    public void loadPod(@NonNull final RRPod pod) {

        loadPod(pod, pod.getProgress());

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

        //Log.i(TAG,"PlayPod");

        if (this.pod == pod) {

            // Pod is the same, no further actions required.
            return;

        }

        loadPod(pod, pod.getProgress());

        if (!playbackRegulator.requestAudioFocus()) {

            Log.e(TAG, "AudioFocus not granted, playback request can therefore not be fulfilled");

            return;

        }

        // Start playback
        continuePod();

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

        if(!isLoaded()){

            return;

        }

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

        Assertions._assert(isLoaded(),"Tried to pause playing when no pod was loaded");

        if (!isPlaying()) {

            Log.e(TAG, "Not playing, no need to pause playback");

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

        Assertions._assert(isLoaded(),"Tried to continue playing when no pod was loaded");

        if (isPlaying()) {

            Log.e(TAG, "Already playing, no need to continue playback");

            return;

        }

        if (!playbackRegulator.requestAudioFocus()) {

            Log.e(TAG, "AudioFocus not granted, playback could therefore not be continued");

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

        //playerPodViewModel.postPlayerProgress(constrainedProgress);

        pod.setProgress(constrainedProgress);

        //podsViewModel.updatePodInStorage(pod);

    }


    private void onPlayingStateChanged(final boolean isPlaying) {

        //playerPodViewModel.setIsPlaying(isPlaying);

        playbackRegulator.regulate(isPlaying);

    }


    void regulate(){

        playbackRegulator.regulate(isPlaying());

    }


    public void setOnCompletionListener(@NonNull OnCompletionListener onCompletionListener){

        this.onCompletionListener = onCompletionListener;

    }

    private void onCompletion() {

        // TODO: Let caller handle playback of next pod after completion

        /*Log.i(TAG, "PodPlayer playback completed");

        final ArrayList<RRPod> podList = podsViewModel.getPodList(pod.getPodType());

        Assertions._assert(podList != null, "Pod list associated with completed pod was null");

        final int completedIndex = podList.indexOf(pod);

        Assertions._assert(completedIndex != -1, "Completed pod index was not found");

        if(completedIndex == 0){

            // No pod found after completed pod, this is the end of the pod list.

            pausePod();

            return;

        }

        playPod(podList.get(completedIndex - 1));*/

    }

}
