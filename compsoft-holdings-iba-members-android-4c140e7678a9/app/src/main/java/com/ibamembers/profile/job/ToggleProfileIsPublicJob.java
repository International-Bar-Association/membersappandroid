package com.ibamembers.profile.job;

import com.ibamembers.app.BaseResponse;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

public class ToggleProfileIsPublicJob extends BaseJob {

    public ToggleProfileIsPublicJob() throws IOException {
        super(JobConfig.PRIORITY_NORMAL);
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<BaseResponse> responseCall = restClient.getApiService().makeProfilePublic();
                Response<BaseResponse> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                     app.getEventBus().post(new Success());

                } else {
                    app.getEventBus().post(new Error(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    public static class Success {
    }

    public static class Error {
        private String errorMessage;
        private int status;

        public Error(String errorMessage, int status) {
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
