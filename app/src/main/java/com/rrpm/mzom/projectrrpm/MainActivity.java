package com.rrpm.mzom.projectrrpm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static com.rrpm.mzom.projectrrpm.PodFeedConstants.RR_FACEBOOK_PAGE_URL;

public class MainActivity extends AppCompatActivity

        implements
        FragmentLoadingHandler,
        PodPlayerControls,
        PodPlayer.OnPodPlayerCompletionListener
{


    // TODO: Enable PodPlayer controlling from headset

    // TODO: Implement notification when downloading


    private static final String TAG = "RRP-MainActivity";


    private ArrayList<RRPod> pods;

    @SuppressWarnings("NullableProblems")
    @NonNull
    private FragmentLoader fragmentLoader = new FragmentLoader(getSupportFragmentManager());

    private PodPlayer podPlayer;

    private PodDownloader podDownloader;

    private PodPlayerFragment podPlayerFragment;
    private PodsFragment podsFragment;
    private PodFragment podFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PermissionsManager.retrieveAllPermissions(this);

        setContentView(R.layout.activity_main);

        initNavigationDrawer();

        if(podPlayer == null){
            podPlayer = new PodPlayer(this,this);
        }

        // Load fragment displaying podcast episodes
        if(podsFragment == null){
            podsFragment = PodsFragment.newInstance();
        }
        fragmentLoader.loadFragment(R.id.frame_main, podsFragment,true);

        if(savedInstanceState != null){

            podDownloader = restorePodDownloader(savedInstanceState);
            podDownloader.downloadFromQueue();

        }else{

            podDownloader = new PodDownloader(this);

        }

        final PodsViewModel podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);
        podsViewModel.retrievePods(RRReader.PodType.MAIN_PODS).observe(this, retrievedPods -> {

            this.pods = retrievedPods;

            final RRPod lastPlayedPod = PodUtils.getPodFromId(new PodStorageHandle(MainActivity.this).getLastPlayedPodId(),pods);

            if (lastPlayedPod != null) {

                Log.i(TAG,"Last played pod: " + lastPlayedPod.getTitle());

                boolean loaded = loadPod(lastPlayedPod, MainActivity.this);

                if (loaded && savedInstanceState != null && savedInstanceState.getBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG)) {
                    continuePod(MainActivity.this);
                }

                podPlayerFragment = (PodPlayerFragment) getSupportFragmentManager().findFragmentByTag(PodPlayerFragment.class.getSimpleName());
                if (podPlayerFragment == null) {
                    podPlayerFragment = PodPlayerFragment.newInstance();
                }

                fragmentLoader.loadFragment(R.id.frame_podplayer, podPlayerFragment, false);

            }

        });

    }

    @NonNull
    private PodDownloader restorePodDownloader(@NonNull final Bundle savedInstanceState){

        final PodDownloader podDownloader = new PodDownloader(this);

        final String downloadQueueJson = savedInstanceState.getString("downloadQueue", null);
        final ArrayList<RRPod> downloadQueue = new Gson().fromJson(downloadQueueJson, new TypeToken<ArrayList<RRPod>>() {}.getType());

        if (downloadQueue != null) {
            podDownloader.setDownloadQueue(downloadQueue);
        }

        return podDownloader;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        // PodPlayer playing state
        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(this).get(PlayerPodViewModel.class);

        Log.i(TAG,"Saved instance, isPlaying: " + playerPodViewModel.isPlaying());

        outState.putBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG, playerPodViewModel.isPlaying());

    }


    private void initNavigationDrawer() {

        ((NavigationDrawer) findViewById(R.id.navigation_drawer_layout))
                .addItem(getString(R.string.list_drawer_item_pod_list), R.drawable.ic_pod_list, () -> {

                })
                .addItem(getString(R.string.list_drawer_item_highlights), R.drawable.ic_highlights, () -> {

                })
                .addItem(getString(R.string.list_drawer_item_random_pod), R.drawable.ic_shuffle, () -> {
                })
                .addItem(getString(R.string.list_drawer_item_facebook), R.drawable.ic_webpage, () -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RR_FACEBOOK_PAGE_URL))))
                .addItem(getString(R.string.list_drawer_item_settings), R.drawable.ic_settings, () -> {
                })
                .addItem(getString(R.string.list_drawer_item_about), R.drawable.ic_about_app, () -> {
                })
                .initDrawer();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (PermissionsManager.allRequestPermissionsGranted(requestCode, grantResults)) {

            // Continue download
            podDownloader.downloadFromQueue();

        }

    }

    @Override
    public void loadPodFragment() {

        if (podFragment != null && podFragment.isAdded()) {

            Log.i(TAG, "PodFragment already loaded");

            return;
        }

        podFragment = PodFragment.newInstance(podDownloader);

        fragmentLoader.loadFragment(
                R.id.frame_main,
                podFragment,
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right,
                true);

    }


    @Override
    public void onPodPlayerCompletion(@NonNull final RRPod completedPod, @NonNull final PodPlayer podPlayer) {

        final int completedIndex = PodUtils.getEqualPodIndex(completedPod,pods);

        if(completedIndex == -1){
            throw new RuntimeException("Completed pod index was not found");
        }

        if(completedIndex > 0){
            final RRPod nextPod = pods.get(completedIndex - 1);
            podPlayer.playPod(nextPod, this);
        }

        Log.i(TAG,"No pod found after completed pod, no further playback");

    }

    @Override
    public void onBackPressed() {

        if(podsFragment != null && podsFragment.isAdded()){

            if(podsFragment.onBackPress()){
                return;
            }

        }

        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {

            // Finish activity if back stack is empty of fragments on back button press
            finish();

        }

        super.onBackPressed();

    }

    @Override
    public boolean loadPod(@NonNull RRPod pod, @NonNull Context context) {
        return podPlayer.loadPod(pod,context);
    }

    @Override
    public void playPod(@NonNull RRPod pod, @NonNull Context context) {
        podPlayer.playPod(pod,context);
    }

    @Override
    public void pauseOrContinuePod(@NonNull Context context) {
        podPlayer.pauseOrContinuePod(context);
    }

    @Override
    public void pausePod() {
        podPlayer.pausePod();
    }

    @Override
    public void continuePod(@NonNull Context context) {
        podPlayer.continuePod(context);
    }

    @Override
    public void jump(int jump) {
        podPlayer.jump(jump);
    }

    @Override
    public void seekTo(int progress) {
        podPlayer.seekTo(progress);
    }

}

