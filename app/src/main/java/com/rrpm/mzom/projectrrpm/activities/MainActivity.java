package com.rrpm.mzom.projectrrpm.activities;

import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandler;
import com.rrpm.mzom.projectrrpm.fragments.MainFragmentsHandlerViewModel;
import com.rrpm.mzom.projectrrpm.notifications.NotificationUtils;
import com.rrpm.mzom.projectrrpm.notifications.NotificationConstants;
import com.rrpm.mzom.projectrrpm.notifications.PodPlayerNotificationBuilder;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloaderRetriever;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalError;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalCallback;
import com.rrpm.mzom.projectrrpm.podplayer.MediaControllerViewModel;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerConstants;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerService;
import com.rrpm.mzom.projectrrpm.podstorage.ConnectionValidator;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawer;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.permissions.PermissionsManager;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerControls;
import com.rrpm.mzom.projectrrpm.ui.NavigationDrawerItem;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static com.rrpm.mzom.projectrrpm.podfeed.PodsFeedConstants.RR_FACEBOOK_PAGE_URL;

public class MainActivity extends AppCompatActivity

        implements
        PodPlayerControls,
        PodDownloaderRetriever {


    // TODO: Enable PodPlayer controlling from headset

    // TODO: Implement player notification


    private static final String TAG = "RRP-MainActivity";


    private PodsViewModel podsViewModel;

    private PodPlayerViewModel podPlayerViewModel;


    private PodDownloader podDownloader;

    private MainFragmentsHandlerViewModel mainFragmentsHandlerViewModel;

    private MainFragmentsHandler mainFragmentsHandler;


    private MediaControllerCompat mMediaController;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Make sure fragments handle is available
        mainFragmentsHandlerViewModel = ViewModelProviders.of(this).get(MainFragmentsHandlerViewModel.class);
        mainFragmentsHandler = mainFragmentsHandlerViewModel.restoreHandleFromActivity(this);


        podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);


        podDownloader = new PodDownloader(this);
        podDownloader.downloadFromQueue();


        podPlayerViewModel = ViewModelProviders.of(this).get(PodPlayerViewModel.class);


        initNavigationDrawer();


        // Update visibility of connection status message
        final ConstraintLayout connectionStatusContainer = findViewById(R.id.connectionStatusContainer);
        connectionStatusContainer.setVisibility(ConnectionValidator.isConnected(this) ? View.GONE : View.VISIBLE);

        ConnectionValidator.attemptToRegisterConnectionListener(this, isConnected -> {

            runOnUiThread(() -> {

                if (!isConnected && podPlayerViewModel.getPlayerPod() != null && !podPlayerViewModel.getPlayerPod().isDownloaded()) {

                    pausePod();

                } else {

                    final PodType podListPodType = mainFragmentsHandler.getPodListPodType();

                    if (podListPodType == null) {

                        Log.e(TAG, "Pod list pod type was null");

                        return;

                    }

                    podsViewModel.requestPodListRetrievalIfNeeded(podListPodType);

                }

                // Update visibility of connection status message
                connectionStatusContainer.setVisibility(isConnected ? View.GONE : View.VISIBLE);

            });

        });


        // Check if activity is being recreated
        final boolean isRecreating = savedInstanceState != null;

        final MediaControllerViewModel mediaControllerViewModel = ViewModelProviders.of(this).get(MediaControllerViewModel.class);
        mediaControllerViewModel.requestMediaController(this, new MediaControllerCompat.Callback() {

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {


            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {

                podPlayerViewModel.setIsPlaying(state.getState() != PlaybackStateCompat.STATE_PAUSED);

            }

        }, mediaController -> {

            this.mMediaController = mediaController;

            if (!isRecreating) {

                registerCompletionListener();

                registerPlaybackIterators();

                // Finish building the UI
                loadDefaultOrLastPlayedPodState();

            }

        });


        if (!isRecreating) {

            PermissionsManager.retrieveAllPermissions(this);

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                buildNotificationGroupsAndChannels();

            }


        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make sure fragments handle is available
        mainFragmentsHandlerViewModel = ViewModelProviders.of(this).get(MainFragmentsHandlerViewModel.class);
        mainFragmentsHandler = mainFragmentsHandlerViewModel.restoreHandleFromActivity(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mainFragmentsHandlerViewModel.saveAndInvalidateHandle();
    }

    private void registerCompletionListener(){

        mMediaController.sendCommand(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REGISTER_COMPLETION_LISTENER,null,new ResultReceiver(new Handler()){

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                Log.i(TAG, "PodPlayer playback completed");

                final RRPod playerPod = podPlayerViewModel.getPlayerPod();

                final ArrayList<RRPod> podList = podsViewModel.getPodList(playerPod.getPodType());

                Assertions._assert(podList != null, "Pod list associated with completed pod was null");

                final int completedIndex = podList.indexOf(playerPod);

                Assertions._assert(completedIndex != -1, "Completed pod index was not found");

                if(completedIndex == 0){

                    // No pod found after completed pod, this is the end of the pod list.

                    pausePod();

                    return;

                }

                playPod(podList.get(completedIndex - 1));


            }
        });

    }

    private void registerPlaybackIterators() {

        final Bundle viewModelIteratorBundle = new Bundle();
        viewModelIteratorBundle.putLong(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_ITERATOR_PERIOD, PodStorageConstants.PROGRESS_REFRESH_FREQ_MS);
        mMediaController.sendCommand(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REGISTER_ITERATOR, viewModelIteratorBundle, new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                final int progress = (int) mMediaController.getPlaybackState().getPosition();

                podPlayerViewModel.postPlayerProgress(progress);

            }
        });

        final Bundle podStorageIteratorBundle = new Bundle();
        podStorageIteratorBundle.putLong(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_ITERATOR_PERIOD, PodStorageConstants.SAVE_PROGRESS_FREQ_MS);
        mMediaController.sendCommand(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_REGISTER_ITERATOR, podStorageIteratorBundle, new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                final RRPod playerPod = podPlayerViewModel.getPlayerPod();

                if (playerPod == null) {

                    return;

                }

                final int progress = (int) mMediaController.getPlaybackState().getPosition();

                playerPod.setProgress(progress);

                podsViewModel.updatePodInStorage(playerPod, true);

            }
        });


    }


    @Override
    protected void onNewIntent(@NonNull Intent intent) {

        super.onNewIntent(intent);

        handleNotificationClickIntent(intent);

    }


    private void handleNotificationClickIntent(@NonNull Intent intent) {

        // Check if intent is from a notification click
        final String clickedNotificationChannelId = intent.getStringExtra(NotificationConstants.INTENT_CLICKED_NOTIFICATION_ID_EXTRA_NAME);

        if (clickedNotificationChannelId == null) {

            // Intent was not caused by a click on any known notification

            return;

        }

        // Handle intent based on the channel id of the clicked notification
        switch (clickedNotificationChannelId) {

            case NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_ID:

                // Retrieve stored download queue
                final ArrayList<RRPod> downloadQueue = new PodStorageHandle(this).getStoredPodDownloadQueue();

                Assertions._assert(downloadQueue != null, "Download queue was null after downloading notification click");

                // Retrieve the currently downloading pod
                final RRPod currentlyDownloadingPod = downloadQueue.get(0);

                Assertions._assert(currentlyDownloadingPod != null, "Currently downloading pod was null");

                // Display the currently downloading pod
                mainFragmentsHandler.loadPodFragment(currentlyDownloadingPod, null);

                break;

            case NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_ID:

                // TODO: Implement a more robust approach for when Activity is completely destroyed and player view model is lost

                mainFragmentsHandler.loadPodPlayerFragment();

                break;

        }


    }


    /**
     * If last played pod is available, load last played pod list and load playback of last played pod.
     * If not, or this process fails, load the default pod list and no playback.
     */

    private void loadDefaultOrLastPlayedPodState() {

        // Get handle to access pod storage
        final PodStorageHandle podStorageHandle = new PodStorageHandle(this);

        final PodType lastPlayedPodType = podStorageHandle.getLastPlayedPodType();
        final PodId lastPlayedPodId = podStorageHandle.getLastPlayedPodId();


        if (lastPlayedPodType == null || lastPlayedPodId == null) {

            Log.i(TAG, "No last played pod available, displaying default pod list");

            requestAndDisplayDefaultPodList();

            return;

        }

        requestAndDisplayLastPlayedPodList(lastPlayedPodType, lastPlayedPodId);

    }


    /**
     * Requests and displays pod list of {@link PodUIConstants#DEFAULT_POD_TYPE}
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
                Log.e(TAG, "Failed retrieval of pod list of default pod type: " + error.getMessage());
            }
        });

        // Load pod list fragment that observes the pod list of default pod type
        mainFragmentsHandler.loadPodListFragment(PodUIConstants.DEFAULT_POD_TYPE);

    }


    private void requestAndDisplayLastPlayedPodList(@NonNull PodType lastPlayedPodType, @NonNull PodId lastPlayedPodId) {

        // Load a pod list fragment with the last played pod type
        mainFragmentsHandler.loadPodListFragment(lastPlayedPodType);

        // Request pod list of last played pod type
        podsViewModel.requestPodListRetrieval(lastPlayedPodType, new PodsRetrievalCallback.PodListRetrievalCallback() {

            @Override
            public void onPodListRetrieved(@NonNull ArrayList<RRPod> retrievedPodList) {

                // Restore usable RRPod representation of last played pod
                final RRPod lastPlayedPod = PodUtils.getPodFromId(lastPlayedPodId, retrievedPodList);

                Assertions._assert(lastPlayedPod != null, "Last played pod id could not be found in retrieved pod list");

                if (lastPlayedPod == null) {

                    return;

                }

                if(mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){

                    mainFragmentsHandler.loadSmallPodPlayerFragment();

                    Log.i(TAG,"Player progress: " + MillisFormatter.toFormat((int)mMediaController.getPlaybackState().getPosition(), MillisFormatter.MillisFormat.HH_MM_SS));

                    podPlayerViewModel.setPlayerPod(lastPlayedPod);
                    podPlayerViewModel.setPlayerDuration(lastPlayedPod.getDuration());

                    return;

                }


                // Attempt to load pod player with the last played pod
                loadPod(lastPlayedPod);

            }

            @Override
            public void onFail(@NonNull PodsRetrievalError error) {

                Log.e(TAG, "Failed retrieval of pod list of last played pod type: " + error.getMessage());

                // If retrieval of last played pod list fails, attempt to display the default pod list
                requestAndDisplayDefaultPodList();

            }

        });

    }


    private void initNavigationDrawer() {

        ((NavigationDrawer) findViewById(R.id.navigation_drawer_layout))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_pod_list), R.drawable.ic_round_list_24px, () -> {

                    mainFragmentsHandler.hideOverlayFragments();

                    podsViewModel.requestPodListRetrievalIfNeeded(PodType.MAIN_PODS);

                    mainFragmentsHandler.loadPodListFragment(PodType.MAIN_PODS);

                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_highlights), R.drawable.ic_round_priority_high_24px, () -> {

                    mainFragmentsHandler.hideOverlayFragments();

                    podsViewModel.requestPodListRetrievalIfNeeded(PodType.ARCHIVE_PODS);

                    mainFragmentsHandler.loadPodListFragment(PodType.ARCHIVE_PODS);

                }))

                .addItem(new NavigationDrawerItem(getString(R.string.list_drawer_item_random_pod), R.drawable.ic_round_shuffle_24px, () -> {

                    mainFragmentsHandler.hideOverlayFragments();

                    // TODO: Create own RandomPodFragment

                    final ArrayList<RRPod> pods = podsViewModel.getPodList(PodType.MAIN_PODS);

                    if (pods == null) {

                        Log.e(TAG, "Pod list was empty, will not load a random pod");

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
    private void buildNotificationGroupsAndChannels() {

        NotificationUtils.buildAndRegisterChannelGroups(this,
                NotificationConstants.DOWNLOAD_NOTIFICATIONS_GROUP_BUILDER,
                NotificationConstants.PLAYBACK_NOTIFICATIONS_GROUP_BUILDER
        );

        NotificationUtils.buildAndRegisterChannels(this,
                NotificationConstants.DOWNLOADING_NOTIFICATION_CHANNEL_BUILDER,
                NotificationConstants.COMPLETED_DOWNLOADS_NOTIFICATION_CHANNEL_BUILDER,
                NotificationConstants.PLAYER_NOTIFICATION_CHANNEL_BUILDER
        );

    }


    @Override
    public void loadPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadSmallPodPlayerFragment();

        mMediaController
                .sendCommand(
                        PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_LOAD_POD,
                        getLoadPodBundle(pod, false),
                        null
                );

        podPlayerViewModel.setPlayerPod(pod);
        podPlayerViewModel.setPlayerDuration(pod.getDuration());

    }

    private Bundle getLoadPodBundle(@NonNull final RRPod pod, final boolean playWhenLoaded) {

        final Bundle loadBundle = new Bundle();
        loadBundle.putParcelable(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_POD_TO_BE_LOADED, pod);
        loadBundle.putBoolean(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_PLAY_WHEN_LOADED, playWhenLoaded);

        return loadBundle;

    }

    @Override
    public void playPod(@NonNull RRPod pod) {

        mainFragmentsHandler.loadSmallPodPlayerFragment();

        mMediaController
                .sendCommand(
                        PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_LOAD_POD,
                        getLoadPodBundle(pod, true),
                        null
                );

        podPlayerViewModel.setPlayerPod(pod);
        podPlayerViewModel.setPlayerDuration(pod.getDuration());

    }

    @Override
    public void pauseOrContinuePod() {


        final int playbackState = mMediaController.getPlaybackState().getState();

        switch (playbackState) {

            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_PLAYING:

                mMediaController.getTransportControls().pause();

                break;

            case PlaybackStateCompat.STATE_PAUSED:

                mMediaController.getTransportControls().play();

                break;

            case PlaybackStateCompat.STATE_FAST_FORWARDING:
            case PlaybackStateCompat.STATE_REWINDING:
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                break;

            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
            default:

                Assertions._assert(false, "Illegal playback state: " + playbackState);

                break;

        }

    }

    @Override
    public void pausePod() {

        mMediaController.getTransportControls().pause();

    }

    @Override
    public void continuePod() {

        mMediaController.getTransportControls().play();

    }

    @Override
    public void jump(int jump) {

        final Bundle jumpBundle = new Bundle();
        jumpBundle.putInt(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_EXTRA_JUMP_AMOUNT, jump);

        mMediaController.sendCommand(PodPlayerConstants.POD_PLAYER_SERVICE_COMMAND_JUMP, jumpBundle, null);

    }

    @Override
    public void seekTo(int progress) {

        mMediaController.getTransportControls().seekTo(progress);

        podPlayerViewModel.postPlayerProgress(progress);

    }

    @NonNull
    @Override
    public PodDownloader retrievePodDownloader() {

        if (podDownloader == null) {

            podDownloader = new PodDownloader(this);

        }

        return this.podDownloader;

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


}

