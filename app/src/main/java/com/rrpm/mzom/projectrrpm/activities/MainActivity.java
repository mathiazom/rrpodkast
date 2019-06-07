package com.rrpm.mzom.projectrrpm.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.notifications.NotificationUtils;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerFresh;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawer;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerConstants;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsManager;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayer;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerControls;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.rss.RRReader;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawerItem;
import com.rrpm.mzom.projectrrpm.podstorage.ConnectionValidator;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podplayer.PlayerPodViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import static com.rrpm.mzom.projectrrpm.rss.PodFeedConstants.RR_FACEBOOK_PAGE_URL;

public class MainActivity extends AppCompatActivity

        implements
        PodPlayerControls

{


    // TODO: Enable PodPlayer controlling from headset

    // TODO: Implement notification when downloading


    private static final String TAG = "RRP-MainActivity";

    private PodsViewModel podsViewModel;

    private PodPlayerControls podPlayerControls;

    private PodDownloader podDownloader;

    private MainFragmentsHandler mainFragmentsHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PermissionsManager.retrieveAllPermissions(this);

        setContentView(R.layout.activity_main);

        podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);

        this.mainFragmentsHandler = new MainFragmentsHandler(this, podDownloader);

        if(podPlayerControls == null){
            podPlayerControls = new PodPlayerFresh(this);
        }

        podDownloader = restorePodDownloader(savedInstanceState);
        podDownloader.downloadFromQueue();

        initNavigationDrawer();

        mainFragmentsHandler.loadLastPlayedPodListFragment();

        loadLastPlayedPodWhenReady(savedInstanceState);


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareNotifications();
        }

    }


    private void loadLastPlayedPodWhenReady(@Nullable Bundle savedInstanceState){

        final RRPod unlistedLastPlayedPod = new PodStorageHandle(this).getLastPlayedPod();

        if(unlistedLastPlayedPod == null){

            Log.i(TAG,"No last played pod found.");

            return;
        }

        final RRReader.PodType lastPlayedPodType = unlistedLastPlayedPod.getPodType();

        final PodsViewModel.PodsPackageRequest podsPackageRequest = new PodsViewModel.PodsPackageRequest(
                //RRReader.PodType.values(), // Request all pod types
                new RRReader.PodType[]{lastPlayedPodType}, // Request only minimal requirement
                podsPackage -> {

                    final LiveData<ArrayList<RRPod>> livePodList = podsPackage.getObservablePodList(lastPlayedPodType);
                    if(livePodList == null){
                        throw new RuntimeException("Pod list of last played pod not available");
                    }

                    final RRPod lastPlayedPod = PodUtils.getEqualPod(unlistedLastPlayedPod, livePodList.getValue());
                    if (lastPlayedPod == null) {
                        throw new RuntimeException("Last played pod was null");
                    }

                    if(!ConnectionValidator.isConnected(this) && !lastPlayedPod.isDownloaded()){
                        Log.i(TAG,"Last played pod is not downloaded, so, since the device is offline, no pod will be loaded at this moment");
                        return;
                    }

                    boolean loaded = loadPod(lastPlayedPod);

                    if (loaded && savedInstanceState != null && savedInstanceState.getBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG)) {
                        continuePod();
                    }

                }
        );

        podsViewModel.requestPodsPackage(podsPackageRequest);

    }


    @NonNull
    private PodDownloader restorePodDownloader(@Nullable final Bundle savedInstanceState){

        final PodDownloader podDownloader = new PodDownloader(this);

        if(savedInstanceState == null){
            return podDownloader;
        }


        final String downloadQueueJson = savedInstanceState.getString("downloadQueue", null);
        final ArrayList<RRPod> downloadQueue = new Gson().fromJson(downloadQueueJson, new TypeToken<ArrayList<RRPod>>() {}.getType());

        if (downloadQueue != null) {
            podDownloader.setDownloadQueue(downloadQueue);
        }

        return podDownloader;

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        // PodPlayer playing state
        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(this).get(PlayerPodViewModel.class);

        Log.i(TAG,"Saved instance, isPlaying: " + playerPodViewModel.isPlaying());

        outState.putBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG, playerPodViewModel.isPlaying());

    }


    private void initNavigationDrawer() {

        ((NavigationDrawer) findViewById(R.id.navigation_drawer_layout))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_pod_list), R.drawable.ic_round_list_24px, () ->
                        mainFragmentsHandler.loadPodListFragment(RRReader.PodType.MAIN_PODS)))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_highlights), R.drawable.ic_round_priority_high_24px, () ->
                        mainFragmentsHandler.loadPodListFragment(RRReader.PodType.ARCHIVE_PODS)))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_random_pod), R.drawable.ic_round_shuffle_24px, () -> {
                    // TODO: Create own RandomPodFragment
                    final ArrayList<RRPod> pods = podsViewModel.getPodList(RRReader.PodType.MAIN_PODS);
                    final RRPod randomPod = pods.get((int)Math.floor(Math.random()*pods.size()));
                    mainFragmentsHandler.loadPodFragment(randomPod);
                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_facebook), R.drawable.ic_round_public_24px, () ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RR_FACEBOOK_PAGE_URL)))))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_settings), R.drawable.ic_round_settings_24px, () ->
                        startActivity(new Intent(this, SettingsActivity.class))))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_about), R.drawable.ic_round_info_24px, () -> {}))

        .initialize();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void prepareNotifications(){

        NotificationUtils.buildAndRegisterChannelGroups(this,
                NotificationConstants.DOWNLOAD_NOTIFICATIONS_GROUP_BUILDER
        );

        NotificationUtils.buildAndRegisterChannels(this,
                NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_BUILDER,
                NotificationConstants.COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_BUILDER
        );

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (PermissionsManager.allRequestPermissionsGranted(requestCode, grantResults)) {

            podDownloader.onPermissionsGranted();

        }

    }


    @Override
    public void onBackPressed() {

        Log.i(TAG,"Back stack pre back press: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));

        // Close navigation drawer if open
        if(((NavigationDrawer) findViewById(R.id.navigation_drawer_layout)).close()){
            return;
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final List<Fragment> fragments = fragmentManager.getFragments();

        final Fragment currentFragment = fragments.get(fragments.size()-1);

        if(currentFragment.isHidden()){
            fragmentManager.popBackStackImmediate();
            onBackPressed();
            return;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {

            // Finish activity if back stack is empty of fragments on back button press
            finish();

        }


        super.onBackPressed();

        Log.i(TAG,"Back stack post back press: " + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));

    }


    @Override
    public boolean loadPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadPodPlayerFragment();

        return podPlayerControls.loadPod(pod);
    }

    @Override
    public void playPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadPodPlayerFragment();

        podPlayerControls.playPod(pod);
    }

    @Override
    public void pauseOrContinuePod() {
        podPlayerControls.pauseOrContinuePod();
    }

    @Override
    public void pausePod() {
        podPlayerControls.pausePod();
    }

    @Override
    public void continuePod() {
        podPlayerControls.continuePod();
    }

    @Override
    public void jump(int jump) {
        podPlayerControls.jump(jump);
    }

    @Override
    public void seekTo(int progress) {
        podPlayerControls.seekTo(progress);
    }

}

