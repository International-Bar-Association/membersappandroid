package com.ibamembers.content.job;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DownloadPDFJob extends Job {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 512;
	private String fileName;

	public DownloadPDFJob(String fileName) {
		super(new Params(JobConfig.PRIORITY_NORMAL));
		this.fileName = fileName;
	}

	@Override
	public void onRun() throws Throwable {
		downloadFile(fileName);
	}

	private void downloadFile(String urlFile) throws IOException {
		File localFile = getLocalFile(getApplicationContext(), fileName);

		if (localFile.exists()) {
			EventBus.getDefault().post(new DownloadFileComplete(localFile));
		}
		else if (localFile.getParentFile().exists() || localFile.getParentFile().mkdirs()) {
			try {
				URL url = new URL(urlFile);

				FileOutputStream out = new FileOutputStream(localFile);

				InputStream is = url.openStream();
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int n;
				while (-1 != (n = is.read(buffer))) {
					out.write(buffer, 0, n);
				}

				EventBus.getDefault().post(new DownloadFileComplete(localFile));

			} catch (IOException ex) {
				if (!localFile.delete()) {
					localFile.deleteOnExit();
				}
				throw ex;
			}
		}
	}

	public static File getLocalFile(Context context, String fromUrl) {
		File dir;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			dir = context.getNoBackupFilesDir();
		}
		else {
			dir = context.getFilesDir();
		}

		String name = FilenameUtils.getBaseName(fromUrl) + "." + FilenameUtils.getExtension(fromUrl);
		return new File(new File(dir, "downloads"), name);
	}

	@Override
	public void onAdded() {

	}

	@Override
	protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		return RetryConstraint.CANCEL;
	}

	public static class DownloadFileComplete{
		private File downloadedFile;

		public DownloadFileComplete(File downloadedFile) {
			this.downloadedFile = downloadedFile;
		}

		public File getDownloadedFile() {
			return downloadedFile;
		}
	}
}