package com.rrpm.mzom.projectrrpm.fragments;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainFragmentsHandlerViewModel extends ViewModel {

    private static final String TAG = "RRP-MainFragsHandlerVM";



    @NonNull private MutableLiveData<MainFragmentsHandler> observableMainFragmentsHandler = new MutableLiveData<>();

    private MainFragmentsHandler savedMainFragmentsHandle;


    @NonNull
    public LiveData<MainFragmentsHandler> getObservableMainFragmentsHandler(){

        return this.observableMainFragmentsHandler;

    }


    public void saveAndInvalidateHandle(){

        // Save instance for later restoration
        this.savedMainFragmentsHandle = observableMainFragmentsHandler.getValue();

        // Invalidate observable to prevent further usage
        observableMainFragmentsHandler.setValue(null);

    }


    @NonNull
    public MainFragmentsHandler restoreHandleFromActivity(@NonNull FragmentActivity fragmentActivity){

        // Check if any handle has been saved for later restoration
        if (savedMainFragmentsHandle != null) {

            Log.i(TAG,"Restoring saved fragments handle");

            // Update handle with new activity
            savedMainFragmentsHandle.setActivity(fragmentActivity);

            // Restore instance for use by any observers
            observableMainFragmentsHandler.setValue(savedMainFragmentsHandle);

            return savedMainFragmentsHandle;

        }


        // No saved instance, start from scratch

        final MainFragmentsHandler newHandle = new MainFragmentsHandler(fragmentActivity);

        observableMainFragmentsHandler.setValue(newHandle);

        return newHandle;

    }
}
