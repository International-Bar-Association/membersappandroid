package com.ibamembers.profile.message.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class HideMessageConferenceJob extends BaseJob {

	private HideMessageConferenceRequest request;

	public HideMessageConferenceJob(HideMessageConferenceRequest request) {
		super(JobConfig.PRIORITY_NORMAL);
		this.request = request;
	}

	@Override
	public void onRun() throws Throwable {
		if (app != null) {
			RestClient restClient = new RestClient(app);

			try {
				Call<Boolean> responseCall = restClient.getApiService().setProfileMessageAsHidden(request);
				Response<Boolean> response = responseCall.execute();

				if (response.isSuccessful() && response.body() != null) {
					Success success = new Success(response.body());
					app.getEventBus().post(success);
				} else {
					app.getEventBus().post(new Failure(response.message(), response.code()));
				}
			} catch (IOException e) {
				rerunJobTillLimit();
			}
		}
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		app.getEventBus().post(new Failure(throwable.getMessage(), 0));
		return RetryConstraint.CANCEL;
	}

	public static class Success {
		private Boolean response;

		public Success(Boolean response) {
			this.response = response;
		}

		public Boolean getResponse() {
			return response;
		}
	}

	public static class Failure {
		private String errorMessage;
		private int status;

		public Failure(String errorMessage, int status) {
			this.errorMessage = errorMessage;
			this.status = status;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public int getStatus() {
			return status;
		}
	}
}