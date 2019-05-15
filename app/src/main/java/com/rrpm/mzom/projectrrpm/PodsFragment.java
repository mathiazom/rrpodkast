package com.rrpm.mzom.projectrrpm;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Objects;

public class PodsFragment extends Fragment implements FilterFragment.FilterFragmentListener {


    private static final String TAG = "RRP-PodsFragment";

    private View view;

    private RRPod playerPod;

    private ArrayList<RRPod> pods;

    private ArrayList<RRPod> filteredPods;

    private PodsRecyclerAdapter podsAdapter;

    private FragmentLoader fragmentLoader;

    private FragmentLoadingHandler fragmentLoadingHandler;

    private PodsFilter podsFilter = PodsFilter.noFilter();

    private boolean isFiltering;

    private FilterFragment filterFragment;

    private SelectedPodViewModel selectedPodViewModel;

    private int savedScrolledItemPosition;


    static PodsFragment newInstance() {

        final PodsFragment fragment = new PodsFragment();

        fragment.filteredPods = new ArrayList<>();

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        fragmentLoader = new FragmentLoader(getChildFragmentManager(), () -> Log.i(TAG,"Back stack change: " + getChildFragmentManager().getBackStackEntryCount()));

        try {
            fragmentLoadingHandler = (FragmentLoadingHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentLoadingHandler");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_pods, container, false);

        loadToolbarActions();

        initPodsRecycler();

        final PodsViewModel podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);
        podsViewModel.retrievePods(RRReader.PodType.MAIN_PODS).observe(this, this::setPods);

        selectedPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SelectedPodViewModel.class);

        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlayerPodViewModel.class);
        playerPodViewModel.getPlayerPod().observe(this, playerPod -> {

            this.playerPod = playerPod;

            if(podsAdapter != null){
                podsAdapter.notifyDataSetChanged();
            }

        });

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();

        initPodsRecycler();

        if(playerPod != null){
            updatePodItemView(playerPod);
        }

    }

    @Override
    public void onPause() {

        super.onPause();

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) podsRecycler.getLayoutManager();

        if(linearLayoutManager != null){
            savedScrolledItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }

    }

    void setPods(@NonNull final ArrayList<RRPod> pods){

        this.pods = pods;

        filteredPods = pods;

        if(podsAdapter != null){
            podsAdapter.setPods(pods);
            podsAdapter.notifyDataSetChanged();
        }

    }

    private void loadToolbarActions() {

        final ImageView showFilterAction = view.findViewById(R.id.pod_filter_toggle);
        showFilterAction.setOnClickListener(view -> setFilterVisibility(!isFiltering));

    }

    private void displayFilterIcon(boolean isFiltering){

        final ImageView filterToggle = view.findViewById(R.id.pod_filter_toggle);

        if (isFiltering) {
            filterToggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_exit));
        } else {
            filterToggle.setImageDrawable(getResources().getDrawable(R.drawable.ic_pod_filter));
        }

    }

    private void setFilterVisibility(boolean visible){

        if (visible) {

            showFilterFragment();

            isFiltering = true;

        } else {

            hideFilterFragment();

            isFiltering = false;

        }

        displayFilterIcon(visible);

    }

    private void showFilterFragment(){

        if(filterFragment != null && filterFragment.isHidden()){

            Log.i(TAG,"Showing filterFragment");

            fragmentLoader.showFragment(
                    filterFragment,
                    R.anim.enter_from_bottom,
                    R.anim.exit_to_bottom,
                    R.anim.enter_from_top,
                    R.anim.exit_to_top
            );

        }else{

            filterFragment = FilterFragment.newInstance(podsFilter,this);

            fragmentLoader.loadFragment(
                    R.id.frame_top,
                    filterFragment,
                    R.anim.enter_from_bottom,
                    R.anim.exit_to_bottom,
                    R.anim.enter_from_top,
                    R.anim.exit_to_top,
                    true
            );

        }

    }

    @Override
    public void hideFilterFragment(){

        if(filterFragment == null || !filterFragment.isVisible()){
            // Already not visible
            return;
        }

        fragmentLoader.hideFragment(
                filterFragment,
                R.anim.enter_from_bottom,
                R.anim.exit_to_bottom,
                R.anim.enter_from_top,
                R.anim.exit_to_top
        );

    }

    private void initPodsRecycler() {

        final RecyclerView podsRecycler = view.findViewById(R.id.podsRecycler);
        podsRecycler.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        podsRecycler.setLayoutManager(linearLayoutManager);

        final PlayerPodViewModel playerPodViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlayerPodViewModel.class);

        podsAdapter = new PodsRecyclerAdapter(filteredPods, playerPodViewModel, pod ->{
            setFilterVisibility(false);
            selectedPodViewModel.selectPod(pod);
            fragmentLoadingHandler.loadPodFragment();
        });

        podsAdapter.setHasStableIds(true);

        podsRecycler.setAdapter(podsAdapter);

        podsRecycler.startLayoutAnimation();

        // Restore scrolling position
        if(savedScrolledItemPosition != RecyclerView.NO_POSITION && savedScrolledItemPosition < filteredPods.size()){
            linearLayoutManager.scrollToPosition(savedScrolledItemPosition);
        }

    }

    @Override
    public ArrayList<RRPod> loadFilteredPods(@NonNull final PodsFilter filter) {

        this.podsFilter = filter;

        filteredPods.clear();

        filteredPods.addAll(filter.filter(pods));

        podsAdapter.notifyDataSetChanged();

        return filteredPods;

    }

    private void updatePodItemView(@NonNull final RRPod pod) {

        final ArrayList<RRPod> pods = podsAdapter.getPods();

        int index = pods.indexOf(pod);

        if (index == -1) {
            for (RRPod p : pods) {
                if (p.getId().equals(pod.getId())) {

                    index = pods.indexOf(p);

                    break;
                }
            }
        }

        if (index == -1) {
            throw new RuntimeException("Pod (" + pod + ") could not be found in pods (size: " + pods.size() + ")");
        }

        pods.set(index, pod);

        podsAdapter.notifyItemChanged(index);

    }


    boolean onBackPress(){

        final FragmentManager childFragmentManager = getChildFragmentManager();

        if(childFragmentManager.getBackStackEntryCount() > 0){

            final String fragName = childFragmentManager.getBackStackEntryAt(0).getName();

            // Check if FilterFragment will be removed
            if(fragName != null && fragName.equals(FilterFragment.class.getSimpleName())){
                isFiltering = !filterFragment.isVisible();
                displayFilterIcon(isFiltering);
            }

            childFragmentManager.popBackStack();

            return true;

        }

        return false;

    }

}
