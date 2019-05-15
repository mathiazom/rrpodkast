package com.rrpm.mzom.projectrrpm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PodPlayerPlaybackRegulator implements AudioManager.OnAudioFocusChangeListener {


    private static final String TAG = "RRP-PlaybackRegulator";


    private Context context;

    private AudioManager audioManager;

    private TaskIterator[] playbackIterators;

    private RRPod playerPod;

    private PodPlayerControls podPlayerControls;

    private boolean isPlaying;


    PodPlayerPlaybackRegulator(@NonNull final Context context, @NonNull final PodPlayerControls podPlayerControls, @Nullable final TaskIterator[] playbackIterators) {

        this.context = context;

        this.podPlayerControls = podPlayerControls;

        this.playbackIterators = playbackIterators;

    }


    void setPlayerPod(@NonNull final RRPod playerPod){
        this.playerPod = playerPod;
    }


    void regulate(final boolean isPlaying) {

        this.isPlaying = isPlaying;

        if (isPlaying) {

            // Pause playback if output device change results in "becoming noisy"
            registerBecomingNoisyReceiver();

            for (TaskIterator iterator : playbackIterators){
                iterator.start();
            }

            // Prevent system from killing the WiFi connection if streaming
            if (!playerPod.isDownloaded()) {
                activateWifiLock();
            }

            // Nothing more to play, so pause playback
            if (playerPod.getProgress() == playerPod.getDuration()) {
                podPlayerControls.pausePod();
            }

        } else {

            // Playback will not "become noisy" when not playing, no need for this receiver
            unregisterBecomingNoisyReceiver();

            for (TaskIterator iterator : playbackIterators){
                iterator.stop();
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

        Log.i(TAG, "AudioFocusRequest result: " + String.valueOf(result));

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

    }

    private boolean transientAudioFocusLoss = false;

    @Override
    public void onAudioFocusChange(int focusChange) {

        Log.i(TAG, "Focus change to: " + String.valueOf(focusChange));

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:

                if (transientAudioFocusLoss && !isPlaying) {
                    podPlayerControls.continuePod(context);
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

    private boolean registeredBecomingNoisyReceiver;


    /**
     * Registers a {@link BroadcastReceiver} to catch any {@link AudioManager#ACTION_AUDIO_BECOMING_NOISY} broadcasts.
     *
     * @see PodPlayerPlaybackRegulator#becomingNoisyReceiver
     */

    private void registerBecomingNoisyReceiver() {

        if (!registeredBecomingNoisyReceiver) {
            context.registerReceiver(
                    becomingNoisyReceiver,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            );
            registeredBecomingNoisyReceiver = true;
        }

    }

    private void unregisterBecomingNoisyReceiver() {

        if (!registeredBecomingNoisyReceiver) {
            context.unregisterReceiver(becomingNoisyReceiver);
            registeredBecomingNoisyReceiver = false;
        }

    }


    private static final String POD_PLAYER_WIFI_LOCK_TAG = "com.rrpm.mzom.projectrrpm.PodPlayer.POD_PLAYER_WIFI_LOCK_TAG";

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
