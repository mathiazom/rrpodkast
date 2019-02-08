package com.rrpm.mzom.projectrrpm;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {

    private static final String TAG = "RRP-DownloadService";

    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_FINISHED = 1;
    private static final int STATUS_ERROR = 2;
    private static final int PROGRESS_UPDATE = 3;

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    private String podName;

    private ResultReceiver receiver;

    final class Constants{
        static final String BROADCAST_ACTION =
                "com.rrpm.mzom.projectrrpm.BROADCAST";
        static final String EXTENDED_DATA_STATUS =
                "com.rrpm.mzom.projectrrpm.STATUS";
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        receiver = intent.getParcelableExtra("receiver");

        String url = intent.getStringExtra("url");

        Log.i("RRP-Download",url);

        podName = intent.getStringExtra("podName");

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            try {
                Uri uri = downloadData(url);
                bundle.putString("path", uri.getPath());
                receiver.send(STATUS_FINISHED, bundle);
            } catch (Exception e) {
                Log.i("RRP-Download","Download error",e);
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        this.stopSelf();
    }

    private Uri downloadData(String requestUrl) throws IOException, DownloadException {
        InputStream inputStream;
        HttpURLConnection urlConnection;

        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        if (statusCode == 200) {

            final File dir = new File(getFilesDir(),"RR-Podkaster");
            if(!dir.exists()){
                if(!dir.mkdir()) throw new DownloadException("Directory \"RR-Podkaster\" could not be created");
            }

            final File file = new File(dir,podName);

            Log.i(TAG,"File exists: " + file.exists());

            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            FileOutputStream f = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int len;
            float progress;

            while ((len = inputStream.read(buffer)) > 0) {
                f.write(buffer, 0, len);
                progress = ((float) file.length() / (float) urlConnection.getContentLength()) * 100;

                // UPDATE PROGRESS BAR
                //Bundle bundle = new Bundle();
                //bundle.putFloat("progress", progress);
                //receiver.send(PROGRESS_UPDATE, bundle);

                Intent progressIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS,progress).putExtra("DOWNLOADING_PODKAST_NAME",podName);
                LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

            }
            f.close();


            Uri uri = Uri.fromFile(getFileStreamPath(podName));

            Log.i("RRP-Download",String.valueOf(uri));

            return uri;
        } else {
            throw new DownloadException("Failed to fetch data");
        }
    }

    private class DownloadException extends Exception {

        private DownloadException(String message) {
            super(message);
        }
    }
}