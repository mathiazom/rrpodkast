package com.rrpm.mzom.projectrrpm.fragments;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilter;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilterViewModel;
import com.rrpm.mzom.projectrrpm.podplayer.SmallPodPlayerFragment;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerFragment;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.SelectedPodViewModel;
import com.rrpm.mzom.projectrrpm.pod.PodType;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class MainFragmentsHandler {


    private static final String TAG = "RRP-MainFragmentsHandlr";


    @NonNull
    private FragmentLoader fragmentLoader;


    private PodsViewModel podsViewModel;
    private SelectedPodViewModel selectedPodViewModel;
    private PodFilterViewModel podListFilterViewModel;


    private SmallPodPlayerFragment smallPodPlayerFragment;
    private PodListFragment podListFragment;
    private PodFilterFragment filterFragment;
    private PodFragment podFragment;
    private PodPlayerFragment podPlayerFragment;


    MainFragmentsHandler(@NonNull final FragmentActivity fragmentActivity){


        this.fragmentLoader = new FragmentLoader(fragmentActivity.getSupportFragmentManager());


        this.podsViewModel = ViewModelProviders.of(fragmentActivity).get(PodsViewModel.class);

        this.selectedPodViewModel = ViewModelProviders.of(fragmentActivity).get(SelectedPodViewModel.class);

        this.podListFilterViewModel = ViewModelProviders.of(fragmentActivity).get(PodFilterViewModel.class);


    }


    public void setActivity(@NonNull final FragmentActivity fragmentActivity){

        this.fragmentLoader = new FragmentLoader(fragmentActivity.getSupportFragmentManager());

    }


    public void loadPodListFragment(@NonNull PodType podType){

        this.podListFragment = PodListFragment.newInstance(podType);

        loadPodListFragment(podListFragment);

    }

    private void loadPodListFragment(@NonNull PodListFragment podListFragment){

        fragmentLoader.loadFragment(
                R.id.frame_main,
                podListFragment,
                FragmentAnimationConstants.POD_LIST_FRAGMENT_ANIMATIONS,
                false
        );

    }

    @Nullable
    public PodType getPodListPodType(){

        return podListFragment != null ? podListFragment.getPodType() : null;

    }


    public void loadPodFragment(@NonNull final RRPod pod){

        loadPodFragment(pod,FragmentAnimationConstants.POD_FRAGMENT_ANIMATIONS);

    }


    public void loadPodFragment(@NonNull final RRPod pod, @Nullable FragmentAnimations animations) {

        final RRPod selectedPod = selectedPodViewModel.getSelectedPodObservable().getValue();

        if(selectedPod == null || !selectedPod.getId().equals(pod.getId())){

            selectedPodViewModel.selectPod(pod);

        }

        if (podFragment == null || !podFragment.isAdded()) {

            podFragment = PodFragment.newInstance();

            fragmentLoader.loadFragment(
                    R.id.frame_main,
                    podFragment,
                    animations,
                    true);

        }

    }


    public void loadPodPlayerFragment() {

        podPlayerFragment = PodPlayerFragment.newInstance();

        fragmentLoader.loadFragment(
                R.id.frame_pod_player,
                podPlayerFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS,
                true);


    }


    public void hidePodPlayerFragment() {

        if(podPlayerFragment == null || !podPlayerFragment.isVisible()){

            // Fragment already hidden, no further actions required
            return;

        }

        fragmentLoader.hideFragment(
                podPlayerFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS
        );

    }



    public void loadSmallPodPlayerFragment() {

        if(smallPodPlayerFragment == null){

            smallPodPlayerFragment = SmallPodPlayerFragment.newInstance();

        }

        if(smallPodPlayerFragment.isAdded() && smallPodPlayerFragment.getFragmentManager() == fragmentLoader.getFragmentManager()){

            fragmentLoader.showFragment(smallPodPlayerFragment);

        }else{

            fragmentLoader.loadFragment(R.id.frame_small_pod_player, smallPodPlayerFragment, false);

        }

    }


    void loadFilterFragment() {

        final ArrayList<RRPod> podList = podsViewModel.getPodList(podListFragment.getPodType());

        if(podList == null){

            Log.e(TAG,"Pod list was null, will not load PodFilterFragment");

            return;

        }else if (podList.isEmpty()){

            Log.e(TAG,"Pod list was null, will not load PodFilterFragment");

            return;

        }

        final boolean filterIsPrepared = podListFilterViewModel.prepareMinimumPodFilter(podList);

        if(!filterIsPrepared){

            Log.e(TAG,"Failed to prepare pod filter, will not load PodFilterFragment");

            return;

        }

        // Check if filter fragment is already loaded, and can simply be shown.
        if(filterFragment != null && filterFragment.isHidden()){

            fragmentLoader.showFragment(
                    filterFragment,
                    FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS
            );

            return;

        }


        // Fresh loading of filter fragment is required

        filterFragment = PodFilterFragment.newInstance();

        fragmentLoader.loadFragment(
                R.id.frame_overlay,
                filterFragment,
                FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS,
                true
        );

    }


    void hideFilterFragment() {

        if(filterFragment == null || !filterFragment.isVisible()){

            // Fragment already hidden, no further actions required
            return;

        }

        fragmentLoader.hideFragment(
                filterFragment,
                FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS
        );

    }


    public void hideOverlayFragments(){

        hideFilterFragment();

        hidePodPlayerFragment();

    }


}
