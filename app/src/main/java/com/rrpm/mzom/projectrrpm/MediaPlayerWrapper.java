package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    enum MediaPlayerState{
        Idle,
        Initialized,
        Prepared,
        Preparing,
        Started,
        Paused,
        Stopped,
        PlaybackCompleted,
        Error
    }

    private MediaPlayerState state;


    MediaPlayerWrapper(){

        final MediaPlayerWrapper mediaPlayerWrapper = this;

        setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                mediaPlayerWrapper.setState(MediaPlayerState.Error);

                return false;
            }
        });


        setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayerWrapper.setState(MediaPlayerState.Prepared);

            }
        });

        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if(mediaPlayerWrapper.isLooping()){
                    return;
                }

                mediaPlayerWrapper.setState(MediaPlayerState.PlaybackCompleted);

            }
        });

        setState(MediaPlayerState.Idle);

    }

    private void setState(MediaPlayerState state){
        this.state = state;
    }

    MediaPlayerState getState(){
        return this.state;
    }

    @Override
    public void reset() {
        super.reset();

        setState(MediaPlayerState.Idle);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(@NonNull AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(afd);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        super.setDataSource(dataSource);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri, headers);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd, offset, length);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers, @Nullable List<HttpCookie> cookies) throws IOException {
        super.setDataSource(context, uri, headers, cookies);

        setState(MediaPlayerState.Initialized);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();

        setState(MediaPlayerState.Preparing);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();

        setState(MediaPlayerState.Prepared);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        setState(MediaPlayerState.Started);
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();

        setState(MediaPlayerState.Stopped);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();

        setState(MediaPlayerState.Paused);
    }
}
