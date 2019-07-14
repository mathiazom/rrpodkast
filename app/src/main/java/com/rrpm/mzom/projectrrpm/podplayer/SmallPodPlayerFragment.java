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

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandlerViewModel;
import com.rrpm.mzom.projectrrpm.pod.RRPod;


public class SmallPodPlayerFragment extends Fragment {


    private static final String TAG = "RRP-SmallPodPlayerFrag";


    private View view;

    private MainFragmentsHandler mainFragmentsHandle;

    private PodPlayerControls podPlayerControls;

    private PodPlayerViewModel playerPodViewModel;

    private RRPod playerPod;


    @NonNull
    public static SmallPodPlayerFragment newInstance() {

        return new SmallPodPlayerFragment();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);


        view = inflater.inflate(R.layout.fragment_podplayer_small, container, false);

        initControlListeners();


        // Keep track of player changes
        playerPodViewModel = ViewModelProviders.of(requireActivity()).get(PodPlayerViewModel.class);

        // Player pod change
        playerPodViewModel.getPlayerPodObservable().observe(this, this::setAndDisplayPod);

        // Player pod progress
        playerPodViewModel.getPlayerProgressObservable().observe(this, this::displayPodProgress);

        // Player playing state
        playerPodViewModel.getIsPlayingObservable().observe(this, this::displayPlayingState);


        // Observe main fragments handle
        final MainFragmentsHandlerViewModel mainFragmentsHandleViewModel = ViewModelProviders.of(requireActivity()).get(MainFragmentsHandlerViewModel.class);
        mainFragmentsHandleViewModel.getObservableMainFragmentsHandler().observe(this, mainFragmentsHandle -> this.mainFragmentsHandle = mainFragmentsHandle);



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

            if (playerPodViewModel.getPlayerPod() == null) {

                Log.e(TAG, "Pod from PodPlayer was null");

                return;

            }

            mainFragmentsHandle.loadPodPlayerFragment();

        });

    }


    private void setAndDisplayPod(@NonNull final RRPod pod){

        this.playerPod = pod;

        displayPodTitle(pod.getTitle());

        displayPlayingState(playerPodViewModel.isPlaying());

        displayPodProgress(pod.getProgress());

    }


    private void displayPodTitle(final String title) {

        final TextView podTitleView = view.findViewById(R.id.podPlayerSmallTitle);
        podTitleView.setText(title);

    }


    private void displayPlayingState(final boolean isPlaying){

        final ImageView playPauseAction = view.findViewById(R.id.podPlayerSmallPlayPause);
        playPauseAction.setImageResource(isPlaying ? R.drawable.ic_round_pause_24px : R.drawable.ic_round_play_arrow_24px);

    }

    private void displayPodProgress(final int progress) {

        final Guideline podProgressGuide = view.findViewById(R.id.podPlayerSmallProgressGuide);
        podProgressGuide.setGuidelinePercent(((float) progress) / playerPod.getDuration());

    }

}
