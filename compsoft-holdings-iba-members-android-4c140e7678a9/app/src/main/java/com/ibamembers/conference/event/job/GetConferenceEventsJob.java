package com.ibamembers.conference.event.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.KotlinUtils;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GetConferenceEventsJob extends BaseJob {

    private int conferenceId;

    public GetConferenceEventsJob(int conferenceId) {
        super(JobConfig.PRIORITY_NORMAL);
        this.conferenceId = conferenceId;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<ConferenceBuildingEventResponse> responseCall = restClient.getApiService().getConferenceEvents(conferenceId);
                Response<ConferenceBuildingEventResponse> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    Success success = new Success(response.body());
                    List<ConferenceEventResponse> eventList =  success.getResponse().getEventList();
                    if (eventList != null) {
                        success.getResponse().setEventList(KotlinUtils.Companion.compareEvents(eventList));
                    }

                    app.getEventBus().postSticky(success);
                } else {
                    app.getEventBus().postSticky(new Failure(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new Failure(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private ConferenceBuildingEventResponse response;

        public Success(ConferenceBuildingEventResponse response) {
            this.response = response;
        }

        public ConferenceBuildingEventResponse getResponse() {
            return response;
        }
    }

    public static class Failure {
        private String errorMessage;
        private int status;

        public Failure(String errorMessage, int status) {
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