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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

// VERSION 1.1

public class MainActivity extends AppCompatActivity

        implements
        PodListFragment.PodListFragmentListener,
        SearchFragment.SearchFragmentListener,
        SettingsFragment.SettingsFragmentListener,
        AboutFragment.AboutFragmentListener,
        RandomPodFragment.RandomPodFragmentListener{

    // PODLIST MODE CONSTANTS
    private static final int ALL_PODCASTS = 0;
    private static final int OFFLINE_ONLY_PODCASTS = 1;
    private static final int ARCHIVE_ONLY_PODCASTS = 2;

    private ArrayList<ArrayList<RRPod>> podLists;
    private ArrayList<RRPod> offlinePods;

    // FRAGMENTS
    private PodListFragment podListFragment;
    private PodPlayerFragment podPlayer;
    private SearchFragment searchFragment;
    private SettingsFragment settingsFragment;
    private AboutFragment aboutFragment;
    private RandomPodFragment randomPodFragment;

    private boolean ALL_PERMISSIONS_GRANTED;

    // "LIFECYCLE OVERRIDES"
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setTitle("Alle podkaster");

        registerDownloadStateReciever();

        restoreFragments(savedInstanceState);

        // PERMISSION GETTER
        verifyPermissions();

        // APP DRAWER ASSEMBLY
        createAppDrawer();

        // CREATE APP TOOLBAR/ACTIONBAR
        //loadToolbar();


        //PRINT ALL SHAREDPREFERENCES
        /*Map<String, ?> allPrefs = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        Set<String> set = allPrefs.keySet();
        for(String s : set) System.out.println( s + "<" + allPrefs.get(s).getClass().getSimpleName() +"> =  " + allPrefs.get(s).toString());*/

    }

    private void registerDownloadStateReciever(){

        // DOWNLOAD BROADCAST RECEIVER AND INTENTFILTER
        IntentFilter progressIntentFilter = new IntentFilter(DownloadService.Constants.BROADCAST_ACTION);
        DownloadStateReceiver mDownloadStateReceiver = new DownloadStateReceiver(new DownloadStateReceiver.DownloadStateReceiverListener() {
            @Override
            public void updateDownloadProgress(String podName, float progress) {
                updateInAppProgressBar((int)progress);
                updateDownloadNotificationProgress(podName, (int)progress);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver,progressIntentFilter);

    }

    private void restoreFragments(Bundle savedInstanceState){

        // RESTORE POD PLAYER FRAGMENT (IF AVAILABLE)
        if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "PodPlayerFragment") != null) {
            podPlayer = (PodPlayerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "PodPlayerFragment");
            podPlayer.setPlayingState(savedInstanceState.getBoolean("playing_state"));
        } else if (podPlayer == null) {
            podPlayer = new PodPlayerFragment();
        }

        // INSERT POD PLAYER
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_podplayer, podPlayer).commit();

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
        else {
            if (savedInstanceState != null && podListFragment == null && getSupportFragmentManager().getFragment(savedInstanceState, "PodListFragment") != null) {
                podListFragment = (PodListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "PodListFragment");
            }
            else {
                podListFragment = PodListFragment.newInstance(ALL_PODCASTS);
            }
            // INSERT POD LIST
            getSupportFragmentManager().beginTransaction().replace(R.id.temp_frame, podListFragment).commit();
        }

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

        // RESTORE SEARCH FRAGMENT (IF AVAILABLE)
        if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "SearchFragment") != null) {
            searchFragment = (SearchFragment) getSupportFragmentManager().getFragment(savedInstanceState, "SearchFragment");
            if(savedInstanceState.getBoolean("searching_state")){
                changeToolbarUI(ENABLED_FILTER_UI,VISIBLE_FILTER_UI);
            }
        }else{
            newFilter();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (podPlayer != null && podPlayer.getPod() != null && podPlayer.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "PodPlayerFragment", podPlayer);
            outState.putBoolean("playing_state", podPlayer.getPlayingState());
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

    // INTERFACE OVERRIDES (FRAGMENTS)

    @Override
    public void onPlayOrStreamPod(RRPod pod) {
        if (podPlayer == null) podPlayer = new PodPlayerFragment();

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_podplayer, podPlayer);
        transaction.commit();

        podPlayer.PlayOrStreamPod(pod);
    }

    public void onPlayOrStreamPod(RRPod pod, int progress) {
        if (podPlayer == null) podPlayer = new PodPlayerFragment();

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_podplayer, podPlayer);
        transaction.commit();

        podPlayer.PlayOrStreamPod(pod,true,progress);
    }


    private static final String SETTINGS_PREFS_NAME = "SettingsPreferences";
    private static final String RANDOM_SELECT_KEY = "RANDOM_SELECT_OPTION";
    // PLAY RANDOM POD (THAT IS NOT LISTENED TO)
    private ArrayList<Integer> random_pods = new ArrayList<>();
    @Override
    public void onPlayRandomPod() {

        if(podLists == null){
            loadPods(ALL_PODCASTS);
        }

        /*if(random_pods.size() == podLists.get(0).size()){
            Snackbar.make(findViewById(R.id.drawer_relative),getResources().getString(R.string.random_select_no_result),Snackbar.LENGTH_LONG).show();
            random_pods = new ArrayList<>();
            return;
        }*/

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

        onPlayOrStreamPod(randomPod);
    }

    @Override
    public void onPodBuild(int podListMode) {
        if (searchFragment == null){
            newFilter();
        }

        loadPodList(podListMode);
        changeToolbarUI(ENABLED_FILTER_UI);

        /*if(findViewById(R.id.search_frame).getVisibility() == View.VISIBLE){
            changeToolbarUI(VISIBLE_FILTER_UI);
        }else{
            changeToolbarUI(INVISIBLE_FILTER_UI);
        }

        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);

        final String recent_pod_name = podkastStorage.getString("recent_pod_name",null);
        final int recent_pod_progress = podkastStorage.getInt("recent_pod_progress",0);

        if(recent_pod_name != null && recent_pod_progress != 0 && podLists != null){
            for(RRPod _pod : podLists.get(ALL_PODCASTS)){
                if(_pod.getTitle().equals(recent_pod_name)){
                    podPlayer.nullifyMP();
                    onPlayOrStreamPod(_pod,recent_pod_progress);
                }
            }
        }*/
    }

    @Override
    public void onBuildWithDate(int day, int month, int year, boolean notListenedTo) {
        podListFragment.BuildPodcastViewsWithDate(this, day, month, year,notListenedTo);
    }

    //UPDATE SEARCH RESULT TEXT AFTER PODLIST FILTERING
    @Override
    public void onPodcastViewsBuilt(int validpods) {
        if (searchFragment != null) searchFragment.viewResultStats(validpods);
        stopTopLoading();
    }

    @Override
    public void toolbarTextChange(String title) {
        TextView toolbarTextView = ((TextView) (findViewById(R.id.toolbar)).findViewById(R.id.toolbar_text));
        toolbarTextView.setText(title);
    }

    @Override
    public void onEnableSelectionMode() {
        changeToolbarUI(DISABLED_FILTER_UI,VISIBLE_SELECTION_UI);
        findViewById(R.id.clear_selected_pods).setVisibility(View.VISIBLE);
        findViewById(R.id.selection_actions).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDisableSelectionMode() {
        /*if(findViewById(R.id.search_frame).getVisibility() == View.GONE){
            changeToolbarUI(INVISIBLE_SELECTION_UI,ENABLED_FILTER_UI);
        }*/
        findViewById(R.id.clear_selected_pods).setVisibility(View.GONE);
        findViewById(R.id.selection_actions).setVisibility(View.GONE);
    }

    @Override
    public void onHidePodFilter() {
        changeToolbarUI(INVISIBLE_FILTER_UI);
    }


    // LOAD PODS
    void loadPodList(int podListMode){

        loadPods(podListMode);

        ArrayList<RRPod> pods;

        switch (podListMode){
            case OFFLINE_ONLY_PODCASTS:
                pods = offlinePods;
                break;
            case ARCHIVE_ONLY_PODCASTS:
                pods = podLists.get(1);
                break;
            default:
                pods = podLists.get(0);
                break;
        }

        initiatePodListFragment(pods,podListMode);
    }

    void loadPods(int podListMode) {

        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);

        if(podListMode == OFFLINE_ONLY_PODCASTS){
            final String json = podkastStorage.getString("offlinepods", null);
            if (json != null) {
                ArrayList<RRPod> registered_offlinepods = new Gson().fromJson(json, new TypeToken<ArrayList<RRPod>>() {
                }.getType());
                offlinePods = new ArrayList<>();
                final String dir = Environment.getExternalStoragePublicDirectory("RR-Podkaster") + File.separator;
                for (RRPod pod : registered_offlinepods) {
                    if (new File(dir + pod.getTitle()).exists()) {
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

        podLists = rrr.RetrievePods();

        // STORING OFFLINE PODS FOR OFFLINE USE
        final ArrayList<RRPod> offlinepods = new ArrayList<>();
        final String dir = Environment.getExternalStoragePublicDirectory("RR-Podkaster") + File.separator;

        for(ArrayList<RRPod> podlist : podLists){
            for (RRPod pod:podlist){
                if (new File(dir + pod.getTitle()).exists()) {
                    offlinepods.add(pod);
                }
            }
        }

        final String offline_json = new Gson().toJson(offlinepods);
        podkastStorage.edit().putString("offlinepods", offline_json).apply();
    }

    private void initiatePodListFragment(final ArrayList<RRPod> podlist,int podListMode) {
        if(podListFragment == null){
            podListFragment = PodListFragment.newInstance(podListMode);
        }else{
            podListFragment.setPodListMode(podListMode);
        }
        podListFragment.setAllpods(podlist);
        // CHECK IF POD LIST IS VISIBLE
        if (!podListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_podlist, podListFragment).commit();
        }else{
            podListFragment.BuildPodcastViews(this);
        }
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
                        final String json = podkastStorage.getString("offlinepods", null);
                        if (json != null) {
                            ArrayList<RRPod> offlinepods = new Gson().fromJson(json, new TypeToken<ArrayList<RRPod>>() {
                            }.getType());
                            offlinepods.add(pod);
                            podkastStorage.edit().putString("offlinepods", new Gson().toJson(offlinepods)).apply();
                        }

                        // UPDATE AFTER DOWNLOAD
                        downloading = false;
                        podListFragment.BuildPodcastViews(context);

                        downloadQueue.remove(pod);
                        if (downloadQueue.isEmpty()) {
                            podListFragment.stopLoadingScreen();
                            return;
                        }

                        // DOWNLOAD NEXT IN QUEUE
                        downloadPod(downloadQueue.get(0), context);
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

    private void downloadPod(final RRPod pod, final Context context) {

        if (!verifyPermissions()) return;

        downloadQueue.add(pod);

        if (downloading) {
            podListFragment.setLoadingText("Laster ned " + String.valueOf(downloadQueue.size() + 1) + " podkaster");
            return;
        }
        podListFragment.startLoadingScreen();

        /* Starting Download Service */
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(getDownloadReceiver(pod, context));
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
                                String dir = Environment.getExternalStoragePublicDirectory("RR-Podkaster") + File.separator;
                                for (RRPod pod : pods) {
                                    if (!pod.getDownloadState()) {
                                        continue;
                                    }
                                    File file = new File(dir + pod.getTitle());
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
                                        podListFragment.BuildPodcastViews(context);
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

                                /*// SHOW SNACKBAR
                                Snackbar.make(findViewById(R.id.drawer_relative),res,Snackbar.LENGTH_LONG).show();*/
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

    private void loadToolbar() {
        // TOOLBAR VIEW FIELDS INIT
        ImageView showFilter = (ImageView) findViewById(R.id.pod_filter_show);
        ImageView hideFilter = (ImageView) findViewById(R.id.pod_filter_hide);

        /*// CHECK IF ALREADY FILTERING (ACTIVITY RESTART AFTER SCREEN-ROTATION ETC.)
        if (searchFragment != null && searchFragment.isAdded() && findViewById(R.id.search_frame).getVisibility() == View.VISIBLE) {
            changeToolbarUI(VISIBLE_FILTER_UI);
        }*/

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
                podListFragment.BuildPodcastViews(ctx);
            }
        });
    }

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
        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setItems(listitems,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // CLICKED DOWNLOAD
                                if(which == DOWNLOAD_LABEL){
                                    for (RRPod currPod : pods) {
                                        if (!currPod.getDownloadState()) downloadPod(currPod,ctx);
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
                                podListFragment.BuildPodcastViews(ctx);
                            }
                        })
                .create()
                .show();
    }

    private void markAsListenedTo(RRPod pod) {
        pod.setListenedToState(true);
        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);
        podkastStorage.edit().putBoolean(pod.getTitle() + "(LT)", true).apply();
        podListFragment.BuildPodcastViews(this);
    }

    private void unMarkAsListenedTo(RRPod pod) {
        pod.setListenedToState(false);
        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(this);
        podkastStorage.edit().putBoolean(pod.getTitle() + "(LT)", false).apply();
        podListFragment.BuildPodcastViews(this);
    }

    private void startTopLoading(){
        /*findViewById(R.id.top_progress).setVisibility(View.VISIBLE);*/
    }

    private void stopTopLoading(){
        /*findViewById(R.id.top_progress).setVisibility(View.GONE);*/
    }


    // DRAWER
    int mPosition;
    private void createAppDrawer() {
        String[] mPlanetTitles = getResources().getStringArray(R.array.DrawerArray);
        int[] imgOps = new int[]{R.drawable.ic_pod_list, R.drawable.ic_highlights,R.drawable.ic_signal_wifi_off, R.drawable.ic_shuffle, R.drawable.ic_webpage,R.drawable.ic_settings, R.drawable.ic_about_app};
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // SET LIST ADAPTER
        mDrawerList.setAdapter(new DrawerListAdapter(this, mPlanetTitles, imgOps));

        // DRAWER CLOSING ON CLICK
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // MARK SELECTION POSTION AND CLOSE DRAWER
                if(mPosition != position || mPosition == 0) startTopLoading();

                mPosition = position;
                drawerLayout.closeDrawers();
            }
        });

        // LOADING THE PAGES ON DRAWER CLOSE
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // CHECK IF NOTHING HAS BEEN SELECTED
                if (mPosition == -1) {
                    return;
                }

                switch (mPosition) {
                    case 0:
                        setMainFrag(ALL_PODCASTS);
                        break;
                    case 1:
                        setMainFrag(ARCHIVE_ONLY_PODCASTS);
                        break;
                    case 2:
                        setMainFrag(OFFLINE_ONLY_PODCASTS);
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
                mPosition = -1;

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    // CHANGE MAIN CONTENT
    private void setMainFrag(int podListMode){
        if(!isOnline()){
            setMainFrag(OFFLINE_ONLY_PODCASTS);
            return;
        }

        newFilter();
        podListFragment = PodListFragment.newInstance(podListMode);
        loadPodList(podListMode);
        changeToolbarUI(INVISIBLE_FILTER_UI);
        updateToolbarTitle();
    }

    private void goToRandomPod() {
        changeToolbarUI(DISABLE_ALL_UI);

        // RANDOM POD FRAGMENT INIT
        if(randomPodFragment == null){
            randomPodFragment = RandomPodFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_podlist, randomPodFragment).commit();
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
        for(int p = 0;p<allpods.size();p++){
            if(allpods.get(p).getDownloadState()) {
                spaceUsage += Environment.getExternalStoragePublicDirectory("RR-Podkaster" + File.separator + allpods.get(p).getTitle()).length() / Math.pow(1024, 2);
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
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_podlist, settingsFragment).commit();
        stopTopLoading();
    }

    private void goToAbout() {
        changeToolbarUI(DISABLE_ALL_UI);

        // ABOUT FRAGMENT INIT
        aboutFragment = new AboutFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_podlist, aboutFragment).commit();
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            ALL_PERMISSIONS_GRANTED = false;
            return;
        }
        ALL_PERMISSIONS_GRANTED = true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST)
            ALL_PERMISSIONS_GRANTED = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }


}

