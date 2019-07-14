package com.rrpm.mzom.projectrrpm.podplayer;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.exoplayer2.Player;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.notifications.PodPlayerNotificationBuilder;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;

import java.util.List;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

public class PodPlayerService extends MediaBrowserServiceCompat implements PodPlayerControls {

    private static final String TAG = "RRP-PodPlayerService";


    private PodPlayer podPlayer;

    private MediaSessionCompat mediaSession;

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

    private final MediaMetadataCompat.Builder mediaMetadataBuilder = new MediaMetadataCompat.Builder();

    private PodPlayerNotificationBuilder podPlayerNotificationBuilder;

    private NotificationManagerCompat notificationManager;

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver resultReceiver) {
            super.onCommand(command, extras, resultReceiver);

            switch (command) {

                case PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_LOAD_POD:

                    final RRPod podToBeLoaded = extras.getParcelable(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_POD_TO_BE_LOADED);

                    Assertions._assert(podToBeLoaded != null, "Pod to be loaded was null");

                    loadPod(podToBeLoaded);

                    if (extras.getBoolean(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_PLAY_WHEN_LOADED)) {

                        continuePod();

                    }

                    break;

                case PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_JUMP:

                    jump(extras.getInt(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_JUMP_AMOUNT));

                    break;

                case PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REQUEST_PROGRESS:

                    Assertions._assert(resultReceiver != null, "Requested progress, but no result receiver was given");

                    final Bundle bundle = new Bundle();
                    bundle.putInt(
                            PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REQUEST_PROGRESS_RESULT_KEY,
                            podPlayer.getProgress()
                    );

                    resultReceiver.send(0, bundle);

                    break;

                case PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REGISTER_ITERATOR:

                    Assertions._assert(resultReceiver != null, "Requested to register iterator, but no result receiver was provided");

                    final long period = extras.getLong(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_ITERATOR_PERIOD);

                    podPlayer.addPlaybackIterator(new TaskIterator(new TimerTask() {
                        @Override
                        public void run() {

                            resultReceiver.send(0, null);

                        }
                    }, period));

                    break;

                case PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REGISTER_COMPLETION_LISTENER:

                    Assertions._assert(resultReceiver != null, "Requested to register completion listener, but no result receiver was provided");

                    podPlayer.setOnCompletionListener(() -> resultReceiver.send(0, null));

                    break;

                default:

                    Assertions._assert(false, "Unknown media session command: " + command);

                    break;


            }

        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {

            Log.i(TAG, "Media button event: " + mediaButtonEvent);

            final String intentAction = mediaButtonEvent.getAction();

            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {

                return super.onMediaButtonEvent(mediaButtonEvent);

            }

            final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null) {

                return super.onMediaButtonEvent(mediaButtonEvent);

            }

            Log.i(TAG,"Keycode: " + String.valueOf(event.getKeyCode()));

            switch (event.getKeyCode()) {

                case KeyEvent.KEYCODE_MEDIA_REWIND:

                    jump(PodPlayerConstants.PLAYER_REWIND_MS);

                    return true;

                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:

                    jump(PodPlayerConstants.PLAYER_SKIP_MS);

                    return true;

                case KeyEvent.KEYCODE_MEDIA_PLAY:

                    continuePod();

                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:

                    pausePod();

                    break;

            }

            return super.onMediaButtonEvent(mediaButtonEvent);

        }

        @Override
        public void onPlay() {
            super.onPlay();

            Log.i(TAG, "Media controller onPlay");

            continuePod();

        }

        @Override
        public void onPause() {
            super.onPause();

            Log.i(TAG, "Media controller onPause");

            pausePod();
        }

        @Override
        public void onFastForward() {
            super.onFastForward();

            Log.i(TAG, "Media controller onFastForward");

            jump(PodPlayerConstants.PLAYER_SKIP_MS);
        }

        @Override
        public void onRewind() {
            super.onRewind();

            Log.i(TAG, "Media controller onRewind");

            jump(PodPlayerConstants.PLAYER_REWIND_MS);
        }

        @Override
        public void onStop() {
            super.onStop();

            Log.i(TAG, "Media controller onStop");

            pausePod();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);

            Log.i(TAG, "Media controller onSeekTo");

            seekTo((int) pos);
        }


    };


    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(TAG, "Pod player service onCreate");

        podPlayer = new PodPlayer(getApplicationContext());

        initMediaSession();

    }

    private void initMediaSession() {

        mediaSession = new MediaSessionCompat(this, PodPlayerConstants.POD_PLAYER_MEDIA_SESSION_TAG);

        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        mediaSession.setCallback(mediaSessionCallback);

        mediaSession.setActive(true);

        stateBuilder.setActions(

                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_REWIND |
                PlaybackStateCompat.ACTION_FAST_FORWARD

        );

        mediaSession.setPlaybackState(stateBuilder.build());

        podPlayer.addPlayerEventListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                updateMetadataAndState();

            }
        });

        podPlayer.addPlaybackIterator(new TaskIterator(new TimerTask() {
            @Override
            public void run() {

                updateMetadataAndState();

            }
        },PodStorageConstants.PROGRESS_REFRESH_FREQ_MS));


        setSessionToken(mediaSession.getSessionToken());


    }


    private void updateMetadataAndState() {

        mediaSession.setMetadata(
                mediaMetadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, podPlayer.getPod().getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                                MillisFormatter.toFormat(podPlayer.getDuration() - podPlayer.getProgress(), MillisFormatter.MillisFormat.MIN_TEXT) +
                                " " +
                                getString(R.string.podplayer_notification_subtitle_minutes_left)
                        )
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "Spiller episode")
                        .build()
        );


        final boolean isPlaying = podPlayer.isPlaying();

        stateBuilder.setState(
                isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                podPlayer.getProgress(),
                1
        );

        stateBuilder.setActions(isPlaying ?

                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_REWIND |
                PlaybackStateCompat.ACTION_FAST_FORWARD
                :
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_REWIND |
                PlaybackStateCompat.ACTION_FAST_FORWARD

        );

        mediaSession.setPlaybackState(stateBuilder.build());


        if(podPlayerNotificationBuilder == null){

            podPlayerNotificationBuilder = new PodPlayerNotificationBuilder(getApplicationContext(), mediaSession.getSessionToken());

        }

        final Notification notification = podPlayerNotificationBuilder.buildNotification();

        if(notification == null){

            Log.e(TAG,"Notification could not be created");

            return;

        }

        if(notificationManager == null){

            notificationManager = NotificationManagerCompat.from(getApplicationContext());

        }

        notificationManager.notify(NotificationConstants.PLAYER_NOTIFICATION_ID, notification);

        if (isPlaying) {

            ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), PodPlayerService.class));

            startForeground(NotificationConstants.PLAYER_NOTIFICATION_ID, notification);

        } else {

            stopForeground(false);

            stopSelf();

        }


    }


    @Override
    public void loadPod(@NonNull RRPod pod) {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.loadPod(pod);

    }

    @Override
    public void playPod(@NonNull RRPod pod) {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.playPod(pod);

    }

    @Override
    public void pauseOrContinuePod() {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.pauseOrContinuePod();

    }

    @Override
    public void pausePod() {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.pausePod();

    }

    @Override
    public void continuePod() {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.continuePod();

    }

    @Override
    public void jump(int jump) {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.jump(jump);

    }

    @Override
    public void seekTo(int progress) {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.seekTo(progress);

    }

    @Override
    public boolean onUnbind(@NonNull Intent intent) {

        Assertions._assert(podPlayer != null, "Pod player has not been set");

        podPlayer.pausePod();

        return false;
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

        // Return empty browser root to allow connection, but not browsing
        // TODO: Allow media browsing (next/previous track etc.)
        return new BrowserRoot(PodPlayerConstants.POD_PLAYER_EMPTY_BROWSER_ROOT_ID, null);

    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        //  Browsing not allowed
        if (TextUtils.equals(PodPlayerConstants.POD_PLAYER_EMPTY_BROWSER_ROOT_ID, parentId)) {

            result.sendResult(null);

            return;

        }

        Assertions._assert(false, "Unknown parent id");

    }


}
