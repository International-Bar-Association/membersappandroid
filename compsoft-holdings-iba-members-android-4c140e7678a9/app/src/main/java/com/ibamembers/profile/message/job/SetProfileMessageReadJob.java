package com.ibamembers.profile.message.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class SetProfileMessageReadJob extends BaseJob {

    private int messageId;

    public SetProfileMessageReadJob(int messageId) {
        super(JobConfig.PRIORITY_NORMAL);
        this.messageId = messageId;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<Boolean> responseCall = restClient.getApiService().setProfileMessageRead(messageId);
                Response<Boolean> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    Success success = new Success(response.body());
                    app.getEventBus().post(success);
                } else {
                    app.getEventBus().post(new SendMessageJobError(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new SendMessageJobError(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private Boolean response;

        public Success(Boolean response) {
            this.response = response;
        }

        public Boolean getResponse() {
            return response;
        }
    }

    public static class SendMessageJobError {
        private String errorMessage;
        private int status;

        public SendMessageJobError(String errorMessage, int status) {
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