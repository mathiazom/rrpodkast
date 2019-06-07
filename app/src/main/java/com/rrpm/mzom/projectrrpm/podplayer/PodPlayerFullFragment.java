package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.ui.SimpleSeekBarChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class PodPlayerFullFragment extends Fragment {


    private View view;

    private MainFragmentsHandler mainFragmentsHandler;

    private PlayerPodViewModel playerPodViewModel;

    private RRPod playerPod;

    private PodPlayerControls podPlayerControls;


    @NonNull
    public static PodPlayerFullFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsHandler) {

        final PodPlayerFullFragment fragment = new PodPlayerFullFragment();

        fragment.mainFragmentsHandler = mainFragmentsHandler;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        this.view = inflater.inflate(R.layout.fragment_podplayer_full,container,false);


        // Keep track of player changes
        playerPodViewModel = ViewModelProviders.of(requireActivity()).get(PlayerPodViewModel.class);

        // Player pod change
        playerPodViewModel.getPlayerPodObservable().observe(this, playerPod -> {

            this.playerPod = playerPod;

            displayPod(playerPod);

        });

        // Player pod progress
        playerPodViewModel.getPlayerProgressObservable().observe(this, this::displayPodProgress);

        // Player playing state
        playerPodViewModel.getIsPlayingObservable().observe(this, this::displayPlayingState);


        initListeners();


        return this.view;

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


    private void initListeners(){

        final SeekBar progressBar = view.findViewById(R.id.podPlayerProgressBar);
        progressBar.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int progress, boolean fromUser) {
                if (fromUser) podPlayerControls.seekTo(progress);
            }
        });

        final ImageView jumpForthButton = view.findViewById(R.id.podPlayerJumpForth);
        jumpForthButton.setOnClickListener(v -> podPlayerControls.jump(PodPlayerConstants.PLAYER_SKIP_MS));

        final ImageView jumpBackButton = view.findViewById(R.id.podPlayerJumpBack);
        jumpBackButton.setOnClickListener(v -> podPlayerControls.jump(-PodPlayerConstants.PLAYER_REWIND_MS));

        final LinearLayout playerPauseContainer = view.findViewById(R.id.podPlayerPlayPauseContainer);
        playerPauseContainer.setOnClickListener(v -> podPlayerControls.pauseOrContinuePod());

        final ImageView launchPodFragmentAction = view.findViewById(R.id.launchPodFragmentAction);
        launchPodFragmentAction.setOnClickListener(v -> {

            mainFragmentsHandler.loadPodFragment(playerPod);

            mainFragmentsHandler.hidePodPlayerFullFragment();

        });

    }


    private void displayPod(@NonNull final RRPod pod){

        displayPodInfo(pod);

        displayPodProgress(pod.getProgress());

        displayPlayingState(playerPodViewModel.isPlaying());

    }


    private void displayPodInfo(@NonNull final RRPod pod){


        final TextView podTitleView = view.findViewById(R.id.podTitle);
        podTitleView.setText(pod.getTitle());

        final TextView podDescriptionView = view.findViewById(R.id.podDescription);
        podDescriptionView.setText(pod.getDescription());

        final TextView podPlayerDuration = view.findViewById(R.id.podPlayerDuration);
        podPlayerDuration.setText(MillisFormatter.toFormat(pod.getDuration(), MillisFormatter.MillisFormat.HH_MM_SS));

        final SeekBar progressBar = view.findViewById(R.id.podPlayerProgressBar);
        progressBar.setMax(pod.getDuration());


    }


    private void displayPodProgress(final int progress){

        final SeekBar progressBar = view.findViewById(R.id.podPlayerProgressBar);
        progressBar.setProgress(progress);

        final TextView podPlayerProgress = view.findViewById(R.id.podPlayerProgress);
        podPlayerProgress.setText(MillisFormatter.toFormat(progress, MillisFormatter.MillisFormat.HH_MM_SS));

    }

    private void displayPlayingState(final boolean isPlaying){

        final ImageView podPlayerPlayPauseIcon = view.findViewById(R.id.podPlayerPlayPauseIcon);

        if (isPlaying){

            podPlayerPlayPauseIcon.setImageResource(R.drawable.ic_round_pause_24px);

        }else{

            podPlayerPlayPauseIcon.setImageResource(R.drawable.ic_round_play_arrow_24px);

        }

    }






}
