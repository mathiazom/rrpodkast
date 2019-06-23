package com.rrpm.mzom.projectrrpm.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.notifications.NotificationUtils;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloadsViewModel;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalError;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalCallback;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayer;
import com.rrpm.mzom.projectrrpm.podstorage.ConnectionValidator;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawer;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerConstants;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsManager;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerControls;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawerItem;
import com.rrpm.mzom.projectrrpm.podplayer.PlayerPodViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static com.rrpm.mzom.projectrrpm.podfeed.PodsFeedConstants.RR_FACEBOOK_PAGE_URL;

public class MainActivity extends AppCompatActivity

        implements
        PodPlayerControls {


    // TODO: Enable PodPlayer controlling from headset

    // TODO: Implement notification when downloading

    // TODO: Implement player notification


    private static final String TAG = "RRP-MainActivity";

    private PodsViewModel podsViewModel;

    private PodPlayerControls podPlayerControls;

    private PodDownloader podDownloader;

    private MainFragmentsHandler mainFragmentsHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        PermissionsManager.retrieveAllPermissions(this);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            prepareNotifications();

        }


        setContentView(R.layout.activity_main);


        podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);

        podDownloader = new PodDownloader(this);
        podDownloader.downloadFromQueue();


        mainFragmentsHandler = new MainFragmentsHandler(this, podDownloader);

        podPlayerControls = new PodPlayer(this);


        ConnectionValidator.attemptToRegisterConnectionListener(this, isConnected -> {

            runOnUiThread(() -> {

                if(!isConnected){

                    podPlayerControls.pausePod();

                }

            });

        });


        initNavigationDrawer();


        loadDefaultOrLastPlayedPodState(savedInstanceState);


    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {

        super.onNewIntent(intent);

        handleIntent(intent);

    }


    private void handleIntent(@NonNull Intent intent){

        // Check if intent is from a notification click
        final String clickedNotificationChannelId = intent.getStringExtra(NotificationConstants.INTENT_CLICKED_NOTIFICATION_ID_EXTRA_NAME);

        if(clickedNotificationChannelId == null){

            // Intent was not caused by a click on any known notification

            return;

        }

        // Handle intent based on the channel id of the clicked notification
        switch (clickedNotificationChannelId){

            case NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID:

                final ArrayList<RRPod> downloadQueue = new PodStorageHandle(this).getStoredPodDownloadQueue();

                Assertions._assert(downloadQueue != null, "Download queue was null after downloading notification click");

                final RRPod currentlyDownloadingPod = downloadQueue.get(0);

                Assertions._assert(currentlyDownloadingPod != null, "Currently downloading pod was null");

                mainFragmentsHandler.loadPodFragment(currentlyDownloadingPod);

                break;

            case NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_ID:

                // TODO: Implement a more robust approach for when Activity is completely destroyed and player view model is lost

                mainFragmentsHandler.loadPodPlayerFragment();

                break;

        }


    }


    /**
     *
     * If last played pod is available, load last played pod list and load playback of last played pod.
     * If not, or this process fails, load the default pod list and no playback.
     *
     * @param savedInstanceState: Holds info about playback of last played pod, if such playback has ever existed.
     *
     */

    private void loadDefaultOrLastPlayedPodState(@Nullable Bundle savedInstanceState){

        // Get handle to access pod storage
        final PodStorageHandle podStorageHandle = new PodStorageHandle(this);

        final PodType lastPlayedPodType = podStorageHandle.getLastPlayedPodType();
        final PodId lastPlayedPodId = podStorageHandle.getLastPlayedPodId();


        if (lastPlayedPodType == null || lastPlayedPodId == null) {

            Log.i(TAG,"No last played pod available, displaying default pod list");

            requestAndDisplayDefaultPodList();

            return;

        }

        requestAndDisplayLastPlayedPodList(lastPlayedPodType, lastPlayedPodId, savedInstanceState);

    }


    /**
     *
     * Requests and displays pod list of {@link PodUIConstants#DEFAULT_POD_TYPE}
     *
     */

    private void requestAndDisplayDefaultPodList() {

        // Request retrieval of pod list of the default pod type
        podsViewModel.requestPodListRetrieval(PodUIConstants.DEFAULT_POD_TYPE, new PodsRetrievalCallback.PodListRetrievalCallback() {
            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {
                // No further actions required, the pod list observers will take it from here.
            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {
                Log.e(TAG,"Failed retrieval of pod list of default pod type: " + error.getMessage());
            }
        });

        // Load pod list fragment that observes the pod list of default pod type
        mainFragmentsHandler.loadPodListFragment(PodUIConstants.DEFAULT_POD_TYPE);

    }


    private void requestAndDisplayLastPlayedPodList(@NonNull PodType lastPlayedPodType, @NonNull PodId lastPlayedPodId, @Nullable Bundle savedInstanceState){

        // Load a pod list fragment with the last played pod type
        mainFragmentsHandler.loadPodListFragment(lastPlayedPodType);

        // Request pod list of last played pod type
        podsViewModel.requestPodListRetrieval(lastPlayedPodType, new PodsRetrievalCallback.PodListRetrievalCallback() {

            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                // Restore usable RRPod representation of last played pod
                final RRPod lastPlayedPod = PodUtils.getPodFromId(lastPlayedPodId,retrievedPodList);

                Assertions._assert(lastPlayedPod != null, "Last played pod id could not be found in retrieved pod list");

                if(lastPlayedPod == null){

                    return;

                }

                // Check if pod was playing before activity was destroyed
                final boolean wasPlaying = savedInstanceState != null && savedInstanceState.getBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG);

                // Attempt to load pod player with the last played pod, and store attempt result (successful or failed)
                final boolean loaded = loadPod(lastPlayedPod);

                if(loaded && wasPlaying){

                    // Commence playback of last played pod
                    continuePod();

                }

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                Log.e(TAG,"Failed retrieval of pod list of last played pod type: " + error.getMessage());

                // If retrieval of last played pod list fails, attempt to display the default pod list
                requestAndDisplayDefaultPodList();

            }

        });

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        // PodPlayer playing state
        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(this).get(PlayerPodViewModel.class);
        outState.putBoolean(PodPlayerConstants.SAVED_INSTANCE_STATE_WAS_PLAYING_TAG, playerPodViewModel.isPlaying());

    }


    private void initNavigationDrawer() {

        ((NavigationDrawer) findViewById(R.id.navigation_drawer_layout))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_pod_list), R.drawable.ic_round_list_24px, () -> {

                    podsViewModel.requestPodListRetrieval(PodType.MAIN_PODS, new PodsRetrievalCallback.PodListRetrievalCallback() {
                        @Override
                        public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {
                            // No further actions needed, pod list observers (e.g. the pod list fragment) will take it from here.
                        }

                        @Override
                        public void onFail(@NonNull PodsRetrievalError error) {
                            Log.e(TAG,"Failed retrieval of " + PodType.MAIN_PODS.name() + ": " + error.getMessage());
                        }
                    });

                    mainFragmentsHandler.loadPodListFragment(PodType.MAIN_PODS);

                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_highlights), R.drawable.ic_round_priority_high_24px, () -> {

                    podsViewModel.requestPodListRetrieval(PodType.ARCHIVE_PODS, new PodsRetrievalCallback.PodListRetrievalCallback() {
                        @Override
                        public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {
                            // No further actions needed, pod list observers (e.g. the pod list fragment) will take it from here.
                        }

                        @Override
                        public void onFail(@NonNull PodsRetrievalError error) {
                            Log.e(TAG,"Failed retrieval of " + PodType.ARCHIVE_PODS.name() + ": " + error.getMessage());
                        }
                    });

                    mainFragmentsHandler.loadPodListFragment(PodType.ARCHIVE_PODS);

                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_random_pod), R.drawable.ic_round_shuffle_24px, () -> {

                    // TODO: Create own RandomPodFragment

                    final ArrayList<RRPod> pods = podsViewModel.getPodList(PodType.MAIN_PODS);

                    if(pods == null){

                        Log.i(TAG,"Pod list was empty, will not load a random pod");

                        return;

                    }

                    final RRPod randomPod = pods.get((int) Math.floor(Math.random() * pods.size()));
                    mainFragmentsHandler.loadPodFragment(randomPod);

                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_facebook), R.drawable.ic_round_public_24px, () ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RR_FACEBOOK_PAGE_URL)))))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_settings), R.drawable.ic_round_settings_24px, () ->
                        startActivity(new Intent(this, SettingsActivity.class))))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_about), R.drawable.ic_round_info_24px, () -> {
                }))

                .initialize();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void prepareNotifications() {

        NotificationUtils.buildAndRegisterChannelGroups(this,
                NotificationConstants.DOWNLOAD_NOTIFICATIONS_GROUP_BUILDER,
                NotificationConstants.PLAYBACK_NOTIFICATIONS_GROUP_BUILDER
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

        // Close navigation drawer if open
        if (((NavigationDrawer) findViewById(R.id.navigation_drawer_layout)).close()) {

            return;

        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final List<Fragment> fragments = fragmentManager.getFragments();

        final Fragment currentFragment = fragments.get(fragments.size() - 1);

        if (currentFragment.isHidden()) {

            fragmentManager.popBackStackImmediate();

            onBackPressed();

            return;

        }

        if (fragmentManager.getBackStackEntryCount() == 0) {

            // Finish activity if back stack is empty of fragments on back button press
            finish();

        }


        super.onBackPressed();

    }


    @Override
    public boolean loadPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadSmallPodPlayerFragment();

        return podPlayerControls.loadPod(pod);
    }

    @Override
    public void playPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadSmallPodPlayerFragment();

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

