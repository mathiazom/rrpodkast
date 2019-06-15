package com.rrpm.mzom.projectrrpm.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.poddownloading.DownloadingConstants;
import com.rrpm.mzom.projectrrpm.podstorage.ConnectionValidator;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerControls;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podplayer.PlayerPodViewModel;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloadsViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.SelectedPodViewModel;

import java.util.Calendar;

public class PodFragment extends Fragment {

    private static final String TAG = "RRP-PodFragment";


    private View view;


    private RRPod pod;

    private PodPlayerControls podPlayerControls;

    private PodDownloader podDownloader;


    private SelectedPodViewModel selectedPodViewModel;

    private PodsViewModel podsViewModel;

    private PlayerPodViewModel playerPodViewModel;

    private PodDownloadsViewModel podDownloadsViewModel;


    private boolean isObservingDownloadProgress;

    private Guideline downloadProgressGuide;

    private boolean hasPlayerPod;


    private View.OnClickListener podOptionPlayClickListener;

    private View.OnClickListener podOptionDownloadClickListener;

    private View.OnClickListener podOptionCompletedClickListener;


    @NonNull
    static PodFragment newInstance(@NonNull PodDownloader podDownloader) {

        final PodFragment fragment = new PodFragment();

        fragment.podDownloader = podDownloader;

        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_pod, container, false);

        this.downloadProgressGuide = view.findViewById(R.id.podOptionDownloadProgressGuide);

