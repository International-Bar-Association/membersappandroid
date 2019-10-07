package com.ibamembers.messages.job;

import android.util.Log;

import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

public class SetMessagesStatusReadJob extends BaseJob {

    public static final String TAG = "MessageStatusRead";
    public static final String MESSAGE_STATUS_RECEIVED_JOB_TAG = "MESSAGE_STATUS_RECEIVED_JOB_TAG";

    private MessageStatusRead messageStatusRead;

    public SetMessagesStatusReadJob(MessageStatusRead messageStatusRead) throws IOException {
        super(JobConfig.PRIORITY_NORMAL, MESSAGE_STATUS_RECEIVED_JOB_TAG);
        this.messageStatusRead = messageStatusRead;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<AllMessagesModel> responseCall = restClient.getApiService().setMessageStatusRead(messageStatusRead);
                Response<AllMessagesModel> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Content read");
                } else {
                    Log.e(TAG, "Response failed, error: " + response.message());
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }
}
