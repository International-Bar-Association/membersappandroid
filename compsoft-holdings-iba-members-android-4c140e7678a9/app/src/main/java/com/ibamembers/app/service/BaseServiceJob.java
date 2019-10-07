package com.ibamembers.app.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.RestClient;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

/**
 */
public abstract class BaseServiceJob<API_RESPONSE> extends BaseJob {

    /**
     * Indicates that the API call has been successful and returns the appropriate response object
     */
    protected abstract void callSuccessful(API_RESPONSE response) throws Throwable;

    /**
     * Indicates that the API call has failed. Appropriate measures should be taken.
     */
    protected abstract void callFailed(int responseCode);

    /**
     * Indicates that the API call has failed. Appropriate measures should be taken.
     */
    protected abstract void callFailed(int cancelReason, @Nullable Throwable throwable);

    /**
     * Creates the API Retrofit Call object.
     *
     * @return Call
     */
    protected abstract Call<API_RESPONSE> createCall();

    @Override
    public void onRun() throws Throwable {
        Response<API_RESPONSE> response = executeCall();
        if (response.isSuccessful()) {
            // On 200 OK response, process the call.
            callSuccessful(response.body());
        } else {
            // Anything else is an error.
            throw new RestClient.HttpException(response.code());
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof RestClient.HttpException) {
            // If the HTTP status code is 4xx then don't retry.
            if (((RestClient.HttpException) throwable).is400ClientError()) {
                return RetryConstraint.CANCEL;
            }
        }
        else if (throwable instanceof LocalException) {
            return RetryConstraint.CANCEL;
        }
        return RetryConstraint.createExponentialBackoff(runCount, retryBackoffMS);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        if (throwable instanceof RestClient.HttpException) {
            callFailed(((RestClient.HttpException) throwable).getStatusCode());
        } else {
            callFailed(cancelReason, throwable);
        }
    }

    protected Response<API_RESPONSE> executeCall() throws IOException {
        Call<API_RESPONSE> call = createCall();
        return call.execute();
    }

    protected void postEvent(Object event) {
        app.getEventBus().post(event);
    }

    public static class ServiceSuccess<REQUEST, RESPONSE> extends BaseJob.JobSuccess {

        private final REQUEST request;
        private final RESPONSE response;

        protected ServiceSuccess(REQUEST request, RESPONSE response) {
            this.request = request;
            this.response = response;
        }

        public REQUEST getRequest() {
            return request;
        }

        public RESPONSE getResponse() {
            return response;
        }

    }

    public static class ServiceFailure extends BaseJob.JobFailure {

        private final int responseCode;

        protected ServiceFailure(int responseCode) {
            super(null);
            this.responseCode = responseCode;
        }

        protected ServiceFailure(Throwable throwable) {
            super(throwable);
            this.responseCode = -1;
        }

        public boolean isHTTPError() {
            return responseCode != -1;
        }
    }

    /**
     * Throw to indicate an exception occurring locally on processing the result of a service call (to stop the service call being retried).
     */
    public static class LocalException extends Exception {

        public LocalException(Throwable cause) {
            super(cause);
        }
    }

}
