package com.rrpm.mzom.projectrrpm.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.rrpm.mzom.projectrrpm.podstorage.PodsPackage;
import com.rrpm.mzom.projectrrpm.R;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.rss.RRReader;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;
import com.rrpm.mzom.projectrrpm.podstorage.PodsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

public class SettingsActivity extends AppCompatActivity {

    //private static final String TAG = "RRP-SettingsActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final PodsViewModel.PodsPackageRequest podsPackageRequest = new PodsViewModel.PodsPackageRequest(
                RRReader.PodType.values(), // Request all pod types
                this::loadInfo
        );

        final PodsViewModel podsViewModel = ViewModelProviders.of(this).get(PodsViewModel.class);
        podsViewModel.requestPodsPackage(podsPackageRequest);

        initToolbar();

    }


    private void loadInfo(@NonNull final PodsPackage podsPackage) {

        float spaceUsage = new PodStorageHandle(this).calculateSpaceUsage(podsPackage);
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
