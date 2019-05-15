package com.rrpm.mzom.projectrrpm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "RRP-SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        PodsRepository.getInstance().retrievePods(this,RRReader.PodType.MAIN_PODS, pods -> {

            if(pods != null){

                loadInfo(pods);

            }
        });

    }


    private void loadInfo(@NonNull final ArrayList<RRPod> pods){

        float spaceUsage = getSpaceUsage(pods);
        int totalDownloaded = getTotalDownloadedPods(pods);

        final String spaceUsageText = String.valueOf(spaceUsage) + " MB" + " (" + String.valueOf(totalDownloaded) + " " + getString(R.string.episoder) + ")";

        final TextView spaceUsageTextView = findViewById(R.id.space_usage_text);
        spaceUsageTextView.setText(spaceUsageText);

        /*final SharedPreferences settings_prefs = getSharedPreferences(SETTINGS_PREFS_NAME,0);
        settings_prefs.edit().putInt("INFO_PODNUM",totalDownloaded).apply();
        settings_prefs.edit().putFloat("INFO_SPACEUSAGE",spaceUsage).apply();*/


    }

    private float getSpaceUsage(@NonNull final ArrayList<RRPod> pods){

        float spaceUsage = 0;

        final File dir = new File(getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);

        for(int p = 0;p<pods.size();p++){
            if(pods.get(p).isDownloaded()) {
                spaceUsage += new File(dir,pods.get(p).getId().toString()).length() / Math.pow(1024, 2);

            }
        }

        return (float) Math.round(spaceUsage * 100) / 100;

    }

    private int getTotalDownloadedPods(@NonNull final ArrayList<RRPod> pods){

        int totalDownloaded = 0;

        for(final RRPod pod : pods){
            if(pod.isDownloaded()) totalDownloaded++;
        }

        return totalDownloaded;

    }


}
