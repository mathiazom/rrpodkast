package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;

import androidx.annotation.NonNull;

public class PodExoPlayer implements PodPlayerControls {


    private final Context context;

    private ExoPlayer exoPlayer;



    PodExoPlayer(@NonNull Context context){

        this.context = context;

        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context);

    }


    @Override
    public boolean loadPod(@NonNull RRPod pod) {


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context,context.getApplicationInfo().name));

        final PodStorageHandle podStorageHandle = new PodStorageHandle(context);

        final ExtractorMediaSource.Factory mediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);

        Uri podUri;

        if(pod.isDownloaded()){

            podUri = podStorageHandle.getPodUri(pod);

        }else{

            podUri = Uri.parse(pod.getUrl());

        }

        exoPlayer.prepare(mediaSourceFactory.createMediaSource(podUri));




        return false;
    }

    @Override
    public void playPod(@NonNull RRPod pod) {

    }

    @Override
    public void pauseOrContinuePod() {

    }

    @Override
    public void pausePod() {

    }

    @Override
    public void continuePod() {

    }

    @Override
    public void jump(int jump) {

    }

    @Override
    public void seekTo(int progress) {

    }
}
