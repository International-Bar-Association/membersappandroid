package com.ibamembers.messages.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.sql.SQLException;

import retrofit2.Call;
import retrofit2.Response;

public class GetSingleMessagesJob extends BaseJob {

    public static final String SINGLE_MESSAGE_JOB_TAG = "SINGLE_MESSAGE_JOB_TAG";

    private App app;
    private int appUserMessageId;

    public GetSingleMessagesJob(int appUserMessageId) throws IOException {
        super(JobConfig.PRIORITY_NORMAL, SINGLE_MESSAGE_JOB_TAG);
        this.appUserMessageId = appUserMessageId;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<AllMessagesModel> responseCall = restClient.getApiService().getSingleMessage(appUserMessageId);
                Response<AllMessagesModel> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                   app.getEventBus().post(new Success(response.body()));
                } else {
                    app.getEventBus().post(new Failed(response.message()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    private String getSessionToken() {
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                return settingDao.getSessionToken();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new Failed(throwable.getMessage()));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
        private AllMessagesModel allMessagesModel;

        public Success(AllMessagesModel allMessagesModel) {
            this.allMessagesModel = allMessagesModel;
        }

        public AllMessagesModel getAllMessagesModel() {
            return allMessagesModel;
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
