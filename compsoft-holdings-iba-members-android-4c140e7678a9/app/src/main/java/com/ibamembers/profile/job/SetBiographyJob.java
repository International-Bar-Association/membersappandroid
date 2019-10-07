package com.ibamembers.profile.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.annotations.SerializedName;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class SetBiographyJob extends BaseJob {

    private String biographyText;

    public SetBiographyJob(String biographyText) throws IOException {
        super(JobConfig.PRIORITY_NORMAL);
        this.biographyText = biographyText;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<Void> responseCall = restClient.getApiService().setBiography(new BiographyRequest(biographyText));
                Response<Void> response = responseCall.execute();

                if (response.isSuccessful()) {
                    app.getEventBus().post(new Success());
                } else {
                    app.getEventBus().post(new Error(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new Error(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class BiographyRequest {

        @SerializedName("Biography")
        private String biography;

        public BiographyRequest(String biography) {
            this.biography = biography;
        }
    }
    public static class Success {

    }

    public static class Error {

        private String errorMessage;
        private int status;

        public Error(String errorMessage, int status) {
            this.errorMessage = errorMessage;
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getStatus() {
            return status;
        }
    }
}
