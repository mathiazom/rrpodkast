package com.rrpm.mzom.projectrrpm.poddownloading;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.podstorage.PodStorageConstants;
import com.rrpm.mzom.projectrrpm.pod.PodId;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.Nullable;

public class PodDownloadService extends IntentService {

    private static final String TAG = "RRP-PodDownloadService";

    private static final int DOWNLOAD_BUFFER_SIZE = 32768;

    private static final int STATUS_RUNNING = 0;
    static final int STATUS_FINISHED = 1;
    static final int STATUS_ERROR = 2;
    static final int STATUS_PROGRESS = 3;

    public PodDownloadService() {
        super(PodDownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent == null){

            Log.e(TAG,"Intent was null");

            return;

        }

        final ResultReceiver receiver = intent.getParcelableExtra(DownloadingConstants.DOWNLOAD_REQUEST_RECEIVER);

        final PodId id = intent.getParcelableExtra(DownloadingConstants.DOWNLOAD_REQUEST_POD_ID);

        if(id == null){

            Log.e(TAG,"Pod id was null");

            return;

        }

        final String url = intent.getStringExtra(DownloadingConstants.DOWNLOAD_REQUEST_POD_URL);

        if(url == null){

            Log.e(TAG,"Pod url was null");

            return;

        }


        Log.i(TAG,"Downloading pod from " + url);

        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {

            downloadData(id,url,receiver);

        } catch (Exception e) {

            Log.e(TAG,"Download error",e);

            final Bundle bundle = new Bundle();
            bundle.putString(Intent.EXTRA_TEXT, e.toString());

            receiver.send(STATUS_ERROR, bundle);

        }

        this.stopSelf();
    }

    private void downloadData(final PodId podId, final String urlString, final ResultReceiver resultReceiver) throws IOException, DownloadException {

        final URL url = new URL(urlString);

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("GET");

        // Check if response code was something other than 200 (OK)
        if(urlConnection.getResponseCode() != 200){

            throw new DownloadException("Bad response code, failed to fetch data");

        }

        // Directory for storage of downloaded pods
        final File downloadDirectory = new File(getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);

        // Make sure directory exists
        if(!downloadDirectory.exists()){

            // Attempt to create the missing directory
            if(!downloadDirectory.mkdir()){

                throw new DownloadException("Directory \"" + downloadDirectory.toString() + "\" could not be created");

            }

        }

        // Specify the download location
        final File downloadLocation = new File(downloadDirectory, podId.toString());

        final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
        final FileOutputStream outputStream = new FileOutputStream(downloadLocation);

        byte[] readBuffer = new byte[DOWNLOAD_BUFFER_SIZE];
        int bytesToWrite;
        float progress;

        final Bundle bundle = new Bundle();

        // Keep reading till the end of the input stream
        while ((bytesToWrite = inputStream.read(readBuffer)) > 0) {

            // Write pod data to the designated "downloadLocation"
            outputStream.write(readBuffer, 0, bytesToWrite);

            progress = ((float) downloadLocation.length() / (float) urlConnection.getContentLength()) * DownloadingConstants.DOWNLOAD_PROGRESS_MAX;

            bundle.putFloat(DownloadingConstants.DOWNLOAD_PROGRESS_TAG, progress);

            resultReceiver.send(STATUS_PROGRESS,bundle);

        }

        // Close all opened streams
        inputStream.close();
        outputStream.close();

        resultReceiver.send(STATUS_FINISHED, Bundle.EMPTY);

    }

    private class DownloadException extends Exception {

        private DownloadException(String message) {
            super(message);
        }

    }
}