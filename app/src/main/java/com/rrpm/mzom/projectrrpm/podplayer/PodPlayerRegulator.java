package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.pod.RRPod;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PodPlayerRegulator implements AudioManager.OnAudioFocusChangeListener {


    private static final String TAG = "RRP-PlaybackRegulator";


    private final Context context;

    private AudioManager audioManager;

    private ArrayList<TaskIterator> playbackIterators = new ArrayList<>();

    private RRPod playerPod;

    private final PodPlayerControls podPlayerControls;

    private boolean isPlaying;


    PodPlayerRegulator(@NonNull final Context context, @NonNull final PodPlayerControls podPlayerControls) {

        this.context = context;

        this.podPlayerControls = podPlayerControls;

    }

    void addPlaybackIterator(@NonNull final TaskIterator taskIterator){

        this.playbackIterators.add(taskIterator);

        if(isPlaying){

            taskIterator.start();

        }

    }


    void setPlayerPod(@NonNull final RRPod playerPod){
        this.playerPod = playerPod;
    }


    void regulate(final boolean isPlaying) {

        if(this.isPlaying == isPlaying){

            // No change in playing state, no further actions required.
            return;

        }

        this.isPlaying = isPlaying;

        if (isPlaying) {

            // Pause playback if output device change results in "becoming noisy"
            registerBecomingNoisyReceiver();

            if (!playbackIterators.isEmpty()) {

                // Start all playback iterators
                for (TaskIterator iterator : playbackIterators){

                    if(!iterator.isStarted()){

                        iterator.start();

                    }
                }

            }

            // Prevent system from killing the WiFi connection if streaming
            if (!playerPod.isDownloaded()) {

                activateWifiLock();

            }

        } else {

            // Playback will not "become noisy" when not playing, no need for this receiver
            unregisterBecomingNoisyReceiver();

            if (playbackIterators != null) {

                // Stop all playback iterators

                for (TaskIterator iterator : playbackIterators){

                    if(iterator.isStarted()){

                        iterator.stop();

                    }
                }

            }

            // Playback is not streaming, so system is free to kill the WiFi connection
            disableWifiLock();

        }


    }


    boolean requestAudioFocus() {

        if (audioManager == null) {

            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager == null) {

                throw new RuntimeException("AudioManager was null");

            }

        }

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //Log.i(TAG, "AudioFocusRequest result: " + String.valueOf(result));

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

    }

    private boolean transientAudioFocusLoss = false;

    @Override
    public void onAudioFocusChange(int focusChange) {

        //Log.i(TAG, "Focus change to: " + String.valueOf(focusChange));

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:

                if (transientAudioFocusLoss && !isPlaying) {

                    podPlayerControls.continuePod();

                }

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                transientAudioFocusLoss = true;
                podPlayerControls.pausePod();

                break;

            case AudioManager.AUDIOFOCUS_LOSS:

                transientAudioFocusLoss = false;
                podPlayerControls.pausePod();

                break;


        }

    }


    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action == null) {

                return;

            }

            if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

                podPlayerControls.pausePod();

            }
        }
    };


    /**
     * Registers a {@link BroadcastReceiver} to catch any {@link AudioManager#ACTION_AUDIO_BECOMING_NOISY} broadcasts.
     *
     * @see PodPlayerRegulator#becomingNoisyReceiver
     */

    private void registerBecomingNoisyReceiver() {

        context.registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        );

    }

    private void unregisterBecomingNoisyReceiver() {

        try{

            context.unregisterReceiver(becomingNoisyReceiver);

        }catch (IllegalArgumentException e){

            Log.e(TAG,"Unregistering failed: " + e);

        }

    }


    private static final String POD_PLAYER_WIFI_LOCK_TAG = "com.rrpm.mzom.projectrrpm.PodPlayer.PodPlayer.POD_PLAYER_WIFI_LOCK_TAG";

    private WifiManager.WifiLock wifiLock;

    private void activateWifiLock() {

        if (wifiLock == null) {

            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager == null) {

                throw new NullPointerException("WifiManager was null");

            }

            // Prevent system from disabling wifi when device is idle
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, POD_PLAYER_WIFI_LOCK_TAG);

        }

        wifiLock.acquire();

    }

    private void disableWifiLock() {

        if (wifiLock == null || !wifiLock.isHeld()) {

            return;

        }

        wifiLock.release();

    }


}
