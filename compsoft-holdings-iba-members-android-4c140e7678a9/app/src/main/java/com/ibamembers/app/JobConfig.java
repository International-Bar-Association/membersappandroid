package com.ibamembers.app;

import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;


public class JobConfig {

    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_LOW = 1;

    protected App app;

    public JobManager configureJobManager(App app, String id) {
        this.app = app;
        Configuration configuration = createConfiguration(app, id).build();
        return new JobManager(configuration);
    }

    protected Configuration.Builder createConfiguration(final App app, String id) {
        return new Configuration.Builder(app)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {
                        Log.v(TAG, String.format(text, args));
                    }
                })
                .id(id)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .injector(new JobInjector(app));
    }

    public interface RequiresApp {
        void setApp(App app, int retryLimit, int retryBackoffMS);
    }
}

