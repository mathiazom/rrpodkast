package com.rrpm.mzom.projectrrpm.podplayer;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class MediaControllerViewModel extends AndroidViewModel {


    private static final String TAG = "RRP-MediaControllerVM";


    private MediaBrowserCompat mediaBrowser;

    private MediaControllerCompat mediaController;



    public MediaControllerViewModel(@NonNull Application application) {
        super(application);
    }

    public interface MediaControllerRequestCallback {

        void onMediaController(final MediaControllerCompat mediaController);

    }


    public void requestMediaController(@NonNull Activity activity, @NonNull MediaControllerCompat.Callback controllerCallback, @NonNull MediaControllerRequestCallback requestCallback){

        if(mediaController != null){

            requestCallback.onMediaController(mediaController);

            return;

        }


        mediaBrowser = new MediaBrowserCompat(
                getApplication(),
                new ComponentName(getApplication(), PodPlayerService.class),
                new MediaBrowserCompat.ConnectionCallback() {

                    @Override
                    public void onConnected() {

                        Log.i(TAG, "The pod player service has connected successfully");

                        mediaController = createMediaController(mediaBrowser.getSessionToken(),controllerCallback);

                        // Create and register media controller
                        MediaControllerCompat.setMediaController(
                                activity,
                                mediaController
                        );

                        requestCallback.onMediaController(mediaController);

                    }

                    @Override
                    public void onConnectionSuspended() {

                        Log.e(TAG, "The pod player service has crashed");
                        // TODO: Disable transport controls until it automatically reconnects

                    }

                    @Override
                    public void onConnectionFailed() {

                        Log.e(TAG, "The pod player service has refused connection");

                    }


                },
                null);

        mediaBrowser.connect();


    }

    private MediaControllerCompat createMediaController(@NonNull final MediaSessionCompat.Token sessionToken, @NonNull final MediaControllerCompat.Callback callback) {

        try {

            final MediaControllerCompat mediaController = new MediaControllerCompat(getApplication(), sessionToken);

            mediaController.registerCallback(callback);

            return mediaController;

        } catch (RemoteException e) {

            Log.e(TAG, "Error creating media controller");

            e.printStackTrace();

            return null;
        }

    }








}
