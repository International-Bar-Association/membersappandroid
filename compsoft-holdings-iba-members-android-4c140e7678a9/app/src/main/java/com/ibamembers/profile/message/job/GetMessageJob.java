package com.ibamembers.profile.message.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetMessageJob extends BaseJob {

	private int id;
	private int skip;
	private int take;

	public GetMessageJob(int id, int skip, int take) throws IOException {
		super(JobConfig.PRIORITY_NORMAL);
		this.id = id;
		this.skip = skip;
		this.take = take;
	}

	@Override
	public void onRun() throws Throwable {
		if (app != null) {
			RestClient restClient = new RestClient(app);

			try {
				Call<GetMessageResponse> responseCall = restClient.getApiService().getMessage(id, skip, take);
				Response<GetMessageResponse> response = responseCall.execute();

				if (response.isSuccessful() && response.body() != null) {
					Success success = new Success(response.body(), skip);
					app.getEventBus().post(success);
				} else {
					app.getEventBus().post(new GetMessageJobError(response.message(), response.code()));
				}
			} catch (IOException e) {
				rerunJobTillLimit();
			}
		}
	}

	@Override
	protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
		app.getEventBus().post(new GetMessageJobError(throwable.getMessage(), 0));
		return RetryConstraint.CANCEL;
	}

	public static class Success {
		private GetMessageResponse response;
		private int skip;

		public Success(GetMessageResponse response, int skip) {
			this.response = response;
			this.skip = skip;
		}

		public GetMessageResponse getResponse() {
			return response;
		}

		public int getSkip() {
			return skip;
		}
	}

	public static class GetMessageJobError {
		private String errorMessage;
		private int status;

		public GetMessageJobError(String errorMessage, int status) {
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