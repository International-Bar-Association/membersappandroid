package com.ibamembers.app;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.di.DependencyInjector;
import com.ibamembers.R;

public class JobInjector implements DependencyInjector {

    private final App app;
    private final int retryLimit;
    private final int retryBackoffMS;

    public JobInjector(final App app) {
        this.app = app;
        retryLimit = app.getResources().getInteger(R.integer.rest_client_retries);
        this.retryBackoffMS = app.getResources().getInteger(R.integer.rest_client_retry_backoff_ms);
    }

    @Override
    public void inject(Job job) {
        if (job instanceof JobConfig.RequiresApp) {
            JobConfig.RequiresApp raJob = (JobConfig.RequiresApp) job;
            raJob.setApp(app, retryLimit, retryBackoffMS);
        }
    }
}
