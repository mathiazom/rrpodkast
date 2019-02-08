package com.rrpm.mzom.projectrrpm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Laget av Mathias Myklebust
 */

public class PodListFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "RRP-PodListFragment";

    // PODLIST MODE CONSTANTS
    private static final int ALL_PODCASTS = 0;
    private static final int OFFLINE_ONLY_PODCASTS = 1;
    private static final int ARCHIVE_ONLY_PODCASTS = 2;

    // PRESENT PODLIST MODE
    private int podListMode;

    private View view;

    private static ArrayList<RRPod> allpods;

    private PodListFragmentListener podListFragmentListener;

    private boolean downloading;

    private ArrayList<RRPod> selectedPods = new ArrayList<>();

    private int mDay = 0;
    private int mMonth = 0;
    private int mYear = 2011;


    public static PodListFragment newInstance(int podListMode) {
        PodListFragment fragment = new PodListFragment();
        fragment.podListMode = podListMode;
        return fragment;
    }

    // "LIFECYCLE OVERRIDES"
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            podListFragmentListener = (PodListFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PodListFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        view = inflater.inflate(R.layout.podlist_fragment, container, false).findViewById(R.id.drawerMainContent);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view = getView();

        if (savedInstanceState != null && savedInstanceState.getBoolean("isDownloadingInList")) {
            startLoadingScreen();
        }

        switch (podListMode){
            case ALL_PODCASTS:
                podListFragmentListener.toolbarTextChange("Alle podkaster");
                break;
            case OFFLINE_ONLY_PODCASTS:
                podListFragmentListener.toolbarTextChange("Nedlastede podkaster");
                break;
            case ARCHIVE_ONLY_PODCASTS:
                podListFragmentListener.toolbarTextChange("Arkiv-podkaster");
                break;
        }

        podListFragmentListener.OnPodBuild(podListMode);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDownloadingInList", downloading);
    }


    // SETTERS AND GETTERS
    public void setAllpods(final ArrayList<RRPod> _allpods) {
        allpods = _allpods;
    }

    // MAIN METHODS
    void buildPodcastViews(final Context ctx) {
        buildPodcastViewsWithDate(ctx, mDay, mMonth, mYear,false);
    }

    void buildPodcastViewsWithDate(final Context ctx, final int day, final int month, final int year){
        buildPodcastViewsWithDate(ctx, day, month, year,false);
    }

    void buildPodcastViewsListenedTo(final Context ctx){
        buildPodcastViewsWithDate(ctx, mDay, mMonth, mYear,true);
    }

    void buildPodcastViewsWithDate(final Context ctx, final int day, final int month, final int year, boolean notListenedTo) {

        // SAVE SCROLLING POSITION
        final ListView listView = (ListView) view.findViewById(R.id.podListView);
        int index = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());

        mDay = day;
        mMonth = month;
        mYear = year;

        if (!isOnline()) {
            final TextView offline_msg = (TextView) view.findViewById(R.id.offline_msg_text);
            offline_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isOnline()) podListFragmentListener.OnPodBuild(podListMode);
                }
            });
            final LinearLayout placeholder = (LinearLayout) view.findViewById(R.id.connection_msg_placeholder);
            placeholder.setVisibility(View.VISIBLE);

            final LinearLayout subcont = (LinearLayout) view.findViewById(R.id.connection_msg_subplaceholder);
                subcont.setAlpha(0);
            subcont.animate().setDuration(500).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    subcont.setVisibility(View.VISIBLE);
                }
            });

            podListMode = OFFLINE_ONLY_PODCASTS;
        } else {
            view.findViewById(R.id.connection_msg_placeholder).setVisibility(View.GONE);
        }

        int curr_mo = -1;
        int curr_ye = -1;

        final ArrayList<RRPod> validPods = new ArrayList<>();

        ArrayList<Integer> availableYears = new ArrayList<>();
        ArrayList<Integer> availableMonths = new ArrayList<>();
        ArrayList<Integer> availableDays = new ArrayList<>();

        final SharedPreferences podkastStorage = PreferenceManager.getDefaultSharedPreferences(ctx);

        final File dir = new File(getContext().getFilesDir(),"RR-Podkaster");

        selectedPods = new ArrayList<>();

        Log.i(TAG,"Pods: " + allpods);

        for (RRPod currPod : allpods) {

            currPod.unSelect();

            String podName = currPod.getTitle();

            final boolean downloaded = new File(dir + File.separator + podName).exists();

            Log.i(TAG,"Pod downloaded: " + downloaded);

            currPod.setDownloadedState(downloaded);

            currPod.setListenedToState(podkastStorage.getBoolean(podName + "(LT)", false));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currPod.getDateObj());

            boolean inRangeDay = calendar.get(Calendar.DAY_OF_MONTH) == day;
            boolean inRangeMonth = (calendar.get(Calendar.MONTH) + 1) == month;
            boolean inRangeYear = calendar.get(Calendar.YEAR) == year;

            boolean inRange = ((day == 0) || (inRangeDay)) && ((month == 0) || (inRangeMonth)) && ((year == 2011) || (inRangeYear)) && (!(podListMode == OFFLINE_ONLY_PODCASTS) || currPod.getDownloadState());

            boolean validState = !currPod.getListenedToState() || !notListenedTo;

            if (inRange && validState) {
                int this_mo = currPod.getMonth();
                int this_ye = currPod.getYear();
                boolean isMonthEnd = this_mo != curr_mo || this_ye != curr_ye;
                currPod.setIsMonthEnd(isMonthEnd);
                if (isMonthEnd) {
                    if (!availableYears.contains(this_ye)) availableYears.add(this_ye);
                    if (!availableMonths.contains(this_mo)) availableMonths.add(this_mo);
                    curr_mo = this_mo;
                    curr_ye = this_ye;
                }
                int this_day = calendar.get(Calendar.DAY_OF_MONTH);
                if (!availableDays.contains(this_day)) availableDays.add(this_day);
                validPods.add(currPod);
            }


        }

        LinearLayout no_result_msg = (LinearLayout) view.findViewById(R.id.no_result_msg);

        if (validPods.size() == 0) {
            if (podListMode == OFFLINE_ONLY_PODCASTS && (day == 0 && month == 0 && year == 2011)) {
                ((TextView) no_result_msg.findViewById(R.id.no_result_msg_text)).setText(getContext().getResources().getString(R.string.no_result_msg_offline));
            }
            no_result_msg.setVisibility(View.VISIBLE);
        } else no_result_msg.setVisibility(View.GONE);

        final RRPod[] pods = validPods.toArray(new RRPod[validPods.size()]);
        listView.setAdapter(new PodArrayAdapter(ctx, pods));

        selectedTitleUpdate();

        // CLICK LISTENERS
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RRPod pod = pods[position];

                ConstraintLayout content = (ConstraintLayout) view.findViewById(R.id.podcastViewContent);

                if (selectedPods.size() > 0) {
                    pod.toggleSelectionState();
                    if (pod.getSelectionState()) {
                        selectedPods.add(pod);
                        if (pod.getDownloadState()) {
                            content.setBackgroundResource(R.drawable.selected_downloaded);
                        } else {
                            content.setBackgroundResource(R.drawable.selected);
                        }
                    } else {
                        selectedPods.remove(pod);
                        if (pod.getDownloadState()) {
                            if (pod.getListenedToState()) {
                                content.setBackgroundResource(R.drawable.downloaded_listened);
                            } else {
                                content.setBackgroundResource(R.drawable.downloaded_streamable);
                            }

                        } else if (pod.getListenedToState()) {
                            content.setBackgroundResource(R.color.listened);
                        } else {
                            content.setBackgroundResource(R.color.streamable);
                        }
                        if (selectedPods.size() == 0) {
                            podListFragmentListener.OnDisableSelectionMode();
                        } else {
                            podListFragmentListener.OnEnableSelectionMode();
                        }
                    }
                    selectedTitleUpdate();
                    return;
                }
                podListFragmentListener.OnPlayOrStreamPod(pods[position]);


            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RRPod pod = pods[position];
                pod.toggleSelectionState();
                ConstraintLayout content = (ConstraintLayout) view.findViewById(R.id.podcastViewContent);

                selectedPods = selectedPods == null ? new ArrayList<RRPod>() : selectedPods;

                if (pod.getSelectionState()) {
                    selectedPods.add(pod);
                    if (pod.getDownloadState()) {
                        content.setBackgroundResource(R.drawable.selected_downloaded);
                    } else {
                        content.setBackgroundResource(R.drawable.selected);
                    }
                } else {
                    selectedPods.remove(pod);
                    if (pod.getDownloadState()) {
                        if (pod.getListenedToState()) {
                            content.setBackgroundResource(R.drawable.downloaded_listened);
                        } else {
                            content.setBackgroundResource(R.drawable.downloaded_streamable);
                        }

                    } else if (pod.getListenedToState()) {
                        content.setBackgroundResource(R.color.listened);
                    } else {
                        content.setBackgroundResource(R.color.streamable);
                    }
                }

                selectedTitleUpdate();

                return true;
            }
        });

        // RETAIN SCROLLING POSITION
        listView.setSelectionFromTop(index, top);

        // BUILD FINISHED
        podListFragmentListener.OnPodcastViewsBuilt(validPods.size());
    }

    private String addZero(int i){
        String s = String.valueOf(i);
        return s.length() > 1 ? s : "0" + s;
    }

    void selectedTitleUpdate(){
        if (selectedPods.size() == 0) {
            podListFragmentListener.OnDisableSelectionMode();
        } else {
            podListFragmentListener.OnEnableSelectionMode();
        }
    }

    ArrayList<RRPod> getSelectedPods() {
        return this.selectedPods;
    }

    int getPodNum(){
        return ((ListView)view.findViewById(R.id.podListView)).getCount();
    }

    // OFFLINE MODE GETTER

    void setPodListMode(int podListMode){
        this.podListMode = podListMode;
    }

    int getPodListMode(){
        return this.podListMode;
    }

    // INTERFACE
    interface PodListFragmentListener {
        void OnPlayOrStreamPod(RRPod pod);

        void OnPodcastViewsBuilt(int validpods);

        void OnPodBuild(int podListMode);

        void toolbarTextChange(String title);

        void OnEnableSelectionMode();

        void OnDisableSelectionMode();
    }

    // MISC
    private boolean isOnline() {
        if (getContext() == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // DOWNLOAD PROGRESS METHODS
    boolean viewAvailable() {
        return view != null;
    }

    boolean getLoadingState() {
        return this.downloading;
    }

    void startLoadingScreen() {
        downloading = true;
        (view.findViewById(R.id.loading_prog_cont)).setVisibility(View.VISIBLE);
    }

    void setLoadingText(String text) {
        ((TextView) view.findViewById(R.id.loading_prog_text)).setText(text);
    }

    void setLoadingProgress(int progress) {
        MaterialProgressBar mpb = (MaterialProgressBar) (view.findViewById(R.id.loading_prog));
        mpb.setProgress(progress);
    }

    void stopLoadingScreen() {
        downloading = false;
        (view.findViewById(R.id.loading_prog_cont)).setVisibility(View.GONE);
    }

}
