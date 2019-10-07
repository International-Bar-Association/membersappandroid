package com.ibamembers.profile.message.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class SendMessageJob extends BaseJob {

    private SendMessageRequest sendMessageRequest;

    public SendMessageJob(SendMessageRequest sendMessageRequest) {
        super(JobConfig.PRIORITY_NORMAL);
        this.sendMessageRequest = sendMessageRequest;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<SendMessageResponse> responseCall = restClient.getApiService().sendMessage(sendMessageRequest);
                Response<SendMessageResponse> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    Success success = new Success(response.body(), sendMessageRequest.getMessage());
                    app.getEventBus().post(success);
                } else {
                    app.getEventBus().post(new SendMessageJobError(response.message(), response.code(), sendMessageRequest.getUuid()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new SendMessageJobError(throwable.getMessage(), 0, sendMessageRequest.getUuid()));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private SendMessageResponse response;
        private String messageRequest;

        public Success(SendMessageResponse response, String messageRequest) {
            this.response = response;
            this.messageRequest = messageRequest;
        }

        public SendMessageResponse getResponse() {
            return response;
        }

        public String getMessageRequest() {
            return messageRequest;
        }
    }

    public static class SendMessageJobError {
        private String uuid;
        private String errorMessage;
        private int status;

        public SendMessageJobError(String errorMessage, int status, String uuid) {
            this.errorMessage = errorMessage;
            this.status = status;
            this.uuid = uuid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getStatus() {
            return status;
        }

        public String getUuid() {
            return uuid;
        }
    }
}