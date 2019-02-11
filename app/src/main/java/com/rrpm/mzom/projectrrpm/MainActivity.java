package com.rrpm.mzom.projectrrpm;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity

        implements
        SearchFragment.SearchFragmentListener,
        SettingsFragment.SettingsFragmentListener,
        AboutFragment.AboutFragmentListener,
        RandomPodFragment.RandomPodFragmentListener,
        PodsFragment.PodsFragmentListener,
        PodPlayer.PodPlayerListener{


    private static final String TAG = "RRP-MainActivity";

    // PODLIST MODE CONSTANTS
    private static final int ALL_PODCASTS = 0;
    private static final int OFFLINE_ONLY_PODCASTS = 1;
    private static final int ARCHIVE_ONLY_PODCASTS = 2;

    private ArrayList<ArrayList<RRPod>> podLists;
    private ArrayList<RRPod> offlinePods;

    private PodPlayer podPlayer;

    // FRAGMENTS
    private PodListFragment podListFragment;
    private SearchFragment searchFragment;
    private SettingsFragment settingsFragment;
    private AboutFragment aboutFragment;
    private RandomPodFragment randomPodFragment;

    private PodPlayerFragment podPlayerFragment;

    private PodsFragment podsFragment;

    private boolean ALL_PERMISSIONS_GRANTED;

    // "LIFECYCLE OVERRIDES"
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyPermissions();


        registerDownloadStateReceiver();

        // TODO: Create PodPlayer restoration
        podPlayer = new PodPlayer(this,this);

        if(restorePodPlayerFragment(savedInstanceState)){
            podPlayerFragment.setPodPlayer(podPlayer);
        }else{
            podPlayerFragment = PodPlayerFragment.newInstance(podPlayer);
        }

        // Insert PodPlayerFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_podplayer, podPlayerFragment).commit();

        restoreDownloadReceiverAndQueue(savedInstanceState);

        initDrawer();


        final ArrayList<RRPod> retrievedPods = retrievePodsFromRSS();

        if(retrievedPods != null){

            if(!restoreFragments(savedInstanceState)){
                loadPodsFragment(retrievedPods);
            }

        }


        //PRINT ALL SHAREDPREFERENCES
        /*Map<String, ?> allPrefs = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        Set<String> set = allPrefs.keySet();
        for(String s : set) System.out.println( s + "<" + allPrefs.get(s).getClass().getSimpleName() +"> =  " + allPrefs.get(s).toString());*/

    }

    private ArrayList<RRPod> retrievePodsFromRSS(){

        // USE READER TO GET PODS
        final RRReader rrr = new RRReader(this, podLists);
        rrr.start();

        try {
            rrr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        podLists = rrr.retrievePods();

        Log.i(TAG,"Pods: " + podLists);

        return podLists.get(0);

    }

    private void registerDownloadStateReceiver(){

        // DOWNLOAD BROADCAST RECEIVER AND INTENTFILTER
        IntentFilter progressIntentFilter = new IntentFilter(DownloadService.Constants.BROADCAST_ACTION);
        DownloadStateReceiver mDownloadStateReceiver = new DownloadStateReceiver(new DownloadStateReceiver.DownloadStateReceiverListener() {
            @Override
            public void updateDownloadProgress(String podName, float progress) {
                Log.i(TAG,"Progress: " + String.valueOf(progress));
                updateInAppProgressBar((int)progress);
                updateDownloadNotificationProgress(podName, (int)progress);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver,progressIntentFilter);

    }

    private void loadPodsFragment(@NonNull final ArrayList<RRPod> pods){

        podsFragment = PodsFragment.newInstance(pods,podPlayer);

        // INSERT POD LIST
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, podsFragment).commit();

    }

    private boolean restorePodPlayerFragment(Bundle savedInstanceState){

        // RESTORE POD PLAYER FRAGMENT (IF AVAILABLE)
        if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "PodPlayerFragment") != null) {

            podPlayerFragment = (PodPlayerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "PodPlayerFragment");

            // TODO: Handle restored playing state
            /*boolean shouldPlay = savedInstanceState.getBoolean("playing_state");
            if(shouldPlay){

                podPlayer.

            }*/

            return true;
        }

        return false;
    }

    private void restoreDownloadReceiverAndQueue(Bundle savedInstanceState){

        // RESTORE QUEUE && RECEIVER FOR DOWNLOAD
        final Context ctx = this;
        if (savedInstanceState != null && savedInstanceState.getParcelable("receiver") != null) {
            downloadQueue = new Gson().fromJson(savedInstanceState.getString("downloadQueue", null), new TypeToken<ArrayList<RRPod>>() {
            }.getType());
            if (downloadQueue.size() > 0) {
                mReceiver = savedInstanceState.getParcelable("receiver");
                mReceiver.setReceiver(getDownloadReceiver(downloadQueue.get(0), ctx));
            }
        }

    }

    private boolean restoreFragments(Bundle savedInstanceState){


        /*// RESTORE SEARCH FRAGMENT (IF AVAILABLE)
        if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "SearchFragment") != null) {
            searchFragment = (SearchFragment) getSupportFragmentManager().getFragment(savedInstanceState, "SearchFragment");
            if(savedInstanceState.getBoolean("searching_state")){
                changeToolbarUI(ENABLED_FILTER_UI,VISIBLE_FILTER_UI);
            }
        }else{
            newFilter();
        }*/


        // RESTORE RANDOM POD FRAGMENT (IF AVAILABLE)
        if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "RandomPodFragment") != null) {

            randomPodFragment = (RandomPodFragment) getSupportFragmentManager().getFragment(savedInstanceState, "RandomPodFragment");
            goToRandomPod();
        }

        // RESTORE SETTINGS FRAGMENT (IF AVAILABLE)
        else if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "SettingsFragment") != null) {

            settingsFragment = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, "SettingsFragment");
            goToSettings();
        }

        // RESTORE ABOUT FRAGMENT (IF AVAILABLE)
        else if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "AboutFragment") != null) {

            aboutFragment = (AboutFragment) getSupportFragmentManager().getFragment(savedInstanceState, "AboutFragment");
            goToAbout();
        }

        // RESTORE POD LIST FRAGMENT
        else if (savedInstanceState != null && podListFragment == null && getSupportFragmentManager().getFragment(savedInstanceState, "PodListFragment") != null) {

            podListFragment = (PodListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "PodListFragment");

            // INSERT POD LIST
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, podListFragment).commit();
        }

        else{
            return false;
        }

        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (podPlayerFragment != null && podPlayer.getPod() != null && podPlayerFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "PodPlayerFragment", podPlayerFragment);
            outState.putBoolean("playing_state", podPlayer.isPlaying());
        }

        if (podListFragment != null && podListFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "PodListFragment", podListFragment);
            outState.putInt("podListMode",podListFragment.getPodListMode());
        }

        /*if (searchFragment != null && searchFragment.isAdded()){
            outState.putBoolean("searching_state", findViewById(R.id.search_frame).getVisibility() == View.VISIBLE);
            getSupportFragmentManager().putFragment(outState, "SearchFragment", searchFragment);
        }*/

        if (randomPodFragment != null && randomPodFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "RandomPodFragment", randomPodFragment);


        if (aboutFragment != null && aboutFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "AboutFragment", aboutFragment);

        if (settingsFragment != null && settingsFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "SettingsFragment", settingsFragment);

        Gson gson = new Gson();
        String json = gson.toJson(downloadQueue);
        outState.putString("downloadQueue", json);
        outState.putParcelable("receiver", mReceiver);
    }


    private static final String SETTINGS_PREFS_NAME = "SettingsPreferences";
    private static final String RANDOM_SELECT_KEY = "RANDOM_SELECT_OPTION";
    // PLAY RANDOM POD (THAT IS NOT LISTENED TO)
    private ArrayList<Integer> random_pods = new ArrayList<>();
    @Override
    public void onPlayRandomPod() {

        /*if(podLists == null){
            loadPods(ALL_PODCASTS);
        }

        *//*if(random_pods.size() == podLists.get(0).size()){
            Snackbar.make(findViewById(R.id.drawer_relative),getResources().getString(R.string.random_select_no_result),Snackbar.LENGTH_LONG).show();
            random_pods = new ArrayList<>();
            return;
        }*//*

        ArrayList<RRPod> pods = podLists.get(ALL_PODCASTS);

        int randomInt = new Random().nextInt(pods.size());

        if(random_pods.indexOf(randomInt) == -1){
            random_pods.add(randomInt);
        }else{
            onPlayRandomPod();
            return;
        }

        RRPod randomPod = pods.get(randomInt);

        final String[] items = getResources().getStringArray(R.array.random_select_from_array);

        boolean[] checkedItems = new boolean[items.length];

        final SharedPreferences settings_prefs = getSharedPreferences(SETTINGS_PREFS_NAME,0);

        for(int i = 0;i<checkedItems.length;i++){
            checkedItems[i] = settings_prefs.getBoolean(RANDOM_SELECT_KEY + " " + i,false);
        }

        if(!checkedItems[0]){
            if(!checkedItems[1] && !randomPod.getListenedToState()){
                onPlayRandomPod();
                return;
            }
            if(!checkedItems[2] && randomPod.getListenedToState()){
                onPlayRandomPod();
                return;
            }
            if(!checkedItems[3] && !randomPod.getDownloadState()){
                onPlayRandomPod();
                return;
            }
            if(!checkedItems[4] && randomPod.getDownloadState()) {
                onPlayRandomPod();
                return;
            }
        }

        onPlayOrStreamPod(randomPod);*/
    }

    @Override
    public void onBuildWithDate(int day, int month, int year, boolean notListenedTo) {
        podListFragment.buildPodcastViewsWithDate(this, day, month, year,notListenedTo);
    }

    @Override
    public void toolbarTextChange(String title) {
        final TextView toolbarTextView = findViewById(R.id.toolbar).findViewById(R.id.toolbar_text);
        toolbarTextView.setText(title);
    }

    @Override
    public void onHidePodFilter() {
        changeToolbarUI(INVISIBLE_FILTER_UI);
    }


    void loadPods(int podListMode) {

        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);

        final String json = podkastStorage.getString("offlinepods", null);
        Log.i(TAG,"Download json: " + json);

        if(podListMode == OFFLINE_ONLY_PODCASTS){
            if (json != null) {
                ArrayList<RRPod> registered_offlinepods = new Gson().fromJson(json, new TypeToken<ArrayList<RRPod>>() {
                }.getType());
                offlinePods = new ArrayList<>();
                final File dir = new File(getFilesDir(),"RR-Podkaster");
                for (RRPod pod : registered_offlinepods) {
                    if (new File(dir + File.separator + pod.getTitle()).exists()) {
                        offlinePods.add(pod);
                    }
                }
            }
            return;
        }

        // USE READER TO GET PODS
        final RRReader rrr = new RRReader(this, podLists);
        rrr.start();

        try {
            rrr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        podLists = rrr.retrievePods();

        Log.i(TAG,"Pods: " + podLists);

        // STORING OFFLINE PODS FOR OFFLINE USE
        final ArrayList<RRPod> offlinepods = new ArrayList<>();
        final File dir = new File(getFilesDir(),"RR-Podkaster");

        for(ArrayList<RRPod> podlist : podLists){
            for (RRPod pod:podlist){
                if (new File(dir + File.separator +  pod.getTitle()).exists()) {
                    offlinepods.add(pod);
                }
            }
        }

        final String offline_json = new Gson().toJson(offlinepods);
        podkastStorage.edit().putString("offlinepods", offline_json).apply();
    }


    // DOWNLOAD PODS

    private boolean downloading;
    private ArrayList<RRPod> downloadQueue = new ArrayList<>();
    private DownloadResultReceiver mReceiver;
    private DownloadResultReceiver.Receiver getDownloadReceiver(final RRPod pod, final Context context) {
        return new DownloadResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case 1:
                        // SAVE AS OFFLINE POD
                        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(context);
                        String json = podkastStorage.getString("offlinepods", null);
                        if(json == null){
                            json = "";
                        }

                        final ArrayList<RRPod> offlinepods = new Gson().fromJson(json, new TypeToken<ArrayList<RRPod>>() {}.getType());
                        offlinepods.add(pod);
                        podkastStorage.edit().putString("offlinepods", new Gson().toJson(offlinepods)).apply();

                        // UPDATE AFTER DOWNLOAD
                        downloading = false;
                        podListFragment.buildPodcastViews(context);

                        downloadQueue.remove(pod);
                        if (downloadQueue.isEmpty()) {
                            podListFragment.stopLoadingScreen();
                            return;
                        }

                        // DOWNLOAD NEXT IN QUEUE
                        downloadPod(downloadQueue.get(0));
                        downloadQueue.remove(downloadQueue.get(0));
                        break;
                    case 0:
                        // FIRST DOWNLOAD IN QUEUE
                        if (downloadQueue.isEmpty())
                            podListFragment.startLoadingScreen();
                        break;
                    /*case 2:
                        // ERROR HANDLER
                        Snackbar.make(findViewById(R.id.drawer_relative),"En feil oppstod, sjekk ledig lagringsplass",Snackbar.LENGTH_LONG).show();
                        podListFragment.stopLoadingScreen();
                        break;*/
                    /*case 3:
                        // PROGRESS UPDATE
                        if (podListFragment.viewAvailable())
                            if (podListFragment.getLoadingState()) {
                                // UPDATING PROGRESS IF VISIBLE
                                int progress = (int) resultData.getFloat("progress");
                                podListFragment.setLoadingProgress(progress);
                                // SINGLE DOWNLOAD
                                if (downloadQueue.size() > 1) {
                                    podListFragment.setLoadingText("Laster ned " + String.valueOf(downloadQueue.size()) + " podkaster");
                                    return;
                                }
                                // OR MULTIPLE DOWNLOAD
                                podListFragment.setLoadingText("Laster ned podkast fra " + downloadQueue.get(0).getTitle());

                            } else {
                                // MAKING PROGRESS VISIBLE
                                podListFragment.startLoadingScreen();
                            }

*/
                }
            }
        };
    }

    private void downloadPod(final RRPod pod) {

        downloadQueue.add(pod);

        if (!verifyPermissions()){

            Log.i(TAG,"Permissions not granted");

            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.drawer_layout), "Trenger tillatelse til å laste ned",Snackbar.LENGTH_LONG)
                    .setAction("GI TILLATELSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.app_white));
            snackbar.show();


            podListFragment.stopLoadingScreen();

            return;
        }

        Log.i(TAG,"Downloading " + pod);

        if (downloading) {
            podListFragment.setLoadingText("Laster ned " + String.valueOf(downloadQueue.size() + 1) + " podkaster");
            return;
        }
        podListFragment.startLoadingScreen();

        /* Starting Download Service */
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(getDownloadReceiver(pod, this));
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

        intent.putExtra("url", pod.getUrl()).putExtra("podName", pod.getTitle()).putExtra("receiver", mReceiver).putExtra("requestId", 101);
        startService(intent);

        downloading = true;
    }


    // DELETE ONE OR MULTIPLE PODS FROM DEVICE
    public void deleteBulk(final ArrayList<RRPod> pods) {
        String msg = pods.size() == 1 ?
                "Er du sikker på at du vil slette " + pods.get(0).getTitle() + " fra enheten?":
                "Er du sikker på at du vil slette " + pods.size() + " podkaster fra enheten?";

        final Context context = this;
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Slette podkast?")
                .setMessage(msg)
                .setPositiveButton(("Ja"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int deleteCount = 0;
                                final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(context);
                                final File dir = new File(getFilesDir(),"RR-Podkaster");
                                for (RRPod pod : pods) {
                                    if (!pod.getDownloadState()) {
                                        continue;
                                    }
                                    File file = new File(dir + File.separator + pod.getTitle());
                                    if (file.delete()) {
                                        deleteCount++;

                                        // Get registered offline-pods
                                        String json = podkastStorage.getString("offlinepods", null);
                                        if (json == null) return;

                                        // From JSON to ArrayList with Gson
                                        Gson gson = new Gson();
                                        ArrayList<RRPod> offlinepods = gson.fromJson(json, new TypeToken<ArrayList<RRPod>>() {
                                        }.getType());
                                        if (offlinepods == null) return;

                                        // Remove deleted pod from registered offline-pods
                                        offlinepods.remove(pod);
                                        json = gson.toJson(offlinepods);

                                        // Save to SP
                                        podkastStorage.edit().putString("offlinepods", json).apply();

                                        // Refresh podlist
                                        podListFragment.buildPodcastViews(context);
                                    }
                                }

                                // SNACKBAR TEXT
                                String res;
                                if(pods.size() == 1){
                                    if(deleteCount == 1){
                                        res = "Slettet " + pods.get(0).getTitle();
                                    }else{
                                        res = "Kunne ikke slette " + pods.get(0).getTitle();
                                    }
                                }
                                else if(pods.size() == deleteCount){
                                    res = "Slettet " + deleteCount + " podkaster";
                                }else{
                                    res = "Slettet " + deleteCount + " podkaster, " + String.valueOf(pods.size() - deleteCount) + " kunne ikke slettes";
                                }

                                // SHOW SNACKBAR
                                Snackbar.make(findViewById(R.id.drawer_layout),res,Snackbar.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton("Nei", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    // TOOLBAR
    private static final int
            VISIBLE_FILTER_UI = 0,
            INVISIBLE_FILTER_UI = 1,
            ENABLED_FILTER_UI = 2,
            DISABLED_FILTER_UI = 3,
            VISIBLE_SELECTION_UI = 4,
            INVISIBLE_SELECTION_UI = 5,
            DISABLE_ALL_UI = 6;

    /*private void loadToolbar() {
        // TOOLBAR VIEW FIELDS INIT
        ImageView showFilter = (ImageView) findViewById(R.id.pod_filter_show);
        ImageView hideFilter = (ImageView) findViewById(R.id.pod_filter_hide);

        // CHECK IF ALREADY FILTERING (ACTIVITY RESTART AFTER SCREEN-ROTATION ETC.)
        if (searchFragment != null && searchFragment.isAdded() && findViewById(R.id.search_frame).getVisibility() == View.VISIBLE) {
            changeToolbarUI(VISIBLE_FILTER_UI);
        }

        // CLICK LISTENERS
        showFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToolbarUI(VISIBLE_FILTER_UI);
            }
        });

        hideFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToolbarUI(INVISIBLE_FILTER_UI);
            }
        });

        findViewById(R.id.selection_actions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedPodsClick(podListFragment.getSelectedPods());
            }
        });

        final Context ctx = this;

        findViewById(R.id.clear_selected_pods).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                podListFragment.buildPodcastViews(ctx);
            }
        });
    }*/

    private void changeToolbarUI(int... prefs){
        /*// SEARCH FRAME
        final FrameLayout search_frame = (FrameLayout) findViewById(R.id.search_frame);

        // TOOLBAR BUTTONS
        final ImageView showFilter = (ImageView) findViewById(R.id.pod_filter_show);
        final ImageView hideFilter = (ImageView) findViewById(R.id.pod_filter_hide);

        // HANDLE COMMANDS/PREFS
        for(int p:prefs){
            switch (p){
                case VISIBLE_FILTER_UI:
                    hideFilter.setVisibility(View.VISIBLE);
                    showFilter.setVisibility(View.GONE);
                    search_frame.setVisibility(View.VISIBLE);
                    toolbarTextChange(getResources().getString(R.string.action_filter_ext));
                    searchFragment.viewResultStats(podListFragment.getPodNum());
                    break;
                case INVISIBLE_FILTER_UI:
                    hideFilter.setVisibility(View.GONE);
                    showFilter.setVisibility(View.VISIBLE);
                    search_frame.setVisibility(View.GONE);
                    updateToolbarTitle();
                    break;
                case ENABLED_FILTER_UI:
                    findViewById(R.id.selection_actions).setVisibility(View.GONE);
                    showFilter.setVisibility(View.VISIBLE);
                    hideFilter.setVisibility(View.GONE);
                    break;
                case DISABLED_FILTER_UI:
                    changeToolbarUI(INVISIBLE_FILTER_UI);
                    showFilter.setVisibility(View.GONE);
                    hideFilter.setVisibility(View.GONE);
                    break;
                case VISIBLE_SELECTION_UI:
                    findViewById(R.id.selection_actions).setVisibility(View.VISIBLE);
                    break;
                case INVISIBLE_SELECTION_UI:
                    findViewById(R.id.selection_actions).setVisibility(View.GONE);
                    break;
                case DISABLE_ALL_UI:
                    changeToolbarUI(DISABLED_FILTER_UI,INVISIBLE_SELECTION_UI);
            }
        }*/
    }

    private void updateToolbarTitle(){
        if(podListFragment == null || !podListFragment.isAdded()){
            return;
        }
        String[] toolbar_titles = new String[]{"Alle podkaster","Nedlastede podkaster","Høydepunkter"};
        toolbarTextChange(toolbar_titles[podListFragment.getPodListMode()]);
    }

        // RESET SEARCH FRAGMENT
    private void newFilter() {
        /*searchFragment = new SearchFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.search_frame, searchFragment).commit();*/
    }

        // POD SELECTION
    private void onSelectedPodsClick(final ArrayList<RRPod> pods) {

        // DIALOG ITEMS LIST
        ArrayList<String> items = new ArrayList<>();

        // GET PODS PROPERTIES
        int listenedToCount = 0;
        int downloadedCount = 0;

        for (RRPod pod : pods) {
            if (pod.getListenedToState()) listenedToCount++;
            if(pod.getDownloadState()) downloadedCount++;
        }

        final boolean listenedTo = listenedToCount > pods.size() / 2;
        final boolean downloadable = downloadedCount < pods.size();
        final boolean deleteable = downloadedCount > 0;

        int down,del,mark;

        if(downloadable){
            items.add("Last ned");
            down = 0;
            if(deleteable){
                items.add("Slett");
                del = 1;
                mark = 2;
            }else{
                del = -1;
                mark = 1;
            }
        }else{
            items.add("Slett");
            down = -1;
            del = 0;
            mark = 1;
        }

        if (listenedTo) items.add("Merk som ikke lyttet til");
        else items.add("Merk som lyttet til");

        items.add("Lukk");

        final int DOWNLOAD_LABEL = down;
        final int DELETE_LABEL = del;
        final int MARK_LABEL = mark;

        String[] listitems = items.toArray(new String[items.size()]);

        // DIALOG TITLE BASED ON NUMBER OF SELECTION
        String title = pods.size() > 1 ? (pods.size() + " podkaster valgt") : pods.get(0).getTitle();

        final Context ctx = this;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(listitems,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // CLICKED DOWNLOAD
                                if(which == DOWNLOAD_LABEL){
                                    for (RRPod currPod : pods) {
                                        if (!currPod.getDownloadState()){
                                            downloadPod(currPod);
                                        }else{
                                            Log.i(TAG,"Already downloaded");
                                        }
                                    }
                                }
                                // CLICKED DELETE
                                else if(which == DELETE_LABEL){
                                    deleteBulk(pods);
                                }
                                // CLICKED MARK AS LISTENED/NOT LISTENED TO
                                else if(which == MARK_LABEL){
                                    for (RRPod currPod : pods) {
                                        if (listenedTo) unMarkAsListenedTo(currPod);
                                        else markAsListenedTo(currPod);
                                    }
                                }
                                // REFRESH PODLIST
                                podListFragment.buildPodcastViews(ctx);
                            }
                        })
                .create()
                .show();
    }

    private void markAsListenedTo(RRPod pod) {
        pod.setListenedToState(true);
        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);
        podkastStorage.edit().putBoolean(pod.getTitle() + "(LT)", true).apply();
        podListFragment.buildPodcastViews(this);
    }

    private void unMarkAsListenedTo(RRPod pod) {
        pod.setListenedToState(false);
        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);
        podkastStorage.edit().putBoolean(pod.getTitle() + "(LT)", false).apply();
        podListFragment.buildPodcastViews(this);
    }

    private void startTopLoading(){
        /*findViewById(R.id.top_progress).setVisibility(View.VISIBLE);*/
    }

    private void stopTopLoading(){
        /*findViewById(R.id.top_progress).setVisibility(View.GONE);*/
    }


    int drawerPos;
    private void initDrawer() {
        String[] mPlanetTitles = getResources().getStringArray(R.array.DrawerArray);
        int[] imgOps = new int[]{R.drawable.ic_pod_list, R.drawable.ic_highlights,R.drawable.ic_signal_wifi_off, R.drawable.ic_shuffle, R.drawable.ic_webpage,R.drawable.ic_settings, R.drawable.ic_about_app};
        final ListView mDrawerList = findViewById(R.id.left_drawer);
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        // SET LIST ADAPTER
        mDrawerList.setAdapter(new DrawerListAdapter(this, mPlanetTitles, imgOps));

        // DRAWER CLOSING ON CLICK
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // MARK SELECTION POSITION AND CLOSE DRAWER
                if(drawerPos != position || drawerPos == 0) startTopLoading();

                drawerPos = position;
                drawerLayout.closeDrawers();
            }
        });

        // LOADING THE PAGES ON DRAWER CLOSE
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

                // CHECK IF NOTHING HAS BEEN SELECTED
                if (drawerPos == -1) {
                    return;
                }

                switch (drawerPos) {
                    case 0:

                        break;
                    case 1:

                        break;
                    case 2:

                        break;
                    case 3:
                        goToRandomPod();
                        break;
                    case 4:
                        goToFBPage();
                        break;
                    case 5:
                        goToSettings();
                        //findViewById(R.id.top_progress).setVisibility(View.GONE);
                        break;
                    case 6:
                        goToAbout();
                        break;

                }

                // INIT NOTHING SELECTED "MARK"
                drawerPos = -1;

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void goToRandomPod() {
        changeToolbarUI(DISABLE_ALL_UI);

        // RANDOM POD FRAGMENT INIT
        if(randomPodFragment == null){
            randomPodFragment = RandomPodFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, randomPodFragment).commit();
        stopTopLoading();
    }

    private void goToSettings() {
        changeToolbarUI(DISABLE_ALL_UI);

        if(podLists == null){
            loadPods(ALL_PODCASTS);
        }

        ArrayList<RRPod> allpods = podLists.get(0);

        float spaceUsage = 0;
        int podnum = 0;

        final File dir = new File(getFilesDir(),"RR-Podkaster");

        for(int p = 0;p<allpods.size();p++){
            if(allpods.get(p).getDownloadState()) {
                spaceUsage += new File(dir + File.separator + allpods.get(p).getTitle()).length() / Math.pow(1024, 2);
                podnum++;
            }
        }

        if(spaceUsage != 0 && podnum != 0){
            spaceUsage = (float)Math.round(spaceUsage * 100) / 100;

            final SharedPreferences settings_prefs = getSharedPreferences(SETTINGS_PREFS_NAME,0);
            settings_prefs.edit().putInt("INFO_PODNUM",podnum).apply();
            settings_prefs.edit().putFloat("INFO_SPACEUSAGE",spaceUsage).apply();
        }



        // SETTINGS FRAGMENT INIT
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, settingsFragment).commit();
        stopTopLoading();
    }

    private void goToAbout() {
        changeToolbarUI(DISABLE_ALL_UI);

        // ABOUT FRAGMENT INIT
        aboutFragment = new AboutFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, aboutFragment).commit();
        stopTopLoading();
    }

    private void goToFBPage(){
        // GO TO URL IN BROWSER
        String url = "https://www.facebook.com/radioresepsjonenp13";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
        stopTopLoading();
    }


    // DOWNLOAD PROGRESS

    private void updateInAppProgressBar(int progress){
        // PROGRESS UPDATE
        if (podListFragment.viewAvailable())
            if (podListFragment.getLoadingState()) {
                // UPDATING PROGRESS IF VISIBLE
                podListFragment.setLoadingProgress(progress);
                // SINGLE DOWNLOAD
                if (downloadQueue.size() > 1) {
                    podListFragment.setLoadingText("Laster ned " + String.valueOf(downloadQueue.size()) + " podkaster");
                    return;
                }
                // OR MULTIPLE DOWNLOAD
                podListFragment.setLoadingText("Laster ned podkast fra " + downloadQueue.get(0).getTitle());

            } else {
                // MAKING PROGRESS VISIBLE
                podListFragment.startLoadingScreen();
            }
    }


    // DOWNLOAD NOTIFICATION

    private static final int DOWNLOAD_NOTIFICATION_ID = 800;
    private static final int DOWNLOAD_NOTIFICATION_CHANNEL_ID = 1000;


    private void updateDownloadNotificationProgress(String podName, int progress){
        final String notifyTitle = "Laster ned podkast";

        NotificationCompat.Style style = new NotificationCompat.BigTextStyle();

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setStyle(style)
                .setContentTitle(notifyTitle)
                .setContentText(podName)
                .setSmallIcon(R.drawable.ic_file_download)
                .setProgress(100,progress,false)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setLights(Color.GREEN,1000,1000);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(DOWNLOAD_NOTIFICATION_ID,mNotifyBuilder.build());
        }
    }





    // CHECK NETWORK CONNECTION
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // PERMISSION GRANTING
    private static final int MY_PERMISSIONS_REQUEST = 1337;

    private boolean verifyPermissions() {
        getPermissions();
        return ALL_PERMISSIONS_GRANTED;
    }

    private void getPermissions() {

        Log.i(TAG, String.valueOf(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)));

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);

            ALL_PERMISSIONS_GRANTED = false;
            return;
        }
        ALL_PERMISSIONS_GRANTED = true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST){
            ALL_PERMISSIONS_GRANTED = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            if(ALL_PERMISSIONS_GRANTED && downloadQueue.size() > 0){

                // Continue download
                final RRPod recentDownload = downloadQueue.get(downloadQueue.size()-1);
                downloadQueue.remove(recentDownload);
                downloadPod(recentDownload);

            }


        }
    }


    @Override
    public void playPod(RRPod pod) {

        if(podPlayer == null){
            podPlayer = new PodPlayer(this,this);
        }

        if (podPlayerFragment == null) podPlayerFragment = PodPlayerFragment.newInstance(podPlayer);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_podplayer, podPlayerFragment);
        transaction.commit();

        podPlayer.playPod(pod);

    }

    @Override
    public void onCurrentPositionChanged(int position, @NonNull RRPod pod) {

    }

    @Override
    public void onPodStarted(@NonNull RRPod pod, int from) {

        podPlayerFragment.onPodStarted(pod,from);

    }

    @Override
    public void onPlayerPaused() {

    }

    @Override
    public void onPlayerContinued() {

    }
}

