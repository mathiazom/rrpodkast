package com.rrpm.mzom.projectrrpm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Laget av Mathias Myklebust
 */

public class PodPlayerFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "RRP-PodPlayerFragment";

    private static RRPod pod;
    static MediaPlayer mp;
    private SeekBar podPlayerProgress;
    private boolean playingState;
    private boolean fromNotification;
    private boolean isMaximized = true;
    private View view;

    private LinearLayout podPlayerLayout;
    private TextView podPlayerElapsed;
    private ImageButton maximizeBtn;
    private ImageButton minimizeBtn;

    private WifiManager.WifiLock wifiLock;

    // "LIFECYCLE OVERRIDES"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View freshView = inflater.inflate(R.layout.fragment_podplayer, container, false);
        setRetainInstance(true);
        registerBecomingNoisyReceiver();
        return freshView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
            isMaximized = savedInstanceState.getBoolean("isMaximized", true);

        view = getView();

        if(view == null){
            return;
        }

        podPlayerLayout = (LinearLayout) view.findViewById(R.id.podplayer_fragment);

        if (pod != null) {
            if (mp != null) {
                viewPodPlayer();
                if (!playingState) PausePod();
            } else if (fromNotification) {
                final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int storedProgress = podkastStorage.getInt("recent_pod_progress", 0);
                playOrStreamPod(pod, true, storedProgress);
                final LinearLayout podPlayerLayout = (LinearLayout) view.findViewById(R.id.podplayer_fragment);
                final TextView podPlayerElapsed = (TextView) podPlayerLayout.findViewById(R.id.podplayer_elapsed);
                podPlayerElapsed.setText(getCleanTime(storedProgress));
                podPlayerProgress.setProgress(storedProgress);
                final ImageButton mainController = ((ImageButton) podPlayerLayout.findViewById(R.id.podplayer_maincontrol));
                mainController.setImageResource(R.drawable.ic_play_pod);
                mainController.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ContinuePod();
                    }
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        outState.putInt("podPlayerViewId", view.getId());
        if (mp != null) {
            this.playingState = mp.isPlaying();
        }

        outState.putBoolean("isMaximized", isMaximized);

    }


    // GETTERS & SETTERS

    RRPod getPod() {
        return pod;
    }

    void setPlayingState(boolean playingState) {
        this.playingState = playingState;
    }

    boolean getPlayingState() {
        return this.playingState;
    }


    // PLAYER NOTIFICATIONS


    NotificationManager mNotifyMgr;

    void sendNotification() {

        Intent onClickIntent = new Intent(getContext(), MainActivity.class);
        onClickIntent.putExtra("player_active", true);
        onClickIntent.putExtra("playing_state", playingState);
        onClickIntent.putExtra("playing_pod", pod);
        PendingIntent onClickPendingIntent = PendingIntent.getActivity(getContext(), 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String msg;
        if (pod.getDownloadState()) {
            msg = "Nedlastet RR-podkast";
        } else {
            msg = "Streamet RR-podkast";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setOngoing(false)
                        .setSmallIcon(R.mipmap.ic_launcher_neo)
                        .setContentTitle(pod.getTitle())
                        .setContentText(msg)
                        .setContentIntent(onClickPendingIntent);

        int mNotificationId = 1337;
        mNotifyMgr = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    // PLAYER MAIN FUNCTIONALITY

    void viewPodPlayer() {

        final String podName = pod.getTitle();

        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TextView podNameTextView = (TextView) podPlayerLayout.findViewById(R.id.podplayer_text);
        podNameTextView.setText(podName);

        podPlayerElapsed = (TextView) podPlayerLayout.findViewById(R.id.podplayer_elapsed);
        final TextView podPlayerDuration = (TextView) podPlayerLayout.findViewById(R.id.podplayer_duration);

        final ImageButton mainController = ((ImageButton) podPlayerLayout.findViewById(R.id.podplayer_maincontrol));
        final ImageButton skipForth = ((ImageButton) podPlayerLayout.findViewById(R.id.podplayer_skipforth));
        final ImageButton skipBack = ((ImageButton) podPlayerLayout.findViewById(R.id.podplayer_skipback));

        maximizeBtn = ((ImageButton) podPlayerLayout.findViewById(R.id.maximize_player));
        minimizeBtn = ((ImageButton) podPlayerLayout.findViewById(R.id.minimize_player));

        if (playingState) mainController.setImageResource(R.drawable.ic_pause_pod);
        else mainController.setImageResource(R.drawable.ic_play_pod);
        mainController.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PausePod();
            }
        });

        skipForth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newPos = mp.getCurrentPosition() + 10000;
                mp.seekTo(newPos);
                int progress = mp.getCurrentPosition();
                podPlayerElapsed.setText(getCleanTime(progress));
                podPlayerProgress.setProgress(progress);
            }
        });

        skipBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newPos = mp.getCurrentPosition() - 10000;
                if (newPos >= 0) {
                    mp.seekTo(newPos);
                } else {
                    mp.seekTo(0);
                }
                podPlayerElapsed.setText(getCleanTime(newPos));
                podPlayerProgress.setProgress(newPos);
            }
        });

        if (isMaximized) maximizePlayer();
        else minimizePlayer();

        maximizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maximizePlayer();
            }
        });

        minimizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizePlayer();
            }
        });

        podPlayerDuration.setText(getCleanTime(mp.getDuration()));

        podPlayerProgress = (SeekBar) podPlayerLayout.findViewById(R.id.progressbar);
        podPlayerProgress.setMax(mp.getDuration());

        podPlayerProgress.getProgressDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.myPodcasts), android.graphics.PorterDuff.Mode.SRC_IN);

        if (mp != null) {
            int progress = mp.getCurrentPosition();
            podPlayerElapsed.setText(getCleanTime(progress));
            podPlayerProgress.setProgress(progress);
            podkastStorage.edit().putString("recent_pod_name",podName).apply();
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mp != null && (mp.isPlaying() || playingState)) {
                                        podPlayerProgress.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                int progress = mp.getCurrentPosition();
                                                if (progress > mp.getDuration()*0.07 && !podkastStorage.getBoolean(podName + "(LT)", false)) {
                                                    podkastStorage.edit().putBoolean(podName + "(LT)", true).apply();
                                                }
                                                podPlayerElapsed.setText(getCleanTime(progress));
                                                podPlayerProgress.setProgress(progress);
                                                podkastStorage.edit().putInt("recent_pod_progress", progress).apply();
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
            }, 0, 100);

        }

        podPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                    if (progress > 60000 && !podkastStorage.getBoolean(podName + "(LT)", false)) {
                        podkastStorage.edit().putBoolean(podName + "(LT)", true).apply();
                    }
                    podPlayerElapsed.setText(getCleanTime(progress));
                    podPlayerProgress.setProgress(progress);
                    podkastStorage.edit().putInt("recent_pod_progress", progress).apply();
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

        podPlayerLayout.setVisibility(View.VISIBLE);
    }

    void nullifyMP(){
        mp = null;
    }

    void minimizePlayer() {
        isMaximized = false;
        minimizeBtn.setVisibility(View.GONE);
        maximizeBtn.setVisibility(View.VISIBLE);
        podPlayerLayout.findViewById(R.id.player_main_content).setVisibility(View.GONE);
        podPlayerLayout.findViewById(R.id.podplayer_text).setVisibility(View.GONE);
        podPlayerLayout.setBackgroundResource(R.color.transparent);
    }
    void maximizePlayer() {
        isMaximized = true;
        maximizeBtn.setVisibility(View.GONE);
        minimizeBtn.setVisibility(View.VISIBLE);
        podPlayerLayout.findViewById(R.id.player_main_content).setVisibility(View.VISIBLE);
        podPlayerLayout.findViewById(R.id.podplayer_text).setVisibility(View.VISIBLE);
        podPlayerLayout.setBackgroundResource(R.color.podplayer_bg);
    }

    public void playOrStreamPod(RRPod pod) {
        playOrStreamPod(pod,false,0);
    }

    public void playOrStreamPod(RRPod pod, boolean onlyPrepare, int msec) {
        if (pod.getDownloadState()) playPod(pod, onlyPrepare, msec);
        else streamPod(pod, onlyPrepare, msec);
    }



    public void playPod(RRPod _pod, boolean onlyPrepare, int msec) {

        if (pod == _pod && !onlyPrepare) {
            return;
        }

        if (mp != null) {
            ConfirmChangePod(_pod);
            return;
        }

        pod = _pod;

        mp = new MediaPlayer();

        final File dir = new File(getContext().getFilesDir(),"RR-Podkaster");

        Uri fileRealUri = Uri.fromFile(new File(dir + File.separator + pod.getTitle()));

        try {
            mp.setDataSource(getContext(), fileRealUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!onlyPrepare){
            mp.start();
            mp.seekTo(msec);
        }

        activateWakeLock();

        playingState = true;
        viewPodPlayer();
    }

    public void streamPod(final RRPod _pod, final boolean onlyPrepare, final int msec) {

        Log.i(TAG,"Streaming pod: " + pod);

        if (pod == _pod && !onlyPrepare) {
            return;
        }

        if (mp != null) {
            ConfirmChangePod(_pod);
            return;
        }

        pod = _pod;

        mp = new MediaPlayer();

        try {
            mp.setDataSource(pod.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.start();
        mp.seekTo(msec);

        if (onlyPrepare){
            PausePod();
            ImageButton mainController = (ImageButton) view.findViewById(R.id.podplayer_maincontrol);
            mainController.setImageResource(R.drawable.ic_play_pod);
            mainController.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ContinuePod();
                }
            });

            disableWifiLock();

            playingState = false;
        }else{
            activateWakeLock();
            activateWifiLock();

            playingState = true;
        }

        viewPodPlayer();
    }

    public void ConfirmChangePod(final RRPod pod) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle("Er du sikker?");
        builder.setMessage("Er du sikker på at du vil stoppe spillende podkast for å starte denne?");
        builder.setPositiveButton(("Ja"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        StopPod();
                        playOrStreamPod(pod);
                    }
                });
        builder.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void StopPod() {
        mp.stop();
        mp.reset();
        mp.release();
        mp = null;
        playingState = false;

        disableWifiLock();
    }

    private void PausePod() {
        try {
            mp.pause();

            ImageButton mainController = (ImageButton) view.findViewById(R.id.podplayer_maincontrol);
            mainController.setImageResource(R.drawable.ic_play_pod);
            mainController.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ContinuePod();
                }
            });

            disableWifiLock();

            playingState = false;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void ContinuePod() {
        try {
            mp.start();
            ImageButton mainController = (ImageButton) view.findViewById(R.id.podplayer_maincontrol);
            mainController.setImageResource(R.drawable.ic_pause_pod);
            mainController.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PausePod();
                }
            });

            // PREVENT SYSTEM FROM STOPPING PLAYBACK WHEN DEVICE IS IDLE
            mp.setWakeMode(getContext().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // PREVENT SYSTEM FROM DISABLING WIFI WHEN DEVICE IS IDLE
            activateWifiLock();

            playingState = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void activateWakeLock(){
        // PREVENT SYSTEM FROM STOPPING PLAYBACK WHEN DEVICE IS IDLE
        mp.setWakeMode(getContext().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void activateWifiLock(){

        // PREVENT SYSTEM FROM DISABLING WIFI WHEN DEVICE IS IDLE
        wifiLock = ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();
    }

    private void disableWifiLock(){
        if(wifiLock == null || !wifiLock.isHeld()){
            return;
        }
        wifiLock.release();
    }



    // "BECOMING NOISY" RECEIVER
    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilterNoisy = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PausePod();
            }
        }, intentFilterNoisy);
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
}
