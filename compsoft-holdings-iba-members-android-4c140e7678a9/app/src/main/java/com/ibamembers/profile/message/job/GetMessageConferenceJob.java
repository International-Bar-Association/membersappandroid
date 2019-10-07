package com.ibamembers.profile.message.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GetMessageConferenceJob extends BaseJob {

	public static final String GET_CONFERENCE_MESSAGE_JOB_TAG = "GET_CONFERENCE_MESSAGE_JOB_TAG";
	public static final String GET_NORMAL_MESSAGE_JOB_TAG = "GET_NORMAL_MESSAGE_JOB_TAG";

	private String TAG;

	public GetMessageConferenceJob(String TAG) {
		super(JobConfig.PRIORITY_NORMAL, TAG);
		this.TAG = TAG;
	}

	@Override
	public void onRun() throws Throwable {
		if (app != null) {
			RestClient restClient = new RestClient(app);

			try {
				Call<List<GetMessageConferenceResponse>> responseCall = restClient.getApiService().getMessageConnections();
				Response<List<GetMessageConferenceResponse>> response = responseCall.execute();

				if (response.isSuccessful() && response.body() != null) {
					Success success = new Success(response.body(), TAG);
					app.getEventBus().post(success);
				} else {
					app.getEventBus().post(new GetMessageConferenceJobError(response.message(), response.code()));
				}
			} catch (IOException e) {
				rerunJobTillLimit();
			}
		}
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		app.getEventBus().post(new GetMessageConferenceJobError(throwable.getMessage(), 0));
		return RetryConstraint.CANCEL;
	}

	public static class Success {
		private List<GetMessageConferenceResponse> response;
		private String TAG;

		public Success(List<GetMessageConferenceResponse> response, String TAG) {
			this.response = response;
			this.TAG = TAG;
		}

		public List<GetMessageConferenceResponse> getResponse() {
			return response;
		}

		public String getTAG() {
			return TAG;
		}
	}

	public static class GetMessageConferenceJobError {
		private String errorMessage;
		private int status;

		public GetMessageConferenceJobError(String errorMessage, int status) {
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