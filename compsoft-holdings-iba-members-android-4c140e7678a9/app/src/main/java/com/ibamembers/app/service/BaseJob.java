package com.ibamembers.app.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;

public abstract class BaseJob extends Job implements JobConfig.RequiresApp {

    private static final String TAG_JOB = "TAG_JOB";
    protected static final int RETRY_LIMIT = 10;

    protected App app;
    protected int retryLimit;
    protected int retryBackoffMS;

    protected int apiFailRetries;

    protected BaseJob() {
        this(JobConfig.PRIORITY_NORMAL);
    }

    protected BaseJob(int priority) {
        super(new Params(priority).addTags(TAG_JOB));
    }

    protected BaseJob(int priority, String jobTag) {
        super(new Params(priority).addTags(jobTag));
    }

    @Override
    public void onRun() throws Throwable {
    }

    protected boolean rerunJobTillLimit() throws Throwable{
        if (apiFailRetries <= RETRY_LIMIT) {
            apiFailRetries = apiFailRetries + 1;
            onRun();
            return true;
        }
        return false;
    }

    @Override
    public void setApp(App app, int retryLimit, int retryBackoffMS) {
        this.app = app;
        this.retryLimit = retryLimit;
        this.retryBackoffMS = retryBackoffMS;
    }

    @Override
    public void onAdded() {

    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected int getRetryLimit() {
        return retryLimit;
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    public static abstract class JobSuccess {}

    public static abstract class JobFailure {

        private final Throwable throwable;

        protected JobFailure(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}
