package com.rrpm.mzom.projectrrpm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
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

    private static final int DOWNLOAD_BUFFER_SIZE = 32768;

    static final String DOWNLOAD_PROGRESS_TAG = "com.rrpm.mzom.projectrrpm.DownloadService.DOWNLOAD_PROGRESS_TAG";

    private static final int STATUS_RUNNING = 0;
    static final int STATUS_FINISHED = 1;
    static final int STATUS_ERROR = 2;
    static final int STATUS_PROGRESS = 3;

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final ResultReceiver receiver = intent.getParcelableExtra(PodDownloader.DOWNLOAD_REQUEST_RECEIVER);

        final PodId id = intent.getParcelableExtra(PodDownloader.DOWNLOAD_REQUEST_POD_ID);
        final String url = intent.getStringExtra(PodDownloader.DOWNLOAD_REQUEST_POD_URL);

        Log.i(TAG,"Downloading pod from " + url);

        final Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {

            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            try {
                downloadData(id,url,receiver);
                receiver.send(STATUS_FINISHED, bundle);
            } catch (Exception e) {
                Log.e(TAG,"Download error",e);
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        this.stopSelf();
    }

    private void downloadData(final PodId id, final String requestUrl, final ResultReceiver resultReceiver) throws IOException, DownloadException {

        URL url = new URL(requestUrl);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        if (statusCode == 200) {

            final File dir = new File(getApplicationContext().getFilesDir(), PodStorageConstants.POD_STORAGE_SUB_DIRECTORY + File.separator);
            if(!dir.exists()){
                if(!dir.mkdir()) throw new DownloadException("Directory \"" + dir.toString() + "\" could not be created");
            }

            final File file = new File(dir,id.toString());

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final FileOutputStream f = new FileOutputStream(file);

            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
            int len;
            float progress;

            final Bundle bundle = new Bundle();

            while ((len = inputStream.read(buffer)) > 0) {

                f.write(buffer, 0, len);
                progress = ((float) file.length() / (float) urlConnection.getContentLength()) * 100;

                bundle.putFloat(DOWNLOAD_PROGRESS_TAG,progress);
                resultReceiver.send(STATUS_PROGRESS,bundle);

            }
            f.close();

        } else {

            throw new DownloadException("Bad response code, failed to fetch data");

        }
    }

    private class DownloadException extends Exception {

        private DownloadException(String message) {
            super(message);
        }

    }
}