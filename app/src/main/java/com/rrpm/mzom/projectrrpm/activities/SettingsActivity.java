package com.rrpm.mzom.projectrrpm.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalError;
import com.rrpm.mzom.projectrrpm.podfeed.PodsRetrievalCallback;
import com.rrpm.mzom.projectrrpm.podstorage.PodsPackage;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "RRP-SettingsActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final PodsViewModel podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);
        podsViewModel.requestPodsPackage(
                PodType.values(), // Request all pod types
                new PodsRetrievalCallback.PodsPackageRetrievalCallback() {
                    @Override
                    public void onPodsPackageRetrieved(@NonNull PodsPackage podsPackage) {
                        loadInfo(podsPackage);
                    }

                    @Override
                    public void onFail(@NonNull PodsRetrievalError error) {

                        Log.e(TAG,"Failed retrieval of pods package: " + error.getMessage());

                    }
                }
        );

        initToolbar();

    }


    private void loadInfo(@NonNull final PodsPackage podsPackage) {

        float spaceUsage = new PodStorageHandle(this).calculateStorageSpaceUsage(podsPackage);
        int totalDownloaded = PodUtils.totalDownloadedPods(podsPackage);

        final String spaceUsageText = getResources().getString(R.string.pods_space_usage_text,spaceUsage,totalDownloaded);

        final TextView spaceUsageTextView = findViewById(R.id.space_usage_text);
        spaceUsageTextView.setText(spaceUsageText);

    }

    private void initToolbar(){

        final ImageView backButton = findViewById(R.id.toolbarBackAction);
        backButton.setOnClickListener(v -> finish());

    }




}
