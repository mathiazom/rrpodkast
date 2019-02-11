package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;


/**
 *
 *  Class to control playback of podcast episodes
 *
 */

class PodPlayer {

    private static final String TAG = "RRP-PodPlayer";

    private final Context context;

    // Currently loaded podcast episode
    private RRPod pod;


    private static MediaPlayerWrapper mp;

    private PodPlayerListener listener;

    interface PodPlayerListener{

        void onCurrentPositionChanged(int position, @NonNull final RRPod pod);

        void onPodStarted(@NonNull final RRPod pod, int from);

        void onPlayerPaused();

        void onPlayerContinued();

    }


    PodPlayer(Context context, PodPlayerListener listener){
        this.context = context;
        this.listener = listener;
    }


    private void setPod(@NonNull final RRPod pod){
        this.pod = pod;
    }

    @Nullable
    RRPod getPod(){
        return this.pod;
    }

    int getCurrentPosition(){

        if(mp == null){

            Log.e(TAG,"Cannot get progress, MediaPlayer was null");

        }

        switch (mp.getState()){

            case Idle:
            case Error:
                Log.e(TAG,"Cannot get progress because of invalid MediaPlayerState, was " + mp.getState());
                return -1;

        }

        return mp.getCurrentPosition();

    }

    int getDuration(){

        if(mp == null){

            Log.e(TAG,"Cannot get duration, MediaPlayer was null");
            return -1;

        }

        switch (mp.getState()){
            case Idle:
            case Initialized:
            case Error:
                Log.e(TAG,"Cannot get duration because of invalid MediaPlayerState, was " + mp.getState());
                return -1;
        }

        return mp.getDuration();
    }

    boolean isPlaying() {

        return mp != null
                && mp.getState() != MediaPlayerWrapper.MediaPlayerState.Error
                && mp.isPlaying();

    }


    /**
     * Starts playback of podcast episode
     *
     * @param pod: the podcast episode to be played
     * @param from: timestamp of where to start the pod from (in milliseconds)
     */

    void playPod(@NonNull final RRPod pod, int from){

        setPod(pod);

        if(mp != null){

            // Free resources before creating new MediaPlayer
            mp.release();
        }

        mp = new MediaPlayerWrapper();

        if(pod.getDownloadState()){

            try{

                // Directory of downloaded pods
                final File dir = new File(context.getFilesDir(),"RR-Podkaster");

                // Path to given pod
                final Uri podUri = Uri.fromFile(new File(dir + File.separator + pod.getTitle()));

                // Load pod to MediaPlayer
                mp.setDataSource(context,podUri);

            }catch (IOException e){

                Log.e(TAG,"Error setting MediaPlayer data source (downloaded pod): \n" + e.toString());
                e.printStackTrace();

            }

        }else{

            try{

                // Load pod to MediaPlayer
                mp.setDataSource(pod.getUrl());

            }catch (IOException e){

                Log.e(TAG,"Error setting MediaPlayer data source, streaming URL was " + pod.getUrl() + "\n" + e.toString());
                e.printStackTrace();

            }

        }

        try {

            mp.prepare();

        } catch (IOException e) {

            Log.e(TAG,"Error preparing MediaPlayer: \n" + e.toString());
            e.printStackTrace();

        }

        // Prepare to start from given timestamp
        mp.seekTo(from);

        // Start playback
        mp.start();

        listener.onPodStarted(pod,from);

    }

    void playPod(@NonNull final RRPod pod){

        playPod(pod,0);

    }


    /**
     *
     * Skip/rewind current playblack position with a given amount of time.
     *
     *  A jump will always be constrained, meaning it will never result in a negative playback position
     *  or a playback position beyond the duration of the playback source.
     *
     * @param jump: amount of milliseconds to jump with,
     *            positive values will skip the playback forwards.
     *            negative values will rewind the playback backwards.
     *
     */

    void jump(int jump){

        if(mp == null){

            Log.e(TAG,"MediaPlayer was null, cannot jump playback");

            return;

        }

        switch (mp.getState()){

            case Idle:
            case Initialized:
            case Stopped:
            case Error:
                Log.e(TAG,"Cannot jump playback because of invalid MediaPlayerState, was " + mp.getState());
                return;

        }

        int duration = mp.getDuration();

        if(duration == -1 && jump > 0){

            Log.e(TAG,"Cannot skip playback forward because duration was not available");

            return;

        }

        seekTo(mp.getCurrentPosition() + jump);

    }

    void seekTo(int timestamp){

        if(mp == null){

            Log.e(TAG,"MediaPlayer was null, could not seek to timestamp");

            return;

        }
        switch (mp.getState()){

            case Idle:
            case Initialized:
            case Stopped:
            case Error:
                Log.e(TAG,"Could not seek to timestamp because of invalid MediaPlayerState, was " + mp.getState());
                return;

        }

        mp.seekTo(timestamp);

        listener.onCurrentPositionChanged(timestamp,pod);

    }


    void pauseOrContinuePod(){

        if(mp == null){

            Log.e(TAG,"MediaPlayer was null, cannot pause/continue playback");

            return;
        }

        if(!mp.isPlaying()){

            continuePod();

            return;
        }

        pausePod();

    }

    private void pausePod(){

        mp.pause();

        listener.onPlayerPaused();

    }

    private void continuePod(){

        if(mp.isPlaying()){

            Log.e(TAG,"Will not pause if pod is playing");

            return;
        }

        if(pod == null){

            Log.e(TAG,"Tried to continue playing when no pod was loaded");

            return;

        }

        if(mp == null){

            Log.e(TAG,"Tried to continue playing when MediaPlayer was null");

            return;

        }

        mp.pause();

        listener.onPlayerContinued();

    }




}
