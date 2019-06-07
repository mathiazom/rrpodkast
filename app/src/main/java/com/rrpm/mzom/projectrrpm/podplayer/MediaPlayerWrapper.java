package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;


/**
 *
 *  Wrapper class to keep track of the MediaPlayer state according to the documentation's State Diagram:
 *  https://developer.android.com/reference/android/media/MediaPlayer
 *
 */

public class MediaPlayerWrapper extends MediaPlayer {

    enum State {
        IDLE,
        INITIALIZED,
        PREPARED,
        PREPARING,
        STARTED,
        PAUSED,
        STOPPED,
        COMPLETED,
        ERROR
    }

    private State state;


    MediaPlayerWrapper(@NonNull Context context, final OnCompletionListener onCompletionListener){

        final MediaPlayerWrapper mediaPlayerWrapper = this;

        // Prevent system from killing the playback process
        mediaPlayerWrapper.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        setOnErrorListener((mediaPlayer, i, i1) -> {

            mediaPlayerWrapper.setState(State.ERROR);

            return false;
        });


        setOnPreparedListener(mediaPlayer -> mediaPlayerWrapper.setState(State.PREPARED));

        setOnCompletionListener(mediaPlayer -> {

            if(mediaPlayerWrapper.isLooping()){
                return;
            }

            mediaPlayerWrapper.setState(State.COMPLETED);

            if(onCompletionListener != null){
                onCompletionListener.onCompletion(mediaPlayerWrapper);
            }

        });

        setState(State.IDLE);

    }

    private void setState(State state){
        this.state = state;
    }

    State getState(){
        return this.state;
    }

    @Override
    public void reset() {
        super.reset();

        setState(State.IDLE);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(@NonNull AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(afd);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        super.setDataSource(dataSource);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri, headers);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd, offset, length);

        setState(State.INITIALIZED);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers, @Nullable List<HttpCookie> cookies) throws IOException {
        super.setDataSource(context, uri, headers, cookies);

        setState(State.INITIALIZED);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();

        setState(State.PREPARING);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();

        setState(State.PREPARED);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        setState(State.STARTED);
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();

        setState(State.STOPPED);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();

        setState(State.PAUSED);
    }
}
