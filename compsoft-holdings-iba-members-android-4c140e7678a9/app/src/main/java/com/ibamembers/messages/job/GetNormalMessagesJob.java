package com.ibamembers.messages.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetNormalMessagesJob extends BaseJob {

    public static final String MESSAGE_JOB_TAG = "MESSAGE_JOB_TAG";
    public static final int MESSAGE_JOB_START = 0;
    public static final int MESSAGE_JOB_LENGTH = 40;

    public GetNormalMessagesJob() {
        super(JobConfig.PRIORITY_NORMAL, MESSAGE_JOB_TAG);
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<AllMessagesModel> responseCall = restClient.getApiService().getAllMessage(MESSAGE_JOB_START, MESSAGE_JOB_LENGTH);
                Response<AllMessagesModel> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    app.getEventBus().post(new GetAllMessagesJobSuccess(response.body()));
                } else {
                    app.getEventBus().post(new GetAllMessagesJobError(response.message()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new GetAllMessagesJobError(throwable.getMessage()));
        return RetryConstraint.CANCEL;
    }

    public static class GetAllMessagesJobSuccess {
        private AllMessagesModel allMessagesModel;

        public GetAllMessagesJobSuccess(AllMessagesModel allMessagesModel) {
            this.allMessagesModel = allMessagesModel;
        }

        public AllMessagesModel getAllMessagesModel() {
            return allMessagesModel;
        }
    }

    public static class GetAllMessagesJobError {

        private String errorMessage;

        public GetAllMessagesJobError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
