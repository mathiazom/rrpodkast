package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;


public class PodPlayerFragment extends Fragment {


    private static final String TAG = "RRP-PodPlayerFragment";


    private View view;


    private FragmentLoadingHandler fragmentLoadingHandler;

    private SelectedPodViewModel selectedPodViewModel;


    private PodPlayerControls podPlayerControls;

    private RRPod playerPod;


    static PodPlayerFragment newInstance() {

        return new PodPlayerFragment();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);


        view = inflater.inflate(R.layout.fragment_podplayer, container, false);

        initControlListeners();


        // Keep track of player changes
        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlayerPodViewModel.class);

        // Player pod change
        playerPodViewModel.getPlayerPod().observe(this, playerPod -> {

            this.playerPod = playerPod;

            displayPod(playerPod,playerPodViewModel.isPlaying());

        });

        // Player pod progress
        playerPodViewModel.getPlayerProgress().observe(this, this::displayPodProgress);

        // Player playing state
        playerPodViewModel.getIsPlayingData().observe(this, this::displayPlayingState);


        selectedPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SelectedPodViewModel.class);


        return view;

    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        try {

            podPlayerControls = (PodPlayerControls) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement PodPlayerControls");

        }

        try {

            fragmentLoadingHandler = (FragmentLoadingHandler) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement FragmentLoadingHandler");

        }

    }


    private void initControlListeners() {

        final SeekBar podPlayerProgressBar = view.findViewById(R.id.podPlayerProgressBar);

        SeekBarPainter.paint(podPlayerProgressBar, ContextCompat.getColor(requireContext(), R.color.colorAccent));

        podPlayerProgressBar.setOnSeekBarChangeListener(new PodProgressBarChangedListener() {
            @Override
            public void onProgressChanged(int progress, boolean fromUser) {
                if (fromUser) podPlayerControls.seekTo(progress);
            }
        });

        final ImageView playPauseButton = view.findViewById(R.id.podPlayerPlayPause);
        playPauseButton.setOnClickListener(v -> podPlayerControls.pauseOrContinuePod(requireContext()));

        final ImageButton jumpForthButton = view.findViewById(R.id.podPlayerJumpForth);
        jumpForthButton.setOnClickListener(v -> podPlayerControls.jump(PodPlayerConstants.PLAYER_SKIP_MS));

        final ImageButton jumpBackButton = view.findViewById(R.id.podPlayerJumpBack);
        jumpBackButton.setOnClickListener(v -> podPlayerControls.jump(-PodPlayerConstants.PLAYER_REWIND_MS));

        view.setOnClickListener(v -> {

            if (playerPod == null) {
                Log.e(TAG, "Pod from PodPlayer was null");
                return;
            }

            selectedPodViewModel.selectPod(playerPod);

            fragmentLoadingHandler.loadPodFragment();

        });

    }


    private void displayPod(@NonNull final RRPod pod, boolean isPlaying){

        displayPodDetails(pod);

        displayPlayingState(isPlaying);

        displayPodProgress(pod.getProgress());


    }

    private void displayPodDetails(@NonNull final RRPod pod) {

        final TextView podTitleView = view.findViewById(R.id.podPlayerPodTitle);
        podTitleView.setText(pod.getTitle());

        final SeekBar podPlayerProgress = view.findViewById(R.id.podPlayerProgressBar);
        int duration = pod.getDuration();
        podPlayerProgress.setMax(duration);

        final String durationText = MillisFormatter.toFormat(duration, MillisFormatter.MillisFormat.HH_MM_SS);
        final TextView podPlayerDurationText = view.findViewById(R.id.podPlayerDuration);
        podPlayerDurationText.setText(durationText);

    }

    private void displayPlayingState(boolean isPlaying){

        final ImageView mainController = view.findViewById(R.id.podPlayerPlayPause);
        mainController.setImageResource(isPlaying ? R.drawable.ic_pause_pod : R.drawable.ic_play_pod);

    }

    private void displayPodProgress(int progress) {

        final SeekBar podPlayerProgress = view.findViewById(R.id.podPlayerProgressBar);
        podPlayerProgress.setProgress(progress);

        final TextView podPlayerProgressText = view.findViewById(R.id.podPlayerProgress);
        final String progressString = MillisFormatter.toFormat(progress, MillisFormatter.MillisFormat.HH_MM_SS);
        podPlayerProgressText.setText(progressString);

    }

}
