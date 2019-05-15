package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PodFragment extends Fragment {

    private static final String TAG = "RRP-PodFragment";

    private RRPod pod;

    private PodPlayerControls podPlayerControls;

    private PodDownloader podDownloader;

    private View view;

    private PodsViewModel podsViewModel;

    private PlayerPodViewModel playerPodViewModel;

    private RRPod playerPod;


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

        final SelectedPodViewModel selectedPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SelectedPodViewModel.class);
        final LiveData<RRPod> selectedPod = selectedPodViewModel.getSelectedPod();
        selectedPod.observe(this, this::setPod);
        setPod(selectedPod.getValue());

        podsViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PodsViewModel.class);
        podsViewModel.retrievePods(RRReader.PodType.MAIN_PODS).observe(this, pods -> {

            final RRPod equalPod = PodUtils.getEqualPod(pod,pods);

            if(equalPod == null){
                throw new NullPointerException("Pod no longer represented in pod list");
            }

            setPod(equalPod);

        });

        playerPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlayerPodViewModel.class);
        playerPodViewModel.getPlayerPod().observe(this,playerPod -> {

            this.playerPod = playerPod;

            // Check if player pod matches fragment pod
            if(playerPod.getId().equals(pod.getId())){
                // Redraw pod in case of changes
                displayPod();
            }

        });
        playerPodViewModel.getIsPlayingData().observe(this, isPlaying -> handlePlayingState());

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

    }


    private View.OnClickListener podOptionPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(pod.getDuration() == pod.getProgress()){
                pod.setProgress(0);
            }

            podPlayerControls.playPod(pod,requireContext());
        }
    };

    private View.OnClickListener podOptionDownloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            podDownloader.requestPodDownload(pod);

            requestDownloadProgressDisplay();
        }
    };

    private View.OnClickListener podOptionListenedToClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final int newProgress = pod.isListenedTo() ? 0 : pod.getDuration();

            pod.setProgress(newProgress);

            new PodStorageHandle(requireContext()).storePodProgress(pod);

            podsViewModel.getPodsPackage().updatePod(pod);
        }
    };

    private void displayPod(){

        displayPodDetails();

        handlePlayingState();

        handleDownloadState();

        displayListenedToButton();

    }

    private void displayPodDetails(){

        final TextView podTitleView = view.findViewById(R.id.podPlayerPodTitle);
        podTitleView.setText(pod.getTitle());

        final TextView podDetailsView = view.findViewById(R.id.podDetails);
        final Calendar podCalendar = Calendar.getInstance();
        podCalendar.setTime(pod.getDate());

        final String podDetails = "Sesong " + String.valueOf(podCalendar.get(Calendar.YEAR) - 2011) + " • " + MillisFormatter.toFormat(pod.getDuration(), MillisFormatter.MillisFormat.HH_MM_SS);
        podDetailsView.setText(podDetails);

        final TextView podDescriptionView = view.findViewById(R.id.podDescription);
        podDescriptionView.setText(pod.getDescription());

    }

    private void handlePlayingState(){

        final Resources resources = requireContext().getResources();

        Drawable optionIconDrawable;

        if(playerPod == pod && playerPodViewModel.isPlaying()){
            optionIconDrawable = resources.getDrawable(R.drawable.ic_pause_white_24dp);
        }else{
            optionIconDrawable = resources.getDrawable(R.drawable.ic_play_arrow_white_24dp);
        }

        final ImageView podOptionIconPlay = view.findViewById(R.id.podOptionIconPlay);
        podOptionIconPlay.setImageDrawable(optionIconDrawable);

        final ConstraintLayout podOptionPlay = view.findViewById(R.id.podOptionPlay);
        podOptionPlay.setOnClickListener(podOptionPlayClickListener);

        final Guideline podProgressGuide = view.findViewById(R.id.podOptionPlayProgressGuide);
        podProgressGuide.setGuidelinePercent(((float)pod.getProgress())/pod.getDuration());

        final LinearLayout podProgress = view.findViewById(R.id.podProgress);
        podProgress.getBackground().setColorFilter(resources.getColor(
                pod.isListenedTo() ? R.color.colorGreyDark : R.color.colorAccent
        ), PorterDuff.Mode.SRC_ATOP);

    }

    private void handleDownloadState(){

        if(pod.isDownloaded()){
            final Guideline downloadProgressGuide = view.findViewById(R.id.podOptionDownloadProgressGuide);
            downloadProgressGuide.setGuidelinePercent(1);
        }

        final ImageView podOptionIconDownload = view.findViewById(R.id.podOptionIconDownload);
        podOptionIconDownload.setImageDrawable(requireContext().getResources().getDrawable(
                pod.isDownloaded() ? R.drawable.ic_check_circle_white_24dp : R.drawable.ic_file_download_white_24dp
        ));

        final ConstraintLayout podOptionDownload = view.findViewById(R.id.podOptionDownload);
        podOptionDownload.setOnClickListener(podOptionDownloadClickListener);

        setPodOptionClickable(!pod.isDownloaded(),podOptionDownload,podOptionIconDownload);

        final ConstraintLayout podOptionPlay = view.findViewById(R.id.podOptionPlay);
        final ImageView podOptionIconPlay = view.findViewById(R.id.podOptionIconPlay);
        setPodOptionClickable(ConnectionValidator.isConnected(requireContext()) || pod.isDownloaded(),podOptionPlay,podOptionIconPlay);

    }

    private void displayListenedToButton(){

        final Button podOptionListenedTo = view.findViewById(R.id.podOptionListenedTo);

        podOptionListenedTo.setText(pod.isListenedTo() ? getString(R.string.button_mark_as_not_listened_to) : getString(R.string.button_mark_as_listened_to));

        podOptionListenedTo.setOnClickListener(podOptionListenedToClickListener);

    }

    private void setPodOptionClickable(boolean clickable, ConstraintLayout podOption, ImageView podOptionIcon){

        podOption.setClickable(clickable);
        podOptionIcon.setImageAlpha(clickable ? 255 : 100);

    }

    @Nullable
    RRPod getPod(){

        return this.pod;

    }

    void setPod(final RRPod pod){

        if(pod == null){
            throw new IllegalStateException("Pod was null");
        }

        this.pod = pod;

        displayPod();

        requestDownloadProgressDisplay();

    }

    private void requestDownloadProgressDisplay(){

        final RRPod downloadingPod = podDownloader.getDownloadingPod();

        if(downloadingPod != null && downloadingPod.getId().equals(pod.getId())){
            registerDownloadProgressTimer(podDownloader);
        }

    }

    private void registerDownloadProgressTimer(@NonNull final PodDownloader podDownloader){

        final Guideline downloadProgressGuide = view.findViewById(R.id.podOptionDownloadProgressGuide);

        final Timer downloadProgressTimer = new Timer();
        downloadProgressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {

                        if(podDownloader.getDownloadingProgress() == 100){
                            downloadProgressTimer.purge();
                            downloadProgressTimer.cancel();
                        }

                        downloadProgressGuide.setGuidelinePercent(podDownloader.getDownloadingProgress()/100f);

                    });
                }
            }
        }, 0, DownloadingConstants.DOWNLOAD_PROGRESS_REFRESH_RATE);

    }


}
