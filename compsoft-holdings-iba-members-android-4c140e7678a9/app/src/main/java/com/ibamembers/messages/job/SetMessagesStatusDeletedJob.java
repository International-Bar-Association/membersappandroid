package com.ibamembers.messages.job;

import android.util.Log;

import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.sql.SQLException;

import retrofit2.Call;
import retrofit2.Response;

public class SetMessagesStatusDeletedJob extends BaseJob {

    public static final String TAG = "MessageStatusReceived";
    public static final String MESSAGE_STATUS_RECEIVED_JOB_TAG = "MESSAGE_STATUS_RECEIVED_JOB_TAG";

    private GeneralMessageModel deleteMessage;
    private MessageStatusDeleted messageStatusDeleted;

    public SetMessagesStatusDeletedJob(GeneralMessageModel deleteMessage, MessageStatusDeleted messageStatusDeleted) throws IOException {
        super(JobConfig.PRIORITY_LOW, MESSAGE_STATUS_RECEIVED_JOB_TAG);
        this.deleteMessage = deleteMessage;
        this.messageStatusDeleted = messageStatusDeleted;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<Void> responseCall = restClient.getApiService().setMessageStatusDeleted(messageStatusDeleted);
                Response<Void> response = responseCall.execute();

                if (response.isSuccessful()) {
                    Log.i(TAG, "Content deleted");
                    app.getEventBus().post(new MessageDeletedSuccess(deleteMessage));
                } else {
                    Log.e(TAG, "Response failed, error: " + response.message());
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

    public static class MessageDeletedSuccess {

        private GeneralMessageModel deleteMessage;

        public MessageDeletedSuccess(GeneralMessageModel deleteMessage) {
            this.deleteMessage = deleteMessage;
        }

        public GeneralMessageModel getDeleteMessage() {
            return deleteMessage;
        }
    }
}
