package com.rrpm.mzom.projectrrpm.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.podfiltering.PodFilter;
import com.rrpm.mzom.projectrrpm.ui.PodsRecyclerAdapter;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.podplayer.PlayerPodViewModel;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilterViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import java.util.ArrayList;

public class PodListFragment extends Fragment {


    private static final String TAG = "RRP-PodListFragment";


    private View view;


    private PodType podType;

    private ArrayList<RRPod> podList;
    private ArrayList<RRPod> filteredPodList;

    private PodFilter podFilter;


    private PodsRecyclerAdapter podsAdapter;


    private MainFragmentsHandler mainFragmentsHandler;


    // Saved state to restore scrolling position in onResume()
    private Parcelable savedLinearLayoutManagerState;


    public PodListFragment(){

        this.podList = new ArrayList<>();

        this.filteredPodList = new ArrayList<>();

    }

    @NonNull
    static PodListFragment newInstance(@NonNull MainFragmentsHandler mainFragmentsHandler, @NonNull final PodType podType) {

        final PodListFragment fragment = new PodListFragment();

        fragment.mainFragmentsHandler = mainFragmentsHandler;
        fragment.podType = podType;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_podlist, container, false);

        initToolbar();

        final PodsViewModel podsViewModel = ViewModelProviders.of(requireActivity()).get(PodsViewModel.class);
        podsViewModel.assureObservablePodList(podType).observe(this, podList -> {

            if(podList == null){

                Log.e(TAG,"Observed pod list was null");

                return;

            }

            this.podList = podList;

            displayPods();

        });

        final PodFilterViewModel podListFilterViewModel = ViewModelProviders.of(requireActivity()).get(PodFilterViewModel.class);
        podListFilterViewModel.resetPodFilter();
        podListFilterViewModel.getObservablePodFilter().observe(this, podFilter -> {

            if(podFilter == null){

                return;

            }

            this.podFilter = podFilter;

            displayPods();

        });

        return view;

    }


    @Override
    public void onResume() {

        super.onResume();

        initPodsRecycler();

    }

    @Override
    public void onPause() {

        super.onPause();

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) podsRecycler.getLayoutManager();

        if(linearLayoutManager != null){

            savedLinearLayoutManagerState = linearLayoutManager.onSaveInstanceState();

        }

    }


    @NonNull
    public PodType getPodType(){

        return this.podType;

    }

    private void displayPods(){

        if(podsAdapter == null){

            // Pods adapter not initialized, recycler needs to be initialized first
            initPodsRecycler();

            return;

        }

        filterPodListIfNeeded(podFilter,podList,filteredPodList);

        // Pods adapter uses the newly updated filteredPodList as data set
        podsAdapter.notifyDataSetChanged();

        setFilterActionClickable(true);

    }


    private void initPodsRecycler() {

        if(podList.isEmpty()){

            Log.i(TAG,"Pod list was empty, will not load recycler");

            setFilterActionClickable(false);

            return;

        }

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);
        podsRecycler.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());

        if(savedLinearLayoutManagerState != null){

            // Restore scrolling state
            linearLayoutManager.onRestoreInstanceState(savedLinearLayoutManagerState);

        }

        podsRecycler.setLayoutManager(linearLayoutManager);

        filterPodListIfNeeded(podFilter,podList,filteredPodList);

        podsAdapter = new PodsRecyclerAdapter(
                filteredPodList,
                ViewModelProviders.of(requireActivity()).get(PlayerPodViewModel.class),
                pod -> {
                    mainFragmentsHandler.hideFilterFragment();
                    mainFragmentsHandler.loadPodFragment(pod);
                }
        );

        podsRecycler.setAdapter(podsAdapter);

        podsRecycler.startLayoutAnimation();

        setFilterActionClickable(true);

    }


    /**
     *
     * Filters a given {@param podList} according to a given {@param podFilter}.
     * The result is stored in a given {@param filteredPodList}.
     *
     * @param podFilter: A {@link PodFilter} handling the filtering process.
     *                 If {@param podFilter} is null, no filtering will be performed, and the {@param filteredPodList} will
     *                 practically become a copy of {@param podList}.
     *
     * @param podList: An {@link ArrayList<RRPod>} holding the pods that will go through the filtering process
     *
     * @param filteredPodList: An {@link ArrayList<RRPod>} to hold the filtered pods
     *
     */

    private void filterPodListIfNeeded(@Nullable final PodFilter podFilter, @NonNull final ArrayList<RRPod> podList, @NonNull final ArrayList<RRPod> filteredPodList){

        if(podFilter == null){

            // No filter, copy original pod list
            filteredPodList.clear();
            filteredPodList.addAll(podList);

            return;
        }

        podFilter.filter(podList,filteredPodList);

    }


    private void initToolbar() {

        final TextView toolbarTitleView = (view.findViewById(R.id.toolbar_title));

        switch (podType){
            case MAIN_PODS:
                toolbarTitleView.setText(getResources().getText(R.string.list_drawer_item_pod_list));
                break;
            case ARCHIVE_PODS:
                toolbarTitleView.setText(getResources().getText(R.string.list_drawer_item_highlights));
        }

        final ImageView showFilterAction = view.findViewById(R.id.podFilterAction);
        showFilterAction.setOnClickListener(view -> mainFragmentsHandler.loadFilterFragment());

    }

    private void setFilterActionClickable(boolean clickable){

        final ImageView filterAction = view.findViewById(R.id.podFilterAction);
        filterAction.setClickable(clickable);
        filterAction.setImageAlpha(clickable ? 255 : 100);

    }

}
