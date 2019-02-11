package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

    private WifiManager.WifiLock wifiLock;


    private PodPlayer podPlayer;

    private SeekBar podPlayerProgress;

    private TextView podPlayerElapsed;

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

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(getContext() == null){
            throw new NullPointerException();
        }

        viewPodPlayer(null,getContext());

    }

    void setPodPlayer(@NonNull final PodPlayer podPlayer){
        this.podPlayer = podPlayer;
    }

    PodPlayer getPodPlayer(){
        return this.podPlayer;
    }


    void viewPodPlayer(@Nullable final RRPod pod, @NonNull final Context context) throws NullPointerException {

        Log.i(TAG,"Pod is null: " + String.valueOf(pod == null));

        final String podName = (pod == null) ? "" : pod.getTitle();

        final SharedPreferences podStorage = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final TextView podNameTextView = view.findViewById(R.id.podplayer_text);
        podNameTextView.setText(podName);

        final ImageButton mainController = view.findViewById(R.id.podplayer_maincontrol);
        if (podPlayer.isPlaying()){
            mainController.setImageResource(R.drawable.ic_pause_pod);
        }
        else{
            mainController.setImageResource(R.drawable.ic_play_pod);
        }

        mainController.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.pauseOrContinuePod();
            }
        });

        final ImageButton skipForth = view.findViewById(R.id.podplayer_skipforth);
        skipForth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.jump(10000);
            }
        });

        final ImageButton skipBack = view.findViewById(R.id.podplayer_skipback);
        skipBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                podPlayer.jump(-10000);
            }
        });


        int tempDuration = podPlayer.getDuration();

        if(tempDuration == -1){
            tempDuration = 0;
        }

        final int duration = tempDuration;

        final TextView podPlayerDuration = view.findViewById(R.id.podplayer_duration);
        podPlayerDuration.setText(getCleanTime(duration));

        podPlayerProgress = view.findViewById(R.id.progressbar);
        podPlayerProgress.setMax(duration);

        podPlayerProgress.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.myPodcasts), android.graphics.PorterDuff.Mode.SRC_IN);

        podPlayerElapsed = view.findViewById(R.id.podplayer_elapsed);

        if(pod != null){

            int progress = podPlayer.getCurrentPosition();

            onCurrentPositionChanged(progress,pod);
            podStorage.edit().putString("recent_pod_name",podName).apply();

            // How often the progress display should be refreshed (in milliseconds)
            int displayProgressFrequency = 1000;

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

                                                int progress = podPlayer.getCurrentPosition();
                                                onCurrentPositionChanged(progress, pod);

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
            }, 0, displayProgressFrequency);

            // How often the current progress should be saved (in milliseconds)
            int saveProgressFrequency = 5000;

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

                                                int position = podPlayer.getCurrentPosition();

                                                podStorage.edit().putInt("recent_pod_progress", position).apply();

                                                if (position > podPlayer.getDuration()*0.20 && !podStorage.getBoolean(podName + "(LT)", false)) {
                                                    podStorage.edit().putBoolean(podName + "(LT)", true).apply();
                                                }
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
            },0,saveProgressFrequency);

            podPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        podPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // STYLE SEEKBAR ORANGE
                    seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ff3d00"), PorterDuff.Mode.SRC_IN);
                    seekBar.getThumb().setColorFilter(Color.parseColor("#ff3d00"), PorterDuff.Mode.SRC_IN);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // STYLE SEEKBAR RED
                    seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#b71c1c"), PorterDuff.Mode.SRC_IN);
                    seekBar.getThumb().setColorFilter(Color.parseColor("#b71c1c"), PorterDuff.Mode.SRC_IN);
                }
            });

        }



        view.setVisibility(View.VISIBLE);
    }


    // POD CURRENT/DURATION MS FORMATING
    private String getCleanTime(int ms) {
        String seconds = addZero(String.valueOf((ms / 1000) % 60));
        String minutes = addZero(String.valueOf(((ms / (1000 * 60)) % 60)));
        String hours = addZero(String.valueOf(((ms / (1000 * 60 * 60)) % 24)));

        return hours + ":" + minutes + ":" + seconds;
    }

    private String addZero(String s) {
        if (s.length() == 1)
            return "0" + s;
        else return s;
    }

    @Override
    public void onCurrentPositionChanged(int position, @NonNull final RRPod pod) {

        podPlayerElapsed.setText(getCleanTime(position));
        podPlayerProgress.setProgress(position);

    }

    @Override
    public void onPodStarted(@NonNull RRPod pod, int from) {

        if(displayProgressTimer != null){
            displayProgressTimer.cancel();
            displayProgressTimer.purge();
        }

        if(saveProgressAndListenedToStateTimer != null){
            saveProgressAndListenedToStateTimer.cancel();
            displayProgressTimer.purge();
        }

        if(getContext() == null){
            throw new NullPointerException("Context was not available when new pod was started");
        }

        viewPodPlayer(pod,getContext());

    }

    @Override
    public void onPlayerPaused() {

    }

    @Override
    public void onPlayerContinued() {

    }
}
