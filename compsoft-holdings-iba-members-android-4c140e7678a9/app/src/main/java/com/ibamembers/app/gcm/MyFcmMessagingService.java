package com.ibamembers.app.gcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.SettingDao;
import com.ibamembers.main.MainActivity;
import com.urbanairship.UAirship;
import com.urbanairship.push.fcm.AirshipFirebaseInstanceIdService;

import org.greenrobot.eventbus.EventBus;

import java.sql.SQLException;

public class MyFcmMessagingService extends FirebaseMessagingService {
    private static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String DEVICE_TYPE = "0";
    private static final String PREF_KEY_UUID = "PREF_KEY_UUID";

    public static final String KEY_NOTIFICATION_COUNT = "KEY_NOTIFICATION_COUNT";
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "MyFcmMessagingService";
    public static final String NOTIFICATION_TITLE_KEY = "com.urbanairship.push.ALERT";
    public static final String NOTIFICATION_ID_KEY = "Id";
    public static final String NOTIFICATION_TYPE_KEY = "Type";

    public enum MessageType{
        Standard,
        P2P_MESSAGE
    }

    public static void enableNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String defaultChannel = context.getString(R.string.main_account_notifications_default_channel);
            String channelName = context.getString(R.string.main_account_notifications_default_channel_name);
            String channelDescription = context.getString(R.string.main_account_notifications_default_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(defaultChannel, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Called when message is received.
     *
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
		//AirshipFirebaseMessagingService.processMessageSync(getApplicationContext(), remoteMessage);
        if (isSignedIn() && remoteMessage.getData() != null) {

            Log.i(TAG, "remoteMessage getDate: " + remoteMessage.getData().toString());
            incrementNotificationCount();

            String title = remoteMessage.getData().get(NOTIFICATION_TITLE_KEY);
            String messageId = remoteMessage.getData().get(NOTIFICATION_ID_KEY);
            String type = remoteMessage.getData().get(NOTIFICATION_TYPE_KEY);

            boolean isInForeground = UAirship.shared().getAnalytics().isAppInForeground();
            if (!isInForeground) {
                saveMessageId(messageId);
                sendNotification(title, messageId, type);
            }

            // Post the notification for an activity
            EventBus.getDefault().post(new NotificationEvent("received message"));
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     * @param title Title of message.
     * @param id Id of message.
     */
    private void sendNotification(String title, String id, String type) {
        Log.i(TAG, "Received notification with title and id: " + String.valueOf(title) + ", " + String.valueOf(id));


        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(NOTIFICATION_ID_KEY, id);
        intent.putExtra(NOTIFICATION_TYPE_KEY, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String defaultChannel = getApplicationContext().getString(R.string.main_account_notifications_default_channel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, defaultChannel)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(title)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (getNotificationCount() > 0) {
            notificationBuilder.setNumber(getNotificationCount());
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
		// Notify Urban Airship that the token is refreshed.

        Log.i(TAG, "Updating UrbanAirship and sending token to server");
		AirshipFirebaseInstanceIdService.processTokenRefresh(getApplicationContext());
    }

    private void saveMessageId(String id){
        App app = (App) getApplication();
        if (app != null) {
            app.saveP2PMessageId(Integer.parseInt(id));
        }
    }

    private boolean isSignedIn() {
        App app = (App) getApplication();
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                return settingDao.isUserRegistered();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private int getNotificationCount() {
        App app = (App) getApplication();
        if (app != null) {
            SharedPreferences sharedPrefs = app.getSharedPreferences();
            return sharedPrefs.getInt(KEY_NOTIFICATION_COUNT, 0);
        }
        return 0;
    }

    private void incrementNotificationCount() {
        App app = (App) getApplication();
        if (app != null) {
            SharedPreferences sharedPrefs = app.getSharedPreferences();
            int count = sharedPrefs.getInt(KEY_NOTIFICATION_COUNT, 0);
            sharedPrefs.edit().putInt(KEY_NOTIFICATION_COUNT, count+1).apply();
        }
    }

    public class NotificationEvent {
        public String getMessage() {
            return message;
        }

        public String message;
        public NotificationEvent(String message) { this.message = message; }
    }

}
