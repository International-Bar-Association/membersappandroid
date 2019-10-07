package com.ibamembers.content.job;

import android.support.annotation.NonNull;
import android.util.Log;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;
import com.ibamembers.messages.job.GetNormalMessagesJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetContentLibraryJob extends BaseJob {

    public static final String CONTENT_LIBRARY_JOB_TAG = "CONTENT_LIBRARY_JOB_TAG";
    public static final int MESSAGE_JOB_START = 0;
    public static final int MESSAGE_JOB_LENGTH = 50;

    public GetContentLibraryJob() {
        super(JobConfig.PRIORITY_NORMAL, CONTENT_LIBRARY_JOB_TAG);
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);
            try {
                Call<ContentLibraryModel> responseCall = restClient.getApiService().getContentLibrary(MESSAGE_JOB_START, MESSAGE_JOB_LENGTH);
                Response<ContentLibraryModel> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                     app.getEventBus().post(new Success(response.body()));
                    Log.i("GetContentLibJob","Success");
                } else {
                    app.getEventBus().post(new Failed(response.message()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new GetNormalMessagesJob.GetAllMessagesJobError(throwable.getMessage()));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private ContentLibraryModel contentLibraryModel;

        public Success(ContentLibraryModel contentLibraryModel) {
            this.contentLibraryModel = contentLibraryModel;
        }

        public ContentLibraryModel getContentlibaryModel() {
            return contentLibraryModel;
        }
    }

    public static class Failed {
        private String errorMessage;

        public Failed(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