        this.podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);

        this.selectedPodViewModel = ViewModelProviders.of(requireActivity()).get(SelectedPodViewModel.class);
        this.selectedPodViewModel.getSelectedPodObservable().observe(this, this::displayPod);

        this.playerPodViewModel = ViewModelProviders.of(requireActivity()).get(PlayerPodViewModel.class);
        this.playerPodViewModel.getPlayerPodObservable().observe(this, playerPod -> {

            if (playerPod.getId().equals(pod.getId())) {

                this.hasPlayerPod = true;

                displayPod();

                return;
            }

            this.hasPlayerPod = false;

        });
        this.playerPodViewModel.getIsPlayingObservable().observe(this, isPlaying -> displayPlayingState());
        this.playerPodViewModel.getPlayerProgressObservable().observe(this, playerProgress -> {

            if(hasPlayerPod){
                displayPodProgress(playerProgress);
            }

        });

        this.podDownloadsViewModel = ViewModelProviders.of(requireActivity()).get(PodDownloadsViewModel.class);

        this.podDownloadsViewModel.getDownloadingPodsObservable().observe(this, downloadingPods -> {

            if(!downloadingPods.contains(pod)){
                if(isObservingDownloadProgress){
                    // Pod download has been removed (completed or failed), no further observing need
                    isObservingDownloadProgress = false;
                }
                return;
            }

            final LiveData<Float> observableProgress = podDownloadsViewModel.getObservablePodProgress(pod);
            if (observableProgress == null) {
                throw new RuntimeException("Pod was not downloading despite its presence in the downloadingPods array");
            }

            Log.i(TAG,"Starting to observe pod download progress");

            observableProgress.observe(this, podProgress -> downloadProgressGuide.setGuidelinePercent(podProgress / (float) DownloadingConstants.DOWNLOAD_PROGRESS_MAX));

            isObservingDownloadProgress = true;

        });

        this.podOptionPlayClickListener = v -> {

            if (pod.getDuration() == pod.getProgress()) {

                pod.setProgress(0);
                podsViewModel.storePod(pod);
                selectedPodViewModel.selectPod(pod);

                if(hasPlayerPod){
                    podPlayerControls.seekTo(0);
                }

            }

            if(hasPlayerPod){
                podPlayerControls.pauseOrContinuePod();
                return;
            }

            podPlayerControls.playPod(pod);

        };

        this.podOptionDownloadClickListener = v -> podDownloader.requestPodDownload(pod);

        this.podOptionCompletedClickListener = v -> {

            pod.setProgress(pod.isCompleted() ? 0 : pod.getDuration());

            podsViewModel.storePod(pod);
            selectedPodViewModel.selectPod(pod);

            if(hasPlayerPod){
                podPlayerControls.pausePod();
                podPlayerControls.seekTo(pod.getProgress());
            }

        };

        initToolbar();

        initOptionListeners();

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


    private void initToolbar(){

        final ImageView backButton = view.findViewById(R.id.toolbarBackAction);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

    }

    private void initOptionListeners(){

        final ConstraintLayout podOptionPlay = view.findViewById(R.id.podOptionPlay);
        podOptionPlay.setOnClickListener(podOptionPlayClickListener);

        final ConstraintLayout podOptionDownload = view.findViewById(R.id.podOptionDownload);
        podOptionDownload.setOnClickListener(podOptionDownloadClickListener);

        final Button podOptionCompleted = view.findViewById(R.id.podOptionCompleted);
        podOptionCompleted.setOnClickListener(podOptionCompletedClickListener);

    }



    private void displayPod(@NonNull final RRPod pod){

        Log.i(TAG,"Back stack pre displaying pod: " + String.valueOf(requireActivity().getSupportFragmentManager().getBackStackEntryCount()));

        this.pod = pod;

        displayPod();

        Log.i(TAG,"Back stack post displaying pod: " + String.valueOf(requireActivity().getSupportFragmentManager().getBackStackEntryCount()));

    }

    private void displayPod() {

        displayPodDetails();

        displayPlayingState();

        displayPodProgress();

        displayDownloadState();

        displayCompletionState();

    }


    private void displayPodDetails() {

        final TextView podTitleView = view.findViewById(R.id.podPlayerPodTitle);
        podTitleView.setText(pod.getTitle());

        final TextView podDetailsView = view.findViewById(R.id.podDetails);
        final Calendar podCalendar = Calendar.getInstance();
        podCalendar.setTime(pod.getDate());

        final String podDetails = String.valueOf(podCalendar.get(Calendar.YEAR)) + " • " + MillisFormatter.toFormat(pod.getDuration(), MillisFormatter.MillisFormat.HH_MM_SS);
        podDetailsView.setText(podDetails);

        final TextView podDescriptionView = view.findViewById(R.id.podDescription);
        podDescriptionView.setText(pod.getDescription());

    }


    private void displayPlayingState() {

        final ImageView podOptionIconPlay = view.findViewById(R.id.podOptionIconPlay);

        if (hasPlayerPod && playerPodViewModel.isPlaying()) {
            podOptionIconPlay.setImageResource(R.drawable.ic_round_pause_24px);
        } else {
            podOptionIconPlay.setImageResource(R.drawable.ic_round_play_arrow_24px);
        }
    }


    private void displayPodProgress(){

        displayPodProgress(pod.getProgress());

    }

    private void displayPodProgress(int progress) {

        final Guideline podProgressGuide = view.findViewById(R.id.podOptionPlayProgressGuide);
        podProgressGuide.setGuidelinePercent(((float) progress) / pod.getDuration());

        final LinearLayout podProgress = view.findViewById(R.id.podProgress);
        podProgress.getBackground().setColorFilter(requireContext().getResources().getColor(
                pod.isCompleted() ? R.color.colorGreyDark : R.color.colorAccent
        ), PorterDuff.Mode.SRC_ATOP);

    }


    private void displayDownloadState() {

        final Guideline downloadProgressGuide = view.findViewById(R.id.podOptionDownloadProgressGuide);
        downloadProgressGuide.setGuidelinePercent(pod.isDownloaded() ? 1:0);

        // Download option
        final ConstraintLayout podOptionDownload = view.findViewById(R.id.podOptionDownload);
        final ImageView podOptionIconDownload = podOptionDownload.findViewById(R.id.podOptionIconDownload);
        podOptionIconDownload.setImageResource(pod.isDownloaded() ? R.drawable.ic_round_check_circle_24px : R.drawable.ic_round_get_app_24px);
        setPodOptionClickable(!pod.isDownloaded(), podOptionDownload, podOptionIconDownload);

        // Play/pause option
        final ConstraintLayout podOptionPlay = view.findViewById(R.id.podOptionPlay);
        final ImageView podOptionIconPlay = view.findViewById(R.id.podOptionIconPlay);
        setPodOptionClickable(ConnectionValidator.isConnected(requireContext()) || pod.isDownloaded(), podOptionPlay, podOptionIconPlay);

    }


    private void displayCompletionState() {

        final Button podOptionCompleted = view.findViewById(R.id.podOptionCompleted);
        podOptionCompleted.setText(pod.isCompleted() ? getString(R.string.button_mark_as_not_completed) : getString(R.string.button_mark_as_completed));

    }


    private void setPodOptionClickable(boolean clickable, ConstraintLayout podOption, ImageView podOptionIcon) {

        podOption.setClickable(clickable);
        podOptionIcon.setImageAlpha(clickable ? 255 : 100);

    }



}
