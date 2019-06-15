package com.rrpm.mzom.projectrrpm.fragments;

import android.util.Log;

import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.poddownloading.PodDownloader;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilter;
import com.rrpm.mzom.projectrrpm.podfiltering.PodFilterViewModel;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerFragment;
import com.rrpm.mzom.projectrrpm.podplayer.PodPlayerFullFragment;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;
import com.rrpm.mzom.projectrrpm.podstorage.SelectedPodViewModel;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.ui.PodUIConstants;

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


    private PodPlayerFragment podPlayerFragment;
    private PodListFragment podListFragment;
    private PodFilterFragment filterFragment;
    private PodFragment podFragment;
    private PodPlayerFullFragment podPlayerFullFragment;


    public MainFragmentsHandler(@NonNull final FragmentActivity fragmentActivity, @NonNull final PodDownloader podDownloader){

        this.fragmentActivity = fragmentActivity;

        this.podDownloader = podDownloader;

        this.fragmentLoader = new FragmentLoader(fragmentActivity.getSupportFragmentManager());

        this.podsViewModel = ViewModelProviders.of(fragmentActivity).get(PodsViewModel.class);

        this.selectedPodViewModel = ViewModelProviders.of(fragmentActivity).get(SelectedPodViewModel.class);

        this.podListFilterViewModel = ViewModelProviders.of(fragmentActivity).get(PodFilterViewModel.class);

    }


    /**
     *
     * Attempts to load a pod list fragment correspondeing to the pod type of the last played pod.
     * If this fails, a pod list with {@link PodUIConstants#DEFAULT_POD_TYPE} will be loaded instead.
     *
     */

    public void loadLastPlayedOrDefaultPodListFragment(){

        final PodType lastPlayedPodType = new PodStorageHandle(fragmentActivity).getLastPlayedPodType();

        if(podListFragment != null){

            loadPodListFragment(podListFragment);

        }else if(lastPlayedPodType != null){

            loadPodListFragment(lastPlayedPodType);

        }else{

            loadPodListFragment(PodUIConstants.DEFAULT_POD_TYPE);

        }

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

            Log.i(TAG,"Selecting pod for PodFragment");

            Log.i(TAG,"Back stack pre selecting pod: " + String.valueOf(fragmentActivity.getSupportFragmentManager().getBackStackEntryCount()));

            selectedPodViewModel.selectPod(pod);

            Log.i(TAG,"Back stack post selecting pod: " + String.valueOf(fragmentActivity.getSupportFragmentManager().getBackStackEntryCount()));

        }

        if (podFragment == null || !podFragment.isAdded()) {

            Log.i(TAG,"Loading new PodFragment");

            podFragment = PodFragment.newInstance(podDownloader);

            fragmentLoader.loadFragment(
                    R.id.frame_main,
                    podFragment,
                    FragmentAnimationConstants.POD_FRAGMENT_ANIMATIONS,
                    true);
        }

    }

    @Override
    public void loadPodPlayerFullFragment() {

        podPlayerFullFragment = PodPlayerFullFragment.newInstance(this);

        fragmentLoader.loadFragment(
                R.id.frame_overlay,
                podPlayerFullFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS,
                true);


    }

    @Override
    public void hidePodPlayerFullFragment() {

        fragmentLoader.hideFragment(
                podPlayerFullFragment,
                FragmentAnimationConstants.POD_PLAYER_FULL_FRAGMENT_ANIMATIONS
        );

    }

    @Override
    public void loadPodPlayerFragment() {

        if(podPlayerFragment == null){
            podPlayerFragment = PodPlayerFragment.newInstance(this);
        }

        if(podPlayerFragment.isAdded()){
            fragmentLoader.showFragment(podPlayerFragment);
        }else{
            fragmentLoader.loadFragment(R.id.frame_podplayer, podPlayerFragment, false);
        }

    }

    @Override
    public void hidePodPlayerFragment() {

        if(podPlayerFragment == null){
            Log.e(TAG,"PodPlayerFragment was null, will not attempt to hide it.");
            return;
        }

        if(!podPlayerFragment.isVisible()){
            Log.e(TAG,"PodPlayerFragment already hidden.");
        }

        fragmentLoader.hideFragment(podPlayerFragment);

    }

    @Override
    public void loadFilterFragment() {

        if(!prepareMinimumPodFilter()){
            Log.e(TAG,"Failed to prepare pod filter, will not load PodFilterFragment");
            return;
        }

        if(filterFragment != null && filterFragment.isHidden()){

            Log.i(TAG,"Showing filterFragment");

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

            Log.i(TAG,"Pod filter was null, create new with widest date range");

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
