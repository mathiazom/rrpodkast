package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class PodPlayerFragment extends Fragment implements PodPlayer.PodPlayerListener{

    private static final String TAG = "RRP-PodPlayerFragment";

    private View view;
    private SeekBar podPlayerProgress;
    private ImageButton mainController;
    private TextView podPlayerDuration;
    private TextView podPlayerElapsed;
    private TextView podNameText;

    private PodPlayer podPlayer;

    private Timer displayProgressTimer;
    private Timer saveProgressAndListenedToStateTimer;


    public static PodPlayerFragment newInstance(@NonNull final PodPlayer podPlayer) {

        PodPlayerFragment fragment = new PodPlayerFragment();

        fragment.podPlayer = podPlayer;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_podplayer,container,false);

        podNameText = view.findViewById(R.id.podplayer_text);

        podPlayerProgress = view.findViewById(R.id.progressbar);

        mainController = view.findViewById(R.id.podplayer_maincontrol);

        podPlayerElapsed = view.findViewById(R.id.podplayer_elapsed);

        podPlayerDuration = view.findViewById(R.id.podplayer_duration);

        initPlayerControls();

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(getContext() == null){
            throw new NullPointerException();
        }

        updatePodPlayer(null,getContext());

    }

    void setPodPlayer(@NonNull final PodPlayer podPlayer){
        this.podPlayer = podPlayer;
    }

    private void initPlayerControls(){

        mainController.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.pauseOrContinuePod();
            }
        });

        final ImageButton skipForth = view.findViewById(R.id.podplayer_skipforth);
        skipForth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.jump(PodPlayerConstants.PLAYER_SKIP_MS);
            }
        });

        final ImageButton skipBack = view.findViewById(R.id.podplayer_skipback);
        skipBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.jump(-PodPlayerConstants.PLAYER_REWIND_MS);
            }
        });

    }

    private void displayPlayingState(){
        mainController.setImageResource(podPlayer.isPlaying() ? R.drawable.ic_pause_pod : R.drawable.ic_play_pod);
    }

    void updatePodPlayer(@Nullable final RRPod pod, @NonNull final Context context) throws NullPointerException {

        final String podName = (pod == null) ? "" : pod.getTitle();

        final SharedPreferences podStorage = PreferenceManager.getDefaultSharedPreferences(getContext());

        podNameText.setText(podName);

        displayPlayingState();

        int duration = podPlayer.getDuration();
        final String durationText = duration == -1 ? "" : getCleanTime(duration);

        podPlayerDuration.setText(durationText);

        podPlayerProgress.setMax(duration);

        colorizeSeekBar(podPlayerProgress,ContextCompat.getColor(context, R.color.myPodcasts));

        if(pod != null){

            int progress = podPlayer.getCurrentPosition();

            onCurrentPositionChanged(progress);
            podStorage.edit().putString("recent_pod_name",podName).apply();

            // Timer keeping track of player progression
            displayProgressTimer = new Timer();
            displayProgressTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (podPlayer.isPlaying()) {
                                        podPlayerProgress.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                updatePodPlayerProgress(podPlayer.getCurrentPosition());

                                            }
                                        });
                                    }
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }, 0, PodPlayerConstants.PROGRESS_REFRESH_FREQ_MS);

            // Timer saving progress and checking if pod should be marked as "listened to"
            saveProgressAndListenedToStateTimer = new Timer();
            saveProgressAndListenedToStateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    if (podPlayer.isPlaying()) {
                                        podPlayerProgress.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                int timeStamp = podPlayer.getCurrentPosition();

                                                storeCurrentPlayerProgress(timeStamp,podStorage);

                                                storeListenedToStateIfChanged(podName,timeStamp,podStorage);

                                            }
                                        });
                                    }
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            },0,PodPlayerConstants.SAVE_PROGRESS_FREQ_MS);

            podPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (fromUser) {
                        podPlayer.seekTo(progress);
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    colorizeSeekBar(seekBar,Color.parseColor("#ff3d00")); // Orange

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    colorizeSeekBar(seekBar,Color.parseColor("#b71c1c")); // Red

                }
            });

        }



        view.setVisibility(View.VISIBLE);
    }

    private void storeCurrentPlayerProgress(final int timeStamp, @NonNull final SharedPreferences podStorage){

        podStorage.edit().putInt(getString(com.rrpm.mzom.projectrrpm.R.string.SPKey_recent_pod_progress), timeStamp).apply();

    }

    private void storeListenedToStateIfChanged(final String podName, final int timeStamp, @NonNull final SharedPreferences podStorage){

        boolean overListenedToMark = timeStamp > podPlayer.getDuration()*(PodPlayerConstants.LISTENED_TO_MARK_PERCENT/100.0);
        boolean storedAsListenedTo = podStorage.getBoolean(podName + getString(com.rrpm.mzom.projectrrpm.R.string.SP_listened_to_marker), false);

        if (overListenedToMark && !storedAsListenedTo) {
            podStorage.edit().putBoolean(podName + getString(com.rrpm.mzom.projectrrpm.R.string.SP_listened_to_marker), true).apply();
        }

    }



    private void colorizeSeekBar(@NonNull final SeekBar seekBar, final int color){

        // Colorize progress
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // Colorize thumb
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

    }

    /**
     *
     * Convert milliseconds to time with format "HH:MM:SS"
     *
     * @param ms: Total number of milliseconds to be represented
     * @return Converted millisecond as a String
     *
     */
    private String getCleanTime(int ms) {

        final String seconds = addZero(String.valueOf((ms / 1000) % 60));
        final String minutes = addZero(String.valueOf(((ms / (1000 * 60)) % 60)));
        final String hours = addZero(String.valueOf(((ms / (1000 * 60 * 60)) % 24)));

        return hours + ":" + minutes + ":" + seconds;
    }

    private String addZero(String s) {
        if (s.length() == 1)
            return "0" + s;
        else return s;
    }


    private void purgeTimer(@Nullable final Timer timer){

        if(timer == null){
            return;
        }

        timer.cancel();
        timer.purge();
    }

    private void updatePodPlayerProgress(int position){

        podPlayerElapsed.setText(getCleanTime(position));
        podPlayerProgress.setProgress(position);

    }


    @Override
    public void onPodLoaded(@NonNull RRPod pod) {

        if(getContext() == null){
            return;
        }

        updatePodPlayer(pod,getContext());

    }

    @Override
    public void onCurrentPositionChanged(int position) {

        updatePodPlayerProgress(position);

    }

    @Override
    public void onPodStarted(@NonNull RRPod pod, int from) {

        purgeTimer(displayProgressTimer);
        purgeTimer(saveProgressAndListenedToStateTimer);

        if(getContext() == null){
            throw new NullPointerException("Context was not available when new pod was started");
        }

        updatePodPlayer(pod,getContext());

    }

    @Override
    public void onPlayerPaused() {
        mainController.setImageResource(podPlayer.isPlaying() ? R.drawable.ic_pause_pod : R.drawable.ic_play_pod);
    }

    @Override
    public void onPlayerContinued() {
        mainController.setImageResource(podPlayer.isPlaying() ? R.drawable.ic_pause_pod : R.drawable.ic_play_pod);
    }
}
