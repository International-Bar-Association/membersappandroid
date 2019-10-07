package com.ibamembers.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibamembers.BuildConfig;
import com.ibamembers.R;
import com.ibamembers.app.gcm.MyFcmMessagingService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.urbanairship.UAirship;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

    private static final String KEY_API_KEY = "com.ibamembers.ApiKey";
    public static final String KEY_SHARED_PREFS = "KEY_SHARED_PREFS";
    public static final String PREF_KEY_EVENT_ID = "PREF_KEY_EVENT_ID";
    public static final String PREF_KEY_MESSAGE_ID = "PREF_KEY_MESSAGE_ID";
    public static final String PREF_KEY_SEEN_CAL_INFO = "PREF_KEY_SEEN_CAL_INFO";
    public static final String PREF_KEY_CONF_FAV_LIST = "PREF_KEY_CONF_FAV_LIST";

    private EventBus eventBus;
    private DatabaseHelper databaseHelper;
    private JobManager jobManager;
    private Map<JobQueueName, JobManager> jobQueues;
    private DataManager dataManager;
    private IntentManager intentManager;
    private DialogManager dialogManager;
    private ConnectionManager connectionManager;
    private SharedPreferences sharedPreferences;

    private Tracker tracker;
    private static GoogleAnalytics analytics;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyFcmMessagingService.enableNotificationChannels(getBaseContext());
        UAirship.shared().getPushManager().setUserNotificationsEnabled(true);
        analytics = GoogleAnalytics.getInstance(this);
        jobQueues = new HashMap<>();
    }

    public synchronized JobManager getJobManager(JobQueueName queue) {
        JobManager manager = jobQueues.get(queue);
        if (manager == null) {
            manager =  new JobConfig().configureJobManager(this, queue.name());
            jobQueues.put(queue, manager);
        }
        return manager;
    }

    public synchronized EventBus getEventBus() {
        if (eventBus == null) {
            EventBus.builder()
                    .logNoSubscriberMessages(BuildConfig.DEBUG)
                    .sendNoSubscriberEvent(BuildConfig.DEBUG);
                    //.addIndex(new AppEventBusIndex()).installDefaultEventBus();
            eventBus = EventBus.getDefault();
        }
        return eventBus;
    }

    public synchronized DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public synchronized DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    public synchronized DialogManager getDialogManager() {
        if (dialogManager == null) {
            dialogManager = new DialogManager();
        }
        return dialogManager;
    }

    public synchronized IntentManager getIntentManager() {
        if (intentManager == null) {
            intentManager = new IntentManager();
        }
        return intentManager;
    }

    public synchronized ConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new ConnectionManager();
        }
        return connectionManager;
    }

    //TODO
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(1800);
            tracker = analytics.newTracker(R.xml.global_tracker);
            tracker.enableExceptionReporting(true);
            tracker.enableAutoActivityTracking(true);
        }

        return tracker;
    }

    public void sendEventToAnalytics(String category, String action, String label) {
        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public void sendScreenViewAnalytics(String screenName) {
        getDefaultTracker().setScreenName(screenName);
        getDefaultTracker().send(new HitBuilders.EventBuilder().build());
    }

    public synchronized SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(KEY_SHARED_PREFS, 0);
        }
        return sharedPreferences;
    }

    public String getServiceApiKey() {
        return getKey(KEY_API_KEY, false);
    }

    /**
     * Used to keep track of loaded event id from calendar links
     */
    public void saveLoadedEventId(int eventId ) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(PREF_KEY_EVENT_ID, eventId).apply();
    }

    public int getLoadedEventId() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt(PREF_KEY_EVENT_ID, 0);
    }

    /**
     * Used to keep track of clicked messageID from push
     */
    public void saveP2PMessageId(int messageId ) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(PREF_KEY_MESSAGE_ID, messageId).apply();
    }

    public int getP2PMessageId() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt(PREF_KEY_MESSAGE_ID, 0);
    }

    /**
     * Used to keep track of clicked messageID from push
     */
    public void setHasSeenAddToCalendarInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(PREF_KEY_SEEN_CAL_INFO, true).apply();
    }

    public boolean getHasSeenAddToCalendarInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getBoolean(PREF_KEY_SEEN_CAL_INFO, false);
    }

    /**
     * Used to keep track of conference favourite schedules
     */
    public void setFavouriteSchedule(int eventItemId) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        ArrayList favouriteList = new ArrayList<>(getListOfFavouriteSchedule());
        if (!favouriteList.contains(eventItemId)) {
            favouriteList.add(eventItemId);
        } else {
            favouriteList.removeAll(Collections.singleton(eventItemId));
        }

        String favouriteString = new Gson().toJson(favouriteList);
        sharedPreferences.edit().putString(PREF_KEY_CONF_FAV_LIST, favouriteString).apply();
    }

    public List<Integer> getListOfFavouriteSchedule() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String favouriteListString =  sharedPreferences.getString(PREF_KEY_CONF_FAV_LIST, null);
        if (favouriteListString != null) {
            return new Gson().fromJson(favouriteListString, new TypeToken<List<Integer>>() {}.getType());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get the value of a meta-data entry from the app manifest.
     * @param key The name of the meta-data key.
     * @param isInt Is the value of the meta-data an integer
     * @return The value of the meta-data with the given key.
     */
    private String getKey(String key, boolean isInt) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (isInt) {
                return Integer.toString(bundle.getInt(key));
            } else {
                return bundle.getString(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public enum JobQueueName {
        Loading,
        Network
    }
}
