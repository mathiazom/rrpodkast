package com.rrpm.mzom.projectrrpm.podplayer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.rrpm.mzom.projectrrpm.podstorage.ConnectionValidator;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.ui.SimpleSeekBarChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class PodPlayerFragment extends Fragment {


    private static final String TAG = "RRP-PodPlayerFragment";


    private View view;

    private MainFragmentsHandler mainFragmentsHandler;

    private PlayerPodViewModel playerPodViewModel;

    private RRPod playerPod;

    private PodPlayerControls podPlayerControls;


    @NonNull
    public static PodPlayerFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsHandler) {

        final PodPlayerFragment fragment = new PodPlayerFragment();

        fragment.mainFragmentsHandler = mainFragmentsHandler;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        this.view = inflater.inflate(R.layout.fragment_podplayer,container,false);


        // Keep track of player changes
        playerPodViewModel = ViewModelProviders.of(requireActivity()).get(PlayerPodViewModel.class);

        // Player pod change
        playerPodViewModel.getPlayerPodObservable().observe(this, playerPod -> {

            this.playerPod = playerPod;

            displayPod();

        });

        // Player pod progress
        playerPodViewModel.getPlayerProgressObservable().observe(this, progress -> {

            playerPod.setProgress(progress);

            displayPodProgress();

        });

        // Player playing state
        playerPodViewModel.getIsPlayingObservable().observe(this, isPlaying ->
                displayPlayingState()
        );


        initListeners();

        ConnectionValidator.attemptToRegisterConnectionListener(requireContext(), isConnected -> {

            if(getActivity() == null){
                return;
            }

            getActivity().runOnUiThread(this::displayPlayability);

        });


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
                if (fromUser){

                    podPlayerControls.seekTo(progress);

                }
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

            mainFragmentsHandler.hidePodPlayerFragment();

        });

        final ImageView toolbarBackAction = view.findViewById(R.id.toolbarBackAction);
        toolbarBackAction.setOnClickListener(v -> requireActivity().onBackPressed());

    }


    private void displayPod(){

        displayPodInfo();

        displayPodDuration();

        displayPodProgress();

        displayPlayingState();

        displayPlayability();

    }

    private void displayPlayability(){

        final boolean isPlayable = playerPod.isDownloaded() || ConnectionValidator.isConnected(requireContext());

        final ConstraintLayout playerContainer = view.findViewById(R.id.podPlayerContainer);
        playerContainer.setAlpha(isPlayable ? 1.0f : 0.3f);
        setDeepEnabled(playerContainer,isPlayable);

    }


    /**
     *
     * An extension of the {@link View#setEnabled(boolean)} where a {@link ViewGroup} can be passed to call
     * {@link View#setEnabled(boolean)} with the same value on all of its children.
     * Passing a {@link View} that is not a {@link ViewGroup} will be identical to calling {@link View#setEnabled(boolean)}
     *
     * @param view: A {@link View}, and any children, to be enabled/disabled.
     * @param enabled: True if views should be enabled, false otherwise.
     *
     */

    private void setDeepEnabled(View view, boolean enabled) {

        view.setEnabled(enabled);

        if ( view instanceof ViewGroup ) {

            ViewGroup group = (ViewGroup)view;

            for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                setDeepEnabled(group.getChildAt(idx), enabled);
            }

        }

    }


    private void displayPodInfo(){

        final TextView podTitleView = view.findViewById(R.id.podTitle);
        podTitleView.setText(playerPod.getTitle());

        final TextView podDescriptionView = view.findViewById(R.id.podDescription);
        podDescriptionView.setText(playerPod.getDescription());

        final TextView podPlayerDuration = view.findViewById(R.id.podPlayerDuration);
        podPlayerDuration.setText(MillisFormatter.toFormat(playerPod.getDuration(), MillisFormatter.MillisFormat.HH_MM_SS));

    }

    private void displayPodDuration(){

        final SeekBar progressBar = view.findViewById(R.id.podPlayerProgressBar);
        progressBar.setMax(playerPod.getDuration());

    }


    private void displayPodProgress(){

        final int progress = playerPod.getProgress();

        final SeekBar progressBar = view.findViewById(R.id.podPlayerProgressBar);
        progressBar.setProgress(progress);

        final TextView podPlayerProgress = view.findViewById(R.id.podPlayerProgress);
        podPlayerProgress.setText(MillisFormatter.toFormat(progress, MillisFormatter.MillisFormat.HH_MM_SS));

    }

    private void displayPlayingState(){

        final boolean isPlaying = playerPodViewModel.isPlaying();

        final ImageView podPlayerPlayPauseIcon = view.findViewById(R.id.podPlayerPlayPauseIcon);

        if (isPlaying){

            podPlayerPlayPauseIcon.setImageResource(R.drawable.ic_round_pause_24px);

        }else{

            podPlayerPlayPauseIcon.setImageResource(R.drawable.ic_round_play_arrow_24px);

        }

    }






}
