package com.rrpm.mzom.projectrrpm.fragments;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class MainFragmentsHandler implements MainFragmentsLoaderInterface {


    private static final String TAG = "RRP-MainFragmentManager";


    @NonNull
    private final FragmentLoader fragmentLoader;

    @NonNull
    private final FragmentActivity fragmentActivity;

    @NonNull
    private final PodDownloader podDownloader;


    private PodsViewModel podsViewModel;
    private SelectedPodViewModel selectedPodViewModel;
    private PodFilterViewModel podListFilterViewModel;


    private SmallPodPlayerFragment smallPodPlayerFragment;
    private PodListFragment podListFragment;
    private PodFilterFragment filterFragment;
    private PodFragment podFragment;
    private PodPlayerFragment podPlayerFragment;

    public MainFragmentsHandler(@NonNull final FragmentActivity fragmentActivity, @NonNull final PodDownloader podDownloader){

        this.fragmentActivity = fragmentActivity;

        this.podDownloader = podDownloader;

        this.fragmentLoader = new FragmentLoader(fragmentActivity.getSupportFragmentManager());

        this.podsViewModel = ViewModelProviders.of(fragmentActivity).get(PodsViewModel.class);

        this.selectedPodViewModel = ViewModelProviders.of(fragmentActivity).get(SelectedPodViewModel.class);

        this.podListFilterViewModel = ViewModelProviders.of(fragmentActivity).get(PodFilterViewModel.class);

    }


    public void loadPodListFragment(@NonNull PodType podType){

        this.podListFragment = PodListFragment.newInstance(this,podType);

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



    @Override
    public void loadPodFragment(@NonNull final RRPod pod) {

        final RRPod selectedPod = selectedPodViewModel.getSelectedPodObservable().getValue();

        if(selectedPod == null || !selectedPod.getId().equals(pod.getId())){

            selectedPodViewModel.selectPod(pod);

        }

        if (podFragment == null || !podFragment.isAdded()) {

            podFragment = PodFragment.newInstance(podDownloader);

            fragmentLoader.loadFragment(
                    R.id.frame_main,
                    podFragment,
                    FragmentAnimationConstants.POD_FRAGMENT_ANIMATIONS,
                    true);

        }

    }

    @Override
    public void loadPodPlayerFragment() {

        podPlayerFragment = PodPlayerFragment.newInstance(this);

        fragmentLoader.loadFragment(
                R.id.frame_overlay,
                podPlayerFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS,
                true);


    }

    @Override
    public void hidePodPlayerFragment() {

        fragmentLoader.hideFragment(
                podPlayerFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS
        );

    }

    @Override
    public void loadSmallPodPlayerFragment() {

        if(smallPodPlayerFragment == null){

            smallPodPlayerFragment = SmallPodPlayerFragment.newInstance(this);

        }

        if(smallPodPlayerFragment.isAdded()){

            fragmentLoader.showFragment(smallPodPlayerFragment);

        }else{

            fragmentLoader.loadFragment(R.id.frame_podplayer, smallPodPlayerFragment, false);

        }

    }

    @Override
    public void hideSmallPodPlayerFragment() {

        if(smallPodPlayerFragment == null){

            Log.e(TAG,"SmallPodPlayerFragment was null, will not attempt to hide it.");

            return;

        }

        if(!smallPodPlayerFragment.isVisible()){

            Log.e(TAG,"SmallPodPlayerFragment already hidden.");

        }

        fragmentLoader.hideFragment(smallPodPlayerFragment);

    }

    @Override
    public void loadFilterFragment() {

        if(!prepareMinimumPodFilter()){

            Log.e(TAG,"Failed to prepare pod filter, will not load PodFilterFragment");

            return;

        }

        if(filterFragment != null && filterFragment.isHidden()){

            fragmentLoader.showFragment(
                    filterFragment,
                    FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS
            );

        }else{

            filterFragment = PodFilterFragment.newInstance(this);

            fragmentLoader.loadFragment(
                    R.id.frame_overlay,
                    filterFragment,
                    FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS,
                    true
            );

        }

    }

    private boolean prepareMinimumPodFilter(){

        final ArrayList<RRPod> podList = podsViewModel.getPodList(podListFragment.getPodType());

        if(podList == null || podList.isEmpty()){
            return false;
        }

        if(podListFilterViewModel.getPodFilter() == null){

            podListFilterViewModel.setPodFilter(
                    new PodFilter(PodUtils.getDateRangeFromPodList(podList))
            );

        }

        return true;

    }

    @Override
    public void hideFilterFragment() {

        if(filterFragment == null || !filterFragment.isVisible()){
            // Already not visible
            return;
        }

        fragmentLoader.hideFragment(
                filterFragment,
                FragmentAnimationConstants.FILTER_FRAGMENT_ANIMATIONS
        );

    }
}
