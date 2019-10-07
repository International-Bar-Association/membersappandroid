package com.ibamembers.conference.event.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetConferenceJob extends BaseJob {

    private int conferenceId;

    public GetConferenceJob(int conferenceId) {
        super(JobConfig.PRIORITY_NORMAL);
        this.conferenceId = conferenceId;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<ConferenceResponse> responseCall = restClient.getApiService().getConference(conferenceId);
                Response<ConferenceResponse> response = responseCall.execute();

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Success success = new Success(response.body(), conferenceId);
                        app.getEventBus().post(success);
                    }//TODO
                } else {
                    app.getEventBus().post(new GetConferenceJobError(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new GetConferenceJobError(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private ConferenceResponse response;
        private int conferenceId;

        public Success(ConferenceResponse response, int conferenceId) {
            this.response = response;
            this.conferenceId = conferenceId;
        }

        public ConferenceResponse getResponse() {
            return response;
        }

        public int getConferenceId() {
            return conferenceId;
        }
    }

    public static class GetConferenceJobError {
        private String errorMessage;
        private int status;

        public GetConferenceJobError(String errorMessage, int status) {
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