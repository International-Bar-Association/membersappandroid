package com.ibamembers.content.job;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownloadMediaJob extends Job implements JobConfig.RequiresApp {

    public static final String TAG = "DownloadMediaJob";
    public static final String DOWNLOAD_JOB_TAG = "DOWNLOAD_JOB_TAG";
    public static final String DOWNLOAD_DIR = "downloads";

    protected App app;
    protected int retryLimit;
    protected int retryBackoffMS;
    private String stringUrl;
    private Context context;
    private boolean isInstantPlay;

    public DownloadMediaJob(Context context, String stringUrl, boolean isInstantPlay) {
        super(new Params(JobConfig.PRIORITY_LOW).addTags(DOWNLOAD_JOB_TAG));
        this.context = context;
        this.stringUrl = stringUrl;
        this.isInstantPlay = isInstantPlay;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        long downloadedSize = 0;
        URL url = new URL(stringUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        OutputStream out = null;

        String filename = String.valueOf(new Date().getTime());
        String extension = stringUrl.substring(stringUrl.lastIndexOf("."), stringUrl.length());
        if (!extension.equals(".pdf") &&
                !extension.equals(".mp3") &&
                !extension.equals(".mp4")) {
            extension = ".html";
        }
        File downloadFile = getImageFileForId(context, filename + extension);

        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            out = new BufferedOutputStream(new FileOutputStream(downloadFile));
            byte buffer[] = new byte[4096];
            int update = 0;
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                downloadedSize += count;
                //TODO for any UI progress updates
//                update++;
//                if (update % 500 == 0) {
//                    Log.i("DownloadMediaJob", String.valueOf(update));
//                }
            }
        }
        catch(Throwable throwable) {
            Log.e(TAG, "Failed to download media");
            app.getEventBus().post(new DownloadFailed());
            return;
        }
        finally {
            if (out != null) {
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            urlConnection.disconnect();
        }

        if (isInstantPlay) {
            app.getEventBus().post(new DownloadAndPlayComplete(downloadFile));
        } else {
            app.getEventBus().post(new DownloadComplete(downloadFile));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    public static File getImageFileForId(Context context, String fileName) {
        File downloadDir = new File(context.getFilesDir() + DOWNLOAD_DIR);
        downloadDir.mkdir();
        return new File(downloadDir, fileName);
    }

    @Override
    public void setApp(App app, int retryLimit, int retryBackoffMS) {
        this.app = app;
        this.retryLimit = retryLimit;
        this.retryBackoffMS = retryBackoffMS;
    }

    public static class DownloadComplete {
        private File downloadFile;

        public DownloadComplete(File downloadFile) {
            this.downloadFile = downloadFile;
        }

        public File getDownloadFile() {
            return downloadFile;
        }
    }

    public static class DownloadAndPlayComplete {
        private File downloadFile;

        public DownloadAndPlayComplete(File downloadFile) {
            this.downloadFile = downloadFile;
        }

        public File getDownloadFile() {
            return downloadFile;
        }
    }

    public static class DownloadFailed {
    }
}
