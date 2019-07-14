package com.rrpm.mzom.projectrrpm.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.activities.MainActivity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

public class PodPlayerNotificationBuilder {


    private Context context;

    private NotificationCompat.Action jumpBack;
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action jumpForth;

    private NotificationCompat.Builder notificationBuilder;

    private MediaSessionCompat.Token sessionToken;


    public PodPlayerNotificationBuilder(@NonNull Context context, @NonNull MediaSessionCompat.Token sessionToken) {

        this.context = context;

        this.sessionToken = sessionToken;

        this.jumpBack = new NotificationCompat.Action(
                R.drawable.ic_round_replay_10_24px,
                context.getString(R.string.notification_jump_back),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_REWIND)
        );

        this.playAction = new NotificationCompat.Action(
                R.drawable.ic_round_play_arrow_24px,
                context.getString(R.string.notification_play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
        );

        this.pauseAction = new NotificationCompat.Action(
                R.drawable.ic_round_pause_24px,
                context.getString(R.string.notification_pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
        );

        this.jumpForth = new NotificationCompat.Action(
                R.drawable.ic_round_forward_10_24px,
                context.getString(R.string.notification_jump_forth),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_FAST_FORWARD)
        );


        final PendingIntent stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP);

        final androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setCancelButtonIntent(stopPendingIntent)
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true);

        this.notificationBuilder =
                new NotificationCompat.Builder(context, NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_ID)
                        .setStyle(mediaStyle)
                        .setDeleteIntent(stopPendingIntent)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.shades))
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setShowWhen(false)
                        .setContentIntent(
                                NotificationUtils.getNotificationClickIntent(
                                        NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_ID,
                                        context,
                                        MainActivity.class
                                )
                        );


    }


    @Nullable
    public Notification buildNotification() {

        MediaControllerCompat mediaController;

        try {
            mediaController = new MediaControllerCompat(context, sessionToken);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }


        // Clear all actions (because of builder reuse)
        notificationBuilder.mActions.clear();

        notificationBuilder.addAction(jumpBack);

        final int playbackState = mediaController.getPlaybackState().getState();
        notificationBuilder.addAction(playbackState == PlaybackStateCompat.STATE_PLAYING ? pauseAction : playAction);

        notificationBuilder.addAction(jumpForth);


        final MediaDescriptionCompat description = mediaController.getMetadata().getDescription();

        return notificationBuilder
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .build();


    }


}
