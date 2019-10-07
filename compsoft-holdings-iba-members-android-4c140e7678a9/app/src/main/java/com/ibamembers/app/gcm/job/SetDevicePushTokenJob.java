package com.ibamembers.app.gcm.job;

import android.support.annotation.NonNull;
import android.util.Log;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.sql.SQLException;

import retrofit2.Call;
import retrofit2.Response;

public class SetDevicePushTokenJob extends BaseJob {

    public static final String TAG = "SetDevicePushTokenJob";
    public static final String DEVICE_PUSH_TOKEN_TAG = "DEVICE_PUSH_TOKEN_TAG";

    private DevicePushTokenModel devicePushTokenModel;

    public SetDevicePushTokenJob(DevicePushTokenModel devicePushTokenModel) {
        super(JobConfig.PRIORITY_NORMAL, DEVICE_PUSH_TOKEN_TAG);
        this.devicePushTokenModel = devicePushTokenModel;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<Void> responseCall = restClient.getApiService().setDevicePushToken(devicePushTokenModel);
                Response<Void> response = responseCall.execute();

                if (response.isSuccessful()) {
                    Log.e(TAG, "Device token received");
                    setIsRegisteredWithServer(true);
                    app.getEventBus().post(new Success());
                } else {
                    Log.e(TAG, "Response failed, error: " + response.message());
                    app.getEventBus().post(new Failed());
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
        app.getEventBus().post(new Failed());
        return RetryConstraint.CANCEL;
    }


    public static class Success {
    }

    public static class Failed {
    }

    private void setIsRegisteredWithServer(boolean isRegsitered) {
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                settingDao.setIsTokenRegisteredWithServer(isRegsitered);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
