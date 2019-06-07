package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;


public class PodPlayerFragment extends Fragment {


    private static final String TAG = "RRP-PodPlayerFragment";


    private View view;

    private MainFragmentsHandler mainFragmentsHandler;

    private PodPlayerControls podPlayerControls;

    private RRPod playerPod;

    private PlayerPodViewModel playerPodViewModel;


    @NonNull
    public static PodPlayerFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsHandler) {

        final PodPlayerFragment fragment = new PodPlayerFragment();

        fragment.mainFragmentsHandler = mainFragmentsHandler;

        return fragment;

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);


        view = inflater.inflate(R.layout.fragment_podplayer_fresh, container, false);

        initControlListeners();


        // Keep track of player changes
        playerPodViewModel = ViewModelProviders.of(requireActivity()).get(PlayerPodViewModel.class);

        // Player pod change
        playerPodViewModel.getPlayerPodObservable().observe(this, playerPod -> {

            this.playerPod = playerPod;

            displayPod();

        });

        // Player pod progress
        playerPodViewModel.getPlayerProgressObservable().observe(this, this::displayPodProgress);

        // Player playing state
        playerPodViewModel.getIsPlayingObservable().observe(this, this::displayPlayingState);


        return view;

    }


    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {

            podPlayerControls = (PodPlayerControls) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement PodPlayerControls");

        }

    }


    private void initControlListeners() {

        final ImageView playPauseAction = view.findViewById(R.id.podPlayerSmallPlayPause);
        playPauseAction.setOnClickListener(v -> podPlayerControls.pauseOrContinuePod());

        view.setOnClickListener(v -> {

            if (playerPod == null) {
                Log.e(TAG, "Pod from PodPlayer was null");
                return;
            }

            mainFragmentsHandler.loadPodPlayerFullFragment();

        });

    }


    private void displayPod(){

        displayPodDetails();

        displayPlayingState();

        displayPodProgress();

    }


    private void displayPodDetails() {

        final TextView podTitleView = view.findViewById(R.id.podPlayerSmallTitle);
        podTitleView.setText(playerPod.getTitle());

    }

    private void displayPlayingState(){

        displayPlayingState(playerPodViewModel.isPlaying());

    }

    private void displayPlayingState(final boolean isPlaying){

        final ImageView playPauseAction = view.findViewById(R.id.podPlayerSmallPlayPause);
        playPauseAction.setImageResource(isPlaying ? R.drawable.ic_round_pause_24px : R.drawable.ic_round_play_arrow_24px);

    }


    private void displayPodProgress(){

        displayPodProgress(playerPod.getProgress());

    }

    private void displayPodProgress(final int progress) {

        final Guideline podProgressGuide = view.findViewById(R.id.podPlayerSmallProgressGuide);
        podProgressGuide.setGuidelinePercent(((float) progress) / playerPod.getDuration());

    }

}
