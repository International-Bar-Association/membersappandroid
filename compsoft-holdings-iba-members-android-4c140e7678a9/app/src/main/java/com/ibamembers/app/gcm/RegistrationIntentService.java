package com.ibamembers.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ibamembers.app.App;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.gcm.job.DevicePushTokenModel;
import com.ibamembers.app.gcm.job.SetDevicePushTokenJob;
import com.urbanairship.push.fcm.AirshipFirebaseInstanceIdService;

import java.sql.SQLException;
import java.util.UUID;

public class RegistrationIntentService extends IntentService {

    private static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String TAG = "RegIntentService";
    private static final String DEVICE_TYPE = "0";
    private static final String PREF_KEY_UUID = "PREF_KEY_UUID";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
					@Override
					public void onComplete(@NonNull Task<InstanceIdResult> task) {
						if (!task.isSuccessful()) {
							Log.w(TAG, "getInstanceId failed", task.getException());
							return;
						}

//						// Get new Instance ID token
						String token = task.getResult().getToken();

						try {
							String savedGcmToken = getGCMToken();

							if (!token.equals(savedGcmToken)) {
								Log.i(TAG, "Saving token to DB");
								saveGCMToken(token);

								AirshipFirebaseInstanceIdService.processTokenRefresh(getApplicationContext());
								sendRegistrationToServer(token,  getUUID());
							} else {
								sendRegistrationToServer(getGCMToken(), getUUID());
							}

						} catch (Exception e) {
							Log.d(TAG, "Failed to complete token refresh", e);
							// If an exception happens while fetching the new token or updating our registration data
							// on a third-party server, this ensures that we'll attempt the update at a later time.
							saveGCMToken("");
						}
					}
				});


        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Only save a new UUID value if it doesn't already exist
     * @return saved UUID
     */
    private String getUUID() {
        SharedPreferences sharedPrefs = ((App) getApplication()).getSharedPreferences();
        String savedUUID = sharedPrefs.getString(PREF_KEY_UUID, null);

        if (savedUUID == null) {
            savedUUID = UUID.randomUUID().toString();
            sharedPrefs.edit().putString(PREF_KEY_UUID, savedUUID).apply();
        }

        return savedUUID;
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, String UUID) {
        App app = (App)getApplication();
        if (app != null) {
            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new SetDevicePushTokenJob(new DevicePushTokenModel(UUID, DEVICE_TYPE, token)));
        }
    }

    private void saveGCMToken(String token) {
        try {
            SettingDao settingDao = ((App) getApplication()).getDatabaseHelper().getSettingDao();
            settingDao.setGCMRegistrationToken(token);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getGCMToken() {
        try {
            SettingDao settingDao = ((App) getApplication()).getDatabaseHelper().getSettingDao();
            return settingDao.getGCMRegistrationToken();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isRegisteredWithServer() {
        try {
            SettingDao settingDao = ((App) getApplication()).getDatabaseHelper().getSettingDao();
            return settingDao.getIsTokenRegisteredWithServer();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
